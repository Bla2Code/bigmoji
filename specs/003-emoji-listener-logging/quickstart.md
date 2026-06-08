# Quickstart: EmojiMessageListener Logging

## Goal

Verify that listener logs are emitted at message receipt and processing-start stages, and that they are visible with the configured log level.

## Prerequisites

- Bot token and local dependencies are configured.
- Application starts successfully.
- Discord test channel is available.

## Configuration

Set logger level for Discord listener package to ensure visibility:

```yaml
logging:
  level:
    com.bigmoji.discord: DEBUG
```

Place this in `src/main/resources/application.yml` (or environment-specific profile used for verification).

## Verification Steps

1. Start the application.
2. Send a regular non-emoji message from a user account.
3. Confirm receipt-stage debug log appears with message context.
4. Send a single-emoji message eligible for handling.
5. Confirm both logs appear in order:
   - message received
   - message processing started
6. Confirm functional behavior remains unchanged:
   - eligible message still follows replacement flow
   - ineligible message still does not trigger replacement

## Test Guidance

- Keep existing listener behavior tests as baseline regression.
- Add or extend tests to verify:
  - receipt-stage log emission
  - processing-stage log emission for eligible messages
- Run focused test suite for listener and full project checks before merge.

## Validation Evidence

- Listener-focused verification command executed:
  - `./gradlew test --tests com.bigmoji.discord.EmojiMessageListenerTest`
- Result: passed.

## Rollback

If log volume is too high in a given environment, reduce logger level (for example to `INFO`) without code rollback.