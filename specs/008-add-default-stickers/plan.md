# Implementation Plan: Additional Default Emoji Stickers

**Branch**: `008-add-default-stickers` | **Date**: 2026-06-29 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/008-add-default-stickers/spec.md`

## Summary

Add four bundled default sticker mappings (`cry`, `open_mouth`, `pensive`, `face_with_bags_under_eyes`) to the existing default catalog, emoji normalization path, fallback replacement flow, and admin UI default sticker list. The implementation reuses the existing default catalog as the source of truth, extends the default sticker API with preview metadata so the UI can render actual sticker previews, and standardizes bundled default sticker assets toward Discord's 320x320 static sticker source canvas where feasible.

## Technical Context

**Language/Version**: Java 21 with Spring Boot 4.0.6 for backend/API; TypeScript 5.7 with React 19 and Vite 8 for the UI

**Primary Dependencies**: Spring Web, JDA 5.6.1, SLF4J/Logback, React, React Router, lucide-react, Vitest, Testing Library, Playwright

**Storage**: No database schema change. Bundled default sticker PNGs live in `src/main/resources/default-stickers/`; custom sticker persistence remains PostgreSQL/JPA plus MinIO and is not changed by this feature.

**Testing**: JUnit 5/Spring Boot tests for default catalog, normalizer, fallback loading, and default sticker API; Vitest + Testing Library for default sticker UI and API client; existing Playwright admin flow checks if responsive preview behavior needs browser validation

**Target Platform**: Linux container runtime, local macOS development, Discord channels supported by the existing bot, and browser-based admin UI served from `ui/`

**Project Type**: Full-stack feature across existing backend service and existing frontend SPA

**Performance Goals**:
- Default lookup remains an in-memory catalog lookup with negligible listener overhead
- Default sticker list API remains small: 9 metadata records plus preview URLs, no image bytes embedded in JSON
- Default preview images load separately through bounded image endpoints and do not shift the UI layout
- Message replacement remains within Discord's existing interaction and webhook/file-send timing expectations

**Constraints**:
- Changes are additive for emoji coverage: existing `smile`, `heart`, `party`, `thumbsup`, and `fire` mappings must remain unchanged
- Server-specific custom mappings continue to take precedence over bundled defaults
- Unsupported emoji and mixed-content messages must not become newly eligible for replacement
- UI must consume backend default catalog data instead of maintaining a conflicting hardcoded list
- Default preview endpoints must not expose filesystem paths, classpath internals, stack traces, or raw resource errors
- Bundled default sticker visuals must remain square and consistent; exact 320x320 source sizing is required when feasible without runtime complexity

**Scale/Scope**:
- 4 new default catalog entries and 4 new normalized emoji mappings
- 9 total bundled default stickers after implementation
- Backend default sticker metadata and preview contract
- Admin UI default sticker list rendering, preview fallback states, fixtures, and component tests
- No new admin editor, no default sticker deletion/upload controls, no database migration, no change to custom mapping authorization

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Code Quality - Linting & Formatting | PASS | Planned Java and TypeScript edits stay inside existing Spotless, TypeScript, ESLint, and Prettier paths. |
| I. Code Quality - Simplicity First | PASS | Reuses `DefaultStickerInitializer.DEFAULTS`, `EmojiNormalizer`, existing fallback resolution, and current UI data loading instead of adding a second catalog. |
| I. Code Quality - Documentation | PASS | Plan, research, data model, API/UI contracts, and quickstart document behavior and validation. |
| I. Code Quality - Error Handling | PASS | Missing default assets and preview load failures become explicit fallback states without crashes or broken UI images. |
| II. Testing - Coverage | PASS | Plan includes backend unit/contract tests and UI component/API tests for all new entries and regressions. |
| II. Testing - Critical Paths | PASS | Emoji normalization, fallback asset loading, custom-over-default precedence, API metadata, and UI rendering are covered. |
| II. Testing - Test Independence | PASS | Tests can use local classpath assets, direct controller calls, mocked fetch responses, and UI fixtures. |
| III. UX Consistency | PASS | New default entries use the same UI section, card treatment, fallback states, and responsive constraints as existing mapping surfaces. |
| III. Accessibility | PASS | UI contract requires accessible labels for default sticker previews and readable fallback shortcode names. |
| IV. Performance - Response Time | PASS | Metadata responses remain lightweight and preview images are fetched separately. |
| IV. Performance - Asset Optimization | PASS | Plan prefers 320x320 PNG source assets for bundled defaults and avoids runtime image resizing dependencies. |

## Project Structure

### Documentation (this feature)

```text
specs/008-add-default-stickers/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── backend-default-stickers-contract.md
│   └── ui-default-stickers-contract.md
└── tasks.md             # Phase 2 output, created by /speckit.tasks
```

### Source Code (repository root)

```text
bigmoji/
├── src/
│   ├── main/java/com/bigmoji/
│   │   ├── api/
│   │   │   ├── DefaultStickersController.java
│   │   │   └── dto/DefaultStickerResponse.java
│   │   ├── emoji/
│   │   │   └── EmojiNormalizer.java
│   │   └── sticker/
│   │       └── DefaultStickerInitializer.java
│   ├── main/resources/default-stickers/
│   │   ├── cry.png
│   │   ├── face_with_bags_under_eyes.png
│   │   ├── open_mouth.png
│   │   └── pensive.png
│   └── test/java/com/bigmoji/
│       ├── api/
│       │   └── DefaultStickersControllerContractTest.java
│       ├── emoji/
│       │   └── EmojiDetectorTest.java
│       └── sticker/
│           └── DefaultStickerInitializerTest.java
└── ui/
    ├── src/
    │   ├── api/
    │   │   ├── mappings.test.ts
    │   │   └── types.ts
    │   ├── features/mappings/
    │   │   ├── DefaultStickerList.tsx
    │   │   ├── DefaultStickerList.test.tsx
    │   │   └── mappings.css
    │   └── test/
    │       └── fixtures.ts
    └── tests/e2e/
        └── mapping-management.spec.ts
