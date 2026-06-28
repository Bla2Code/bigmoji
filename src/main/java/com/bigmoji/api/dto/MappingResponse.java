package com.bigmoji.api.dto;

import com.bigmoji.discord.CustomEmojiMetadataProvider.CustomEmojiMetadata;
import com.bigmoji.domain.entity.StickerMapping;
import java.time.Instant;
import java.util.UUID;

public record MappingResponse(
    UUID id,
    String guildId,
    String emojiName,
    boolean isDefault,
    Instant createdAt,
    Instant updatedAt,
    String stickerPreviewUrl,
    String stickerPreviewState,
    ServerEmojiPreview emojiPreview) {
  public static MappingResponse from(StickerMapping mapping) {
    return from(mapping, null, null);
  }

  public static MappingResponse from(
      StickerMapping mapping, String stickerPreviewUrl, CustomEmojiMetadata emojiMetadata) {
    return new MappingResponse(
        mapping.getId(),
        mapping.getGuildId(),
        mapping.getEmojiName(),
        mapping.isDefault(),
        mapping.getCreatedAt(),
        mapping.getUpdatedAt(),
        stickerPreviewUrl,
        hasPreviewUrl(stickerPreviewUrl) ? "available" : "unavailable",
        ServerEmojiPreview.from(emojiMetadata));
  }

  private static boolean hasPreviewUrl(String stickerPreviewUrl) {
    return stickerPreviewUrl != null && !stickerPreviewUrl.isBlank();
  }

  public record ServerEmojiPreview(
      String id,
      String name,
      String shortcode,
      String imageUrl,
      boolean animated,
      boolean available) {
    static ServerEmojiPreview from(CustomEmojiMetadata metadata) {
      if (metadata == null) {
        return null;
      }

      return new ServerEmojiPreview(
          metadata.id(),
          metadata.name(),
          metadata.shortcode(),
          metadata.imageUrl(),
          metadata.animated(),
          metadata.available());
    }
  }
}
