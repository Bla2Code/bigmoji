# Tasks: Additional Default Emoji Stickers

**Input**: Design documents from `specs/008-add-default-stickers/`
**Prerequisites**: [plan.md](plan.md), [spec.md](spec.md), [research.md](research.md), [data-model.md](data-model.md), [contracts/](contracts/), [quickstart.md](quickstart.md)
**Tests**: Included because the project constitution requires tests and the plan defines backend/UI coverage for this feature.
**Organization**: Tasks are grouped by user story to enable independent implementation and validation.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel with other tasks in the same phase because it touches different files or only test data.
- **[Story]**: Maps to the user story from `spec.md`.
- Every task includes exact file paths.

## Phase 1: Setup (Shared Catalog And Preview Shape)

**Purpose**: Prepare shared contracts and fixtures that later backend and UI story tasks depend on.

- [X] T001 [P] Add preview-capable `DefaultSticker` TypeScript fields in `ui/src/api/types.ts`
- [X] T002 [P] Add `stickerPreviewUrl` and `stickerPreviewState` fields to `DefaultStickerResponse` in `src/main/java/com/bigmoji/api/dto/DefaultStickerResponse.java`
- [X] T003 [P] Expand default sticker fixtures to all nine defaults with preview states in `ui/src/test/fixtures.ts`
- [X] T004 [P] Record current bundled PNG dimension baseline for all defaults in `specs/008-add-default-stickers/quickstart.md`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Add shared backend catalog helpers and default preview routing that user stories use.

**Critical**: No user story implementation should begin until this phase is complete.

- [X] T005 Add catalog lookup by shortcode name in `src/main/java/com/bigmoji/sticker/DefaultStickerInitializer.java`
- [X] T006 Add bundled default asset byte loading by catalog entry in `src/main/java/com/bigmoji/sticker/DefaultStickerInitializer.java`
- [X] T007 Add default sticker preview URL assembly to list responses in `src/main/java/com/bigmoji/api/DefaultStickersController.java`
- [X] T008 Add `/api/stickers/default/{shortcodeName}/preview` endpoint for bundled PNG bytes in `src/main/java/com/bigmoji/api/DefaultStickersController.java`

**Checkpoint**: Backend can expose default sticker metadata and preview URLs without UI or replacement-specific behavior complete.

---

## Phase 3: User Story 1 - Replace newly supported emojis with default stickers (Priority: P1) MVP

**Goal**: Messages containing the four newly supported emojis or shortcode names resolve to matching bundled default stickers when no custom mapping exists.

**Independent Test**: Send or simulate each new emoji input and verify it resolves to `cry`, `open_mouth`, `pensive`, and `face_with_bags_under_eyes` bundled defaults.

### Tests for User Story 1

Write these tests first and verify they fail before implementation.

- [X] T009 [P] [US1] Add default catalog count, key presence, and fallback loading tests for the four new keys in `src/test/java/com/bigmoji/sticker/DefaultStickerInitializerTest.java`
- [X] T010 [P] [US1] Add Unicode and shortcode normalization assertions for the four new keys in `src/test/java/com/bigmoji/emoji/EmojiDetectorTest.java`
- [X] T011 [P] [US1] Add fallback resolution test for one new key when custom mappings are absent in `src/test/java/com/bigmoji/sticker/StickerMappingServiceTest.java`
- [X] T012 [P] [US1] Add listener replacement tests for representative new emoji inputs in `src/test/java/com/bigmoji/discord/EmojiMessageListenerTest.java`

### Implementation for User Story 1

