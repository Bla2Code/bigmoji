# REST API Contract: Bigmoji Sticker Mappings

**Base URL**: `http://localhost:8080/api`
**Authentication**: Discord OAuth2 login issues an HttpOnly `bigmoji_session` cookie. Mapping endpoints require this session cookie and verify the authenticated user can manage the requested Discord guild.
**Content-Type**: `application/json` (except upload which uses `multipart/form-data`)

---

## Endpoints

### 1. Start Discord Login

**GET** `/api/auth/discord/login`

Redirect the browser to Discord OAuth2 authorization using `identify guilds` scopes.

**Authentication**: Not required

**Response 302 Found**:
- `Location`: Discord OAuth2 authorize URL
- `Set-Cookie`: short-lived `bigmoji_oauth_state` CSRF/state cookie

---

### 2. Discord OAuth2 Callback

**GET** `/api/auth/discord/callback?code={code}&state={state}`

Exchange Discord OAuth2 code for user identity and guild permissions, then issue a signed server session cookie.

**Authentication**: Discord callback only

**Response 302 Found**:
- `Location`: configured UI URL
- `Set-Cookie`: HttpOnly `bigmoji_session` cookie

**Error Responses**:
- `401 Unauthorized`: Invalid OAuth state or failed Discord OAuth2 exchange

---

### 3. Current Session

**GET** `/api/auth/me`

Return the authenticated Discord user and manageable guilds available to the UI.

**Authentication**: Required (`bigmoji_session` cookie)

**Response 200 OK**:
```json
{
  "userId": "111111111111111111",
  "username": "admin",
  "globalName": "Admin",
  "manageableGuilds": [
    {
      "id": "123456789012345678",
      "name": "My Discord Server"
    }
  ],
  "expiresAt": "2026-06-23T20:00:00Z"
}
```

**Error Responses**:
- `401 Unauthorized`: Missing, invalid, or expired session

---

### 4. Logout

**POST** `/api/auth/logout`

Clear the signed session cookie.

**Authentication**: Not required

**Response 204 No Content**

---

### 5. Get Discord Bot Install URL

**GET** `/api/auth/discord/install-url?guildId={guildId}`

Return a Discord OAuth2 bot install URL. When `guildId` is supplied, the URL preselects that guild and disables guild selection.

**Authentication**: Not required

**Response 200 OK**:
```json
{
  "url": "https://discord.com/api/oauth2/authorize?client_id=..."
}
```

---

### 6. Upload Sticker Mapping

**POST** `/api/mappings`

Upload a new sticker image and create an emoji-to-sticker mapping.

**Authentication**: Required (`bigmoji_session` cookie). User must manage the submitted `guildId`.

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
  "createdAt": "2026-06-03T15:00:00Z",
  "updatedAt": "2026-06-03T15:00:00Z"
}
```

**Error Responses**:
- `400 Bad Request`: Missing required fields, invalid file format/size
- `401 Unauthorized`: Missing, invalid, or expired session
- `403 Forbidden`: Authenticated user cannot manage the submitted guild
- `500 Internal Server Error`: MinIO upload failure

---

### 7. List Mappings for Guild

**GET** `/api/mappings/{guildId}`

Retrieve all persisted emoji-to-sticker mappings for a specific Discord server. Runtime local fallback stickers are excluded from this endpoint.

**Authentication**: Required (`bigmoji_session` cookie). User must manage `guildId`.

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
    "createdAt": "2026-06-03T16:00:00Z",
    "updatedAt": "2026-06-03T16:00:00Z"
  }
]
```

**Error Responses**:
- `401 Unauthorized`: Missing, invalid, or expired session
- `403 Forbidden`: Authenticated user cannot manage the requested guild
- `404 Not Found`: Guild has no mappings

---

### 8. Delete Mapping

**DELETE** `/api/mappings/{id}`

Delete a specific emoji-to-sticker mapping and remove the image from MinIO.

**Authentication**: Required (`bigmoji_session` cookie). User must manage the guild that owns the mapping.

**Path Parameters**:
- `id` (UUID): Mapping identifier

**Response 204 No Content**: Mapping deleted successfully

**Error Responses**:
- `401 Unauthorized`: Missing, invalid, or expired session
- `403 Forbidden`: Authenticated user cannot manage the mapping's guild
- `404 Not Found`: Mapping not found

---

### 9. Get Default Stickers

**GET** `/api/stickers/default`

Return the list of default emoji-to-sticker mappings available to all servers.

**Authentication**: Required (`bigmoji_session` cookie)

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

### 10. Health Check

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

Mapping and session-info endpoints require the `bigmoji_session` cookie issued by the Discord OAuth2 callback.

- Login scope: Discord OAuth2 `identify guilds`
- Session cookie: `bigmoji_session`, signed by server secret, HttpOnly, SameSite=Lax
- Authorization: A request for `guildId` is allowed only if the authenticated Discord user is guild owner, Administrator, or has Manage Server permission for that guild
- Static API keys: Not accepted for UI/admin mapping operations
- Failure: Returns `401 Unauthorized` for missing/invalid sessions and `403 Forbidden` for guild authorization failures
