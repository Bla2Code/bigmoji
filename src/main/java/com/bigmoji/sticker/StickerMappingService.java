package com.bigmoji.sticker;

import com.bigmoji.domain.entity.StickerMapping;
import com.bigmoji.domain.repository.StickerMappingRepository;
import com.bigmoji.emoji.EmojiNormalizer;
import com.bigmoji.storage.MinioStorageService;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StickerMappingService {
  private static final Logger log = LoggerFactory.getLogger(StickerMappingService.class);

  private final StickerMappingRepository repository;
  private final StickerMappingCache cache;
  private final MinioStorageService storage;
  private final Optional<DefaultStickerInitializer> defaultInitializer;
  private final Random random = new Random();
  private final EmojiNormalizer normalizer = new EmojiNormalizer();

  public StickerMappingService(
      StickerMappingRepository repository,
      StickerMappingCache cache,
      MinioStorageService storage,
      Optional<DefaultStickerInitializer> defaultInitializer) {
    this.repository = repository;
    this.cache = cache;
    this.storage = storage;
    this.defaultInitializer = defaultInitializer;
  }

  public Optional<StickerMapping> pickRandomMapping(String guildId, String emojiName) {
    String normalizedEmojiName = normalizeEmojiName(emojiName);
    Optional<List<StickerMapping>> refreshedMappings =
        refreshedMappings(guildId, normalizedEmojiName);
    if (refreshedMappings.isEmpty()) {
      return Optional.empty();
    }
    List<StickerMapping> mappings = refreshedMappings.get();
    if (mappings.isEmpty()) {
      log.debug(
          "Sticker mapping lookup miss: guildId={}, normalizedEmoji={}",
          guildId,
          normalizedEmojiName);
      return Optional.empty();
    }

    StickerMapping selected = mappings.get(random.nextInt(mappings.size()));
    log.debug(
        "Sticker mapping lookup hit: guildId={}, normalizedEmoji={}, mappingCount={}, selectedObjectKey={}",
        guildId,
        normalizedEmojiName,
        mappings.size(),
        selected.getMinioObjectKey());
    return Optional.of(selected);
  }

  public Optional<StickerAsset> resolveSticker(String guildId, String emojiName) throws Exception {
    String normalizedEmojiName = normalizeEmojiName(emojiName);
    Optional<List<StickerMapping>> refreshedMappings =
        refreshedMappings(guildId, normalizedEmojiName);
    if (refreshedMappings.isEmpty()) {
      return Optional.empty();
    }

    List<StickerMapping> mappings = refreshedMappings.get();
    if (!mappings.isEmpty()) {
      StickerMapping selected = mappings.get(random.nextInt(mappings.size()));
      log.debug(
          "Sticker mapping lookup hit: guildId={}, normalizedEmoji={}, mappingCount={}, selectedObjectKey={}",
          guildId,
          normalizedEmojiName,
          mappings.size(),
          selected.getMinioObjectKey());
      return Optional.of(
          new StickerAsset(
              extractFileName(selected.getMinioObjectKey()),
              storage.downloadFile(selected.getMinioBucketName(), selected.getMinioObjectKey()),
              false,
              selected.getMinioObjectKey()));
    }

    log.debug(
        "Sticker mapping lookup miss: guildId={}, normalizedEmoji={}",
        guildId,
        normalizedEmojiName);
    return defaultInitializer.flatMap(initializer -> initializer.fallbackFor(normalizedEmojiName));
  }

  public StickerMapping create(
      String guildId, String emojiName, MultipartFile file, boolean isDefault) throws Exception {
    String normalizedEmojiName = normalizeEmojiName(emojiName);
    if (normalizedEmojiName.isBlank()) {
      throw new IllegalArgumentException("Emoji name is required");
    }

    String ext = extension(file.getOriginalFilename());
    String objectKey;
    try (InputStream is = file.getInputStream()) {
      objectKey = storage.upload(guildId, ext, is, file.getSize(), file.getContentType());
    }
    StickerMapping mapping = new StickerMapping();
    mapping.setGuildId(guildId);
    mapping.setEmojiName(emojiName.trim());
    mapping.setMinioBucketName(storage.bucketForGuild(guildId));
    mapping.setMinioObjectKey(objectKey);
    mapping.setDefault(isDefault);
    StickerMapping saved = repository.save(mapping);
    cache.refreshGuildEmoji(guildId, normalizedEmojiName);
    return saved;
  }

  public List<StickerMapping> listByGuild(String guildId) {
    return repository.findByGuildId(guildId);
  }

  public byte[] previewBytesFor(StickerMapping mapping) throws Exception {
    return storage.downloadFile(mapping.getMinioBucketName(), mapping.getMinioObjectKey());
  }

  public StickerMapping getById(UUID id) {
    return repository.findById(id).orElseThrow(() -> new StickerMappingNotFoundException(id));
  }

  public void deleteById(UUID id) throws Exception {
    StickerMapping mapping = getById(id);
    storage.delete(mapping.getMinioBucketName(), mapping.getMinioObjectKey());
    repository.deleteById(id);
    cache.refreshGuildEmoji(mapping.getGuildId(), mapping.getEmojiName());
  }

  private String extension(String name) {
    if (name == null || !name.contains(".")) return ".bin";
    return name.substring(name.lastIndexOf('.'));
  }

  private Optional<List<StickerMapping>> refreshedMappings(String guildId, String normalizedEmoji) {
    try {
      cache.refreshGuildEmoji(guildId, normalizedEmoji);
      return Optional.of(cache.find(guildId, normalizedEmoji));
    } catch (RuntimeException ex) {
      log.warn(
          "Sticker mapping lookup failed: guildId={}, normalizedEmoji={}, error={}",
          guildId,
          normalizedEmoji,
          ex.getMessage());
      return Optional.empty();
    }
  }

  private String extractFileName(String objectKey) {
    int lastDot = objectKey.lastIndexOf('.');
    if (lastDot > 0) {
      return "sticker" + objectKey.substring(lastDot);
    }
    return "sticker.bin";
  }

  private String normalizeEmojiName(String emojiName) {
    return normalizer.normalize(emojiName);
  }
}
