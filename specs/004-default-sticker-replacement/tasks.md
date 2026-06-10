# Tasks: Default Emoji Sticker Replacement and Diagnostic Logging

**Input**: Design documents from `/specs/004-default-sticker-replacement/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Prepare baseline verification and scope alignment for implementation.

- [x] T001 Align validation scenarios and expected outcomes in specs/004-default-sticker-replacement/quickstart.md
- [x] T002 [P] Confirm decision states and checkpoint logs in specs/004-default-sticker-replacement/contracts/replacement-logging-contract.md

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core baseline updates required before user-story implementation.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [x] T003 Add/adjust default emoji mapping bootstrap for bundled keys in src/main/java/com/bigmoji/sticker/DefaultStickerInitializer.java
- [x] T004 [P] Verify mapping-cache lookup behavior for normalized emoji keys in src/main/java/com/bigmoji/sticker/StickerMappingCache.java
- [x] T005 [P] Add listener-level regression baseline assertions for unsupported inputs in src/test/java/com/bigmoji/discord/EmojiMessageListenerTest.java

**Checkpoint**: Foundation ready - user story implementation can now begin.

---

## Phase 3: User Story 1 - Replace known default emojis with stickers (Priority: P1) 🎯 MVP

**Goal**: Ensure supported emojis are detected, mapped, and replaced with corresponding bundled default stickers.

**Independent Test**: Send messages with supported default emojis and verify corresponding sticker replacement for each key.

### Tests for User Story 1

- [x] T006 [P] [US1] Add mapped-emoji replacement test cases for supported default keys in src/test/java/com/bigmoji/discord/EmojiMessageListenerTest.java
- [x] T007 [P] [US1] Add normalization-to-mapping lookup tests in src/test/java/com/bigmoji/sticker/StickerMappingServiceTest.java

### Implementation for User Story 1

- [x] T008 [US1] Update emoji extraction and normalization flow wiring in src/main/java/com/bigmoji/discord/EmojiMessageListener.java
- [x] T009 [US1] Implement normalized-code default mapping resolution path in src/main/java/com/bigmoji/sticker/StickerMappingService.java
- [x] T010 [US1] Ensure mapped default sticker send is triggered in src/main/java/com/bigmoji/discord/EmojiMessageListener.java
- [x] T011 [US1] Handle multi-emoji message behavior per existing listener rules in src/main/java/com/bigmoji/discord/EmojiMessageListener.java

**Checkpoint**: User Story 1 is independently functional and testable.

---

## Phase 4: User Story 2 - Distinguish mapped, unmapped, and fallback paths in logs (Priority: P2)

**Goal**: Emit clear logs for mapped, unmapped, and no-emoji outcomes at all key processing checkpoints.

**Independent Test**: Run mapped, unmapped, and no-emoji scenarios and verify logs explicitly show terminal decision path and intermediate checkpoints.

### Tests for User Story 2

- [x] T012 [P] [US2] Add decision-path log assertions for mapped/unmapped/no-emoji states in src/test/java/com/bigmoji/discord/EmojiMessageListenerTest.java
- [x] T013 [P] [US2] Add missing-resource log-path assertion in src/test/java/com/bigmoji/discord/EmojiMessageListenerTest.java

### Implementation for User Story 2

- [x] T014 [US2] Add message-received and emoji-detection logs with correlation context in src/main/java/com/bigmoji/discord/EmojiMessageListener.java
- [x] T015 [US2] Add normalization and mapping-hit/miss logs in src/main/java/com/bigmoji/discord/EmojiMessageListener.java
- [x] T016 [US2] Add terminal decision-state logging (`MAPPED_DEFAULT`, `UNMAPPED_EMOJI`, `NO_EMOJI`, `RESOURCE_MISSING`) in src/main/java/com/bigmoji/discord/EmojiMessageListener.java

**Checkpoint**: User Stories 1 and 2 are independently functional and testable.

---

## Phase 5: User Story 3 - Preserve existing non-target behavior (Priority: P3)

**Goal**: Keep behavior unchanged for unsupported/non-target inputs while adding replacement and logging capabilities.

**Independent Test**: Re-run listener regression scenarios and verify unchanged behavior for unsupported inputs with no incorrect sticker sends.

### Tests for User Story 3

- [x] T017 [P] [US3] Extend behavior-parity regression coverage for unsupported and plain-text messages in src/test/java/com/bigmoji/discord/EmojiMessageListenerTest.java
- [x] T018 [P] [US3] Add negative assertion tests to prevent incorrect sticker sends for unmapped content in src/test/java/com/bigmoji/discord/EmojiMessageListenerTest.java

### Implementation for User Story 3

- [x] T019 [US3] Refine control-flow guards to preserve pre-existing skip conditions in src/main/java/com/bigmoji/discord/EmojiMessageListener.java
- [x] T020 [US3] Add graceful fallback for missing default sticker resources without listener crash in src/main/java/com/bigmoji/sticker/StickerMappingService.java

**Checkpoint**: All user stories are independently functional.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final consistency checks and documentation updates across all stories.

- [x] T021 [P] Synchronize final verification steps and decision-state examples in specs/004-default-sticker-replacement/quickstart.md
- [x] T022 Run targeted listener and sticker tests for this feature via src/test/java/com/bigmoji/discord/EmojiMessageListenerTest.java and src/test/java/com/bigmoji/sticker/StickerMappingServiceTest.java
- [x] T023 [P] Update implementation notes and outcomes in specs/004-default-sticker-replacement/plan.md

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately.
- **Foundational (Phase 2)**: Depends on Setup completion - blocks all user stories.
- **User Stories (Phase 3+)**: Depend on Foundational completion.
- **Polish (Phase 6)**: Depends on all target user stories complete.

### User Story Dependencies

- **US1 (P1)**: Starts after Phase 2, no dependency on other stories.
- **US2 (P2)**: Starts after Phase 2, depends on US1 replacement path for mapped/unmapped observability.
- **US3 (P3)**: Starts after Phase 2, validates parity over behavior introduced in US1/US2.

### Parallel Opportunities

- T002, T004, and T005 can run in parallel after T001/T003 sequencing constraints.
- US1 tests T006 and T007 can run in parallel.
- US2 tests T012 and T013 can run in parallel.
- US3 tests T017 and T018 can run in parallel.
- T021 and T023 are parallelizable in the polish phase.

---

## Parallel Example: User Story 1

```bash
Task: "T006 [US1] Add mapped-emoji replacement test cases for supported default keys in src/test/java/com/bigmoji/discord/EmojiMessageListenerTest.java"
Task: "T007 [US1] Add normalization-to-mapping lookup tests in src/test/java/com/bigmoji/sticker/StickerMappingServiceTest.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 and Phase 2.
2. Complete Phase 3 (US1).
3. Validate supported emoji replacement independently.

### Incremental Delivery

1. Deliver US1 (default sticker replacement).
2. Deliver US2 (decision-path logging).
3. Deliver US3 (non-target behavior parity hardening).
4. Finish with Phase 6 verification and documentation.

### Parallel Team Strategy

1. Engineer A implements listener flow tasks in src/main/java/com/bigmoji/discord/EmojiMessageListener.java.
2. Engineer B implements mapping-service tasks in src/main/java/com/bigmoji/sticker/StickerMappingService.java.
3. Engineer C expands regression tests in src/test/java/com/bigmoji/discord/EmojiMessageListenerTest.java.

---

## Notes

- All tasks follow required checklist format with Task ID and explicit file path.
- `[P]` tasks are safe for parallel execution.
- Each user story remains independently testable by its stated checkpoint criteria.