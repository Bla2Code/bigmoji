package com.bigmoji.api.dto;

public record DefaultStickerResponse(
    String emojiName,
    String shortcodeName,
    String description,
    String stickerPreviewUrl,
    String stickerPreviewState) {}
