# Tasks: Display Custom Assets

**Input**: Design documents from `specs/007-display-custom-assets/`
**Prerequisites**: [plan.md](plan.md), [spec.md](spec.md), [research.md](research.md), [data-model.md](data-model.md), [contracts/](contracts/), [quickstart.md](quickstart.md)
**Tests**: Included because the project constitution requires tests and the plan defines backend/UI coverage for this feature.
**Organization**: Tasks are grouped by user story to enable independent implementation and validation.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel with other tasks in the same phase because it touches different files or only test data.
- **[Story]**: Maps to the user story from `spec.md`.
- Every task includes exact file paths.

## Phase 1: Setup (Shared Preview Contract)

**Purpose**: Establish shared response and fixture shapes used by all stories.

- [X] T001 Add preview-capable `StickerMapping`, `ServerEmojiPreview`, and `StickerPreviewState` TypeScript fields in `ui/src/api/types.ts`
- [X] T002 [P] Add custom emoji, Unicode trigger, available sticker, and unavailable preview fixture mappings in `ui/src/test/fixtures.ts`
- [X] T003 [P] Add preview response fields and nested preview records to `src/main/java/com/bigmoji/api/dto/MappingResponse.java`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Add shared backend and frontend plumbing that all user stories depend on.

**Critical**: No user story implementation should begin until this phase is complete.

- [X] T004 Create `CustomEmojiMetadataProvider` skeleton with a guild-scoped lookup method in `src/main/java/com/bigmoji/discord/CustomEmojiMetadataProvider.java`
- [X] T005 Add response assembly hooks for sticker preview state and emoji preview metadata in `src/main/java/com/bigmoji/api/StickerMappingController.java`
- [X] T006 Update mapping API fixture expectations for preview-capable payloads in `ui/src/api/mappings.test.ts`
- [X] T007 Ensure mapping list grouping uses a stable display key derived from preview metadata or `emojiName` in `ui/src/features/mappings/MappingList.tsx`

**Checkpoint**: Backend and UI can compile against the new preview-capable mapping shape.

---

## Phase 3: User Story 1 - Recognize Server Custom Emoji Triggers (Priority: P1)

**Goal**: Admins see the actual server custom emoji image and readable name in each mapping group header.

**Independent Test**: Open a server with at least one custom emoji mapping and verify the group header shows the custom emoji image plus readable shortcode/name, while Unicode/text triggers still render readably.

### Tests for User Story 1

Write these tests first and verify they fail before implementation.

- [X] T008 [P] [US1] Add `CustomEmojiMetadataProvider` tests for custom shortcode match, animated image URL, missing guild, and Unicode fallback in `src/test/java/com/bigmoji/discord/CustomEmojiMetadataProviderTest.java`
- [X] T009 [P] [US1] Add backend contract tests for available `emojiPreview` and absent `emojiPreview` fallback in `src/test/java/com/bigmoji/api/StickerMappingControllerContractTest.java`
- [X] T010 [P] [US1] Add mapping list tests for custom emoji image/name rendering and Unicode/text fallback rendering in `ui/src/features/mappings/MappingList.test.tsx`

### Implementation for User Story 1

- [X] T011 [US1] Implement JDA guild custom emoji lookup, shortcode normalization, image URL selection, and unavailable metadata handling in `src/main/java/com/bigmoji/discord/CustomEmojiMetadataProvider.java`
- [X] T012 [US1] Populate `emojiPreview` in `MappingResponse` without changing persisted `emojiName` in `src/main/java/com/bigmoji/api/dto/MappingResponse.java`
- [X] T013 [US1] Call `CustomEmojiMetadataProvider` only after guild authorization succeeds in `src/main/java/com/bigmoji/api/StickerMappingController.java`
- [X] T014 [US1] Render custom emoji image, readable shortcode/name, and fallback trigger in mapping group headers in `ui/src/features/mappings/MappingList.tsx`
- [X] T015 [US1] Add stable custom emoji preview cell styles for desktop and mobile layouts in `ui/src/features/mappings/mappings.css`

**Checkpoint**: User Story 1 is independently functional and testable.

---

## Phase 4: User Story 2 - Preview Uploaded Stickers in Mappings (Priority: P1)

**Goal**: Admins see the actual uploaded sticker preview for each mapping after upload and after reopening the mapping list.

