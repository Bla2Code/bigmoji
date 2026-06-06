package com.bigmoji.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "sticker_mapping",
    indexes = {
      @Index(name = "idx_sticker_mapping_guild_emoji", columnList = "guild_id, emoji_name"),
      @Index(name = "idx_sticker_mapping_guild", columnList = "guild_id")
    })
public class StickerMapping {
  @Id @GeneratedValue private UUID id;

  @NotBlank
  @Column(name = "guild_id", nullable = false, length = 32)
  private String guildId;

  @NotBlank
  @Column(name = "emoji_name", nullable = false, length = 64)
  private String emojiName;

  @Column(name = "minio_bucket_name", nullable = false, length = 128)
  private String minioBucketName;

  @Column(name = "minio_object_key", nullable = false, length = 256)
  private String minioObjectKey;

  @Column(name = "is_default", nullable = false)
  private boolean isDefault;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @PrePersist
  void prePersist() {
    Instant now = Instant.now();
    createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = Instant.now();
  }

  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }
  public String getGuildId() { return guildId; }
  public void setGuildId(String guildId) { this.guildId = guildId; }
  public String getEmojiName() { return emojiName; }
  public void setEmojiName(String emojiName) { this.emojiName = emojiName; }
  public String getMinioBucketName() { return minioBucketName; }
  public void setMinioBucketName(String minioBucketName) { this.minioBucketName = minioBucketName; }
  public String getMinioObjectKey() { return minioObjectKey; }
  public void setMinioObjectKey(String minioObjectKey) { this.minioObjectKey = minioObjectKey; }
  public boolean isDefault() { return isDefault; }
  public void setDefault(boolean aDefault) { isDefault = aDefault; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
