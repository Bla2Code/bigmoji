---
description: "Task list for Discord Emoji-to-Sticker Bot (Bigmoji)"
---

# Tasks: Discord Emoji-to-Sticker Bot (Bigmoji)

**Input**: Design documents from `/specs/002-discord-emoji-stickers/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/api-contract.md, quickstart.md, .specify/memory/constitution.md

**Tests**: Included because the specification and constitution explicitly require unit/integration/contract coverage.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: User story label (US1, US2, US3, US4)
- Every task includes an exact file path

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Initialize Spring Boot + Discord bot project scaffolding and baseline tooling.

- [X] T001 Initialize Gradle Spring Boot project skeleton in build.gradle.kts and settings.gradle.kts
- [X] T002 Configure base application and environment placeholders in src/main/resources/application.yml and .env.example
- [X] T003 [P] Configure code formatting and static analysis plugins in build.gradle.kts
- [X] T004 [P] Add containerized local infrastructure definitions in docker-compose.yml and Dockerfile
- [X] T005 Create application entrypoint and package structure in src/main/java/com/bigmoji/BigmojiApplication.java

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure required before implementing any user story.

**⚠️ CRITICAL**: No user story work starts before this phase is complete.

- [X] T006 Create StickerMapping JPA entity with indexes and validation fields in src/main/java/com/bigmoji/domain/entity/StickerMapping.java
- [X] T007 [P] Create StickerMappingRepository query interface in src/main/java/com/bigmoji/domain/repository/StickerMappingRepository.java
- [X] T008 [P] Implement MinIO client configuration in src/main/java/com/bigmoji/config/MinioConfig.java
- [X] T009 [P] Implement Discord JDA configuration and gateway intents in src/main/java/com/bigmoji/config/JdaConfig.java
- [X] T010 Implement async executor configuration for message processing in src/main/java/com/bigmoji/config/AsyncConfig.java
- [X] T011 Implement API key authentication interceptor in src/main/java/com/bigmoji/config/ApiKeyInterceptor.java
- [X] T012 Configure interceptor registration and API route protection in src/main/java/com/bigmoji/config/WebMvcConfig.java
- [X] T013 Implement global API error response contract in src/main/java/com/bigmoji/api/exception/GlobalExceptionHandler.java and src/main/java/com/bigmoji/api/dto/ErrorResponse.java
- [X] T014 Implement MinIO storage service (upload/delete/presigned URL) in src/main/java/com/bigmoji/storage/MinioStorageService.java
- [X] T015 Implement in-memory mapping cache bootstrap and refresh logic in src/main/java/com/bigmoji/sticker/StickerMappingCache.java

**Checkpoint**: Foundation ready; user stories can now be implemented.

---

## Phase 3: User Story 1 - Single Emoji Message Auto-Replacement (Priority: P1) 🎯 MVP

**Goal**: Detect exact single-emoji messages, delete the original message, and send a mapped sticker in the same channel.

**Independent Test**: Send one Unicode emoji and one shortcode-only message and verify delete + sticker post when mapping exists; verify no delete when mapping is absent.

### Tests for User Story 1

- [X] T016 [P] [US1] Add unit tests for single-emoji and shortcode detection in src/test/java/com/bigmoji/emoji/EmojiDetectorTest.java
- [X] T017 [P] [US1] Add unit tests for mapping selection and no-mapping behavior in src/test/java/com/bigmoji/sticker/StickerMappingServiceTest.java
- [X] T018 [US1] Add integration test for message delete + sticker send flow in src/test/java/com/bigmoji/discord/EmojiMessageListenerTest.java

### Implementation for User Story 1

- [X] T019 [P] [US1] Implement emoji normalization rules for Unicode and shortcode input in src/main/java/com/bigmoji/emoji/EmojiNormalizer.java
- [X] T020 [P] [US1] Implement exact single-emoji detection logic in src/main/java/com/bigmoji/emoji/EmojiDetector.java
- [X] T021 [US1] Implement sticker lookup and random selection service in src/main/java/com/bigmoji/sticker/StickerMappingService.java
- [X] T022 [US1] Implement Discord sticker sender using attachment workflow in src/main/java/com/bigmoji/discord/StickerSenderService.java
- [X] T023 [US1] Implement Discord message listener flow for detect→lookup→delete→send in src/main/java/com/bigmoji/discord/EmojiMessageListener.java

**Checkpoint**: User Story 1 should be independently functional and demo-ready (MVP).

---

## Phase 4: User Story 2 - Multi-Emoji and Text Message Ignored (Priority: P1)

**Goal**: Ensure the bot ignores messages that are not exactly one emoji (multi-emoji, text+emoji, embedded emoji, whitespace-only).

**Independent Test**: Send multi-emoji and mixed-content messages and verify no deletion and no sticker response.

### Tests for User Story 2

- [X] T024 [P] [US2] Extend detector unit tests for multi-emoji and mixed-content negative cases in src/test/java/com/bigmoji/emoji/EmojiDetectorTest.java
- [X] T025 [US2] Add listener integration tests validating ignore behavior for invalid patterns in src/test/java/com/bigmoji/discord/EmojiMessageListenerTest.java

### Implementation for User Story 2

- [X] T026 [US2] Harden detection guards for mixed-content and whitespace edge cases in src/main/java/com/bigmoji/emoji/EmojiDetector.java
- [X] T027 [US2] Add listener early-return logic for ignored message categories and self-message loop prevention in src/main/java/com/bigmoji/discord/EmojiMessageListener.java
- [X] T028 [US2] Add structured debug logging for ignored-message reasons in src/main/java/com/bigmoji/discord/EmojiMessageListener.java

**Checkpoint**: User Story 2 is independently testable with zero false-positive replacements for invalid message shapes.

---

## Phase 5: User Story 3 - Server Admin Manages Sticker Mappings via REST API (Priority: P2)

**Goal**: Provide authenticated REST API to upload, list, and delete guild-scoped sticker mappings.

**Independent Test**: Call upload/list/delete API endpoints with valid API key and verify persistence and storage side effects; verify unauthorized calls fail with 401.

### Tests for User Story 3

- [X] T029 [P] [US3] Add API contract tests for POST/GET/DELETE mapping endpoints in src/test/java/com/bigmoji/api/StickerMappingControllerContractTest.java
- [X] T030 [P] [US3] Add integration tests for repository/cache synchronization after API writes in src/test/java/com/bigmoji/api/StickerMappingControllerIntegrationTest.java
- [X] T031 [US3] Add authentication interceptor tests for missing/invalid API key in src/test/java/com/bigmoji/config/ApiKeyInterceptorTest.java

### Implementation for User Story 3

- [X] T032 [P] [US3] Create API DTOs for mapping upload and response payloads in src/main/java/com/bigmoji/api/dto/MappingUploadRequest.java and src/main/java/com/bigmoji/api/dto/MappingResponse.java
- [X] T033 [US3] Implement REST controller endpoints for upload/list/delete mappings in src/main/java/com/bigmoji/api/StickerMappingController.java
- [X] T034 [US3] Implement service-layer upload/list/delete workflow with DB + MinIO + cache updates in src/main/java/com/bigmoji/sticker/StickerMappingService.java
- [X] T035 [US3] Add upload validation for file format/size and guildId/emojiName constraints in src/main/java/com/bigmoji/api/StickerMappingController.java

**Checkpoint**: User Story 3 APIs are independently usable for guild-specific mapping management.

---

## Phase 6: User Story 4 - Local Fallback Stickers for First Use (Priority: P3)

**Goal**: Provide built-in local fallback stickers for popular emojis without persisting defaults in the database.

**Independent Test**: For a new guild with no mappings, verify fallback stickers are used; after adding custom mapping(s) for an emoji, verify only DB-mapped stickers are used for that emoji.

### Tests for User Story 4

- [X] T036 [P] [US4] Add integration tests for fallback resolution rules (DB-first, fallback-second) in src/test/java/com/bigmoji/sticker/DefaultStickerInitializerTest.java
- [X] T037 [US4] Add API contract test verifying mapping list excludes fallback resources in src/test/java/com/bigmoji/api/StickerMappingControllerContractTest.java

### Implementation for User Story 4

- [X] T038 [P] [US4] Add local fallback sticker image assets for popular emojis in src/main/resources/default-stickers/smile.png, src/main/resources/default-stickers/heart.png, src/main/resources/default-stickers/party.png, src/main/resources/default-stickers/thumbsup.png, and src/main/resources/default-stickers/fire.png
- [X] T039 [US4] Implement runtime fallback catalog loader (no DB seeding) in src/main/java/com/bigmoji/sticker/DefaultStickerInitializer.java
- [X] T040 [US4] Implement API endpoint for listing fallback sticker metadata in src/main/java/com/bigmoji/api/DefaultStickersController.java
- [X] T041 [US4] Integrate DB-first lookup with local fallback and DB-failure no-fallback behavior in src/main/java/com/bigmoji/discord/EmojiMessageListener.java and src/main/java/com/bigmoji/sticker/StickerMappingService.java

**Checkpoint**: User Story 4 provides immediate first-use value with runtime fallback resources and no default DB records.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Improve operability, quality gates, and production readiness across all stories.

- [X] T042 [P] Add OpenAPI documentation annotations and endpoint examples in src/main/java/com/bigmoji/api/StickerMappingController.java and src/main/java/com/bigmoji/api/DefaultStickersController.java
- [X] T043 Add actuator health contributors for PostgreSQL and MinIO readiness in src/main/java/com/bigmoji/config/HealthConfig.java
- [X] T044 [P] Add performance-focused integration tests for cache-hit lookup latency and concurrent message handling in src/test/java/com/bigmoji/perf/MessageProcessingPerformanceTest.java
- [X] T045 Validate quickstart runbook and update operational notes in specs/002-discord-emoji-stickers/quickstart.md

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: no dependencies
- **Phase 2 (Foundational)**: depends on Phase 1; blocks all user stories
- **Phases 3-6 (User Stories)**: depend on Phase 2 completion
- **Phase 7 (Polish)**: depends on completion of desired user stories

### User Story Dependencies

- **US1 (P1)**: starts after Foundational; no story dependency
- **US2 (P1)**: starts after Foundational; logically independent but shares detector/listener components with US1
- **US3 (P2)**: starts after Foundational; independent API path, enables admin management used by US1/US4 in real deployments
- **US4 (P3)**: starts after Foundational; depends on storage/repository/service infrastructure and integrates with US1 runtime flow using DB-first fallback logic

### Recommended Completion Order

1. Setup → Foundational
2. US1 (MVP)
3. US2
4. US3
5. US4
6. Polish

### Within Each User Story

- Tests first (should fail initially)
- Core domain/model/service logic
- Controller/listener integration
- Validation + logging + edge-case handling

---

## Parallel Opportunities

- **Setup**: T003 and T004 can run in parallel after T001/T002 start
- **Foundational**: T007, T008, and T009 can run in parallel after T006
- **US1**: T016 and T017 in parallel; T019 and T020 in parallel
- **US2**: T024 parallel with updates in T026 when coordinated
- **US3**: T029 and T030 in parallel; T032 parallelizable before T033/T034
- **US4**: T036 and T038 in parallel before T039/T041
- **Polish**: T042 and T044 can run in parallel

### Parallel Example: User Story 1

```bash
Task: "T016 [US1] Emoji detector unit tests in src/test/java/com/bigmoji/emoji/EmojiDetectorTest.java"
Task: "T017 [US1] Mapping service unit tests in src/test/java/com/bigmoji/sticker/StickerMappingServiceTest.java"

