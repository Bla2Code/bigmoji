# Contract: Webhook Sticker Sender Service

## Interface

```java
public interface WebhookStickerSender {
    /**
     * Sends a sticker file via webhook impersonating the original message author.
     *
     * @param channel       the target Discord channel
     * @param stickerBytes  the sticker file data
     * @param fileName      the filename for the attachment (e.g., "sticker.png")
     * @param authorName    the original message author's display name
     * @param authorAvatarUrl the original message author's avatar URL
     * @return true if sent via webhook, false if fell back to bot name
     */
    boolean sendAsAuthor(
        MessageChannelUnion channel,
        byte[] stickerBytes,
        String fileName,
        String authorName,
        String authorAvatarUrl
    );
}
```

## Behavior Contract

### Success Path

1. **Given** a valid channel, sticker bytes, and author info
2. **When** `sendAsAuthor()` is called
3. **Then** the service:
   - Looks up or creates a webhook for the channel
   - Sanitizes the author name (remove `@`/`#`, truncate to 28 chars, append " БОТ")
   - Sends the file via `webhook.sendFiles()` with the sanitized username and avatar URL
   - Returns `true`

### Webhook Creation

1. **Given** no cached webhook exists for the channel
2. **When** the service needs to send a sticker
3. **Then** it calls `channel.createWebhook("Bigmoji Sticker Bot")` and caches the result

### Webhook Cache Invalidation

1. **Given** a cached webhook that has been deleted externally or lost permissions
2. **When** `webhook.sendFiles()` throws an exception
3. **Then** the service:
   - Removes the webhook from cache
   - Attempts to recreate the webhook
   - If recreation fails, falls back to `sendFiles()` under bot name

### Fallback Path

1. **Given** webhook creation or execution fails (missing permissions, rate limit, etc.)
2. **When** the service cannot send via webhook
3. **Then** it:
   - Logs a warning with the failure reason
   - Sends the sticker via `channel.sendFiles()` under the bot's own name
   - Returns `false`

### Username Sanitization

| Input | Output |
|-------|--------|
| `"Alice"` | `"Alice БОТ"` |
| `"@lice#123"` | `"lice123 БОТ"` |
| `"A".repeat(50)` | `"AAAAAAAAAAAAAAAAAAAAAAAAAAAA БОТ"` (28 A's + " БОТ" = 32 chars) |
| `""` | `"БОТ"` |
| `"  "` | `"БОТ"` |

## Error Handling

| Error | Behavior |
|-------|----------|
| `PermissionException` (Missing MANAGE_WEBHOOKS) | Log warning, fallback to bot name |
| `ErrorResponse` (10013 - Unknown Webhook) | Invalidate cache, retry once, then fallback |
| `ErrorResponse` (50013 - Missing Permissions) | Log warning, fallback to bot name |
| `RateLimitedException` | Log warning, fallback to bot name |
| `IOException` (Minio download) | Log error, skip send entirely |

## Dependencies

- JDA 5.x `Webhook`, `WebhookClient`, `FileUpload`
- `MinioStorageService.downloadFile(bucket, objectKey): byte[]`
- SLF4J Logger
