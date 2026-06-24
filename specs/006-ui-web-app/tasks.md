# Tasks: Bigmoji Web UI

**Input**: Design documents from `specs/006-ui-web-app/`

**Prerequisites**: [plan.md](plan.md), [spec.md](spec.md), [research.md](research.md), [data-model.md](data-model.md), [contracts/](contracts/), [quickstart.md](quickstart.md)

**Tests**: Included because the project constitution requires test coverage for every feature and the plan defines Vitest, Testing Library, and Playwright.

**Organization**: Tasks are grouped by user story so each story can be implemented and tested independently.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel because it touches different files and does not depend on incomplete tasks.
- **[Story]**: Maps task to the user story phase, e.g. [US1], [US2], [US3], [US4].
- Every task includes exact file paths.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Initialize the separate UI application and baseline tooling.

- [X] T001 Create the `ui/` directory structure from the implementation plan in `ui/src/`, `ui/public/`, and `ui/tests/e2e/`
- [X] T002 Initialize React + TypeScript + Vite dependencies and scripts in `ui/package.json` and `ui/package-lock.json`
- [X] T003 [P] Configure TypeScript project settings in `ui/tsconfig.json`, `ui/tsconfig.node.json`, and `ui/vite.config.ts`
- [X] T004 [P] Configure linting and formatting in `ui/eslint.config.js`, `ui/.prettierrc`, and `ui/.prettierignore`
- [X] T005 [P] Configure Vitest and Testing Library bootstrap in `ui/vite.config.ts` and `ui/src/test/setup.ts`
- [X] T006 [P] Configure Playwright browser tests in `ui/playwright.config.ts` and `ui/tests/e2e/`
- [X] T007 Create the SPA entry files in `ui/index.html`, `ui/src/main.tsx`, and `ui/src/App.tsx`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Shared types, API client, styling, and app shell required before user story work.

**CRITICAL**: No user story implementation should begin until this phase is complete.

- [X] T008 Define client-side API and view models from `data-model.md` in `ui/src/api/types.ts`
- [X] T009 Implement normalized API errors and credentialed fetch wrapper in `ui/src/api/client.ts`
- [X] T010 Implement auth API functions for session lookup, logout, sign-in URL, and install URL in `ui/src/api/auth.ts`
- [X] T011 Implement sticker mapping API functions for defaults, list, upload, and delete in `ui/src/api/mappings.ts`
- [X] T012 [P] Define visual tokens and global responsive base styles in `ui/src/styles/tokens.css` and `ui/src/styles/global.css`
- [X] T013 [P] Create shared UI primitives in `ui/src/components/Button.tsx`, `ui/src/components/ErrorBanner.tsx`, `ui/src/components/LoadingState.tsx`, and `ui/src/components/EmptyState.tsx`
- [X] T014 [P] Create accessible modal primitive in `ui/src/components/Modal.tsx`
- [X] T015 Implement application shell layout in `ui/src/components/AppShell.tsx`
- [X] T016 [P] Create deterministic API fixtures for unit and e2e tests in `ui/src/test/fixtures.ts`

**Checkpoint**: Foundation ready. User story implementation can now proceed.

---

## Phase 3: User Story 1 - Understand Bigmoji From the Homepage (Priority: P1) MVP

**Goal**: A visitor can open `/` and understand that Bigmoji uploads stickers, maps emojis, and replaces emoji-only Discord messages with large stickers.

**Independent Test**: Open the unauthenticated homepage and verify the first viewport shows the product purpose, upload step, emoji trigger step, replacement result, and sign-in/register entry point on desktop and mobile.

### Tests for User Story 1

- [X] T017 [P] [US1] Add component tests for homepage demo steps and reduced-motion behavior in `ui/src/features/homepage-demo/HomepageDemo.test.tsx`
- [X] T018 [P] [US1] Add component tests for unauthenticated homepage content and CTA visibility in `ui/src/pages/HomePage.test.tsx`
- [X] T019 [P] [US1] Add Playwright tests for homepage desktop and 360px mobile layouts in `ui/tests/e2e/homepage.spec.ts`

### Implementation for User Story 1

