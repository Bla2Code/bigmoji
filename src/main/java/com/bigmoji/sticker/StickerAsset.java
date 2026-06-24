package com.bigmoji.sticker;

public record StickerAsset(String fileName, byte[] bytes, boolean isDefault, String debugKey) {}