**Independent Test**: Upload a valid sticker for a selected guild, confirm the new mapping row shows the sticker preview, refresh the view, and confirm the same preview remains visible.

### Tests for User Story 2

Write these tests first and verify they fail before implementation.

- [X] T016 [P] [US2] Add backend contract tests for `stickerPreviewUrl` and `stickerPreviewState` on list and upload responses in `src/test/java/com/bigmoji/api/StickerMappingControllerContractTest.java`
- [X] T017 [P] [US2] Add service/controller tests that verify protected preview bytes are loaded only through the authorized preview endpoint in `src/test/java/com/bigmoji/sticker/StickerMappingServiceTest.java`
- [X] T018 [P] [US2] Add mapping list tests for sticker preview image rendering, multiple stickers in one group, and correct sticker count in `ui/src/features/mappings/MappingList.test.tsx`

### Implementation for User Story 2

- [X] T019 [US2] Add `stickerPreviewUrl` and `stickerPreviewState` values to `MappingResponse` mapping output in `src/main/java/com/bigmoji/api/dto/MappingResponse.java`
- [X] T020 [US2] Generate same-origin sticker preview URLs for authorized list and upload responses in `src/main/java/com/bigmoji/api/StickerMappingController.java`
- [X] T021 [US2] Preserve the current upload validation, deletion authorization, and Discord replacement behavior while adding preview response data in `src/main/java/com/bigmoji/sticker/StickerMappingService.java`
- [X] T022 [US2] Render uploaded sticker preview images and accessible labels in each mapping row in `ui/src/features/mappings/MappingList.tsx`
- [X] T023 [US2] Add fixed-size sticker preview row styles with object-fit containment in `ui/src/features/mappings/mappings.css`
- [X] T024 [US2] Update mapping API client tests for preview-capable list and upload payloads in `ui/src/api/mappings.test.ts`

**Checkpoint**: User Stories 1 and 2 are independently functional and testable.

---

## Phase 5: User Story 3 - Handle Missing or Slow Assets Clearly (Priority: P2)

**Goal**: Admins can still understand and manage mappings when emoji or sticker previews are unavailable, deleted, slow, or fail to load.

**Independent Test**: View mappings with unavailable emoji and sticker previews and verify the list shows clear fallback states, no broken images, stable layout, and reachable delete controls.

### Tests for User Story 3

Write these tests first and verify they fail before implementation.

- [X] T025 [P] [US3] Add backend contract tests for unavailable sticker previews, unavailable emoji previews, and no storage internals in responses in `src/test/java/com/bigmoji/api/StickerMappingControllerContractTest.java`
- [X] T026 [P] [US3] Add mapping list tests for image load errors, unavailable placeholders, and delete confirmation with unavailable previews in `ui/src/features/mappings/MappingList.test.tsx`
- [X] T027 [P] [US3] Add responsive admin mapping coverage for available and unavailable preview states at desktop and 360px widths in `ui/tests/e2e/mapping-management.spec.ts`

### Implementation for User Story 3

- [X] T028 [US3] Stream sticker preview bytes through an authorized endpoint and let image load failures fall back to unavailable UI in `src/main/java/com/bigmoji/api/StickerMappingController.java`
- [X] T029 [US3] Return unavailable custom emoji metadata when Discord guild emoji lookup fails without hiding the mapping in `src/main/java/com/bigmoji/discord/CustomEmojiMetadataProvider.java`
- [X] T030 [US3] Add per-image load error state and broken-image suppression for emoji and sticker previews in `ui/src/features/mappings/MappingList.tsx`
- [X] T031 [US3] Add unavailable, loading, wrapping, and mobile stacking styles that prevent overlap and horizontal scrolling in `ui/src/features/mappings/mappings.css`
- [X] T032 [US3] Ensure the delete confirmation uses readable trigger text when previews are unavailable in `ui/src/features/mappings/MappingList.tsx`
- [X] T033 [US3] Add unavailable preview and deleted-asset fixtures for UI and API tests in `ui/src/test/fixtures.ts`

**Checkpoint**: All user stories are independently functional and testable.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Validate quality gates and keep docs aligned with the completed implementation.

