# Data Model: Bigmoji Web UI

The UI does not own persistent storage. All persistent state remains in the backend. These models describe client-side view models and backend payloads consumed by the UI.

## Entities

### CurrentSession

Represents the authenticated Discord session returned by the backend.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `status` | `"loading" \| "guest" \| "authenticated" \| "expired" \| "error"` | Required | UI-auth state derived from `/api/auth/me` |
| `userId` | string | Required when authenticated | Discord user snowflake |
| `username` | string | Required when authenticated | Discord username |
| `globalName` | string | Optional | Discord display name |
| `manageableGuilds` | ManageableGuild[] | Required when authenticated | Guilds available for configuration |
| `expiresAt` | string | ISO timestamp | Backend session expiration |

**Validation Rules**:
- `manageableGuilds` must be treated as authoritative for UI access.
- Missing or expired session maps to a sign-in prompt, not a fatal app error.

**State Transitions**:
- `loading` -> `guest` when `/api/auth/me` returns 401.
- `loading` -> `authenticated` when `/api/auth/me` returns 200.
- `authenticated` -> `expired` when a protected request returns 401.
- Any state -> `error` when the backend is unavailable.

### ManageableGuild

Represents a Discord server the signed-in user can manage.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | string | Required | Discord guild snowflake |
| `name` | string | Required | Server display name |

**Relationships**:
- A `CurrentSession` has zero or more `ManageableGuild` records.
- A selected `ManageableGuild` scopes mapping list, default sticker display, upload, and delete actions.

### StickerMapping

Represents one persisted custom mapping returned by the backend.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | string | Required UUID | Mapping identifier used for delete |
| `guildId` | string | Required | Guild that owns the mapping |
| `emojiName` | string | Required | Unicode emoji or shortcode trigger |
| `minioBucketName` | string | Optional for UI display | Internal storage bucket; UI should not expose unless needed for diagnostics |
| `minioObjectKey` | string | Optional for UI display | Internal storage key; UI should not expose unless needed for diagnostics |
| `createdAt` | string | ISO timestamp | Creation time |
| `updatedAt` | string | ISO timestamp | Last update time |

**Validation Rules**:
- UI must never let a mapping be deleted without explicit confirmation.
- UI must not display storage URLs or raw object storage credentials.

### DefaultSticker

Represents a globally available default sticker entry.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `emojiName` | string | Required | Emoji trigger |
| `shortcodeName` | string | Required | Friendly shortcode |
| `description` | string | Required | Human-readable description |

**Relationships**:
- Defaults are displayed separately from `StickerMapping` so admins understand which stickers are custom and which are global fallback behavior.

### StickerUploadDraft

Represents an in-progress upload form before backend submission.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `guildId` | string | Required | Selected manageable guild |
| `emojiName` | string | Required | Emoji trigger selected by admin |
| `file` | File | Required | Local image selected by admin |
| `previewUrl` | string | Optional | Browser object URL for local preview |
| `status` | `"idle" \| "validating" \| "uploading" \| "success" \| "error"` | Required | Form state |
| `errorMessage` | string | Optional | User-facing validation or upload failure |

**Validation Rules**:
- File is required.
- Emoji trigger is required.
- Selected guild must exist in `CurrentSession.manageableGuilds`.
- Client-side validation may catch obvious file type/size issues, but backend validation remains authoritative.

**State Transitions**:
- `idle` -> `validating` when the admin selects/submits a file.
- `validating` -> `uploading` when required form fields pass.
- `uploading` -> `success` when backend returns 201.
- `uploading` -> `error` for 400/401/403/5xx/network failure.
- `success` -> `idle` after mapping list refresh and form reset.

### HomepageDemoState

Represents the presentational walkthrough on the homepage.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `activeStep` | `"upload" \| "emoji" \| "replace"` | Required | Current demo stage |
| `sampleEmoji` | string | Required | Example emoji trigger |
| `sampleStickerName` | string | Required | Demo sticker label |
| `isReducedMotion` | boolean | Derived | Whether animations should be minimized |

**Validation Rules**:
- Demo data is illustrative only and must not be submitted to backend.
- Demo animation must respect reduced-motion preferences.

### ApiError

Normalized UI representation of backend or network failures.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `kind` | `"validation" \| "unauthenticated" \| "forbidden" \| "notFound" \| "backendUnavailable" \| "unknown"` | Required | Drives UI state and messaging |
| `message` | string | Required | Clear user-facing message |
| `status` | number | Optional | HTTP status when available |
| `fieldErrors` | Record<string, string> | Optional | Field-level form validation messages |

**Validation Rules**:
- Raw stack traces, storage URLs, and sensitive identifiers must not be displayed.
- 401 maps to sign-in/expired-session UI.
- 403 maps to access-denied UI for the selected guild.

### DeploymentConfig

Represents container runtime configuration.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `apiBasePath` | string | Defaults to `/api` | Browser-facing API path |
| `backendBaseUrl` | string | Required for Docker proxy | Internal or external backend URL used by nginx |
| `publicUiUrl` | string | Required in backend config | Public UI URL used by OAuth redirects |

**Validation Rules**:
- UI browser code should use `apiBasePath`, not direct backend URLs.
- `backendBaseUrl` is only used by nginx/container configuration.
