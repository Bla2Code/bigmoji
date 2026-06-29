# Contract: Mapping Preview Backend API

**Consumer**: `ui/` React admin application  
**Provider**: Bigmoji backend  
**Scope**: Authenticated mapping management for one selected guild  
**Credentials**: Browser requests include backend session cookies

## Authorization Rules

- All endpoints remain scoped to the authenticated admin session.
- The selected `guildId` must be present in the authenticated user's manageable guild list.
- Users must not receive custom emoji metadata or sticker preview URLs for guilds they are not authorized to manage.
- Delete authorization remains based on the mapping's owning `guildId`.

## List Custom Mappings With Previews

```http
GET /api/mappings/{guildId}
```

### Response 200

```json
[
  {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "guildId": "123456789012345678",
    "emojiName": ":party_blob:",
    "isDefault": false,
    "createdAt": "2026-06-24T16:00:00Z",
    "updatedAt": "2026-06-24T16:00:00Z",
    "stickerPreviewUrl": "/api/mappings/a1b2c3d4-e5f6-7890-abcd-ef1234567890/preview",
    "stickerPreviewState": "available",
    "emojiPreview": {
      "id": "987654321098765432",
      "name": "party_blob",
      "shortcode": ":party_blob:",
      "imageUrl": "https://cdn.discordapp.com/emojis/987654321098765432.png",
      "animated": false,
      "available": true
    }
  }
]
```

### Required Fields

| Field | Required | Notes |
|-------|----------|-------|
| `id` | Yes | UUID string used for delete |
| `guildId` | Yes | Owning guild |
| `emojiName` | Yes | Stored trigger fallback |
| `isDefault` | Yes | Custom/default classification |
| `createdAt` | Yes | ISO timestamp |
| `updatedAt` | Yes | ISO timestamp |
| `stickerPreviewState` | Yes | `available` or `unavailable` |
| `stickerPreviewUrl` | No | Present only when preview is available |
| `emojiPreview` | No | Present when the trigger matches a server custom emoji or display metadata is known |

### Unavailable Sticker Preview

```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "guildId": "123456789012345678",
  "emojiName": ":party_blob:",
  "isDefault": false,
  "createdAt": "2026-06-24T16:00:00Z",
  "updatedAt": "2026-06-24T16:00:00Z",
  "stickerPreviewState": "unavailable",
  "emojiPreview": {
    "id": "987654321098765432",
    "name": "party_blob",
    "shortcode": ":party_blob:",
    "animated": false,
    "available": false
  }
}
```

Backend behavior:
- Return the mapping even when a preview URL cannot be generated.
- Do not expose raw MinIO exceptions, bucket names, object keys, or stack traces in the response body.
- Log internal preview generation failures for operators.

### Standard Unicode Emoji or Text Trigger

If `emojiName` does not match a server custom emoji, omit `emojiPreview` and preserve `emojiName` as the readable fallback:

```json
{
  "id": "b1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "guildId": "123456789012345678",
  "emojiName": "😊",
  "isDefault": false,
  "createdAt": "2026-06-24T16:10:00Z",
  "updatedAt": "2026-06-24T16:10:00Z",
  "stickerPreviewUrl": "/api/mappings/b1b2c3d4-e5f6-7890-abcd-ef1234567890/preview",
  "stickerPreviewState": "available"
}
```

## Upload Sticker Mapping

```http
POST /api/mappings
Content-Type: multipart/form-data
```

Fields remain unchanged:

| Field | Required | Description |
|-------|----------|-------------|
| `guildId` | Yes | Selected manageable guild ID |
| `emojiName` | Yes | Unicode emoji, custom emoji shortcode, or text trigger |
| `file` | Yes | Uploaded sticker image |

### Response 201

The response uses the same preview-capable shape as `GET /api/mappings/{guildId}` for the newly created mapping. This lets the UI show a fresh upload with the same preview treatment as existing mappings.

## Preview Uploaded Sticker

```http
GET /api/mappings/{id}/preview
```

Behavior:
- Backend loads the mapping by `id`.
- Backend verifies the authenticated admin can manage the mapping's owning guild.
- Backend streams the uploaded sticker bytes with an image content type when possible.
- `401`, `403`, and `404` follow the same authorization and not-found rules as delete.

## Delete Mapping

```http
DELETE /api/mappings/{id}
```

Behavior remains unchanged:
- `204 No Content` after successful delete.
- `401` when session is missing or expired.
- `403` when the authenticated user cannot manage the mapping's guild.
- `404` when the mapping does not exist.

## Error Handling

| Backend Result | UI Meaning | Required Display Behavior |
|----------------|------------|---------------------------|
| `200` with `stickerPreviewState: "available"` | Same-origin preview endpoint can be requested | Render image and accessible label |
| `200` with `stickerPreviewState: "unavailable"` | Mapping exists, sticker preview unavailable | Render unavailable placeholder and keep delete available |
| `200` with missing `emojiPreview` | No server custom emoji match | Render readable `emojiName` fallback |
| `401` | Session expired | Prompt sign-in |
| `403` | Guild access denied | Hide upload/delete controls for invalid guild |
| `404` on list | No mappings or guild not found per existing handling | Render empty state if appropriate |
| `5xx` or network error | Backend unavailable | Show retryable error state |
