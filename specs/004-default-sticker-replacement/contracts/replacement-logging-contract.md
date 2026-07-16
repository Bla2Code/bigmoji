# Contract: Default Sticker Replacement Decision and Logging

## Purpose

Define expected observable behavior for emoji detection, default sticker mapping, and diagnostic logging in the listener processing flow.

## Input Contract

### Message Processing Trigger

A message event entering listener processing must provide:
- Event correlation identifiers
- Message content text
- Source metadata required for troubleshooting context

## Decision Contract

For each processed message, the system must produce exactly one terminal decision state:

1. `MAPPED_DEFAULT`
   - Condition: Supported normalized emoji code is detected and mapped to an available default sticker asset.
   - Outcome: Corresponding default sticker send is attempted.

2. `UNMAPPED_EMOJI`
   - Condition: Emoji detected and normalized, but no supported default mapping exists.
   - Outcome: No default sticker replacement.

3. `NO_EMOJI`
   - Condition: No emoji candidate is detected from message content.
   - Outcome: No default sticker replacement.

4. `RESOURCE_MISSING`
   - Condition: Mapping exists but referenced default sticker asset cannot be resolved.
   - Outcome: Replacement is skipped with actionable error log.

## Logging Contract

System must emit logs at these checkpoints:

1. **Message received**
   - Includes event correlation metadata and processing start marker.

2. **Emoji detection result**
   - Indicates whether emoji candidate found.

3. **Normalization result**
   - Indicates normalized lookup code when available.

4. **Mapping lookup result**
   - Indicates mapping hit/miss and mapped default key when hit.

5. **Final decision outcome**
   - Emits terminal state (`MAPPED_DEFAULT`, `UNMAPPED_EMOJI`, `NO_EMOJI`, `RESOURCE_MISSING`).

## Error Handling Contract

- Missing or unreadable default sticker assets must not crash message processing.
- Failures in mapping/resource resolution must be visible through explicit log entries.
- Unsupported or malformed emoji inputs must resolve to non-replacement outcomes without side effects.

## Compatibility Contract

- Existing behavior for non-supported emoji and non-emoji messages remains unchanged.
- Feature is additive for observability and default-replacement correctness only.