- [X] T020 [P] [US1] Create homepage demo sample data in `ui/src/features/homepage-demo/demoData.ts`
- [X] T021 [US1] Implement upload, emoji trigger, and Discord-like replacement demo in `ui/src/features/homepage-demo/HomepageDemo.tsx`
- [X] T022 [US1] Implement the unauthenticated sign-in/register panel presentation in `ui/src/features/auth/AuthPanel.tsx`
- [X] T023 [US1] Compose the first-viewport homepage in `ui/src/pages/HomePage.tsx`
- [X] T024 [US1] Add homepage-specific responsive styling in `ui/src/pages/HomePage.css` and `ui/src/features/homepage-demo/HomepageDemo.css`
- [X] T025 [US1] Register the `/` route and homepage shell behavior in `ui/src/App.tsx`

**Checkpoint**: User Story 1 is independently testable as the public homepage MVP.

---

## Phase 4: User Story 2 - Start Admin Onboarding (Priority: P1)

**Goal**: A server admin can start Discord sign-in from the homepage, return with session state, and see manageable servers or a no-server empty state.

**Independent Test**: Start sign-in from the homepage, mock or complete Discord OAuth, and verify the UI shows authenticated user state plus manageable guilds or the no-guild state.

### Tests for User Story 2

- [X] T026 [P] [US2] Add unit tests for session state transitions in `ui/src/features/auth/useSession.test.tsx`
- [X] T027 [P] [US2] Add component tests for auth panel sign-in, retry, and authenticated CTA states in `ui/src/features/auth/AuthPanel.test.tsx`
- [X] T028 [P] [US2] Add Playwright tests for guest, authenticated, expired-session, and no-guild onboarding states in `ui/tests/e2e/admin-onboarding.spec.ts`

### Implementation for User Story 2

- [X] T029 [US2] Implement current session loading, logout, expired-session, and backend-unavailable handling in `ui/src/features/auth/useSession.ts`
- [X] T030 [US2] Wire sign-in navigation to `/api/auth/discord/login` and authenticated dashboard CTA in `ui/src/features/auth/AuthPanel.tsx`
- [X] T031 [P] [US2] Implement manageable guild selection and no-guild install guidance in `ui/src/features/mappings/GuildSelector.tsx`
- [X] T032 [US2] Implement authenticated admin landing states in `ui/src/pages/AdminPage.tsx`
- [X] T033 [US2] Add `/app` route guard, guest redirect/prompt behavior, and logout shell integration in `ui/src/App.tsx` and `ui/src/components/AppShell.tsx`

**Checkpoint**: User Story 2 is independently testable as the authenticated onboarding flow.

---

## Phase 5: User Story 3 - Manage Emoji-to-Sticker Mappings (Priority: P2)

**Goal**: An authenticated admin can select a server, upload a sticker for an emoji, view custom/default mappings, and delete mappings with confirmation.

**Independent Test**: Sign in as an authorized admin, open one manageable guild, upload a valid sticker, confirm it appears in the list, then delete it after confirmation.

### Tests for User Story 3

- [X] T034 [P] [US3] Add unit tests for mapping API functions and normalized 400/401/403/404/5xx handling in `ui/src/api/mappings.test.ts`
- [X] T035 [P] [US3] Add component tests for sticker upload validation, preview, upload success, and upload failure in `ui/src/features/mappings/StickerUploadForm.test.tsx`
- [X] T036 [P] [US3] Add component tests for mapping list grouping, default sticker separation, and delete confirmation in `ui/src/features/mappings/MappingList.test.tsx`
- [X] T037 [P] [US3] Add Playwright tests for mapping upload/delete and unauthorized guild access in `ui/tests/e2e/mapping-management.spec.ts`

### Implementation for User Story 3

- [X] T038 [US3] Implement default sticker display separate from custom mappings in `ui/src/features/mappings/DefaultStickerList.tsx`
- [X] T039 [US3] Implement sticker upload form with emoji input, file preview, validation, loading, and error states in `ui/src/features/mappings/StickerUploadForm.tsx`
- [X] T040 [US3] Implement mapping list, same-emoji grouping, delete confirmation, and empty states in `ui/src/features/mappings/MappingList.tsx`
- [X] T041 [US3] Wire `/app/guilds/:guildId` data loading, authorization guard, upload refresh, and delete refresh in `ui/src/pages/AdminPage.tsx`
- [X] T042 [US3] Add admin mapping view responsive styling in `ui/src/pages/AdminPage.css` and `ui/src/features/mappings/mappings.css`
- [X] T043 [US3] Ensure storage bucket/object keys are not exposed in normal UI rendering in `ui/src/features/mappings/MappingList.tsx`

