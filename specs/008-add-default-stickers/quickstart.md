# Quickstart: Validate Additional Default Emoji Stickers

## Goal

Verify that the four new bundled default stickers work in replacement, appear in the admin UI with previews, and keep old default behavior unchanged.

## Prerequisites

- Branch `008-add-default-stickers` is checked out.
- The four new PNG assets exist under `src/main/resources/default-stickers/`:
  - `cry.png`
  - `open_mouth.png`
  - `pensive.png`
  - `face_with_bags_under_eyes.png`
- Backend and UI dependencies are installed.
- A Discord test server is available for manual output-size acceptance testing.

## Automated Validation

### Backend

1. Run formatting and tests:

   ```bash
   ./gradlew spotlessCheck test
   ```

2. Confirm backend tests cover:
   - 9 default catalog entries;
   - `fallbackFor` loads all four new keys;
   - Unicode and shortcode normalization for `cry`, `open_mouth`, `pensive`, and `face_with_bags_under_eyes`;
   - old default keys still resolve;
   - default sticker API returns preview metadata;
   - preview endpoint returns PNG bytes for known defaults and rejects unknown names.

### UI

1. Run UI checks:

   ```bash
   cd ui
   npm run typecheck
   npm run lint
   npm run test
   ```

2. Confirm UI tests cover:
   - `:cry:`, `:open_mouth:`, `:pensive:`, and `:face_with_bags_under_eyes:` render in the default stickers section;
   - existing defaults remain visible;
   - preview images render when available;
   - preview fallback state appears when a preview is unavailable or image load fails.

## Manual Backend/API Validation

1. Start the backend.
2. Sign in through the existing admin flow.
3. Request:

   ```http
   GET /api/stickers/default
   ```

4. Confirm the response includes 9 entries and the four new `shortcodeName` values.
5. Request each preview URL from the response and confirm it returns PNG image bytes.

## Manual Discord Validation

For a test server without custom mappings for the target emoji names:

1. Send 😢 as a single-message emoji and confirm the `cry` default sticker is sent.
2. Send 😮 as a single-message emoji and confirm the `open_mouth` default sticker is sent.
3. Send 😔 as a single-message emoji and confirm the `pensive` default sticker is sent.
4. Send 🫩 as a single-message emoji and confirm the `face_with_bags_under_eyes` default sticker is sent.
5. Repeat with `:cry:`, `:open_mouth:`, `:pensive:`, and `:face_with_bags_under_eyes:` when Discord/client input supports those text forms.
6. Send a mixed-content message such as `hi 😢` and confirm no new replacement occurs.
7. Add a custom mapping for one new key and confirm the custom mapping remains preferred over the bundled default.

## Output Size Acceptance

### Bundled Asset Dimension Evidence

Initial bundled PNG baseline before normalization:

| Asset | Baseline size |
|-------|---------------|
| `smile.png` | 512x512 |
| `heart.png` | 512x512 |
| `party.png` | 512x512 |
| `thumbsup.png` | 512x512 |
| `fire.png` | 512x512 |
| `cry.png` | 512x512 |
| `open_mouth.png` | 512x512 |
| `pensive.png` | 512x512 |
| `face_with_bags_under_eyes.png` | 512x512 |

Implemented bundled PNG source dimensions:

| Asset | Implemented size | Target met |
|-------|------------------|------------|
| `smile.png` | 320x320 | Yes |
| `heart.png` | 320x320 | Yes |
| `party.png` | 320x320 | Yes |
| `thumbsup.png` | 320x320 | Yes |
| `fire.png` | 320x320 | Yes |
| `cry.png` | 320x320 | Yes |
| `open_mouth.png` | 320x320 | Yes |
| `pensive.png` | 320x320 | Yes |
| `face_with_bags_under_eyes.png` | 320x320 | Yes |

The implementation achieved exact 320x320 source sizing for all bundled defaults. Automated validation is covered by `DefaultStickerInitializerTest`.

### Discord Output Evidence

Manual Discord display-size acceptance was not executed in this local implementation run because it requires a configured Discord test server and bot session. The delivery surface still sends these PNG files as Discord attachments/webhook uploads rather than registered Discord sticker objects, so client display dimensions remain controlled by Discord. The source assets are now consistent across the bundled set.

1. Inspect all bundled default PNGs and confirm they are square.
2. Confirm the implementation either:
   - uses 320x320 source PNGs for bundled defaults; or
   - documents why exact 320x320 source sizing could not be enforced in the current delivery path.
3. Send representative old and new bundled defaults in Discord.
4. Record whether the observed Discord display is consistent across the bundled set and note any client-side limitation.

## Success Confirmation

The feature is ready when:

- all automated backend and UI checks pass;
- all four new default mappings resolve correctly;
- the admin UI shows all four new defaults with previews or readable fallback states;
- existing defaults and unsupported-message behavior are unchanged;
- output-size acceptance evidence is recorded.

## Implementation Validation Results

Validated on 2026-06-29:

- `./gradlew spotlessCheck test` passed.
- `cd ui && npm run typecheck && npm run lint && npm run test` passed.
- Focused backend validation passed with `./gradlew test --tests '*DefaultStickersControllerContractTest' --tests '*DefaultStickerInitializerTest'`.
- Focused UI validation passed with `npm exec vitest -- run DefaultStickerList mappings`. The equivalent `npm run test -- DefaultStickerList mappings` selection passed its selected tests but failed global coverage thresholds because it intentionally ran only a subset; full UI coverage passed in the complete UI gate above.