- [X] T013 [US1] Add `cry`, `open_mouth`, `pensive`, and `face_with_bags_under_eyes` entries to `DefaultStickerInitializer.DEFAULTS` in `src/main/java/com/bigmoji/sticker/DefaultStickerInitializer.java`
- [X] T014 [US1] Map 😢, 😮, 😔, and 🫩 to the new canonical default keys in `src/main/java/com/bigmoji/emoji/EmojiNormalizer.java`
- [X] T015 [US1] Ensure fallback loading returns the new bundled PNG file names from `src/main/java/com/bigmoji/sticker/DefaultStickerInitializer.java`
- [X] T016 [US1] Verify listener replacement uses normalized new keys without special-case branching in `src/main/java/com/bigmoji/discord/EmojiMessageListener.java`

**Checkpoint**: User Story 1 is independently functional and testable as the MVP.

---

## Phase 4: User Story 2 - Keep the existing default sticker set unchanged (Priority: P2)

**Goal**: Existing defaults, unsupported inputs, mixed-content messages, and custom-over-default precedence remain unchanged after adding the new defaults.

**Independent Test**: Re-run old default mapping and unsupported-input regression checks and confirm behavior is unchanged.

### Tests for User Story 2

Write these tests first and verify they fail only where current assertions still expect five defaults.

- [X] T017 [P] [US2] Update old default catalog regression assertions for `smile`, `heart`, `party`, `thumbsup`, and `fire` in `src/test/java/com/bigmoji/sticker/DefaultStickerInitializerTest.java`
- [X] T018 [P] [US2] Add regression assertions that old Unicode emoji and shortcode names still normalize correctly in `src/test/java/com/bigmoji/emoji/EmojiDetectorTest.java`
- [X] T019 [P] [US2] Add unsupported and mixed-content negative replacement assertions for new emoji cases in `src/test/java/com/bigmoji/discord/EmojiMessageListenerTest.java`
- [X] T020 [P] [US2] Add custom mapping precedence assertion for a new default key in `src/test/java/com/bigmoji/sticker/StickerMappingServiceTest.java`

### Implementation for User Story 2

- [X] T021 [US2] Preserve existing default catalog entries and descriptions while inserting new entries in `src/main/java/com/bigmoji/sticker/DefaultStickerInitializer.java`
- [X] T022 [US2] Preserve existing normalizer mappings for `smile`, `heart`, `party`, `thumbsup`, and `fire` while adding new mappings in `src/main/java/com/bigmoji/emoji/EmojiNormalizer.java`
- [X] T023 [US2] Keep custom mapping lookup before bundled fallback lookup in `src/main/java/com/bigmoji/sticker/StickerMappingService.java`
- [X] T024 [US2] Confirm mixed-content guard behavior remains unchanged for shortcode and Unicode inputs in `src/main/java/com/bigmoji/emoji/EmojiDetector.java`

**Checkpoint**: User Stories 1 and 2 are independently functional and regression-safe.

---

## Phase 5: User Story 3 - Show new defaults in the management UI (Priority: P3)

**Goal**: Admins see the four new default entries in the management UI with shortcode labels and actual sticker previews.

**Independent Test**: Open the default stickers area in the admin UI and verify the four new entries, existing entries, preview images, and fallback states render correctly.

### Tests for User Story 3

Write these tests first and verify they fail before UI/API implementation.

- [X] T025 [P] [US3] Add default sticker API contract tests for nine entries, new preview fields, and one preview byte response in `src/test/java/com/bigmoji/api/DefaultStickersControllerContractTest.java`
- [X] T026 [P] [US3] Add API client test coverage for default sticker preview fields in `ui/src/api/mappings.test.ts`
- [X] T027 [P] [US3] Add default sticker list tests for the four new shortcode labels and existing labels in `ui/src/features/mappings/DefaultStickerList.test.tsx`
- [X] T028 [P] [US3] Add default sticker preview image and image-error fallback tests in `ui/src/features/mappings/DefaultStickerList.test.tsx`
- [X] T029 [P] [US3] Add admin page fixture assertion that backend-provided default entries flow into the UI in `ui/src/pages/AdminPage.test.tsx`

### Implementation for User Story 3

