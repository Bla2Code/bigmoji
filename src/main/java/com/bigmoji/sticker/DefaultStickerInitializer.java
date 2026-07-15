package com.bigmoji.sticker;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class DefaultStickerInitializer {
  private static final Logger log = LoggerFactory.getLogger(DefaultStickerInitializer.class);

  public static final List<DefaultSticker> DEFAULTS =
      List.of(
          new DefaultSticker("😄", "smile", "Smiling face", "smile.png"),
          new DefaultSticker("❤️", "heart", "Red heart", "heart.png"),
          new DefaultSticker("🎉", "party", "Party popper", "party.png"),
          new DefaultSticker("👍", "thumbsup", "Thumbs up", "thumbsup.png"),
          new DefaultSticker("🔥", "fire", "Fire", "fire.png"),
          new DefaultSticker("😢", "cry", "Crying face", "cry.png"),
          new DefaultSticker("😮", "open_mouth", "Face with open mouth", "open_mouth.png"),
          new DefaultSticker("😔", "pensive", "Pensive face", "pensive.png"),
          new DefaultSticker(
              "🫩",
              "face_with_bags_under_eyes",
              "Face with bags under eyes",
              "face_with_bags_under_eyes.png"));

  private final ResourceLoader resourceLoader;

  public DefaultStickerInitializer(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  public Optional<StickerAsset> fallbackFor(String emojiName) {
    return DEFAULTS.stream()
        .filter(def -> def.shortcodeName().equals(emojiName) || def.emojiName().equals(emojiName))
        .findFirst()
        .flatMap(this::load);
  }

  public Optional<DefaultSticker> findByShortcodeName(String shortcodeName) {
    return DEFAULTS.stream().filter(def -> def.shortcodeName().equals(shortcodeName)).findFirst();
  }

  public Optional<StickerAsset> assetForShortcodeName(String shortcodeName) {
    return findByShortcodeName(shortcodeName).flatMap(this::load);
  }

  public boolean hasAsset(DefaultSticker def) {
    try {
      return resourceLoader.getResource("classpath:default-stickers/" + def.fileName()).exists();
    } catch (RuntimeException ex) {
      log.warn(
          "Default sticker resource availability check failed: fileName={}, error={}",
          def.fileName(),
          ex.getMessage());
      return false;
    }
  }

  public List<DefaultSticker> defaults() {
    return DEFAULTS;
  }

  private Optional<StickerAsset> load(DefaultSticker def) {
    try {
      var resource = resourceLoader.getResource("classpath:default-stickers/" + def.fileName());
      if (!resource.exists()) {
        log.warn("Default sticker resource is missing: fileName={}", def.fileName());
        return Optional.empty();
      }
      try (var inputStream = resource.getInputStream()) {
        return Optional.of(
            new StickerAsset(
                def.fileName(),
                inputStream.readAllBytes(),
                true,
                "default-stickers/" + def.fileName()));
      }
    } catch (Exception ex) {
      log.warn(
          "Default sticker resource could not be loaded: fileName={}, error={}",
          def.fileName(),
          ex.getMessage());
      return Optional.empty();
    }
  }

  public record DefaultSticker(
      String emojiName, String shortcodeName, String description, String fileName) {}
}
