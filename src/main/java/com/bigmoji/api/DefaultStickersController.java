package com.bigmoji.api;

import com.bigmoji.api.dto.DefaultStickerResponse;
import com.bigmoji.sticker.DefaultStickerInitializer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stickers")
@Tag(name = "Default Stickers")
public class DefaultStickersController {
  private final DefaultStickerInitializer initializer;

  public DefaultStickersController(DefaultStickerInitializer initializer) {
    this.initializer = initializer;
  }

  @Operation(summary = "List default sticker metadata")
  @GetMapping("/default")
  public List<DefaultStickerResponse> defaults() {
    return initializer.defaults().stream()
        .map(d -> new DefaultStickerResponse(d.emojiName(), d.shortcodeName(), d.description()))
        .toList();
  }
}
