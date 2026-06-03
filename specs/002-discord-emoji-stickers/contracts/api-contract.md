# REST API Contract: Bigmoji Sticker Mappings

**Base URL**: `http://localhost:8080/api`
**Authentication**: `X-API-Key` header required for all endpoints except health check
**Content-Type**: `application/json` (except upload which uses `multipart/form-data`)

---

## Endpoints

### 1. Upload Sticker Mapping

**POST** `/api/mappings`

Upload a new sticker image and create an emoji-to-sticker mapping.

**Authentication**: Required (`X-API-Key` header)

**Request**:
- Content-Type: `multipart/form-data`
- Fields:
  - `emojiName` (string, required): Emoji identifier (Unicode character or shortcode name)
  - `guildId` (string, required): Discord server ID
  - `file` (file, required): Sticker image file (PNG, JPG, WEBP; max 512KB)

**Response 201 Created**:
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "guildId": "123456789012345678",
  "emojiName": "😊",
  "minioBucketName": "bigmoji-123456789012345678",
  "minioObjectKey": "f47ac10b-58cc-4372-a567-0e02b2c3d479.png",
  "isDefault": false,
  "createdAt": "2026-06-03T15:00:00Z",
  "updatedAt": "2026-06-03T15:00:00Z"
}
```

**Error Responses**:
- `400 Bad Request`: Missing required fields, invalid file format/size
- `401 Unauthorized`: Missing or invalid API key
- `500 Internal Server Error`: MinIO upload failure

---

### 2. List Mappings for Guild

**GET** `/api/mappings/{guildId}`

Retrieve all emoji-to-sticker mappings for a specific Discord server.

**Authentication**: Required (`X-API-Key` header)

**Path Parameters**:
- `guildId` (string): Discord server ID

**Response 200 OK**:
```json
[
  {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "guildId": "123456789012345678",
    "emojiName": "😊",
    "minioBucketName": "bigmoji-123456789012345678",
    "minioObjectKey": "f47ac10b-58cc-4372-a567-0e02b2c3d479.png",
    "isDefault": true,
    "createdAt": "2026-06-03T15:00:00Z",
    "updatedAt": "2026-06-03T15:00:00Z"
  },
  {
    "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "guildId": "123456789012345678",
    "emojiName": "😊",
    "minioBucketName": "bigmoji-123456789012345678",
    "minioObjectKey": "a1b2c3d4-e5f6-7890-abcd-ef9876543210.png",
    "isDefault": false,
    "createdAt": "2026-06-03T16:00:00Z",
    "updatedAt": "2026-06-03T16:00:00Z"
  }
]
```

**Error Responses**:
- `401 Unauthorized`: Missing or invalid API key
- `404 Not Found`: Guild has no mappings

---

### 3. Delete Mapping

**DELETE** `/api/mappings/{id}`

Delete a specific emoji-to-sticker mapping and remove the image from MinIO.

**Authentication**: Required (`X-API-Key` header)

**Path Parameters**:
- `id` (UUID): Mapping identifier

**Response 204 No Content**: Mapping deleted successfully

**Error Responses**:
- `401 Unauthorized`: Missing or invalid API key
- `404 Not Found`: Mapping not found

---

### 4. Get Default Stickers

**GET** `/api/stickers/default`

Return the list of default emoji-to-sticker mappings available to all servers.

**Authentication**: Required (`X-API-Key` header)

**Response 200 OK**:
```json
[
  {
    "emojiName": "😊",
    "shortcodeName": "smile",
    "description": "Smiling face"
  },
  {
    "emojiName": "❤️",
    "shortcodeName": "heart",
    "description": "Red heart"
  },
  {
    "emojiName": "🎉",
    "shortcodeName": "party",
    "description": "Party popper"
  },
  {
    "emojiName": "👍",
    "shortcodeName": "thumbsup",
    "description": "Thumbs up"
  },
  {
    "emojiName": "🔥",
    "shortcodeName": "fire",
    "description": "Fire"
  }
]
```

---

### 5. Health Check

**GET** `/actuator/health`

Spring Boot Actuator health endpoint.

**Authentication**: Not required

**Response 200 OK**:
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "minio": { "status": "UP" }
  }
}
```

---

## Error Response Format

All error responses (except 204) follow this format:

```json
{
  "timestamp": "2026-06-03T15:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "File size exceeds maximum allowed size of 512KB",
  "path": "/api/mappings"
}
```

---

## Authentication

All endpoints (except `/actuator/health`) require the `X-API-Key` header.

- Header name: `X-API-Key`
- Value: Static API key configured via `API_KEY` environment variable
- Validation: Performed by `ApiKeyInterceptor` before controller execution
- Failure: Returns `401 Unauthorized` with error response format above
