package com.bigmoji.discord;

import com.bigmoji.domain.entity.StickerMapping;
import com.bigmoji.emoji.EmojiDetector;
import com.bigmoji.sticker.StickerMappingService;
import com.bigmoji.storage.MinioStorageService;
import java.util.Optional;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class EmojiMessageListener extends ListenerAdapter {
  private static final Logger log = LoggerFactory.getLogger(EmojiMessageListener.class);

  private static final String STATE_MAPPED_DEFAULT = "MAPPED_DEFAULT";
  private static final String STATE_UNMAPPED_EMOJI = "UNMAPPED_EMOJI";
  private static final String STATE_NO_EMOJI = "NO_EMOJI";
  private static final String STATE_RESOURCE_MISSING = "RESOURCE_MISSING";

  private final EmojiDetector detector;
  private final StickerMappingService mappingService;
  private final StickerSenderService senderService;
  private final MinioStorageService storageService;

  public EmojiMessageListener(
      EmojiDetector detector,
      StickerMappingService mappingService,
      StickerSenderService senderService,
      MinioStorageService storageService) {
    this.detector = detector;
    this.mappingService = mappingService;
    this.senderService = senderService;
    this.storageService = storageService;
  }

  @Override
  @Async("messageExecutor")
  public void onMessageReceived(MessageReceivedEvent event) {
    Message message = event.getMessage();
    String raw = message.getContentRaw();
    String sourceGuildId = event.isFromGuild() ? event.getGuild().getId() : "DM";
    String channelId = event.getChannel().getId();
    String authorId = event.getAuthor().getId();

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
      String url =
          storageService.presignedGetUrl(
              selected.getMinioBucketName(), selected.getMinioObjectKey());
      senderService.send(event.getChannel(), url);
      message.delete().queue();
      log.debug(
          "Decision state: state={}, guildId={}, channelId={}, normalizedEmoji={}, objectKey={}",
          STATE_MAPPED_DEFAULT,
          guildId,
          channelId,
          normalized,
          selected.getMinioObjectKey());
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
}