- [X] T030 [US3] Populate `stickerPreviewUrl` and `stickerPreviewState` in default list responses in `src/main/java/com/bigmoji/api/DefaultStickersController.java`
- [X] T031 [US3] Serve known bundled default preview PNGs and reject unknown shortcode names in `src/main/java/com/bigmoji/api/DefaultStickersController.java`
- [X] T032 [US3] Preserve default sticker preview fields through `listDefaultStickers()` in `ui/src/api/mappings.ts`
- [X] T033 [US3] Render default sticker preview images, accessible labels, and fallback states in `ui/src/features/mappings/DefaultStickerList.tsx`
- [X] T034 [US3] Add stable square default preview card styles and long-name wrapping in `ui/src/features/mappings/mappings.css`
- [X] T035 [US3] Update admin default sticker fixtures to include `:cry:`, `:open_mouth:`, `:pensive:`, and `:face_with_bags_under_eyes:` in `ui/src/test/fixtures.ts`

**Checkpoint**: User Story 3 is independently functional and visible in the admin UI.

---

## Phase 6: User Story 4 - Present default stickers at a consistent Discord-oriented size (Priority: P4)

**Goal**: Bundled default stickers use a consistent square presentation and target Discord's 320x320 static sticker source canvas where feasible.

**Independent Test**: Verify bundled PNG dimensions and compare representative old and new stickers in Discord for consistent square output.

### Tests for User Story 4

Write these tests first and verify they fail until asset sizing and UI constraints are updated.

- [X] T036 [P] [US4] Add bundled default PNG dimension validation for all nine assets in `src/test/java/com/bigmoji/sticker/DefaultStickerInitializerTest.java`
- [X] T037 [P] [US4] Add long-name responsive rendering coverage for `:face_with_bags_under_eyes:` in `ui/src/features/mappings/DefaultStickerList.test.tsx`

### Implementation for User Story 4

- [X] T038 [US4] Normalize `src/main/resources/default-stickers/smile.png`, `src/main/resources/default-stickers/heart.png`, `src/main/resources/default-stickers/party.png`, `src/main/resources/default-stickers/thumbsup.png`, and `src/main/resources/default-stickers/fire.png` to the selected square source size
- [X] T039 [US4] Normalize `src/main/resources/default-stickers/cry.png`, `src/main/resources/default-stickers/open_mouth.png`, `src/main/resources/default-stickers/pensive.png`, and `src/main/resources/default-stickers/face_with_bags_under_eyes.png` to the selected square source size
- [X] T040 [US4] Record whether exact 320x320 source sizing was achieved for all bundled defaults in `specs/008-add-default-stickers/quickstart.md`
- [X] T041 [US4] Record manual Discord output-size acceptance evidence or delivery-surface limitation in `specs/008-add-default-stickers/quickstart.md`

**Checkpoint**: All user stories are independently functional and final output-size acceptance is documented.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Run quality gates and keep docs aligned with the completed implementation.

