# Contract: Mapping Preview UI

**Surface**: Authenticated admin route `/app/guilds/:guildId`  
**Component area**: Custom mappings list  
**Primary files**: `ui/src/features/mappings/MappingList.tsx`, `ui/src/features/mappings/mappings.css`, `ui/src/api/types.ts`, `ui/src/api/mappings.ts`

## Rendering Rules

### Mapping Group Header

Each group header must show:
- a reserved emoji preview cell;
- the server custom emoji image when `emojiPreview.available` and `emojiPreview.imageUrl` are present;
- readable fallback text from `emojiPreview.shortcode`, `emojiPreview.name`, or `emojiName` when no image is available;
- the readable group name;
- the custom sticker count.

The group header must not use mapping IDs as the primary content.

### Sticker Row

Each mapping row must show:
- a reserved sticker preview cell;
- the uploaded sticker image when `stickerPreviewState` is `available` and `stickerPreviewUrl` is present;
- an unavailable preview placeholder when the URL is absent or image loading fails;
- creation date;
- delete action after confirmation.

The row may include a shortened mapping ID as secondary diagnostic text only if the visual sticker preview and readable trigger remain primary.

## Accessibility Rules

- Emoji preview images must have accessible names such as `Custom emoji :party_blob:`.
- Sticker preview images must have accessible names such as `Uploaded sticker for :party_blob:`.
- Decorative duplicate visuals may use `aria-hidden`, but each mapping must still have readable text naming the trigger and preview state.
- Unavailable previews must expose readable text such as `Sticker preview unavailable`.
- Delete buttons must remain keyboard reachable and associated with the correct mapping.

## Loading and Error States

| State | Required UI Behavior |
|-------|----------------------|
| Mapping list loading | Existing page loading state may be used before mapping cards render |
| Emoji image loading | Reserve fixed preview cell dimensions; show fallback text or neutral loading style |
| Emoji image load error | Hide broken image and show readable fallback trigger |
| Sticker image loading | Reserve fixed preview cell dimensions; do not shift row controls |
| Sticker image load error | Hide broken image and show unavailable placeholder |
| Preview URL unavailable from API | Show unavailable placeholder immediately |
| Delete while preview unavailable | Delete confirmation and delete request remain available |

## Responsive Layout Rules

- At widths down to 360px, mapping group header content, sticker preview, labels, counts, dates, and delete buttons must not overlap.
- Preview cells must have stable dimensions so image load success/failure does not resize the row unpredictably.
- Long emoji names and shortcode labels must wrap or truncate without pushing delete controls off-screen.
- Mobile rows may stack action controls below mapping details.

## Test Expectations

Component tests must cover:
- custom emoji image and name render when `emojiPreview` is available;
- fallback trigger render when `emojiPreview` is absent or unavailable;
- sticker preview image render when `stickerPreviewUrl` is available;
- unavailable placeholder render when sticker preview is unavailable or image load fails;
- multiple mappings grouped under one trigger with correct count;
- delete confirmation still works when previews are unavailable;
- storage bucket/object key are not shown as primary display content.

E2E or visual checks should cover:
- mapping list at desktop width;
- mapping list at 360px mobile width;
- at least one custom emoji mapping with uploaded sticker preview;
- at least one unavailable preview fallback.
