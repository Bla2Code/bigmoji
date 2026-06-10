# Quickstart: Validate Default Sticker Replacement and Decision Logging

## Goal

Verify that supported emojis trigger default sticker replacement and that logs clearly show mapped/unmapped/no-emoji outcomes.

## Prerequisites

- Application runs with Discord listener enabled.
- Default sticker resources are present under `src/main/resources/default-stickers/`.
- Runtime logs are accessible.

## Validation Scenarios

### Scenario A: Supported default emoji

1. Send a message containing one supported emoji.
2. Confirm replacement action uses the corresponding default sticker.
3. Confirm logs include:
   - message received
   - emoji detected
   - normalized code
   - mapping hit
   - final state `MAPPED_DEFAULT`

### Scenario B: Unmapped emoji-like content

1. Send a message with emoji-like content that is not supported by default mapping.
2. Confirm no default sticker is sent.
3. Confirm logs include final state `UNMAPPED_EMOJI`.

### Scenario C: Message without emoji

1. Send a plain text message without emoji.
2. Confirm no default sticker is sent.
3. Confirm logs include final state `NO_EMOJI`.

### Scenario D: Missing mapped asset (fault simulation)

1. Simulate unavailable default sticker asset for a mapped key.
2. Send message containing corresponding emoji.
3. Confirm processing does not crash.
4. Confirm logs include actionable error and final state `RESOURCE_MISSING`.

## Regression Checks

- Re-run existing listener tests and ensure unsupported-input behavior is unchanged.
- Verify no incorrect sticker sends occur for non-supported inputs.

## Success Confirmation

Feature is validated when:
- Supported emojis consistently trigger the correct default sticker.
- Every processed message has a clear terminal decision log.
- Non-target behavior remains unchanged.