- [X] T042 [P] Run backend formatting and tests from `specs/008-add-default-stickers/quickstart.md` and fix failures in `src/test/java/com/bigmoji/sticker/DefaultStickerInitializerTest.java`
- [X] T043 [P] Run UI typecheck, lint, and tests from `specs/008-add-default-stickers/quickstart.md` and fix failures in `ui/src/features/mappings/DefaultStickerList.test.tsx`
- [X] T044 [P] Run focused default sticker API and UI validation from `specs/008-add-default-stickers/contracts/backend-default-stickers-contract.md`
- [X] T045 Update final implementation notes and any changed validation details in `specs/008-add-default-stickers/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 Setup**: No dependencies; start immediately.
- **Phase 2 Foundational**: Depends on Phase 1; blocks default API preview work and UI preview rendering.
- **Phase 3 US1**: Depends on Phase 2 for shared catalog helpers, but can validate backend replacement independently.
- **Phase 4 US2**: Depends on US1 changes being present enough to verify additive behavior and regressions.
- **Phase 5 US3**: Depends on Phase 2 and can proceed after backend preview contract tasks are available; it benefits from US1 catalog entries.
- **Phase 6 US4**: Depends on US1 catalog entries and US3 preview rendering so asset sizing can be validated across backend, UI, and Discord output.
- **Phase 7 Polish**: Depends on selected user stories being complete.

### User Story Dependencies

- **US1 Replace newly supported emojis**: MVP; starts after foundational tasks and does not depend on UI.
- **US2 Preserve existing behavior**: Depends on US1 catalog/normalizer changes to verify nothing else changed.
- **US3 Show new defaults in UI**: Depends on backend default catalog and preview response shape; can be implemented alongside US2 if file conflicts are coordinated.
- **US4 Consistent Discord-oriented size**: Depends on final asset set and UI preview rendering.

### Within Each User Story

- Write tests first and verify they fail.
- Backend catalog and normalization before listener/service validation.
- API response contract before UI rendering.
- UI rendering before responsive and fallback styling.
- Asset resizing before final Discord output-size acceptance evidence.

## Parallel Opportunities

- `T001`, `T002`, `T003`, and `T004` can run in parallel during setup.
- `T009`, `T010`, `T011`, and `T012` can run in parallel for US1 tests.
- `T017`, `T018`, `T019`, and `T020` can run in parallel for US2 tests.
- `T025`, `T026`, `T027`, `T028`, and `T029` can run in parallel for US3 tests.
- `T036` and `T037` can run in parallel for US4 tests.
- `T042`, `T043`, and `T044` can run in parallel during polish after implementation.

## Parallel Example: User Story 1

```bash
Task: "T009 [US1] Add default catalog tests in src/test/java/com/bigmoji/sticker/DefaultStickerInitializerTest.java"
Task: "T010 [US1] Add normalization assertions in src/test/java/com/bigmoji/emoji/EmojiDetectorTest.java"
Task: "T011 [US1] Add fallback resolution test in src/test/java/com/bigmoji/sticker/StickerMappingServiceTest.java"
Task: "T012 [US1] Add listener replacement tests in src/test/java/com/bigmoji/discord/EmojiMessageListenerTest.java"
```

## Parallel Example: User Story 3

```bash
Task: "T025 [US3] Add backend API contract tests in src/test/java/com/bigmoji/api/DefaultStickersControllerContractTest.java"
Task: "T026 [US3] Add API client tests in ui/src/api/mappings.test.ts"
Task: "T027 [US3] Add default label rendering tests in ui/src/features/mappings/DefaultStickerList.test.tsx"
Task: "T029 [US3] Add admin page fixture assertion in ui/src/pages/AdminPage.test.tsx"
```

## Implementation Strategy

### MVP First

1. Complete Phase 1 and Phase 2.
2. Complete Phase 3 for US1.
3. Stop and validate the four new default replacements independently with backend tests.

### Incremental Delivery

1. Deliver US1 to make new emojis replace with bundled defaults.
2. Deliver US2 to prove old behavior and precedence remain unchanged.
3. Deliver US3 to expose the new defaults and previews in the admin UI.
4. Deliver US4 to standardize and document output sizing.
5. Run Phase 7 verification before merging or deploying.

### Parallel Team Strategy

1. Team completes Phase 1 and Phase 2 together.
2. After foundational tasks:
   - Backend engineer: US1 and US2 catalog, normalization, service, listener tests.
   - Frontend engineer: US3 UI types, fixtures, component, CSS, and UI tests.
   - Asset/QA owner: US4 PNG sizing and Discord output-size acceptance evidence.
3. Integrate through the quickstart validation commands before completion.

## Notes

- `[P]` tasks are safe for parallel execution when file conflicts are coordinated.
- Each user story has independent test criteria and a checkpoint.
- Keep the backend default catalog as the source of truth for UI default entries.
- Do not add database persistence for bundled defaults.
- Do not change custom mapping upload, delete, or authorization behavior outside the listed files.
