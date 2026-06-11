# Data Model: Fix Sticker Delivery and Bot Name Replacement

## Entities

### StickerFile (existing, no schema change)

Represents a sticker image stored in Minio.

| Field | Type | Description |
|-------|------|-------------|
| minioBucketName | String | Minio bucket for the guild |
| minioObjectKey | String | Object key (UUID + extension) |

**State transitions**: None changed. File lifecycle remains: upload → store → download → send.

### WebhookCache (NEW)

In-memory cache of Discord webhooks keyed by channel ID.

| Field | Type | Description |
|-------|------|-------------|
| channelId | String | Discord channel ID (cache key) |
| webhook | Webhook | JDA Webhook object |
| createdAt | Instant | When the webhook was created |

**State transitions**:
- `MISS` → `CREATED`: Webhook created via `channel.createWebhook()` on first send
- `CREATED` → `INVALIDATED`: Webhook deleted externally or permissions revoked
- `INVALIDATED` → `CREATED`: Webhook recreated on next send

**Concurrency**: `ConcurrentHashMap<String, Webhook>` for thread-safe access.

### WebhookUsername (NEW, derived)

Sanitized username for webhook impersonation.

| Field | Type | Description |
|-------|------|-------------|
| rawName | String | Original author display name |
| sanitizedName | String | Sanitized name + " БОТ" suffix, max 32 chars |
| avatarUrl | String | Author's avatar URL |

**Derivation rules**:
1. Remove `@` and `#` characters from rawName
2. Trim whitespace
3. Truncate to 28 characters (leaving 4 for " БОТ")
4. Append " БОТ"
5. Result max length: 32 characters

### StickerDeliveryContext (NEW, internal)

Transient context object passed through the delivery pipeline.

| Field | Type | Description |
|-------|------|-------------|
| channel | MessageChannelUnion | Target Discord channel |
| stickerBytes | byte[] | Downloaded sticker file data |
| stickerFileName | String | Filename for the attachment |
| authorName | String | Original message author's display name |
| authorAvatarUrl | String | Original message author's avatar URL |
| useWebhook | boolean | Whether to attempt webhook impersonation |

## Flow State Machine

```
[Emoji Detected]
       │
       ▼
[Mapping Found] ──no──→ [Skip: no mapping]
       │
      yes
       ▼
[Download from Minio] ─fail──→ [Log error, skip]
       │
     success
       ▼
[Attempt Webhook Send] ──fail──→ [Fallback: sendFiles under bot name]
       │
     success
       ▼
[Delete Original Message]
       │
       ▼
[Done]
```

## Validation Rules

- Sticker file size MUST be ≤ 8MB (Discord limit)
- Webhook username MUST be ≤ 32 characters after sanitization
- Webhook username MUST NOT contain `@` or `#`
- Minio download MUST complete within 5 seconds (presigned URL validity window)
