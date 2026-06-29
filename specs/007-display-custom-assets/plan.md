# Implementation Plan: Display Custom Assets

**Branch**: `007-display-custom-assets` | **Date**: 2026-06-28 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/007-display-custom-assets/spec.md`

## Summary

Fix the authenticated mapping management view so admins see the actual server custom emoji trigger and uploaded sticker preview instead of text-only placeholders or storage internals. The implementation extends the existing Java/Spring mapping API with safe display metadata and same-origin sticker preview URLs, adds selected-guild custom emoji metadata for authorized admins, and updates the React mapping list to render stable, accessible preview states with fallbacks for missing assets.

The plan keeps the change scoped to management display: upload validation, deletion authorization, cache lookup, and Discord message replacement behavior remain unchanged.

## Technical Context

**Language/Version**: Java 21 with Spring Boot 4.0.6 for backend API; TypeScript 5.7 with React 19 and Vite 8 for the UI

**Primary Dependencies**: Spring Web, Spring Validation, Spring Data JPA, JDA 5.6.1, MinIO Java SDK, React, React Router, lucide-react, Vitest, Testing Library, Playwright

**Storage**: PostgreSQL/JPA for persisted `StickerMapping`; MinIO for uploaded sticker objects; Discord/JDA guild emoji metadata is read from the bot session and not persisted by this feature

**Testing**: JUnit/Spring Boot tests for backend DTO/controller/service behavior; Vitest + Testing Library for UI component/API behavior; Playwright for responsive admin mapping flow checks when needed

**Target Platform**: Java backend service plus browser-based admin UI served from the existing `ui/` application

**Project Type**: Full-stack feature across existing web service API and separate frontend SPA

**Performance Goals**:
- Mapping list data remains available within the existing backend p95 response target under normal load
- Sticker preview URLs are generated during mapping list responses without embedding sticker bytes in JSON
- UI shows loading or fallback states immediately and avoids layout jumps when image assets load
- Mapping list remains usable at 360px mobile width and with at least 20 mappings

**Constraints**:
- Preview data MUST be scoped to the authenticated admin and selected guild
- UI MUST NOT expose MinIO bucket names, object keys, credentials, bot tokens, raw storage errors, or stack traces as primary display content
- Preview URLs MUST be safe for browser display and bounded by backend authorization
- Custom emoji metadata MUST come from the selected Discord guild and degrade cleanly if the emoji is missing, deleted, animated, or inaccessible
- Existing message replacement rules, sticker upload validation, and delete authorization MUST remain unchanged
- New UI must use stable dimensions for preview cells so loading images do not shift controls or overlap text

**Scale/Scope**:
- Backend mapping response display fields for uploaded sticker preview
- Backend selected-guild custom emoji metadata for mapping display
- UI mapping API types and grouping view models
- Mapping list preview rendering, loading/unavailable/fallback states, accessibility labels, and responsive CSS
- Backend contract tests plus UI component/API tests for available and unavailable preview states
- No new sticker editor, emoji picker, bulk management, CDN migration, or Discord replacement behavior changes

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| **I. Code Quality - Linting & Formatting** | PASS | Backend remains formatted with Spotless/google-java-format; UI remains TypeScript/ESLint/Prettier controlled. |
| **I. Code Quality - Simplicity First** | PASS | Reuse existing mapping endpoints, MinIO download support, JDA guild metadata, and local React components; no new storage or UI state library. |
| **I. Code Quality - Documentation** | PASS | Plan, research, data model, contracts, and quickstart define the API/UI contract and verification path. |
| **I. Code Quality - Error Handling** | PASS | Missing preview assets become explicit display states; backend failures do not leak storage internals to users. |
| **II. Testing - Coverage** | PASS | Adds backend contract/service tests and UI component/API tests for preview fields, grouping, fallbacks, and authorization handling. |
| **II. Testing - Critical Paths** | PASS | Auth-scoped list mappings, fresh upload preview display, unavailable asset fallback, and delete reachability are explicitly tested. |
| **II. Testing - Independence** | PASS | Backend tests mock storage/Discord metadata; UI tests use fixtures and mocked API payloads. |
| **III. UX Consistency** | PASS | Uses existing `MappingList`, shared buttons/modals/errors, and mapping CSS while adding preview states. |
| **III. Accessibility** | PASS | Visual previews require readable labels/descriptions; delete controls remain keyboard reachable. |
| **IV. Performance - Response Time** | PASS | Presigned URLs are generated without downloading objects; UI image loading is browser-managed with stable placeholders. |
| **IV. Performance - Asset Optimization** | PASS | Preview images are constrained by CSS and object-fit; no large image bytes are embedded in API JSON. |

## Project Structure

### Documentation (this feature)

```text
specs/007-display-custom-assets/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── backend-api-preview-contract.md
│   └── ui-preview-contract.md
└── tasks.md             # Phase 2 output, created by /speckit.tasks
```

### Source Code (repository root)

```text
bigmoji/
├── src/
│   ├── main/java/com/bigmoji/
│   │   ├── api/
│   │   │   ├── StickerMappingController.java
│   │   │   └── dto/MappingResponse.java
│   │   ├── discord/
│   │   │   └── CustomEmojiMetadataProvider.java
│   │   ├── sticker/
│   │   │   └── StickerMappingService.java
│   │   └── storage/
│   │       └── MinioStorageService.java
│   └── test/java/com/bigmoji/
│       ├── api/
│       │   ├── StickerMappingControllerContractTest.java
│       │   └── StickerMappingControllerIntegrationTest.java
│       ├── discord/
│       │   └── CustomEmojiMetadataProviderTest.java
│       └── sticker/
│           └── StickerMappingServiceTest.java
└── ui/
    ├── src/
    │   ├── api/
    │   │   ├── mappings.ts
    │   │   ├── mappings.test.ts
    │   │   └── types.ts
    │   ├── features/mappings/
    │   │   ├── MappingList.tsx
    │   │   ├── MappingList.test.tsx
    │   │   └── mappings.css
    │   └── test/
    │       └── fixtures.ts
    └── tests/e2e/
        └── mapping-management.spec.ts
