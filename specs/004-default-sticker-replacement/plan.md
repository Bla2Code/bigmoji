# Implementation Plan: Default Emoji Sticker Replacement and Diagnostic Logging

**Branch**: `feature/004-default-sticker-replacement` | **Date**: 2026-06-09 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/004-default-sticker-replacement/spec.md`

## Summary

Restore expected behavior where supported incoming emojis are converted into default stickers from the bundled default set, and add structured diagnostic logging across detection, normalization, lookup, and replacement decision points. Preserve existing behavior for unsupported or non-emoji inputs.

## Technical Context

**Language/Version**: Java 21

**Primary Dependencies**: Spring Boot 4.0.6, JDA 5.x, SLF4J/Logback, existing emoji and sticker domain services

**Storage**: Existing persistence/cache for sticker mappings (no schema changes expected)

**Testing**: JUnit 5, Mockito, existing listener/service tests, targeted regression and log-path assertions

**Target Platform**: Linux container runtime and local macOS development

**Project Type**: Backend service with Discord event listener

**Performance Goals**:
- Preserve current listener responsiveness under normal message throughput
- Keep incremental processing overhead from additional lookup/logging within 5% median latency increase

**Constraints**:
- Default replacement must only trigger for supported normalized emoji keys
- Unsupported emoji content must not produce incorrect sticker sends
- Logging must expose mapped/unmapped/no-emoji outcomes without leaking sensitive content
- Changes must remain compatible with existing listener and sender flow

**Scale/Scope**:
- Primarily impacts emoji listener flow, mapping resolution path, and diagnostic logging
- Scope is bounded to current supported default stickers (fire, heart, party, smile, thumbsup)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Code Quality - Linting & Formatting | ✅ PASS | Planned changes are localized and follow existing Java/Spring style |
| I. Code Quality - Simplicity First | ✅ PASS | Reuses existing detection/normalization/mapping services without new layers |
| I. Code Quality - Error Handling | ✅ PASS | Missing mapping/resource cases explicitly logged and handled without crashes |
| II. Testing Standards | ✅ PASS | Plan includes unit + integration-path verification for mapped/unmapped/no-emoji flows |
| III. UX Consistency | ✅ PASS | User-visible behavior is improved only for intended default emoji replacements |
| IV. Performance Requirements | ✅ PASS | Logging and lookup additions are bounded and validated against existing listener behavior |

## Project Structure

### Documentation (this feature)

```text
specs/004-default-sticker-replacement/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── replacement-logging-contract.md
└── tasks.md
```

### Source Code (repository root)

```text
src/
├── main/
│   ├── java/com/bigmoji/discord/
│   │   └── EmojiMessageListener.java
│   ├── java/com/bigmoji/emoji/
│   │   ├── EmojiDetector.java
│   │   └── EmojiNormalizer.java
│   ├── java/com/bigmoji/sticker/
│   │   ├── StickerMappingCache.java
│   │   └── StickerMappingService.java
│   └── resources/
│       └── default-stickers/
└── test/
    └── java/com/bigmoji/discord/
        └── EmojiMessageListenerTest.java
```

**Structure Decision**: Keep single-module Spring Boot backend layout and constrain changes to listener, emoji/mapping interaction points, and listener-focused tests.

## Phase 0: Research Outcomes

Research outcomes are documented in [research.md](research.md) and resolve:
- Default emoji-to-sticker key matching strategy
- Logging decision schema for mapped/unmapped/no-emoji outcomes
- Safe fallback behavior for missing resources and unmapped codes

## Phase 1: Design & Contracts

Design artifacts:
- [data-model.md](data-model.md): entities and state transitions for emoji replacement decision flow
- [contracts/replacement-logging-contract.md](contracts/replacement-logging-contract.md): observable decision and logging contract
- [quickstart.md](quickstart.md): end-to-end validation flow and expected runtime outcomes

## Post-Design Constitution Check Re-Evaluation

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Code Quality - Simplicity First | ✅ PASS | Design keeps existing components and adds only required decision points |
| II. Testing Standards | ✅ PASS | Artifacts define measurable checks for all primary and edge flows |
| III. UX Consistency | ✅ PASS | Supported emoji now consistently map to expected default sticker outcomes |
| IV. Performance Requirements | ✅ PASS | Lookup and logging remain lightweight and bounded to message processing path |

## Complexity Tracking

No constitution violations identified. No complexity exemptions required.
