# Research: Fix Sticker Delivery and Bot Name Replacement

## R-001: JDA 5.x File Attachment API

**Decision**: Use `MessageChannelUnion.sendFiles(FileUpload.fromData(byte[], String))` to send sticker images as file attachments.

**Rationale**: JDA 5.x provides `sendFiles()` which accepts `FileUpload` objects. This sends the file as a Discord attachment, rendering it as an image in chat. The current `sendMessage(String)` only sends text.

**Alternatives considered**:
- `sendMessageEmbeds()` with image embed — renders as preview, not as native attachment. Rejected because it doesn't match the expected sticker UX.
- `sendFiles(FileUpload.fromStream())` — requires keeping an InputStream open during the async send. Rejected in favor of `fromData(byte[])` which is simpler and avoids stream lifecycle issues.

**Implementation note**: `FileUpload.fromData(byte[] data, String name)` requires the full file bytes in memory. Sticker files are small (<1MB typically), so this is acceptable.

---

## R-002: Minio File Download Approach

**Decision**: Use `MinioClient.getObject()` to download files directly, rather than downloading via presigned URL with OkHttp.

**Rationale**: `MinioClient.getObject(GetObjectArgs)` returns an `InputStream` that can be read directly. This avoids the extra HTTP hop through the presigned URL and is more reliable (no URL expiry concerns during download). The MinioClient is already configured and available in the service.

**Alternatives considered**:
- Download via presigned URL using OkHttp/RestTemplate — adds an extra HTTP client dependency and is subject to URL expiry. Rejected.
- Use `MinioClient.getObject()` with `InputStream.readAllBytes()` — simple, direct, uses existing MinioClient. Chosen.

**Implementation note**: Add a new method `downloadFile(String bucket, String objectKey): byte[]` to `MinioStorageService`.

---

## R-003: Discord Webhook Lifecycle Management

**Decision**: Create a new webhook per channel on first use, cache it in memory, and reuse it for subsequent sends. Fall back to bot name if webhook creation fails.

**Rationale**: Discord webhooks are channel-scoped. Creating a webhook per channel and caching it avoids repeated API calls. The cache is a simple `ConcurrentHashMap<String, Webhook>` keyed by channel ID. If the bot lacks `MANAGE_WEBHOOKS` permission, webhook creation throws an exception, and we fall back to `sendFiles()` under the bot's own name.

**Alternatives considered**:
- Create a new webhook for every message — excessive API calls, clutters channel webhook list. Rejected.
- Pre-create webhooks at startup — requires knowing all channels upfront, not practical for dynamic guilds. Rejected.
- Use `channel.retrieveWebhooks()` to find existing bot-created webhooks — adds an API call per message. Rejected in favor of in-memory cache with lazy creation.

**Implementation note**: Webhook cache should handle webhook deletion (if a webhook is deleted externally, the next send should recreate it). Cache entries should be invalidated on `ErrorResponse` with code 10013 (Unknown Webhook) or 50013 (Missing Permissions).

---

## R-004: Username Sanitization for Discord Webhooks

**Decision**: Sanitize webhook usernames by: (1) removing `@` and `#` characters, (2) truncating to 32 characters total including the " БОТ" suffix, (3) trimming whitespace.

**Rationale**: Discord webhook usernames have strict rules: max 32 characters, cannot contain `@` or `#` (to prevent mention abuse). The " БОТ" suffix is 4 characters (space + 3 Cyrillic chars), leaving 28 characters for the author name.

**Alternatives considered**:
- Reject messages with invalid names — poor UX, users can't control their Discord names. Rejected.
- Use a fixed fallback name like "Пользователь БОТ" — loses author identity. Rejected.
- Truncate + sanitize — preserves author identity while complying with Discord rules. Chosen.

**Implementation note**: The sanitization function should be a pure utility method, easily testable. Example: `"@lice#123456789012345678901234567890"` → `"lice123456789012345678901234 БОТ"` (28 chars of name + " БОТ" = 32 chars total).

---

## R-005: Fallback Strategy for Webhook Failures

**Decision**: If webhook creation or execution fails, fall back to sending the sticker file under the bot's own name using `sendFiles()`. Log a warning with the failure reason.

**Rationale**: The primary goal is to deliver the sticker image. Webhook impersonation is a secondary enhancement. If webhooks fail (missing permissions, rate limits, etc.), the user should still receive the sticker — just under the bot's name.

**Alternatives considered**:
- Skip sending entirely on webhook failure — message loss, poor UX. Rejected.
- Retry webhook creation — adds latency, may hit rate limits. Rejected for v1; can be added later if needed.
- Fall back to bot name — ensures message delivery, logs the issue for investigation. Chosen.
