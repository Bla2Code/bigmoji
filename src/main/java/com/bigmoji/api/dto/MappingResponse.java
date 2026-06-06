package com.bigmoji.api.dto;

import com.bigmoji.domain.entity.StickerMapping;
import java.time.Instant;
import java.util.UUID;

public record MappingResponse(
    UUID id,
    String guildId,
    String emojiName,
    String minioBucketName,
    String minioObjectKey,
    boolean isDefault,
    Instant createdAt,
    Instant updatedAt) {
  public static MappingResponse from(StickerMapping mapping) {
    return new MappingResponse(
        mapping.getId(),
        mapping.getGuildId(),
        mapping.getEmojiName(),
        mapping.getMinioBucketName(),
        mapping.getMinioObjectKey(),
        mapping.isDefault(),
        mapping.getCreatedAt(),
        mapping.getUpdatedAt());
  }
}
