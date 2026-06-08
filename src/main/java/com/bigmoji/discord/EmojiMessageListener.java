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

    log.debug(
        "Discord message received: guildId={}, channelId={}, authorId={}, isBot={}, content={}",
        sourceGuildId,
        event.getChannel().getId(),
        event.getAuthor().getId(),
        event.getAuthor().isBot(),
        raw);

    if (event.getAuthor().isBot()) {
      return;
    }

    if (!detector.isSingleEmojiMessage(raw)) {
      return;
    }

    String normalized = detector.normalize(raw);
    String guildId = event.getGuild().getId();

    log.debug("Processing single-emoji message: guildId={}, normalizedEmoji={}", guildId, normalized);

    Optional<StickerMapping> mapping = mappingService.pickRandomMapping(guildId, normalized);
    if (mapping.isEmpty()) {
      return;
    }

    try {
      String url =
          storageService.presignedGetUrl(
              mapping.get().getMinioBucketName(), mapping.get().getMinioObjectKey());
      senderService.send(event.getChannel(), url);
      message.delete().queue();
    } catch (Exception e) {
      log.debug("Failed to send sticker replacement: {}", e.getMessage());
    }
  }
}
