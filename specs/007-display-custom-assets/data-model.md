# Data Model: Display Custom Assets

This feature does not add persistent storage. Existing `StickerMapping` records remain the source of truth for custom mappings. New models describe backend response shapes and UI view models used to display custom emoji and sticker previews safely.

## Entities

### StickerMapping

Represents one persisted custom emoji-to-sticker mapping owned by a Discord guild.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | UUID/string | Required | Mapping identifier used for delete actions |
| `guildId` | string | Required Discord guild snowflake | Guild that owns the mapping |
| `emojiName` | string | Required, non-blank | Stored trigger value submitted by admin |
| `minioBucketName` | string | Internal | Storage bucket for the uploaded sticker object |
| `minioObjectKey` | string | Internal | Storage object key for the uploaded sticker |
| `isDefault` | boolean | Required | Whether the mapping is a default mapping |
| `createdAt` | timestamp | Required | Mapping creation time |
| `updatedAt` | timestamp | Required | Last mapping update time |

**Validation Rules**:
- `emojiName` must remain readable even when no custom emoji image is available.
- Internal storage fields may exist server-side but must not be used as primary user-facing mapping content.
- Delete behavior remains authorized by the owning `guildId`.

### MappingPreviewResponse

Represents one custom mapping returned to the UI for admin display.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | string | Required UUID | Mapping identifier |
| `guildId` | string | Required | Guild that owns the mapping |
| `emojiName` | string | Required | Original readable trigger value |
| `isDefault` | boolean | Required | Mapping category |
| `createdAt` | string | Required ISO timestamp | Creation time |
| `updatedAt` | string | Required ISO timestamp | Update time |
| `stickerPreviewUrl` | string | Optional | Browser-safe preview URL for the uploaded sticker |
| `stickerPreviewState` | `"available" \| "unavailable"` | Required | Whether the sticker preview can be shown |
| `emojiPreview` | ServerEmojiPreview | Optional | Server custom emoji display metadata when matched |

**Validation Rules**:
- `stickerPreviewUrl` is present only when `stickerPreviewState` is `available`.
- When `stickerPreviewState` is `unavailable`, UI must show a clear fallback and keep delete action available.
- Storage bucket and object key are not required by the UI and should not be the primary display contract.

### ServerEmojiPreview

Represents display metadata for a server custom emoji in the selected guild.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | string | Required when custom emoji is matched | Discord emoji snowflake |
| `name` | string | Required | Emoji display name |
| `shortcode` | string | Required | Human-readable shortcode such as `:party_blob:` |
| `imageUrl` | string | Optional | Browser-safe custom emoji image URL |
| `animated` | boolean | Required | Whether Discord marks the emoji as animated |
| `available` | boolean | Required | Whether visual preview can be displayed |
| `unicodeEmoji` | string | Optional | Unicode glyph resolved from a standard Discord shortcode |

**Validation Rules**:
- Metadata is scoped to the selected guild.
- A name-only trigger may use a unique match from the bot's global JDA emoji cache when the selected guild is unavailable; duplicate names must remain unresolved.
- A full Discord custom emoji mention supplies its own ID and animation flag, allowing a CDN preview without JDA guild access.
- For a standard Discord shortcode, `unicodeEmoji` provides the visual glyph while `shortcode` remains the readable label.
- If neither `imageUrl` nor `unicodeEmoji` is available, UI uses `shortcode` or `name` as fallback text.
- Duplicate emoji names should remain distinguishable by visual preview where available and by stable grouping context.

### MappingGroupViewModel

Represents UI grouping for mappings with the same trigger.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `groupKey` | string | Required | Stable grouping key derived from trigger identity |
| `displayName` | string | Required | Readable trigger name shown in heading |
| `emojiPreview` | ServerEmojiPreview | Optional | Visual custom emoji metadata shared by the group |
| `fallbackTrigger` | string | Required | Unicode emoji, shortcode, or text fallback |
| `stickerCount` | number | Required, >= 1 | Number of mappings in the group |
| `mappings` | MappingPreviewResponse[] | Required | Mappings in the group |

**Validation Rules**:
- Groups must sort predictably by readable display name.
- Group headers must remain stable while image previews load.
- The group count must match the number of mappings shown.

### AssetPreviewState

Represents the UI display state for an emoji or sticker preview.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `kind` | `"loading" \| "available" \| "unavailable"` | Required | Current display state |
| `label` | string | Required | Accessible label or fallback text |
| `url` | string | Optional | Image URL when available |
| `isAnimated` | boolean | Optional | Whether the preview may animate |

**State Transitions**:
- `loading` -> `available` when the image loads successfully.
- `loading` -> `unavailable` when the image fails to load.
- `available` -> `unavailable` when the browser reports an image error after initial render.

**Validation Rules**:
- `unavailable` state must not render a broken image icon.
- Preview containers must reserve stable space before images load.
- Accessible labels must describe the emoji or sticker purpose.

## Relationships

- A selected guild has zero or more `StickerMapping` records.
- Each `StickerMapping` maps to one `MappingPreviewResponse`.
- A `MappingPreviewResponse` may have one `ServerEmojiPreview`.
- Multiple `MappingPreviewResponse` items may be grouped into one `MappingGroupViewModel`.
- Each group and mapping row has emoji and sticker `AssetPreviewState` values for UI rendering.
