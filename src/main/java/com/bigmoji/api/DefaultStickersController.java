package com.bigmoji.api;

import com.bigmoji.api.dto.DefaultStickerResponse;
import com.bigmoji.sticker.DefaultStickerInitializer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
        .map(
            d -> {
              boolean available = initializer.hasAsset(d);
              return new DefaultStickerResponse(
                  d.emojiName(),
                  d.shortcodeName(),
                  d.description(),
                  available ? previewUrl(d.shortcodeName()) : null,
                  available ? "available" : "unavailable");
            })
        .toList();
  }

  @Operation(summary = "Preview bundled default sticker")
  @GetMapping(value = "/default/{shortcodeName}/preview", produces = MediaType.IMAGE_PNG_VALUE)
  public ResponseEntity<byte[]> preview(@PathVariable String shortcodeName) {
    return initializer
        .assetForShortcodeName(shortcodeName)
        .map(asset -> ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(asset.bytes()))
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  private String previewUrl(String shortcodeName) {
    return "/api/stickers/default/" + shortcodeName + "/preview";
  }
}
