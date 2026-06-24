package com.bigmoji.sticker;

import java.util.UUID;

public class StickerMappingNotFoundException extends RuntimeException {
  public StickerMappingNotFoundException(UUID id) {
    super("Mapping not found: " + id);
  }
}
