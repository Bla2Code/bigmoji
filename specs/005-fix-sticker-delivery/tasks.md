# Tasks: Fix Sticker Delivery and Bot Name Replacement

**Input**: Design documents from `specs/005-fix-sticker-delivery/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Tests are included per the constitution requirement (80%+ coverage, critical paths 100%).

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Single project**: `src/`, `tests/` at repository root
- Paths follow existing project structure: `src/main/java/com/bigmoji/`, `src/test/java/com/bigmoji/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: No new project setup needed — existing Spring Boot project is already configured.

- [x] T001 Verify current branch is `feature/005-fix-sticker-delivery` and all existing tests pass: `./gradlew test`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [x] T002 [P] Add `downloadFile(String bucket, String objectKey): byte[]` method to `src/main/java/com/bigmoji/storage/MinioStorageService.java` using `MinioClient.getObject()`
- [x] T003 [P] Create `WebhookUsernameSanitizer` utility class in `src/main/java/com/bigmoji/discord/WebhookUsernameSanitizer.java` with static method `sanitize(String rawName): String` (remove @/#, truncate to 28 chars, append " БОТ", max 32 total)
- [x] T004 Create `WebhookStickerSender` service in `src/main/java/com/bigmoji/discord/WebhookStickerSender.java` with `sendAsAuthor()` method per `contracts/webhook-service-contract.md` (depends on T002, T003)
- [x] T005 Modify `StickerSenderService` in `src/main/java/com/bigmoji/discord/StickerSenderService.java` to accept `byte[]` and `String fileName` and use `channel.sendFiles(FileUpload.fromData(...))` instead of `channel.sendMessage(String)`
- [x] T006 [P] Write unit tests for `WebhookUsernameSanitizer` in `src/test/java/com/bigmoji/discord/WebhookUsernameSanitizerTest.java`
- [ ] T007 [P] Write unit tests for `MinioStorageService.downloadFile()` in `src/test/java/com/bigmoji/storage/MinioStorageServiceTest.java`

**Checkpoint**: Foundation ready — user story implementation can now begin

---

## Phase 3: User Story 1 - Send sticker as image file instead of URL (Priority: P1) 🎯 MVP

**Goal**: Stickers are sent as actual image file attachments via Discord's `sendFiles()` API, not as raw Minio presigned URLs.

**Independent Test**: Send a message with a supported emoji and verify that the bot responds with an actual image file attachment, not a URL string.

### Tests for User Story 1

- [ ] T008 [P] [US1] Write unit test for `StickerSenderService.send()` with file bytes in `src/test/java/com/bigmoji/discord/StickerSenderServiceTest.java`
- [ ] T009 [P] [US1] Write unit test for `EmojiMessageListener` verifying `sendFiles()` is called instead of `sendMessage()` in `src/test/java/com/bigmoji/discord/EmojiMessageListenerTest.java`

### Implementation for User Story 1

- [ ] T010 [US1] Modify `EmojiMessageListener.onMessageReceived()` in `src/main/java/com/bigmoji/discord/EmojiMessageListener.java` to: (1) download sticker bytes via `storageService.downloadFile()`, (2) pass bytes + filename to `senderService.send()`, (3) remove presigned URL logic
- [ ] T011 [US1] Update `StickerSenderService.send()` signature and implementation in `src/main/java/com/bigmoji/discord/StickerSenderService.java` to use `channel.sendFiles(FileUpload.fromData(bytes, fileName))`
- [ ] T012 [US1] Add error handling for Minio download failures in `EmojiMessageListener` — log error, skip send, do not crash (FR-007)
- [ ] T013 [US1] Add file size validation — skip send and log warning if sticker exceeds 8MB Discord limit

**Checkpoint**: At this point, User Story 1 should be fully functional — stickers arrive as image files, not URLs

---

## Phase 4: User Story 2 - Send sticker as the original message author with "БОТ" suffix (Priority: P2)

**Goal**: Sticker responses appear under the original message author's name with "БОТ" suffix via Discord webhooks, with fallback to bot name on failure.

**Independent Test**: Send a message with a supported emoji as user "Alice" and verify the sticker appears from "Alice БОТ" via webhook.

### Tests for User Story 2

- [ ] T014 [P] [US2] Write unit tests for `WebhookStickerSender` in `src/test/java/com/bigmoji/discord/WebhookStickerSenderTest.java` covering: success path, webhook creation, cache invalidation, fallback to bot name
- [ ] T015 [P] [US2] Write integration test for webhook send flow in `src/test/java/com/bigmoji/discord/EmojiMessageListenerTest.java` (extend existing tests)

### Implementation for User Story 2

- [ ] T016 [US2] Implement `WebhookStickerSender.sendAsAuthor()` in `src/main/java/com/bigmoji/discord/WebhookStickerSender.java` per `contracts/webhook-service-contract.md` — webhook lookup/creation, cache, send via `webhook.sendFiles()`
- [ ] T017 [US2] Implement webhook cache (`ConcurrentHashMap<String, Webhook>`) with lazy creation and invalidation on failure in `WebhookStickerSender`
- [ ] T018 [US2] Implement fallback logic — if webhook creation/execution fails, log warning and call `channel.sendFiles()` under bot's own name
- [ ] T019 [US2] Integrate `WebhookStickerSender` into `EmojiMessageListener.onMessageReceived()` — after downloading sticker bytes, attempt webhook send before falling back to direct send
- [ ] T020 [US2] Add diagnostic logging for webhook success/fallback paths in `EmojiMessageListener`

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently — stickers arrive as images under author name with "БОТ" suffix

---

## Phase 5: User Story 3 - Preserve existing behavior for non-target messages (Priority: P3)

**Goal**: Listener keeps existing behavior for unsupported inputs while fixing sticker delivery and name replacement.

**Independent Test**: Re-run current listener regression checks for supported and unsupported message inputs and confirm no unrelated behavior changes.

### Tests for User Story 3

- [ ] T021 [P] [US3] Run existing `EmojiMessageListenerTest` and verify all current tests still pass
- [ ] T022 [P] [US3] Add regression test: bot messages are still skipped, non-emoji messages are still ignored, DM messages are still skipped

### Implementation for User Story 3

- [ ] T023 [US3] Verify existing guard clauses in `EmojiMessageListener.onMessageReceived()` are preserved (bot check, single-emoji check, guild check)
- [ ] T024 [US3] Verify original message deletion (`message.delete().queue()`) still occurs after successful sticker delivery

**Checkpoint**: All user stories should now be independently functional with no regressions

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] T025 [P] Update `src/test/java/com/bigmoji/discord/EmojiMessageListenerTest.java` to cover all new code paths (webhook success, webhook fallback, download failure, file too large)
- [ ] T026 Run full test suite: `./gradlew test` — verify all tests pass
- [ ] T027 Run quickstart.md validation flow (manual Discord test with emoji message)
- [ ] T028 [P] Code cleanup — remove unused presigned URL logic from `EmojiMessageListener` if no longer needed elsewhere

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion — BLOCKS all user stories
- **User Stories (Phase 3+)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (P1 → P2 → P3)
- **Polish (Phase 6)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) — No dependencies on other stories
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) — Builds on US1's file download, adds webhook layer
- **User Story 3 (P3)**: Can start after Foundational (Phase 2) — Regression verification, no new code dependencies

