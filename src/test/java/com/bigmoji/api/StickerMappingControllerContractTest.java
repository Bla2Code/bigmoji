package com.bigmoji.api;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bigmoji.api.dto.MappingResponse;
import com.bigmoji.auth.AuthSession;
import com.bigmoji.auth.AuthorizedGuild;
import com.bigmoji.auth.GuildAccessDeniedException;
import com.bigmoji.auth.GuildAuthorizationService;
import com.bigmoji.discord.CustomEmojiMetadataProvider;
import com.bigmoji.discord.CustomEmojiMetadataProvider.CustomEmojiMetadata;
import com.bigmoji.domain.entity.StickerMapping;
import com.bigmoji.sticker.StickerMappingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

class StickerMappingControllerContractTest {
  private static final String GUILD_ID = "123456789012345678";
  private final AuthSession session =
      new AuthSession(
          "user-1",
          "admin",
          "Admin",
          List.of(new AuthorizedGuild(GUILD_ID, "Guild")),
          Instant.now().plusSeconds(60));

  @Test
  void listReturnsEmojiPreviewForCustomEmojiAndFallbackForUnicode() throws Exception {
    StickerMapping custom = mapping(":party_blob:");
    StickerMapping unicode = mapping("😊");
    StickerMappingService service = mock(StickerMappingService.class);
    CustomEmojiMetadataProvider metadataProvider = mock(CustomEmojiMetadataProvider.class);
    when(service.listByGuild(GUILD_ID)).thenReturn(List.of(custom, unicode));
    when(metadataProvider.findFor(GUILD_ID, ":party_blob:"))
        .thenReturn(
            Optional.of(
                new CustomEmojiMetadata(
                    "987654321098765432",
                    "party_blob",
                    ":party_blob:",
                    "https://cdn.discordapp.com/emojis/987.png",
                    false,
                    true)));
    when(metadataProvider.findFor(GUILD_ID, "😊")).thenReturn(Optional.empty());

    List<MappingResponse> responses = controller(service, metadataProvider).list(session, GUILD_ID);

    assertEquals(2, responses.size());
    MappingResponse first = responses.get(0);
    assertEquals("available", first.stickerPreviewState());
    assertEquals("/api/mappings/" + custom.getId() + "/preview", first.stickerPreviewUrl());
    assertEquals(":party_blob:", first.emojiPreview().shortcode());
    assertEquals("https://cdn.discordapp.com/emojis/987.png", first.emojiPreview().imageUrl());
    assertEquals(
        "/api/mappings/" + unicode.getId() + "/preview", responses.get(1).stickerPreviewUrl());
    assertNull(responses.get(1).emojiPreview());
  }

  @Test
  void uploadReturnsPreviewCapableMappingShape() throws Exception {
    StickerMapping mapping = mapping(":party_blob:");
    StickerMappingService service = mock(StickerMappingService.class);
    CustomEmojiMetadataProvider metadataProvider = mock(CustomEmojiMetadataProvider.class);
    MockMultipartFile file =
        new MockMultipartFile("file", "party.png", "image/png", new byte[] {1, 2, 3});
    when(service.create(GUILD_ID, ":party_blob:", file, false)).thenReturn(mapping);
    when(metadataProvider.findFor(GUILD_ID, ":party_blob:"))
        .thenReturn(
            Optional.of(
                new CustomEmojiMetadata(
                    "987654321098765432",
                    "party_blob",
                    ":party_blob:",
                    "https://cdn.discordapp.com/emojis/987.png",
                    false,
                    true)));

    MappingResponse response =
        controller(service, metadataProvider).upload(session, GUILD_ID, ":party_blob:", file);

    assertEquals("available", response.stickerPreviewState());
    assertEquals("/api/mappings/" + mapping.getId() + "/preview", response.stickerPreviewUrl());
    assertEquals(":party_blob:", response.emojiPreview().shortcode());
  }