- [X] T034 [P] Update verification notes if commands or preview response fields changed in `specs/007-display-custom-assets/quickstart.md`
- [X] T035 [P] Run backend focused tests from `specs/007-display-custom-assets/quickstart.md` and fix failures in `src/test/java/com/bigmoji/api/StickerMappingControllerContractTest.java`
- [X] T036 [P] Run UI lint, typecheck, and focused tests from `specs/007-display-custom-assets/quickstart.md` and fix failures in `ui/src/features/mappings/MappingList.test.tsx`
- [X] T037 Run full backend and UI verification from `specs/007-display-custom-assets/quickstart.md` before marking the feature complete

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 Setup**: No dependencies; start immediately.
- **Phase 2 Foundational**: Depends on Phase 1; blocks all user stories.
- **Phase 3 US1**: Depends on Phase 2; MVP for custom emoji recognition.
- **Phase 4 US2**: Depends on Phase 2; can be developed after or alongside US1, but final UI benefits from US1 group headers.
- **Phase 5 US3**: Depends on US1 and US2 behavior being present enough to exercise fallback states.
- **Phase 6 Polish**: Depends on selected user stories being complete.

### User Story Dependencies

- **US1 Recognize Server Custom Emoji Triggers**: Starts after foundational preview types/provider hooks; no dependency on US2.
- **US2 Preview Uploaded Stickers in Mappings**: Starts after foundational preview response shape; no dependency on US1 for backend behavior.
- **US3 Handle Missing or Slow Assets Clearly**: Depends on preview rendering from US1 and US2 so failures can be represented and tested.

### Within Each User Story

- Write tests first and verify they fail.
- Backend provider/DTO work before controller response integration.
- API response shape before UI rendering.
- UI rendering before responsive and fallback polish.
- Story checkpoint must pass before moving to the next priority if working sequentially.

## Parallel Opportunities

- `T001`, `T002`, and `T003` can run in parallel during setup.
- `T008`, `T009`, and `T010` can run in parallel for US1 tests.
- `T016`, `T017`, and `T018` can run in parallel for US2 tests.
- `T025`, `T026`, and `T027` can run in parallel for US3 tests.
- Backend and UI implementation tasks for different stories can run in parallel after Phase 2 if file conflicts are coordinated.
- Polish tasks `T034`, `T035`, and `T036` can run in parallel after stories are complete.

## Parallel Example: User Story 1

```bash
Task: "T008 [US1] Add CustomEmojiMetadataProvider tests in src/test/java/com/bigmoji/discord/CustomEmojiMetadataProviderTest.java"
Task: "T009 [US1] Add backend contract tests in src/test/java/com/bigmoji/api/StickerMappingControllerContractTest.java"
Task: "T010 [US1] Add MappingList tests in ui/src/features/mappings/MappingList.test.tsx"
```

## Parallel Example: User Story 2

```bash
Task: "T016 [US2] Add backend contract tests in src/test/java/com/bigmoji/api/StickerMappingControllerContractTest.java"
Task: "T017 [US2] Add service/controller tests in src/test/java/com/bigmoji/sticker/StickerMappingServiceTest.java"
Task: "T018 [US2] Add MappingList tests in ui/src/features/mappings/MappingList.test.tsx"
```

## Parallel Example: User Story 3

```bash
Task: "T025 [US3] Add backend fallback tests in src/test/java/com/bigmoji/api/StickerMappingControllerContractTest.java"
Task: "T026 [US3] Add UI fallback tests in ui/src/features/mappings/MappingList.test.tsx"
Task: "T027 [US3] Add responsive coverage in ui/tests/e2e/mapping-management.spec.ts"
```

## Implementation Strategy

### MVP First

1. Complete Phase 1 and Phase 2.
2. Complete Phase 3 for US1 so custom emoji triggers are visually recognizable.
3. Stop and validate US1 independently using the backend contract tests and `MappingList` tests.

### Incremental Delivery

1. Deliver US1 to fix custom emoji trigger recognition.
2. Deliver US2 to show uploaded sticker previews after upload and reload.
3. Deliver US3 to harden missing, deleted, slow, and inaccessible asset states.
4. Run Phase 6 verification before merging or deploying.

### Notes

- Keep storage bucket/object key values out of primary UI display.
- Keep preview data scoped to the selected authorized guild.
- Do not change sticker upload validation, delete authorization, or Discord message replacement behavior.
- Commit after each task or logical group once the working tree is clean enough to avoid mixing unrelated changes.
