# Quickstart: Fix Sticker Delivery and Bot Name Replacement

## End-to-End Validation Flow

### Prerequisites

1. Bot is running with `MANAGE_WEBHOOKS` permission in the target channel
2. Minio is running and contains at least one sticker mapping
3. At least one default sticker exists in `src/main/resources/default-stickers/`

### Step 1: Send a test emoji message

Send a message in Discord containing a single supported emoji (e.g., 🔥, ❤️, 🎉, , 👍).

**Expected outcome**:
- The original emoji message is deleted
- A sticker image appears in the channel as a file attachment (not a URL)
- The sticker appears under the sender's name with Discord's standard bot badge (e.g., username `Alice` plus the `БОТ` badge)
- The sticker uses the sender's avatar

### Step 2: Verify fallback behavior

Remove `MANAGE_WEBHOOKS` permission from the bot in the target channel. Send another emoji message.

**Expected outcome**:
- The sticker image still appears as a file attachment
- The sticker appears under the bot's own name ("Bigmoji БОТ")
- A warning is logged about webhook creation failure

### Step 3: Verify nickname preservation

Set the sender's visible server nickname to `Ne_Tort` and send a single supported emoji.

**Expected outcome**:
- The sticker appears under username `Ne_Tort` with Discord's standard `БОТ` badge
- The username is not lowercased or prefixed, e.g. not `.ne_tort`
- The username does not contain textual `БОТ`

### Step 4: Verify non-target messages

Send a message with:
- No emoji (plain text)
- Multiple emojis
- An unsupported emoji

**Expected outcome**:
- No sticker is sent
- No errors in logs
- Original messages remain untouched

### Step 5: Verify logging

Check application logs after sending test messages.

**Expected log entries**:
- `Emoji detection result: ... isSingleEmojiMessage=true`
- `Mapping lookup result: ... found=true, objectKey=...`
- `Sticker downloaded: bucket=..., objectKey=..., sizeBytes=...`
- `Sticker sent via webhook: channel=..., author=Ne_Tort` (success path)
- OR `Webhook failed, falling back to bot name: reason=...` (fallback path)

## Running Tests

```bash
# Unit tests
./gradlew test

# Specific test classes
./gradlew test --tests "com.bigmoji.discord.EmojiMessageListenerTest"
./gradlew test --tests "com.bigmoji.discord.WebhookStickerSenderTest"
./gradlew test --tests "com.bigmoji.storage.MinioStorageServiceTest"
```

### Verification Results

- 2026-06-17: `./gradlew test --tests "com.bigmoji.discord.EmojiMessageListenerTest" --tests "com.bigmoji.discord.WebhookUsernameSanitizerTest"` passed.
- 2026-06-17: `./gradlew test --tests "com.bigmoji.discord.*"` passed.
- 2026-06-17: `./gradlew test` passed.
- 2026-06-17: `./gradlew spotlessApply` passed.
- 2026-06-18: `./gradlew test --tests "com.bigmoji.discord.EmojiMessageListenerTest" --tests "com.bigmoji.discord.WebhookUsernameSanitizerTest"` passed after removing textual `БОТ` from webhook usernames.
- 2026-06-18: `./gradlew spotlessApply` passed.
- 2026-06-18: `./gradlew test` passed.

## Local Development

```bash
# Start Minio
docker compose up -d minio

# Run application
./gradlew bootRun

# Or with debug
./gradlew bootRun --debug-jvm
```
