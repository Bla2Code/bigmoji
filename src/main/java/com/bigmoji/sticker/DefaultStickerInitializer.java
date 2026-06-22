package com.bigmoji.sticker;

import com.bigmoji.domain.entity.StickerMapping;
import com.bigmoji.domain.repository.StickerMappingRepository;
import com.bigmoji.storage.MinioStorageService;
import java.io.InputStream;
import java.util.List;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DefaultStickerInitializer {
  public static final List<DefaultSticker> DEFAULTS =
      List.of(
          new DefaultSticker("😊", "smile", "Smiling face", "smile.png"),
          new DefaultSticker("❤️", "heart", "Red heart", "heart.png"),
          new DefaultSticker("🎉", "party", "Party popper", "party.png"),
          new DefaultSticker("👍", "thumbsup", "Thumbs up", "thumbsup.png"),
          new DefaultSticker("🔥", "fire", "Fire", "fire.png"));

  private final StickerMappingRepository repository;
  private final MinioStorageService storageService;
  private final ResourceLoader resourceLoader;

  public DefaultStickerInitializer(
      StickerMappingRepository repository,
      MinioStorageService storageService,
      ResourceLoader resourceLoader) {
    this.repository = repository;
    this.storageService = storageService;
    this.resourceLoader = resourceLoader;
  }

  @Transactional
  public void initializeForGuildIfMissing(String guildId) {
    for (DefaultSticker def : DEFAULTS) {
      if (!repository.findByGuildIdAndEmojiName(guildId, def.shortcodeName()).isEmpty()) {
        continue;
      }

      try {
        var bucketName = storageService.bucketForGuild(guildId);
        var resource = resourceLoader.getResource("classpath:default-stickers/" + def.fileName());
        long size = resource.contentLength();
        String objectKey;
        try (InputStream is = resource.getInputStream()) {
          objectKey = storageService.upload(guildId, ".png", is, size, "image/png");
        }
        var mapping = new StickerMapping();
        mapping.setGuildId(guildId);
        mapping.setEmojiName(def.shortcodeName());
        mapping.setMinioBucketName(bucketName);
        mapping.setMinioObjectKey(objectKey);
        mapping.setDefault(true);
        repository.save(mapping);
      } catch (Exception e) {
        throw new RuntimeException("Failed to initialize default sticker: " + def.fileName(), e);
      }
    }
  }

  public List<DefaultSticker> defaults() {
    return DEFAULTS;
  }

  public record DefaultSticker(
      String emojiName, String shortcodeName, String description, String fileName) {}
}
