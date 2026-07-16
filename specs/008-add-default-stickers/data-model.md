# Data Model: Additional Default Emoji Stickers

## Entity: DefaultStickerCatalogEntry

**Description**: One bundled default sticker available for fallback replacement and admin UI display.

**Fields**:
- `emojiName` (string): Unicode emoji shown as the human-readable emoji identity.
- `shortcodeName` (string): Canonical default sticker key used for lookup and shortcode display, without surrounding colons.
- `description` (string): Human-readable label shown in API/UI metadata.
- `fileName` (string): Bundled PNG file name in the default sticker asset set.
- `previewUrl` (string, optional): Browser-loadable URL for admin UI sticker preview.
- `previewState` (enum): `available` or `unavailable`.

**Validation Rules**:
- `shortcodeName` must be unique across the default catalog.
- `emojiName` must be unique for the primary bundled default emoji identity.
- `fileName` must be unique and end in `.png`.
- `previewState=available` requires a preview URL and a loadable resource.
- `previewState=unavailable` must not expose classpath, filesystem, or internal exception details.

## Entity: EmojiNameMapping

**Description**: Normalized mapping from incoming emoji input to the default sticker key used by replacement.

**Fields**:
- `rawInput` (string): Incoming Unicode emoji, shortcode, custom emoji token, or plain text name.
- `normalizedCode` (string): Canonical lookup key after normalization.
- `isBundledDefault` (boolean): Whether the normalized code belongs to the bundled default catalog.

**Validation Rules**:
- Unicode 😢 normalizes to `cry`.
- Unicode 😮 normalizes to `open_mouth`.
- Unicode 😔 normalizes to `pensive`.
- Unicode 🫩 normalizes to `face_with_bags_under_eyes`.
- Shortcodes `:cry:`, `:open_mouth:`, `:pensive:`, and `:face_with_bags_under_eyes:` normalize to the same keys through existing shortcode parsing.
- Existing normalized outputs for `smile`, `heart`, `party`, `thumbsup`, and `fire` remain unchanged.

## Entity: DefaultStickerPreview

**Description**: Admin UI representation of a bundled default sticker preview.

**Fields**:
- `shortcodeName` (string): Links the preview to a catalog entry.
- `previewUrl` (string, optional): URL used by the browser to request the bundled PNG.
- `previewState` (enum): `available`, `unavailable`, or `failedInBrowser`.
- `accessibleName` (string): Text alternative for the preview image or fallback state.

**Validation Rules**:
- Available previews must have an accessible image name such as `Default sticker :cry:`.
- Browser image load failures transition only the UI presentation to `failedInBrowser`; they do not mutate backend catalog data.
- Fallback text must keep the shortcode readable.

## Entity: StickerOutputPresentation

**Description**: Expected visual sizing and shape for bundled default stickers in Discord output and admin UI previews.

**Fields**:
- `sourceWidthPx` (number): Source image width.
- `sourceHeightPx` (number): Source image height.
- `aspectRatio` (string): Expected to be `1:1`.
- `targetSourceCanvasPx` (string): Target canvas, `320x320` when feasible.
- `observedDiscordDisplay` (string, optional): Manual acceptance note for how Discord displayed the uploaded file.

**Validation Rules**:
- Source image dimensions for bundled defaults should be square.
- Source image target is 320x320 when feasible for static PNG defaults.
- UI preview cells must use stable square dimensions and `object-fit: contain`.
- Discord output acceptance must record whether the observed display is consistent across all bundled defaults.

## Relationships

- `DefaultStickerCatalogEntry` 1→1 `EmojiNameMapping` for bundled default normalized keys.
- `DefaultStickerCatalogEntry` 1→1 `DefaultStickerPreview` for admin UI display.
- `DefaultStickerCatalogEntry` 1→1 `StickerOutputPresentation` for asset and display validation.
- Server-specific custom mappings remain separate and continue to override bundled default fallback selection.

## State Transitions

### Default Replacement

1. Incoming message qualifies under existing single-emoji rules.
2. Emoji input normalizes to a canonical code.
3. Custom mapping lookup runs first under existing precedence.
4. If no custom mapping exists and the code is a bundled default key, the matching default asset is loaded.
5. Missing or unreadable asset produces a safe non-send outcome and maintainable diagnostic log.

### Admin UI Preview

1. UI requests default sticker metadata.
2. Backend returns 9 catalog entries with preview state.
3. UI renders each preview URL when available.
4. Browser load failure hides the broken image and shows readable fallback text.
