package com.bigmoji.api;

import com.bigmoji.api.dto.MappingResponse;
import com.bigmoji.auth.AuthSession;
import com.bigmoji.auth.AuthSessionInterceptor;
import com.bigmoji.auth.GuildAuthorizationService;
import com.bigmoji.sticker.StickerMappingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequestMapping("/api/mappings")
@Tag(name = "Sticker Mappings")
public class StickerMappingController {
  private final StickerMappingService service;
  private final GuildAuthorizationService authorizationService;

  public StickerMappingController(
      StickerMappingService service, GuildAuthorizationService authorizationService) {
    this.service = service;
    this.authorizationService = authorizationService;
  }

  @Operation(summary = "Upload sticker mapping")
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public MappingResponse upload(
      @RequestAttribute(AuthSessionInterceptor.REQUEST_ATTRIBUTE) AuthSession session,
      @RequestParam @Pattern(regexp = "^\\d+$") String guildId,
      @RequestParam String emojiName,
      @RequestParam MultipartFile file)
      throws Exception {
    authorizationService.requireManageAccess(session, guildId);
    validateFile(file);
    return MappingResponse.from(service.create(guildId, emojiName, file, false));
  }

  @Operation(summary = "List mappings for guild")
  @GetMapping("/{guildId}")
  public List<MappingResponse> list(
      @RequestAttribute(AuthSessionInterceptor.REQUEST_ATTRIBUTE) AuthSession session,
      @PathVariable String guildId) {
    authorizationService.requireManageAccess(session, guildId);
    return service.listByGuild(guildId).stream().map(MappingResponse::from).toList();
  }

  @Operation(summary = "Delete mapping")
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @RequestAttribute(AuthSessionInterceptor.REQUEST_ATTRIBUTE) AuthSession session,
      @PathVariable UUID id)
      throws Exception {
    authorizationService.requireManageAccess(session, service.getById(id).getGuildId());
    service.deleteById(id);
  }

  private void validateFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File is required");
    }
    if (file.getSize() > 512 * 1024) {
      throw new IllegalArgumentException("File size exceeds maximum allowed size of 512KB");
    }
    String ct = file.getContentType() == null ? "" : file.getContentType();
    if (!(ct.contains("png") || ct.contains("jpeg") || ct.contains("webp"))) {
      throw new IllegalArgumentException("Only PNG, JPG, WEBP are allowed");
    }
  }
}
