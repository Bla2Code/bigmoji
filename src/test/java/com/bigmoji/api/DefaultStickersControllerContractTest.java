package com.bigmoji.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bigmoji.sticker.DefaultStickerInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class DefaultStickersControllerContractTest {
  @Test
  void returnsDefaultsWithPreviewMetadata() {
    DefaultStickerInitializer i = new DefaultStickerInitializer(new DefaultResourceLoader());
    DefaultStickersController c = new DefaultStickersController(i);

    var defaults = c.defaults();

    assertEquals(9, defaults.size());
    assertTrue(defaults.stream().anyMatch(d -> d.shortcodeName().equals("cry")));
    assertTrue(defaults.stream().anyMatch(d -> d.shortcodeName().equals("open_mouth")));
    assertTrue(defaults.stream().anyMatch(d -> d.shortcodeName().equals("pensive")));
    assertTrue(
        defaults.stream().anyMatch(d -> d.shortcodeName().equals("face_with_bags_under_eyes")));
    for (var def : defaults) {
      assertEquals("available", def.stickerPreviewState(), def.shortcodeName());
      assertEquals(
          "/api/stickers/default/" + def.shortcodeName() + "/preview",
          def.stickerPreviewUrl(),
          def.shortcodeName());
    }
  }

  @Test
  void returnsBundledPngBytesForKnownPreview() {
    DefaultStickerInitializer i = new DefaultStickerInitializer(new DefaultResourceLoader());
    DefaultStickersController c = new DefaultStickersController(i);

    var response = c.preview("cry");

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(MediaType.IMAGE_PNG, response.getHeaders().getContentType());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().length > 0);
  }

  @Test
  void rejectsUnknownPreviewNames() {
    DefaultStickerInitializer i = new DefaultStickerInitializer(new DefaultResourceLoader());
    DefaultStickersController c = new DefaultStickersController(i);

    var response = c.preview("../smile");

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }
}
