# Contract: UI Backend API Usage

**Consumer**: `ui/` React application
**Provider**: Existing Bigmoji backend
**Browser Base Path**: `/api`
**Container Proxy Target**: `BIGMOJI_BACKEND_URL`
**Credentials**: Browser requests include cookies (`credentials: "include"`)

The UI calls relative `/api` URLs. In Docker, nginx proxies `/api` to the backend. This keeps OAuth/session cookies same-origin from the browser perspective.

## Authentication Endpoints

### Start Discord Sign-In

**UI Action**: User clicks sign in/register on homepage.

**Request**:

```http
GET /api/auth/discord/login
```

**Expected Result**:
- Browser is redirected to Discord OAuth.
- Backend sets OAuth state cookie as needed.

**UI Handling**:
- This is a full browser navigation, not an AJAX call.
- Sign-in button should be disabled only while navigation is being triggered.

### OAuth Callback

**Request**:

```http
GET /api/auth/discord/callback?code={code}&state={state}
```

**Expected Result**:
- Backend creates session cookie.
- Backend redirects to `UI_URL`.

**Deployment Rule**:
- For UI-on-port-3000 local Docker, backend `DISCORD_REDIRECT_URI` should be `http://localhost:3000/api/auth/discord/callback`.

### Current Session

**Request**:

```http
GET /api/auth/me
```

**Response 200**:

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
  "expiresAt": "2026-06-24T20:00:00Z"
}
```

**UI Handling**:
- 200: show authenticated admin UI.
- 401: show guest/sign-in state.
- Network/5xx: show backend unavailable state.

### Logout

**Request**:

```http
POST /api/auth/logout
```

**Expected Result**:
- 204 clears session.

**UI Handling**:
- Clear local session state and return to homepage or guest state.

## Guild and Bot Install

### Bot Install URL

**Request**:

```http
GET /api/auth/discord/install-url?guildId={guildId}
```

**Response 200**:

```json
{
  "url": "https://discord.com/api/oauth2/authorize?client_id=..."
}
```

**UI Handling**:
- Use for empty/no-bot-installed guidance.
- Opening the URL is a browser navigation or new tab.

## Sticker Data

### List Default Stickers

**Request**:

```http
GET /api/stickers/default
```

**Response 200**:

```json
[
  {
    "emojiName": "😊",
    "shortcodeName": "smile",
    "description": "Smiling face"
  }
]
```

**UI Handling**:
- Display separately from custom mappings.
- 401 prompts sign-in.

### List Custom Mappings for Guild

**Request**:

```http
GET /api/mappings/{guildId}
```

**Response 200**:

```json
[
  {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "guildId": "123456789012345678",
    "emojiName": "😊",
    "minioBucketName": "bigmoji-123456789012345678",
    "minioObjectKey": "f47ac10b-58cc-4372-a567-0e02b2c3d479.png",
    "createdAt": "2026-06-24T16:00:00Z",
    "updatedAt": "2026-06-24T16:00:00Z"
  }
]
```

**UI Handling**:
- 200: render mapping list.
- 401: session expired/sign-in prompt.
- 403: access denied for selected guild.
- 404: render empty mapping state, not fatal error.

### Upload Sticker Mapping

**Request**:

```http
POST /api/mappings
Content-Type: multipart/form-data
```

Fields:

| Field | Required | Description |
|-------|----------|-------------|
| `emojiName` | Yes | Unicode emoji or shortcode |
| `guildId` | Yes | Selected manageable guild ID |
| `file` | Yes | Sticker image file |

**Response 201**:

```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "guildId": "123456789012345678",
  "emojiName": "😊",
  "minioBucketName": "bigmoji-123456789012345678",
  "minioObjectKey": "f47ac10b-58cc-4372-a567-0e02b2c3d479.png",
  "createdAt": "2026-06-24T16:00:00Z",
  "updatedAt": "2026-06-24T16:00:00Z"
}
```

**UI Handling**:
- 201: show success, reset form, refresh list.
- 400: show validation errors near form controls.
- 401: prompt sign-in.
- 403: show access denied.
- 5xx/network: show retryable backend unavailable error.

### Delete Mapping

**Request**:

```http
DELETE /api/mappings/{id}
```

**Expected Result**:
- 204 means deleted.

**UI Handling**:
- Must require explicit confirmation before calling.
- On 204, remove from list or refresh mappings.
- 404 after confirmation can be treated as already deleted and then refresh.

## Normalized UI Error Mapping

| Backend Result | UI Kind | User Action |
|----------------|---------|-------------|
| 400 | `validation` | Correct fields/file and retry |
| 401 | `unauthenticated` | Sign in again |
| 403 | `forbidden` | Select another server or verify Discord permissions |
| 404 | `notFound` | Show empty or already-deleted state |
| 5xx | `backendUnavailable` | Retry later |
| Network failure | `backendUnavailable` | Check backend/container connectivity |
