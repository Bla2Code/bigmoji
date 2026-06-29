# Contract: Default Stickers Management UI

**Surface**: Authenticated admin route `/app/guilds/:guildId`  
**Component area**: Default stickers section  
**Primary files**: `ui/src/features/mappings/DefaultStickerList.tsx`, `ui/src/features/mappings/mappings.css`, `ui/src/api/types.ts`, `ui/src/test/fixtures.ts`

## Data Contract

The UI consumes `GET /api/stickers/default` through `listDefaultStickers()` and treats the backend response as the authoritative catalog.

Expected `DefaultSticker` fields:

| Field | Required | UI Use |
|-------|----------|--------|
| `emojiName` | Yes | Human-readable emoji identity and fallback visual |
| `shortcodeName` | Yes | Rendered as `:{shortcodeName}:` |
| `description` | Yes | Secondary label |
| `stickerPreviewUrl` | No | Source for sticker image preview |
| `stickerPreviewState` | Yes | Controls image vs fallback rendering |

## Rendering Rules

The default sticker section must:

- show all backend-provided default stickers in one default sticker area;
- include visible entries for `:cry:`, `:open_mouth:`, `:pensive:`, and `:face_with_bags_under_eyes:`;
- keep existing `:smile:`, `:heart:`, `:party:`, `:thumbsup:`, and `:fire:` entries visible;
- render the actual sticker preview image when `stickerPreviewState` is `available` and `stickerPreviewUrl` is present;
- render readable fallback content when preview data is unavailable or the image fails to load;
- avoid showing broken image icons;
- avoid exposing backend resource paths or internal error text.

## Accessibility Rules

- Default sticker preview images must have accessible names such as `Default sticker :cry:`.
- Fallback preview states must expose readable text such as `Preview unavailable`.
- The shortcode-style name must remain visible as text for every default entry.
- Decorative duplicate emoji glyphs may be `aria-hidden` only if the shortcode and preview state remain readable.

## Loading and Error States

| State | Required UI Behavior |
|-------|----------------------|
| Default list loading | Existing page-level loading state may be used |
| Default list empty | Existing empty state may be used |
| Preview URL available | Render image in a stable square preview cell |
| Preview URL missing or state unavailable | Render readable fallback state immediately |
| Preview image load error | Hide failed image and render fallback state |
| Long shortcode name | Wrap or truncate without overlapping adjacent text or resizing preview cells |

## Responsive Layout Rules

- Default sticker cards must remain readable down to 360px viewport width.
- Preview cells must have stable square dimensions so image load success/failure does not shift the card layout.
- `:face_with_bags_under_eyes:` must wrap or fit without clipping or overlapping the description.
- Cards must keep the existing management UI visual style and avoid nested card structures.

## Test Expectations

UI tests must verify:

- the four new default shortcode labels render;
- existing default labels still render;
- available default previews render with accessible image names;
- unavailable or failed previews render fallback text instead of broken images;
- `:face_with_bags_under_eyes:` remains readable in the default sticker card;
- `listDefaultStickers()` preserves preview fields from backend responses.

Optional browser/e2e checks should verify:

- default sticker section at desktop width;
- default sticker section at 360px mobile width;
- no text overlap or layout shift around long default names.
