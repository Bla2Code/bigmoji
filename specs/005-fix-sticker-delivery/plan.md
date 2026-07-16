# Implementation Plan: Fix Sticker Delivery and Bot Name Replacement

**Branch**: `feature/005-fix-sticker-delivery` | **Date**: 2026-06-17 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/005-fix-sticker-delivery/spec.md`

## Summary

Fix two critical bugs in the emoji-to-sticker replacement flow: (1) stickers are sent as raw Minio presigned URLs instead of image file attachments, and (2) stickers must appear under the original guild display name with Discord's standard bot badge via Discord webhooks. The author-name fix must preserve visible nicknames such as `Ne_Tort`, not derive webhook usernames from normalized account names or append textual `БОТ`.

## Technical Context

**Language/Version**: Java 21

**Primary Dependencies**: Spring Boot 4.0.6, JDA 5.x (net.dv8tion.jda), Minio Java client, SLF4J/Logback, OkHttp (for Minio file download)

**Storage**: Minio (S3-compatible object storage) for sticker files; no database schema changes

**Testing**: JUnit 5, Mockito, existing listener/service tests

**Target Platform**: Linux container runtime and local macOS development

**Project Type**: Backend service with Discord event listener

**Performance Goals**:
- Discord bot interactions MUST respond within Discord's 3-second interaction window
- Sticker download + send MUST complete within 2 seconds median
- Preserve current listener responsiveness under normal message throughput

**Constraints**:
- Bot requires `MANAGE_WEBHOOKS` permission for webhook-based impersonation
- Sticker files MUST be within Discord's 8MB file size limit
- Webhook usernames MUST be max 32 characters, no `@` or `#`
- Webhook usernames MUST use `event.getMember().getEffectiveName()` for guild messages, falling back to `event.getAuthor().getName()` only when member context is unavailable
- Webhook usernames MUST preserve allowed display-name casing and underscores and MUST NOT append textual `БОТ`
- Must fall back to bot name if webhook creation fails
- Minio presigned URLs expire after 300 seconds

**Scale/Scope**:
- Impacts emoji listener flow, sticker sender service, and adds webhook service
- Scope bounded to sticker delivery path; no changes to emoji detection or mapping logic

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Code Quality - Linting & Formatting | ✅ PASS | Changes follow existing Java/Spring style |
| I. Code Quality - Simplicity First | ✅ PASS | Reuses existing services; adds webhook service only where needed |
| I. Code Quality - Error Handling | ✅ PASS | Webhook failures, download failures, and permission issues all handled with fallbacks |
| II. Testing Standards | ✅ PASS | Unit + integration tests for webhook service, sticker download, and listener flow |
| III. UX Consistency | ✅ PASS | Fixes broken UX (URL instead of image) and improves impersonation consistency |
| IV. Performance Requirements | ✅ PASS | File download and webhook send bounded within Discord's 3s window |

## Project Structure

### Documentation (this feature)

```text
specs/005-fix-sticker-delivery/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── webhook-service-contract.md
── tasks.md
```

### Source Code (repository root)

```text
src/
├── main/
│   ├── java/com/bigmoji/discord/
│   │   ├── EmojiMessageListener.java      # Modified: use new send flow
│   │   ├── StickerSenderService.java      # Modified: send file instead of URL
│   │   ── WebhookStickerSender.java      # NEW: webhook-based impersonation
│   ├── java/com/bigmoji/sticker/
│   │   └── StickerMappingService.java     # No changes expected
│   └── java/com/bigmoji/storage/
│       └── MinioStorageService.java       # Modified: add download method
└── test/
    └── java/com/bigmoji/discord/
        ├── EmojiMessageListenerTest.java  # Modified: new send flow tests
        └── WebhookStickerSenderTest.java  # NEW: webhook service tests
```

**Structure Decision**: Keep single-module Spring Boot backend layout. Add `WebhookStickerSender` service for webhook-based impersonation. Extend `MinioStorageService` with a download method. Modify `StickerSenderService` to send file attachments instead of URLs.

## Phase 0: Research Outcomes

Research outcomes are documented in [research.md](research.md) and resolve:
- JDA 5.x file attachment API (`sendFiles()` vs `sendMessage()`)
- Minio file download approach (presigned URL + OkHttp vs MinioClient.getObject)
- Discord webhook lifecycle management (create vs reuse vs cache)
- Username source and sanitization rules for Discord webhooks, including guild display name precedence and avoiding textual `БОТ` because Discord renders the standard bot badge

## Phase 1: Design & Contracts

Design artifacts:
- [data-model.md](data-model.md): entities and state transitions for sticker delivery flow
- [contracts/webhook-service-contract.md](contracts/webhook-service-contract.md): webhook service interface and behavior contract
- [quickstart.md](quickstart.md): end-to-end validation flow and expected runtime outcomes

## Post-Design Constitution Check Re-Evaluation

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Code Quality - Simplicity First | ✅ PASS | Design adds one new service (WebhookStickerSender) and extends existing services minimally |
| II. Testing Standards | ✅ PASS | Artifacts define measurable checks for all primary and edge flows |
| III. UX Consistency | ✅ PASS | Sticker delivery now consistent: image file + author impersonation |
| IV. Performance Requirements | ✅ PASS | Download + webhook send bounded; fallback path ensures no message loss |

## Complexity Tracking

No constitution violations identified. No complexity exemptions required.
