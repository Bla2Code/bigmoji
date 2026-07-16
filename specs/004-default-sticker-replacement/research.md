# Phase 0 Research: Default Emoji Sticker Replacement and Diagnostic Logging

## Decision 1: Canonical default key matching through normalized emoji codes

**Decision**: Use the normalized emoji code as the single lookup key, then resolve against the supported default key set (`fire`, `heart`, `party`, `smile`, `thumbsup`) before replacement.

**Rationale**:
- Keeps matching deterministic and easy to debug.
- Aligns with existing detector/normalizer responsibilities in current architecture.
- Prevents ambiguous matching across textual aliases.

**Alternatives considered**:
- Direct raw-message token matching: rejected because it is brittle across emoji variants.
- Fuzzy alias matching: rejected because it introduces unpredictable behavior and false positives.

## Decision 2: Explicit decision-state logging for observability

**Decision**: Emit structured log records at key checkpoints with explicit outcome state: `MAPPED_DEFAULT`, `UNMAPPED_EMOJI`, `NO_EMOJI`.

**Rationale**:
- Directly satisfies troubleshooting need from spec.
- Enables maintainers to isolate failure stage (detection, normalization, lookup, decision).
- Supports fast issue triage without code-level debugging.

**Alternatives considered**:
- Single summary log only: rejected because it hides intermediate failure points.
- Verbose payload logging of full message: rejected to avoid unnecessary data exposure and log noise.

## Decision 3: Safe fallback on missing mapping or missing default asset

**Decision**: When mapping or resource is unavailable, skip replacement, emit actionable warning/error log, and continue listener execution.

**Rationale**:
- Preserves availability and avoids listener crashes.
- Maintains compatibility with existing behavior for unsupported inputs.
- Aligns with constitution requirement for explicit error handling.

**Alternatives considered**:
- Throw runtime exception and fail processing: rejected due to reliability risk.
- Silent skip without logs: rejected because it prevents diagnosis.

## Decision 4: Test strategy centered on behavior parity plus decision-path verification

**Decision**: Extend listener tests to verify mapped/unmapped/no-emoji outcomes and assert that non-target behavior remains unchanged.

**Rationale**:
- Covers feature requirements and regression risk in one suite.
- Keeps tests close to listener contract where the behavior is decided.
- Supports constitution testing standards for edge cases and critical paths.

**Alternatives considered**:
- Manual-only verification in Discord: rejected as insufficient for regression protection.
- Broad end-to-end only tests: rejected due to slower feedback and harder root-cause isolation.