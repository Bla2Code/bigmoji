package com.bigmoji.sticker;

import com.bigmoji.domain.repository.StickerMappingRepository;
import java.util.List;
import org.springframework.stereotype.Component;

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

  public DefaultStickerInitializer(StickerMappingRepository repository) {
    this.repository = repository;
  }

  public void initializeForGuildIfMissing(String guildId) {
    if (!repository.existsByGuildId(guildId)) {
      // minimal hook; fully populating defaults happens during API upload or bootstrap flow.
    }
  }

  public List<DefaultSticker> defaults() {
    return DEFAULTS;
  }

  public record DefaultSticker(String emojiName, String shortcodeName, String description, String fileName) {}
}
