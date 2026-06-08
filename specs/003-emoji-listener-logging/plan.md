# Implementation Plan: Emoji Message Listener Logging

**Branch**: `003-emoji-listener-logging` | **Date**: 2026-06-08 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/003-emoji-listener-logging/spec.md`

## Summary

Add structured logging to the Discord message listener at two points: message intake and message processing start. Keep message-handling behavior unchanged. Ensure logs are visible in runtime output by setting an explicit logger level for the Discord listener package.

## Technical Context

**Language/Version**: Java 21

**Primary Dependencies**: Spring Boot 4.0.6, JDA 5.x, SLF4J/Logback (via Spring Boot)

**Storage**: N/A (no storage changes)

**Testing**: JUnit 5, Mockito, existing listener tests plus log-verification tests

**Target Platform**: Linux server (Docker container) and local macOS development

**Project Type**: Backend web service + Discord bot listener

**Performance Goals**:
- Message processing remains within existing SLA (no measurable degradation)
- Added logging does not increase median message handling latency by more than 5%

**Constraints**:
- Only logging-related changes are allowed in listener logic
- Existing bot behavior for eligible/ineligible messages must remain identical
- Logging level must be configured so intake/processing logs are visible during verification

**Scale/Scope**:
- Applies to one listener class and logging configuration
- Supports existing target scale of up to 100 concurrent Discord servers

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Code Quality - Linting & Formatting | ✅ PASS | Changes are limited and compatible with existing style/Spotless setup |
| I. Code Quality - Simplicity First | ✅ PASS | No new abstraction; only additive logs + logger-level config |
| I. Code Quality - Error Handling | ✅ PASS | Existing error paths remain unchanged |
| II. Testing Standards | ✅ PASS | Add focused tests for log emission/visibility without changing behavior tests |
| III. UX Consistency | ✅ PASS | No user-facing behavior changes |
| IV. Performance Requirements | ✅ PASS | Logging overhead expected minimal; validated with existing performance baseline |

## Project Structure

### Documentation (this feature)

```text
specs/003-emoji-listener-logging/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── logging-contract.md
└── tasks.md
```

### Source Code (repository root)

```text
src/
├── main/
│   ├── java/com/bigmoji/discord/
│   │   └── EmojiMessageListener.java
│   └── resources/
│       └── application.yml
└── test/
    └── java/com/bigmoji/discord/
        └── EmojiMessageListenerTest.java
```

**Structure Decision**: Keep the existing single Spring Boot service structure. Touch only listener logging points, logging configuration, and listener-focused tests.

## Phase 0: Research Outcomes

Research artifacts documented in [research.md](research.md) confirm:
- Recommended log level for message-intake diagnostics
- Safe context fields for observability
- Test strategy for log verification without altering behavior

## Phase 1: Design & Contracts

Design artifacts:
- [data-model.md](data-model.md): log event entities and validation rules
- [contracts/logging-contract.md](contracts/logging-contract.md): expected log semantics and fields
- [quickstart.md](quickstart.md): verification flow including log-level configuration

## Post-Design Constitution Check Re-Evaluation

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Code Quality - Simplicity First | ✅ PASS | Design keeps listener flow unchanged; additive logs only |
| II. Testing Standards | ✅ PASS | Defines explicit log-emission and regression behavior checks |
| IV. Performance Requirements | ✅ PASS | Log level is controlled via configuration; can be tuned per environment |

## Implementation Notes

- Applied listener receipt and processing logs in `src/main/java/com/bigmoji/discord/EmojiMessageListener.java`.
- Added logger visibility configuration in `src/main/resources/application.yml`:
  - `logging.level.com.bigmoji.discord: DEBUG`
- Listener-focused test verification passed:
  - `./gradlew test --tests com.bigmoji.discord.EmojiMessageListenerTest`

## Complexity Tracking

No constitution violations identified. No complexity exemptions required.
