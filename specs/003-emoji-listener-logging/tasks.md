# Tasks: Emoji Message Listener Logging

**Input**: Design documents from `/specs/003-emoji-listener-logging/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Prepare specification artifacts and test scope for implementation.

- [x] T001 Align verification commands and expected log samples in specs/003-emoji-listener-logging/quickstart.md
- [x] T002 [P] Confirm logging contract context keys and stage names in specs/003-emoji-listener-logging/contracts/logging-contract.md

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Baseline configuration and regression safety required before story work.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [x] T003 Add package-level logger visibility config in src/main/resources/application.yml
- [x] T004 [P] Prepare regression assertions for unchanged listener behavior in src/test/java/com/bigmoji/discord/EmojiMessageListenerTest.java

**Checkpoint**: Foundation ready - user story implementation can now begin.

---

## Phase 3: User Story 1 - Verify message intake visibility (Priority: P1) 🎯 MVP

**Goal**: Emit receipt-stage log entries for every observed Discord message event.

**Independent Test**: Send message events and verify receipt log appears with correlation context for each event.

### Tests for User Story 1

- [x] T005 [P] [US1] Add/extend test case for receipt-stage log emission in src/test/java/com/bigmoji/discord/EmojiMessageListenerTest.java

### Implementation for User Story 1

- [x] T006 [US1] Add receipt-stage structured debug log in src/main/java/com/bigmoji/discord/EmojiMessageListener.java
- [x] T007 [US1] Ensure receipt log includes required context keys from contract in src/main/java/com/bigmoji/discord/EmojiMessageListener.java

**Checkpoint**: User Story 1 is independently functional and testable.

---

## Phase 4: User Story 2 - Verify processing stage visibility (Priority: P2)

**Goal**: Emit processing-start log entries for eligible single-emoji messages.

**Independent Test**: Send eligible single-emoji messages and verify processing-stage log appears after receipt-stage log.

### Tests for User Story 2

- [x] T008 [P] [US2] Add/extend test case for processing-stage log emission order in src/test/java/com/bigmoji/discord/EmojiMessageListenerTest.java

### Implementation for User Story 2

- [x] T009 [US2] Add processing-start structured debug log in src/main/java/com/bigmoji/discord/EmojiMessageListener.java
- [x] T010 [US2] Include normalized emoji and guild context in processing log in src/main/java/com/bigmoji/discord/EmojiMessageListener.java

**Checkpoint**: User Stories 1 and 2 both work independently.

---

## Phase 5: User Story 3 - Preserve existing behavior (Priority: P3)

**Goal**: Keep listener behavior unchanged while adding observability logs.

**Independent Test**: Run previous behavior scenarios and confirm outcomes match baseline with only additional logs.

### Tests for User Story 3

- [x] T011 [P] [US3] Add/extend behavior parity tests for eligible/ineligible message flows in src/test/java/com/bigmoji/discord/EmojiMessageListenerTest.java

### Implementation for User Story 3

- [x] T012 [US3] Refine listener logging placement to avoid any control-flow changes in src/main/java/com/bigmoji/discord/EmojiMessageListener.java
- [x] T013 [US3] Validate and preserve existing error-path logging behavior in src/main/java/com/bigmoji/discord/EmojiMessageListener.java

**Checkpoint**: All user stories are independently functional.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final validation and documentation updates across stories.

- [x] T014 [P] Run listener-focused tests and capture results in specs/003-emoji-listener-logging/quickstart.md
- [x] T015 Run full test suite and verify no regressions from logging changes via build.gradle.kts
- [x] T016 [P] Update implementation notes in specs/003-emoji-listener-logging/plan.md to reflect completed logger-level configuration and verification evidence

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately.
- **Foundational (Phase 2)**: Depends on Setup completion - blocks all user stories.
- **User Stories (Phase 3+)**: Depend on Foundational completion.
- **Polish (Phase 6)**: Depends on all targeted user stories complete.

### User Story Dependencies

- **US1 (P1)**: Starts after Phase 2, no dependency on other stories.
- **US2 (P2)**: Starts after Phase 2, depends on US1 receipt logging structure for ordering assertions.
- **US3 (P3)**: Starts after Phase 2, validates parity across outputs of US1 and US2.

### Parallel Opportunities

- T002 and T004 can run in parallel.
- T005 can run in parallel with implementation preparation tasks after T004.
- T008 and T011 are parallelizable test tasks.
- T014 and T016 can run in parallel in final phase.

---

## Parallel Example: User Story 1

```bash
# Parallelizable tasks for US1:
Task: "T005 [US1] Add/extend test case for receipt-stage log emission in src/test/java/com/bigmoji/discord/EmojiMessageListenerTest.java"
Task: "T006 [US1] Add receipt-stage structured debug log in src/main/java/com/bigmoji/discord/EmojiMessageListener.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 and Phase 2.
2. Complete Phase 3 (US1).
3. Validate intake visibility independently.

### Incremental Delivery

1. Deliver US1 (receipt visibility).
2. Deliver US2 (processing visibility).
3. Deliver US3 (behavior parity hardening).
4. Finish with cross-cutting validation and documentation.

### Parallel Team Strategy

1. One engineer handles listener code tasks in src/main/java/com/bigmoji/discord/EmojiMessageListener.java.
2. Another engineer handles test expansion in src/test/java/com/bigmoji/discord/EmojiMessageListenerTest.java.
3. Documentation/verification updates proceed in parallel during Phase 6.

---

## Notes

- All tasks follow required checklist format with Task ID and explicit file path.
- `[P]` tasks are safe for parallel execution.
- User stories remain independently testable with scoped checkpoints.