```

**Structure Decision**: Implement this as a narrow full-stack change in the existing backend API and existing `ui/` SPA. Backend owns authorization, storage URL generation, and Discord guild emoji metadata. UI owns presentation, grouping, preview loading/fallback states, and responsive layout.

## Phase 0: Research Summary

Research is captured in [research.md](research.md). All technical unknowns are resolved with these decisions:

- Extend existing mapping responses with display-oriented preview fields instead of exposing storage internals.
- Use same-origin backend sticker preview URLs rather than embedding image bytes or browser-direct MinIO internals.
- Resolve server custom emoji display metadata through the existing Discord bot/JDA guild context and degrade to readable shortcode fallback when unavailable.
- Keep UI preview state local to the mapping list with stable CSS dimensions and accessible text labels.

## Phase 1: Design Summary

Design artifacts are captured in:

- [data-model.md](data-model.md)
- [contracts/backend-api-preview-contract.md](contracts/backend-api-preview-contract.md)
- [contracts/ui-preview-contract.md](contracts/ui-preview-contract.md)
- [quickstart.md](quickstart.md)

## Post-Design Constitution Check Re-Evaluation

*Re-checked after Phase 1 design completion.*

| Principle | Status | Notes |
|-----------|--------|-------|
| **I. Code Quality - Linting & Formatting** | PASS | Planned files are inside existing Java and TypeScript formatting/linting paths. |
| **I. Code Quality - Simplicity First** | PASS | Data model and contracts reuse `StickerMapping`, `MappingResponse`, storage download support, and existing `MappingList`; no new persistence layer. |
| **I. Code Quality - Documentation** | PASS | Contracts specify payload shape, fallback behavior, and UI display rules. |
| **I. Code Quality - Error Handling** | PASS | Contracts require per-asset fallback states and prohibit broken image display or storage error leakage. |
| **II. Testing - Coverage** | PASS | Quickstart identifies backend and UI tests for the critical paths; task phase should create tests before implementation where practical. |
| **II. Testing - Critical Paths** | PASS | API contract covers authorized list/upload response, unavailable sticker URL, unavailable emoji metadata, and delete reachability. |
| **II. Testing - Test Independence** | PASS | Tests can use mocked MinIO URL generation, mocked JDA emoji metadata, and UI fixtures. |
| **III. UX Consistency** | PASS | UI contract uses existing mapping card structure and shared controls while replacing text placeholders with previews. |
| **III. Accessibility** | PASS | Data model includes accessible preview labels and UI contract requires alt text or labelled fallback states. |
| **IV. Performance - Response Time** | PASS | Design generates relative URLs only and lets browser fetch images separately; no large binary payloads in JSON. |
| **IV. Performance - Asset Optimization** | PASS | UI contract constrains preview size and layout; animated emoji fallback is allowed if preview is unavailable. |

## Complexity Tracking

> No constitution violations identified. The selected approach is the smallest change that satisfies the display requirements while preserving authorization, storage isolation, and existing bot behavior.