**Checkpoint**: User Story 3 is independently testable as the core admin management workflow.

---

## Phase 6: User Story 4 - Deploy UI Independently (Priority: P3)

**Goal**: An operator can build and run the UI as a separate Docker container that serves the SPA and proxies `/api` to an existing Bigmoji backend.

**Independent Test**: Build the UI Docker image, run it with `BIGMOJI_BACKEND_URL`, open `http://localhost:3000`, and verify `/api/auth/me` reaches the backend through the proxy.

### Tests for User Story 4

- [X] T044 [P] [US4] Add nginx proxy and SPA fallback smoke test script in `ui/tests/e2e/proxy-smoke.spec.ts`
- [X] T045 [P] [US4] Add Docker build/run smoke script for proxy verification in `ui/tests/docker-smoke.sh`

### Implementation for User Story 4

- [X] T046 [US4] Add multi-stage production image in `ui/Dockerfile`
- [X] T047 [US4] Add nginx static serving, SPA fallback, health endpoint, and `/api` proxy template in `ui/nginx.conf.template`
- [X] T048 [US4] Add Docker build context exclusions in `ui/.dockerignore`
- [X] T049 [US4] Configure Vite dev proxy for `/api` to local backend in `ui/vite.config.ts`
- [X] T050 [US4] Add optional UI service wiring to `docker-compose.yml`
- [X] T051 [US4] Document UI local dev, Docker run, OAuth redirect values, and troubleshooting in `ui/README.md`

**Checkpoint**: User Story 4 is independently testable as a deployable UI container.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Quality, accessibility, responsive verification, and final validation across all stories.

- [X] T052 [P] Add accessibility-focused tests for keyboard navigation, form labels, modal focus, and error announcements in `ui/src/App.accessibility.test.tsx`
- [X] T053 [P] Add responsive visual Playwright screenshots for desktop and 360px mobile in `ui/tests/e2e/responsive-visual.spec.ts`
- [X] T054 Optimize demo sticker assets and document asset size expectations in `ui/public/demo-stickers/README.md`
- [X] T055 Run and fix `npm run lint`, `npm run typecheck`, and `npm test` from `ui/package.json`
- [X] T056 Run and fix `npm run test:e2e` from `ui/package.json`
- [X] T057 Build and validate the production image with `docker build -t bigmoji-ui ./ui`
- [X] T058 Validate the smoke path from `specs/006-ui-web-app/quickstart.md` against a running backend

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies; can start immediately.
- **Foundational (Phase 2)**: Depends on Setup; blocks every user story.
- **User Story 1 (Phase 3)**: Depends on Foundation; MVP.
- **User Story 2 (Phase 4)**: Depends on Foundation; can proceed in parallel with US1 after shared auth shell is understood, but should be validated after US1 route/homepage exists.
- **User Story 3 (Phase 5)**: Depends on Foundation and benefits from US2 session/guild selection.
- **User Story 4 (Phase 6)**: Depends on Setup and Foundation; can proceed in parallel with US1-US3 after routes and build output exist.
- **Polish (Phase 7)**: Depends on all desired user stories being complete.

### User Story Dependencies

- **US1 - Homepage**: No dependency on other user stories after Foundation.
- **US2 - Admin Onboarding**: Depends on Foundation and reuses US1 auth entry point, but session/admin states can be tested with mocked API responses.
- **US3 - Mapping Management**: Depends on US2 session and guild selection behavior.
- **US4 - Independent Deployment**: Depends on UI build output and API client base-path convention; can be completed once any usable route exists, then validated again after US3.

### Within Each User Story

- Tests come before implementation and should fail before the related implementation task is completed.
- Types and API clients must exist before hooks/pages consume them.
- Shared components should exist before story-specific composition uses them.
- Page wiring comes after feature components for that story.
- Each checkpoint should be validated independently before moving to lower-priority stories.

