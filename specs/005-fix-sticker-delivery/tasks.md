# Tasks: Fix Sticker Delivery and Bot Name Replacement

**Input**: Design documents from `specs/005-fix-sticker-delivery/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: Tests are required by the project constitution and by the feature spec for sticker delivery, webhook impersonation, and regression behavior.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependency on an incomplete task)
- **[Story]**: User story label for story-scoped tasks only
- Every task includes an exact repository path

---

## Phase 1: Setup

**Purpose**: Confirm the feature branch, inputs, and existing baseline before implementation.

- [X] T001 Confirm branch `feature/005-fix-sticker-delivery` and review feature artifacts in `specs/005-fix-sticker-delivery/plan.md`
- [X] T002 [P] Review nickname and webhook requirements in `specs/005-fix-sticker-delivery/spec.md`
- [X] T003 [P] Review webhook service contract examples in `specs/005-fix-sticker-delivery/contracts/webhook-service-contract.md`

---

## Phase 2: Foundational

**Purpose**: Shared delivery utilities required before user-story implementation.

**Critical**: Complete this phase before starting any user story implementation.

- [X] T004 [P] Add or verify `downloadFile(String bucket, String objectKey): byte[]` in `src/main/java/com/bigmoji/storage/MinioStorageService.java`
- [X] T005 [P] Add or verify file attachment sending with `FileUpload.fromData(...)` in `src/main/java/com/bigmoji/discord/StickerSenderService.java`
- [X] T006 [P] Add or verify `WebhookUsernameSanitizer.sanitize(String rawName)` in `src/main/java/com/bigmoji/discord/WebhookUsernameSanitizer.java`
- [ ] T007 [P] Add unit coverage for Minio file downloads in `src/test/java/com/bigmoji/storage/MinioStorageServiceTest.java`
- [ ] T008 [P] Add unit coverage for direct sticker file sending in `src/test/java/com/bigmoji/discord/StickerSenderServiceTest.java`

**Checkpoint**: Sticker bytes can be downloaded and sent as file attachments independently of listener wiring.

---

## Phase 3: User Story 1 - Send Sticker as Image File Instead of URL (Priority: P1)

**Goal**: Supported emoji replacement sends a Discord file attachment, not a Minio URL string.

**Independent Test**: Send a supported emoji and verify the bot responds with an image file attachment in the same channel.

### Tests for User Story 1

- [ ] T009 [P] [US1] Add listener test for successful custom sticker byte download in `src/test/java/com/bigmoji/discord/EmojiMessageListenerTest.java`
- [ ] T010 [P] [US1] Add listener test proving no URL text message is sent for mapped stickers in `src/test/java/com/bigmoji/discord/EmojiMessageListenerTest.java`
- [ ] T011 [P] [US1] Add listener test for Minio download failure logging and no crash in `src/test/java/com/bigmoji/discord/EmojiMessageListenerTest.java`
- [ ] T012 [P] [US1] Add listener test for sticker payloads larger than Discord's 8MB limit in `src/test/java/com/bigmoji/discord/EmojiMessageListenerTest.java`

### Implementation for User Story 1

- [ ] T013 [US1] Update `EmojiMessageListener.onMessageReceived()` to download sticker bytes before delivery in `src/main/java/com/bigmoji/discord/EmojiMessageListener.java`
- [ ] T014 [US1] Update `EmojiMessageListener.onMessageReceived()` to pass `byte[]` and filename to sticker senders in `src/main/java/com/bigmoji/discord/EmojiMessageListener.java`
- [ ] T015 [US1] Add graceful Minio download failure handling in `src/main/java/com/bigmoji/discord/EmojiMessageListener.java`
- [ ] T016 [US1] Add or verify 8MB Discord file size validation in `src/main/java/com/bigmoji/discord/WebhookStickerSender.java`

**Checkpoint**: User Story 1 is independently functional: stickers arrive as image attachments and URL delivery is gone.

---

## Phase 4: User Story 2 - Send Sticker as Original Author with Discord Bot Badge (Priority: P2)

**Goal**: Sticker responses use webhook delivery with the original guild display name, original avatar, and Discord's standard bot badge only.

**Independent Test**: Send a supported emoji as a member with visible nickname `Ne_Tort` and verify the sticker appears as username `Ne_Tort` plus Discord's standard bot badge.

### Tests for User Story 2

- [ ] T017 [P] [US2] Add webhook sender success-path unit test in `src/test/java/com/bigmoji/discord/WebhookStickerSenderTest.java`
- [ ] T018 [P] [US2] Add webhook sender permission/fallback unit test in `src/test/java/com/bigmoji/discord/WebhookStickerSenderTest.java`
- [ ] T019 [P] [US2] Add webhook cache invalidation unit test in `src/test/java/com/bigmoji/discord/WebhookStickerSenderTest.java`
- [X] T020 [P] [US2] Add sanitizer regression tests for `Ne_Tort`, removing textual `БОТ`, `@/#`, blank, and long names in `src/test/java/com/bigmoji/discord/WebhookUsernameSanitizerTest.java`
- [X] T021 [P] [US2] Add listener regression test proving guild `Member#getEffectiveName()` is passed as webhook author name in `src/test/java/com/bigmoji/discord/EmojiMessageListenerTest.java`
- [X] T022 [P] [US2] Add listener fallback test for unavailable member context using `User#getName()` in `src/test/java/com/bigmoji/discord/EmojiMessageListenerTest.java`

### Implementation for User Story 2