```

**Structure Decision**: Implement as a narrow full-stack change in the existing backend catalog/API and existing admin UI. Backend owns the authoritative default catalog and resource loading. UI renders backend-provided defaults and previews with local fallback state only.

## Phase 0: Research Outcomes

Research is captured in [research.md](research.md). All technical unknowns are resolved with these decisions:

- Extend the existing backend default catalog and normalizer as the single source of truth for new mappings.
- Add default sticker preview metadata and a classpath-backed preview endpoint instead of hardcoding image assets in the UI.
- Normalize bundled PNG source assets to 320x320 where feasible, and record actual Discord attachment rendering in acceptance evidence.
- Keep tests focused on additive behavior and regressions for existing default mappings, unsupported inputs, and UI entries.

## Phase 1: Design & Contracts

Design artifacts are captured in:

- [data-model.md](data-model.md)
- [contracts/backend-default-stickers-contract.md](contracts/backend-default-stickers-contract.md)
- [contracts/ui-default-stickers-contract.md](contracts/ui-default-stickers-contract.md)
- [quickstart.md](quickstart.md)

## Post-Design Constitution Check Re-Evaluation

*Re-checked after Phase 1 design completion.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Code Quality - Linting & Formatting | PASS | Planned files are already covered by existing backend and UI formatting/test paths. |
| I. Code Quality - Simplicity First | PASS | Design extends existing catalog, DTO, endpoint, UI component, and tests with no new storage or abstraction layer. |
| I. Code Quality - Documentation | PASS | Contracts define backend payloads, preview endpoint behavior, UI rendering states, and quickstart validation. |
| I. Code Quality - Error Handling | PASS | Missing assets return unavailable preview states or non-success preview responses without leaking internals. |
| II. Testing - Coverage | PASS | Quickstart and contracts identify tests for all new keys, old-key regression, unsupported-message regression, and UI display. |
| II. Testing - Critical Paths | PASS | Emoji replacement, default fallback, default list API, preview endpoint, and admin UI are all represented. |
| II. Testing - Test Independence | PASS | Backend tests do not need Discord network access; UI tests use fixture payloads and mocked fetch. |
| III. UX Consistency | PASS | UI contract uses existing default sticker section and stable preview cells matching mapping preview behavior. |
| III. Accessibility | PASS | Default preview images require accessible names and fallback text remains readable. |
| IV. Performance - Response Time | PASS | Metadata remains small and preview bytes are served only when browsers request them. |
| IV. Performance - Asset Optimization | PASS | 320x320 PNG target avoids runtime resizing and keeps assets appropriate for sticker-style display. |

## Complexity Tracking

No constitution violations identified. No complexity exemptions required.