Task: "T019 [US1] Emoji normalization in src/main/java/com/bigmoji/emoji/EmojiNormalizer.java"
Task: "T020 [US1] Single-emoji detector in src/main/java/com/bigmoji/emoji/EmojiDetector.java"
```

### Parallel Example: User Story 3

```bash
Task: "T029 [US3] Contract tests in src/test/java/com/bigmoji/api/StickerMappingControllerContractTest.java"
Task: "T030 [US3] Integration tests in src/test/java/com/bigmoji/api/StickerMappingControllerIntegrationTest.java"

Task: "T032 [US3] DTOs in src/main/java/com/bigmoji/api/dto/MappingUploadRequest.java and src/main/java/com/bigmoji/api/dto/MappingResponse.java"
Task: "T031 [US3] API key interceptor tests in src/test/java/com/bigmoji/config/ApiKeyInterceptorTest.java"
```

### Parallel Example: User Story 4

```bash
Task: "T036 [US4] Default seeding integration tests in src/test/java/com/bigmoji/sticker/DefaultStickerInitializerTest.java"
Task: "T038 [US4] Default asset bundle in src/main/resources/default-stickers/"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 (Setup)
2. Complete Phase 2 (Foundational)
3. Complete Phase 3 (US1)
4. Validate end-to-end single-emoji replacement in Discord test guild
5. Ship MVP

### Incremental Delivery

1. Deliver US1 (core replacement)
2. Deliver US2 (false-positive protection)
3. Deliver US3 (admin mapping management API)
4. Deliver US4 (runtime fallback first-use onboarding)
5. Run Polish tasks for operational hardening

### Validation Gates Per Increment

- Unit/integration/contract tests passing for the story
- Story-independent acceptance criteria verified
- No regressions in previously delivered stories