- [X] T023 [US2] Implement webhook lookup, creation, send, and cache handling in `src/main/java/com/bigmoji/discord/WebhookStickerSender.java`
- [X] T024 [US2] Update `EmojiMessageListener.onMessageReceived()` to resolve author name from `event.getMember().getEffectiveName()` for guild messages in `src/main/java/com/bigmoji/discord/EmojiMessageListener.java`
- [X] T025 [US2] Update `EmojiMessageListener.onMessageReceived()` to fall back to `event.getAuthor().getName()` only when member context is unavailable in `src/main/java/com/bigmoji/discord/EmojiMessageListener.java`
- [X] T026 [US2] Update `WebhookUsernameSanitizer` to preserve allowed casing and underscores while removing only Discord-disallowed `@` and `#` in `src/main/java/com/bigmoji/discord/WebhookUsernameSanitizer.java`
- [X] T027 [US2] Update `WebhookUsernameSanitizer` to remove textual `БОТ` and keep final usernames within 32 characters in `src/main/java/com/bigmoji/discord/WebhookUsernameSanitizer.java`
- [X] T028 [US2] Add webhook success and fallback diagnostic logging in `src/main/java/com/bigmoji/discord/WebhookStickerSender.java`

**Checkpoint**: User Story 2 is independently functional: `Ne_Tort` appears as username `Ne_Tort` with Discord's standard bot badge, not `Ne_Tort БОТ` plus the badge.

---

## Phase 5: User Story 3 - Preserve Existing Behavior for Non-Target Messages (Priority: P3)

**Goal**: Unsupported inputs and skipped listener flows remain unchanged while delivery and nickname behavior are fixed.

**Independent Test**: Existing listener regression scenarios still pass for bot authors, DMs, non-emoji messages, multi-emoji messages, and unmapped emoji.

### Tests for User Story 3

- [ ] T029 [P] [US3] Add or verify bot-author skip regression in `src/test/java/com/bigmoji/discord/EmojiMessageListenerTest.java`
- [ ] T030 [P] [US3] Add or verify DM skip regression in `src/test/java/com/bigmoji/discord/EmojiMessageListenerTest.java`
- [ ] T031 [P] [US3] Add or verify non-emoji and multi-emoji skip regressions in `src/test/java/com/bigmoji/discord/EmojiMessageListenerTest.java`
- [ ] T032 [P] [US3] Add or verify unmapped emoji no-send regression in `src/test/java/com/bigmoji/discord/EmojiMessageListenerTest.java`

### Implementation for User Story 3

- [ ] T033 [US3] Preserve listener guard clauses for bot authors, non-single-emoji content, and DMs in `src/main/java/com/bigmoji/discord/EmojiMessageListener.java`
- [ ] T034 [US3] Verify original emoji messages are deleted only after successful sticker delivery in `src/main/java/com/bigmoji/discord/EmojiMessageListener.java`

**Checkpoint**: User Story 3 confirms no unrelated listener behavior changes.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Align docs and run verification after all selected user stories are implemented.

- [X] T035 [P] Update manual validation notes for nickname preservation in `specs/005-fix-sticker-delivery/quickstart.md`
- [X] T036 [P] Ensure webhook username examples remain aligned in `specs/005-fix-sticker-delivery/contracts/webhook-service-contract.md`
- [X] T037 Run targeted Discord tests with `./gradlew test --tests "com.bigmoji.discord.*"` and record outcome in `specs/005-fix-sticker-delivery/quickstart.md`
- [X] T038 Run full test suite with `./gradlew test` and record outcome in `specs/005-fix-sticker-delivery/quickstart.md`
- [X] T039 Run formatter with `./gradlew spotlessApply` before handoff for source files under `src/main/java/com/bigmoji/`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies.
- **Foundational (Phase 2)**: Depends on Setup; blocks all user stories.
- **User Story 1 (Phase 3)**: Depends on Foundational.
- **User Story 2 (Phase 4)**: Depends on Foundational and can be validated independently, but complete end-to-end replacement benefits from US1 file delivery.
- **User Story 3 (Phase 5)**: Depends on Foundational; should be run after US1/US2 changes to catch regressions.
- **Polish (Phase 6)**: Depends on selected user stories.

### User Story Dependencies

- **US1 (P1)**: MVP path for image attachment delivery.
- **US2 (P2)**: Webhook impersonation and nickname preservation; uses shared file delivery utilities.
- **US3 (P3)**: Regression preservation; validates unchanged skip behavior.

### Parallel Opportunities

- T002 and T003 can run in parallel.
- T004 through T008 can run in parallel after setup.
- T009 through T012 can run in parallel before US1 implementation.
- T017 through T022 can run in parallel before US2 implementation.
- T029 through T032 can run in parallel before US3 verification.
- T035 and T036 can run in parallel during polish.

---

## Parallel Example: User Story 2

```text
Task: "T017 [US2] Add webhook sender success-path unit test in src/test/java/com/bigmoji/discord/WebhookStickerSenderTest.java"
Task: "T020 [US2] Add sanitizer regression tests for Ne_Tort, removing textual БОТ, @/#, blank, and long names in src/test/java/com/bigmoji/discord/WebhookUsernameSanitizerTest.java"
Task: "T021 [US2] Add listener regression test proving guild Member#getEffectiveName() is passed as webhook author name in src/test/java/com/bigmoji/discord/EmojiMessageListenerTest.java"
```

---

## Implementation Strategy

### MVP First

1. Complete Phase 1 and Phase 2.
2. Complete User Story 1 to restore image attachment delivery.
3. Validate with targeted listener and sender tests.

### Incremental Delivery

1. Add User Story 2 to preserve original guild display names and webhook identity.
2. Add User Story 3 regression checks before handoff.
3. Run polish verification and update quickstart with test outcomes.

### Suggested Scope for Current Bug

For the reported nickname issue, prioritize T020 through T027, then run T037. This covers `Ne_Tort -> Ne_Tort` with Discord's standard bot badge and prevents textual `БОТ` duplication.
