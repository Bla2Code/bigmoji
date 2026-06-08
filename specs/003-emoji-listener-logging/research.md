# Research: Emoji Message Listener Logging

## Decision 1: Use `DEBUG` level for listener observability logs

- **Decision**: Emit intake/processing diagnostics at `DEBUG` level and enable package-level logger visibility via configuration (`com.bigmoji.discord=DEBUG`) in environments used for verification.
- **Rationale**: Intake and processing logs are high-frequency operational diagnostics; `DEBUG` is appropriate and avoids noisy `INFO` in production by default.
- **Alternatives considered**:
  - `INFO` for all message events: rejected due to excessive volume in active guilds.
  - `TRACE`: rejected because too low-level for routine troubleshooting and often disabled.

## Decision 2: Log only safe correlation context

- **Decision**: Include guild ID, channel ID, author ID, bot-flag, raw content, and normalized emoji (when processing starts).
- **Rationale**: This context is sufficient to confirm receipt and processing transitions without changing business logic.
- **Alternatives considered**:
  - Minimal context (only message text): rejected due to weak troubleshooting value.
  - Expanded context with full metadata: rejected as unnecessary scope increase.

## Decision 3: Validate through focused listener tests and runtime config check

- **Decision**: Keep existing functional tests for behavior parity and add/extend assertions for log emission where practical; verify logger-level visibility through configuration-driven run.
- **Rationale**: Ensures feature objective (observability) is met while preserving no-logic-change requirement.
- **Alternatives considered**:
  - Manual-only verification: rejected because it is non-repeatable.
  - Broad integration-only logging checks: rejected due to slower feedback and unnecessary complexity.