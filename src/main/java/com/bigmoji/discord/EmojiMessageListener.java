package com.bigmoji.discord;

import com.bigmoji.domain.entity.StickerMapping;
import com.bigmoji.emoji.EmojiDetector;
import com.bigmoji.sticker.StickerMappingService;
import com.bigmoji.storage.MinioStorageService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class EmojiMessageListener extends ListenerAdapter {
    private static final Logger log = LoggerFactory.getLogger(EmojiMessageListener.class);

    private static final String STATE_MAPPED_DEFAULT = "MAPPED_DEFAULT";
    private static final String STATE_UNMAPPED_EMOJI = "UNMAPPED_EMOJI";
    private static final String STATE_NO_EMOJI = "NO_EMOJI";
    private static final String STATE_RESOURCE_MISSING = "RESOURCE_MISSING";
    private static final String STATE_WEBHOOK_FALLBACK = "WEBHOOK_FALLBACK";

    private final EmojiDetector detector;
    private final StickerMappingService mappingService;
    private final StickerSenderService senderService;
    private final WebhookStickerSender webhookSender;
    private final MinioStorageService storageService;

    public EmojiMessageListener(
            EmojiDetector detector,
            StickerMappingService mappingService,
            StickerSenderService senderService,
            WebhookStickerSender webhookSender,
            MinioStorageService storageService) {
        this.detector = detector;
        this.mappingService = mappingService;
        this.senderService = senderService;
        this.webhookSender = webhookSender;
        this.storageService = storageService;
    }

    @Override
    @Async("messageExecutor")
    public void onMessageReceived(MessageReceivedEvent event) {
        var message = event.getMessage();
        var raw = message.getContentRaw();
        var sourceGuildId = event.isFromGuild() ? event.getGuild().getId() : "DM";
        var channelId = event.getChannel().getId();
        var authorId = event.getAuthor().getId();

        log.debug(
                "Message received: guildId={}, channelId={}, authorId={}, isBot={}, rawContent={}",
                sourceGuildId,
                channelId,
                authorId,
                event.getAuthor().isBot(),
                raw);

        if (event.getAuthor().isBot()) {
            log.debug(
                    "Message skipped: reason=BOT_AUTHOR, guildId={}, channelId={}", sourceGuildId, channelId);
            return;
        }

        boolean singleEmoji = detector.isSingleEmojiMessage(raw);
        log.debug(
                "Emoji detection result: guildId={}, channelId={}, rawContent={}, isSingleEmojiMessage={}",
                sourceGuildId,
                channelId,
                raw,
                singleEmoji);

        if (!singleEmoji) {
            log.debug(
                    "Decision state: state={}, guildId={}, channelId={}, reason=NO_SINGLE_EMOJI",
                    STATE_NO_EMOJI,
                    sourceGuildId,
                    channelId);
            return;
        }

        if (!event.isFromGuild()) {
            log.debug(
                    "Decision state: state={}, guildId={}, channelId={}, reason=DM_NOT_SUPPORTED",
                    STATE_UNMAPPED_EMOJI,
                    sourceGuildId,
                    channelId);
            return;
        }

        String normalized = detector.normalize(raw);
        String guildId = event.getGuild().getId();

        log.debug(
                "Emoji normalization result: guildId={}, channelId={}, rawContent={}, normalizedEmoji={}",
                guildId,
                channelId,
                raw,
                normalized);

        Optional<StickerMapping> mapping = mappingService.pickRandomMapping(guildId, normalized);
        if (mapping.isEmpty()) {
            log.debug(
                    "Mapping lookup result: guildId={}, normalizedEmoji={}, found=false",
                    guildId,
                    normalized);
            log.debug(
                    "Decision state: state={}, guildId={}, channelId={}, normalizedEmoji={}",
                    STATE_UNMAPPED_EMOJI,
                    guildId,
                    channelId,
                    normalized);
            return;
        }

        StickerMapping selected = mapping.get();
        log.debug(
                "Mapping lookup result: guildId={}, normalizedEmoji={}, found=true, objectKey={}, isDefault={}",
                guildId,
                normalized,
                selected.getMinioObjectKey(),
                selected.isDefault());

        try {
            byte[] stickerBytes =
                    storageService.downloadFile(selected.getMinioBucketName(), selected.getMinioObjectKey());
            String fileName = extractFileName(selected.getMinioObjectKey());

            log.debug(
                    "Sticker downloaded: bucket={}, objectKey={}, sizeBytes={}",
                    selected.getMinioBucketName(),
                    selected.getMinioObjectKey(),
                    stickerBytes.length);

            String authorName = event.getAuthor().getName();
            String authorAvatarUrl = event.getAuthor().getEffectiveAvatarUrl();

            boolean sentViaWebhook =
                    webhookSender.sendAsAuthor(
                            event.getChannel(), stickerBytes, fileName, authorName, authorAvatarUrl);

            if (!sentViaWebhook) {
                senderService.send(event.getChannel(), stickerBytes, fileName);
                log.debug(
                        "Decision state: state={}, guildId={}, channelId={}, normalizedEmoji={}, objectKey={}, sendMethod=FALLBACK_BOT",
                        STATE_WEBHOOK_FALLBACK,
                        guildId,
                        channelId,
                        normalized,
                        selected.getMinioObjectKey());
            }

            message.delete().queue();
            log.debug(
                    "Decision state: state={}, guildId={}, channelId={}, normalizedEmoji={}, objectKey={}, sendMethod={}",
                    STATE_MAPPED_DEFAULT,
                    guildId,
                    channelId,
                    normalized,
                    selected.getMinioObjectKey(),
                    sentViaWebhook ? "WEBHOOK" : "DIRECT");
        } catch (Exception e) {
            log.warn(
                    "Decision state: state={}, guildId={}, channelId={}, normalizedEmoji={}, objectKey={}, error={}",
                    STATE_RESOURCE_MISSING,
                    guildId,
                    channelId,
                    normalized,
                    selected.getMinioObjectKey(),
                    e.getMessage());
        }
    }

    private String extractFileName(String objectKey) {
        int lastDot = objectKey.lastIndexOf('.');
        if (lastDot > 0) {
            return "sticker" + objectKey.substring(lastDot);
        }
        return "sticker.bin";
    }
}