### Within Each User Story

- Tests MUST be written and FAIL before implementation
- Models/utilities before services
- Services before listener integration
- Core implementation before integration
- Story complete before moving to next priority

### Parallel Opportunities

- T002, T003, T006, T007 can run in parallel (different files, no dependencies)
- T008, T009 can run in parallel (different test files)
- T014, T015 can run in parallel (different test files)
- T021, T022 can run in parallel (regression tests)
- T025, T028 can run in parallel (polish tasks)

---

## Parallel Example: Foundational Phase

```bash
# Launch all independent foundational tasks together:
Task: "Add downloadFile() to MinioStorageService" (T002)
Task: "Create WebhookUsernameSanitizer" (T003)
Task: "Write WebhookUsernameSanitizer tests" (T006)
Task: "Write MinioStorageService.downloadFile tests" (T007)

# Then (after T002, T003 complete):
Task: "Create WebhookStickerSender service" (T004)
Task: "Modify StickerSenderService to send files" (T005)
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001)
2. Complete Phase 2: Foundational — T002, T003, T005, T006, T007 (skip T004 webhook for MVP)
3. Complete Phase 3: User Story 1 (T008-T013)
4. **STOP and VALIDATE**: Send emoji in Discord, verify image file arrives (not URL)
5. Deploy/demo if ready

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (MVP: stickers as images!)
3. Add User Story 2 → Test independently → Deploy/Demo (webhook impersonation)
4. Add User Story 3 → Regression verification → Deploy/Demo
5. Each story adds value without breaking previous stories

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Verify tests fail before implementing
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Avoid: vague tasks, same file conflicts, cross-story dependencies that break independence
