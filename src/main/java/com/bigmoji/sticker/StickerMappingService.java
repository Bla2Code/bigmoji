package com.bigmoji.sticker;

import com.bigmoji.domain.entity.StickerMapping;
import com.bigmoji.domain.repository.StickerMappingRepository;
import com.bigmoji.storage.MinioStorageService;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StickerMappingService {
  private final StickerMappingRepository repository;
  private final StickerMappingCache cache;
  private final MinioStorageService storage;
  private final Optional<DefaultStickerInitializer> defaultInitializer;
  private final Random random = new Random();

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
    ensureDefaults(guildId);
    List<StickerMapping> mappings = cache.find(guildId, emojiName);
    if (mappings.isEmpty()) return Optional.empty();
    return Optional.of(mappings.get(random.nextInt(mappings.size())));
  }

  public StickerMapping create(String guildId, String emojiName, MultipartFile file, boolean isDefault)
      throws Exception {
    String ext = extension(file.getOriginalFilename());
    String objectKey;
    try (InputStream is = file.getInputStream()) {
      objectKey = storage.upload(guildId, ext, is, file.getSize(), file.getContentType());
    }
    StickerMapping mapping = new StickerMapping();
    mapping.setGuildId(guildId);
    mapping.setEmojiName(emojiName);
    mapping.setMinioBucketName(storage.bucketForGuild(guildId));
    mapping.setMinioObjectKey(objectKey);
    mapping.setDefault(isDefault);
    StickerMapping saved = repository.save(mapping);
    cache.refreshGuildEmoji(guildId, emojiName);
    return saved;
  }

  public List<StickerMapping> listByGuild(String guildId) {
    return repository.findByGuildId(guildId);
  }

  public void deleteById(UUID id) throws Exception {
    StickerMapping mapping =
        repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Mapping not found"));
    storage.delete(mapping.getMinioBucketName(), mapping.getMinioObjectKey());
    repository.deleteById(id);
    cache.refreshGuildEmoji(mapping.getGuildId(), mapping.getEmojiName());
  }

  private String extension(String name) {
    if (name == null || !name.contains(".")) return ".bin";
    return name.substring(name.lastIndexOf('.'));
  }

  private void ensureDefaults(String guildId) {
    defaultInitializer.ifPresent(initializer -> initializer.initializeForGuildIfMissing(guildId));
  }
}