### Parallel Opportunities

- Setup tasks T003-T006 can run in parallel.
- Foundational style/components/test fixture tasks T012-T014 and T016 can run in parallel.
- US1 tests T017-T019 can run in parallel; T020 can run in parallel with tests.
- US2 tests T026-T028 and GuildSelector T031 can run in parallel after Foundation.
- US3 tests T034-T037 can run in parallel; feature components T038-T040 can be implemented in parallel once API contracts are stable.
- US4 Docker/nginx tasks T046-T048 can run in parallel with Vite proxy/documentation tasks T049-T051 after Setup.
- Polish test additions T052-T053 can run in parallel after user stories are functional.

---

## Parallel Example: User Story 1

```text
Task: "Add component tests for homepage demo steps and reduced-motion behavior in ui/src/features/homepage-demo/HomepageDemo.test.tsx"
Task: "Add component tests for unauthenticated homepage content and CTA visibility in ui/src/pages/HomePage.test.tsx"
Task: "Add Playwright tests for homepage desktop and 360px mobile layouts in ui/tests/e2e/homepage.spec.ts"
Task: "Create homepage demo sample data in ui/src/features/homepage-demo/demoData.ts"
```

## Parallel Example: User Story 2

```text
Task: "Add unit tests for session state transitions in ui/src/features/auth/useSession.test.tsx"
Task: "Add component tests for auth panel sign-in, retry, and authenticated CTA states in ui/src/features/auth/AuthPanel.test.tsx"
Task: "Add Playwright tests for guest, authenticated, expired-session, and no-guild onboarding states in ui/tests/e2e/admin-onboarding.spec.ts"
Task: "Implement manageable guild selection and no-guild install guidance in ui/src/features/mappings/GuildSelector.tsx"
```

## Parallel Example: User Story 3

```text
Task: "Add unit tests for mapping API functions and normalized 400/401/403/404/5xx handling in ui/src/api/mappings.test.ts"
Task: "Add component tests for sticker upload validation, preview, upload success, and upload failure in ui/src/features/mappings/StickerUploadForm.test.tsx"
Task: "Add component tests for mapping list grouping, default sticker separation, and delete confirmation in ui/src/features/mappings/MappingList.test.tsx"
Task: "Add Playwright tests for mapping upload/delete and unauthorized guild access in ui/tests/e2e/mapping-management.spec.ts"
```

## Parallel Example: User Story 4

```text
Task: "Add multi-stage production image in ui/Dockerfile"
Task: "Add nginx static serving, SPA fallback, health endpoint, and /api proxy template in ui/nginx.conf.template"
Task: "Add Docker build context exclusions in ui/.dockerignore"
Task: "Configure Vite dev proxy for /api to local backend in ui/vite.config.ts"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup.
2. Complete Phase 2: Foundation.
3. Complete Phase 3: User Story 1 homepage.
4. Stop and validate: open `/`, run US1 component tests, run `ui/tests/e2e/homepage.spec.ts`, and inspect desktop/mobile screenshots.
5. Demo the homepage before building admin flows.

### Incremental Delivery

1. Setup + Foundation -> runnable empty UI shell.
2. US1 -> public homepage MVP.
3. US2 -> authenticated onboarding and guild selection.
4. US3 -> real sticker mapping management.
5. US4 -> independent Docker deployment.
6. Polish -> accessibility, visual checks, production build validation.

### Parallel Team Strategy

1. Team completes Setup + Foundation together.
2. Developer A owns US1 homepage.
3. Developer B owns US2 session/onboarding.
4. Developer C owns US4 Docker/nginx packaging.
5. US3 starts after US2 APIs and guild selection behavior are stable.

## Notes

- [P] tasks are safe to parallelize because they touch separate files or isolated test files.
- Every user-story phase has its own independent test criteria.
- Keep browser API calls relative to `/api`; do not hardcode backend hostnames in React code.
- Do not expose MinIO URLs, bucket names, object keys, bot tokens, session secrets, or raw backend errors in user-facing UI.
- Commit after each task or small logical group.
