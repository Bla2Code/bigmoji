package com.bigmoji.domain.repository;

import com.bigmoji.domain.entity.StickerMapping;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StickerMappingRepository extends JpaRepository<StickerMapping, UUID> {
  List<StickerMapping> findByGuildIdAndEmojiName(String guildId, String emojiName);

  List<StickerMapping> findByGuildId(String guildId);

  void deleteByGuildIdAndEmojiName(String guildId, String emojiName);

  boolean existsByGuildId(String guildId);
}
