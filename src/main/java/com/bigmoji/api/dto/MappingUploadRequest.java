package com.bigmoji.api.dto;

import jakarta.validation.constraints.NotBlank;

public record MappingUploadRequest(@NotBlank String guildId, @NotBlank String emojiName) {}