  @Test
  void previewEndpointServesStickerBytesAfterAuthorization() throws Exception {
    StickerMapping mapping = mapping(":party_blob:");
    StickerMappingService service = mock(StickerMappingService.class);
    CustomEmojiMetadataProvider metadataProvider = mock(CustomEmojiMetadataProvider.class);
    when(service.getById(mapping.getId())).thenReturn(mapping);
    when(service.previewBytesFor(mapping)).thenReturn(new byte[] {1, 2, 3});

    ResponseEntity<byte[]> response =
        controller(service, metadataProvider).preview(session, mapping.getId());

    assertEquals(MediaType.IMAGE_PNG, response.getHeaders().getContentType());
    assertArrayEquals(new byte[] {1, 2, 3}, response.getBody());
  }

  @Test
  void doesNotExposeStorageInternalsInMappingResponseJson() throws Exception {
    StickerMapping mapping = mapping(":party_blob:");
    StickerMappingService service = mock(StickerMappingService.class);
    CustomEmojiMetadataProvider metadataProvider = mock(CustomEmojiMetadataProvider.class);
    when(service.listByGuild(GUILD_ID)).thenReturn(List.of(mapping));
    when(metadataProvider.findFor(GUILD_ID, ":party_blob:")).thenReturn(Optional.empty());

    MappingResponse response = controller(service, metadataProvider).list(session, GUILD_ID).get(0);
    String json = new ObjectMapper().findAndRegisterModules().writeValueAsString(response);

    assertFalse(json.contains("minioBucketName"));
    assertFalse(json.contains("minioObjectKey"));
    assertFalse(json.contains("bucket-guild"));
    assertFalse(json.contains("internal/party.png"));
  }

  @Test
  void doesNotResolvePreviewsBeforeGuildAuthorization() {
    StickerMappingService service = mock(StickerMappingService.class);
    CustomEmojiMetadataProvider metadataProvider = mock(CustomEmojiMetadataProvider.class);
    AuthSession unauthorized =
        new AuthSession(
            "user-1",
            "admin",
            "Admin",
            List.of(new AuthorizedGuild("another-guild", "Other")),
            Instant.now().plusSeconds(60));

    assertThrows(
        GuildAccessDeniedException.class,
        () -> controller(service, metadataProvider).list(unauthorized, GUILD_ID));
    verify(service, never()).listByGuild(any());
    verify(metadataProvider, never()).findFor(any(), any());
  }

  @Test
  void previewEndpointDoesNotDownloadBeforeGuildAuthorization() throws Exception {
    StickerMapping mapping = mapping(":party_blob:");
    StickerMappingService service = mock(StickerMappingService.class);
    CustomEmojiMetadataProvider metadataProvider = mock(CustomEmojiMetadataProvider.class);
    when(service.getById(mapping.getId())).thenReturn(mapping);
    AuthSession unauthorized =
        new AuthSession(
            "user-1",
            "admin",
            "Admin",
            List.of(new AuthorizedGuild("another-guild", "Other")),
            Instant.now().plusSeconds(60));

    assertThrows(
        GuildAccessDeniedException.class,
        () -> controller(service, metadataProvider).preview(unauthorized, mapping.getId()));
    verify(service, never()).previewBytesFor(any());
  }

  private StickerMappingController controller(
      StickerMappingService service, CustomEmojiMetadataProvider metadataProvider) {
    return new StickerMappingController(service, new GuildAuthorizationService(), metadataProvider);
  }

  private StickerMapping mapping(String emojiName) {
    StickerMapping mapping = new StickerMapping();
    mapping.setId(UUID.randomUUID());
    mapping.setGuildId(GUILD_ID);
    mapping.setEmojiName(emojiName);
    mapping.setMinioBucketName("bucket-guild");
    mapping.setMinioObjectKey("internal/party.png");
    mapping.setDefault(false);
    mapping.setCreatedAt(Instant.parse("2026-06-24T16:00:00Z"));
    mapping.setUpdatedAt(Instant.parse("2026-06-24T16:00:00Z"));
    return mapping;
  }
}
