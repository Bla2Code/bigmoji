# Phase 0 Research: Additional Default Emoji Stickers

## Decision 1: Extend the existing default catalog as the source of truth

**Decision**: Add `cry`, `open_mouth`, `pensive`, and `face_with_bags_under_eyes` to the existing default sticker catalog used by backend fallback replacement and default sticker listing.

**Rationale**:
- The current default flow already centralizes bundled defaults in one catalog.
- The admin UI already loads defaults from the backend, so extending the backend catalog prevents UI/backend drift.
- Additive catalog entries preserve the current custom-over-default precedence rules.

**Alternatives considered**:
- Maintain a separate UI-only list: rejected because it can diverge from actual replacement behavior.
- Create persisted database rows for bundled defaults: rejected because existing bundled defaults are classpath assets and do not require persistence.

## Decision 2: Normalize new emojis to the requested sticker keys

**Decision**: Extend default emoji normalization so 😢, 😮, 😔, and 🫩 normalize to `cry`, `open_mouth`, `pensive`, and `face_with_bags_under_eyes`; shortcode inputs such as `:cry:` already normalize to the same key through existing shortcode parsing.

**Rationale**:
- Matches the user requirement to use sticker names for auto replacement.
- Keeps Unicode emoji and text shortcode inputs converging on one canonical lookup key.
- Reuses existing variation-selector and skin-tone stripping behavior for compatible inputs.

**Alternatives considered**:
- Match raw emoji strings in the replacement service: rejected because it bypasses the established normalizer.
- Add multiple aliases per default sticker immediately: rejected because the feature scope names four explicit emoji identities and should avoid unexpected replacements.

## Decision 3: Add preview metadata for default stickers through the backend API

**Decision**: Extend default sticker metadata with preview URL/state and add a backend endpoint that serves bundled default sticker bytes by shortcode name.

**Rationale**:
- The UI requirement asks for visible sticker previews, not only emoji glyphs.
- Existing default sticker API currently returns only emoji/name/description, so it cannot render real sticker previews.
- Serving previews from the backend keeps classpath resources authoritative and avoids duplicating PNGs in the UI app.

**Alternatives considered**:
- Copy bundled PNGs into `ui/public`: rejected because it duplicates assets and creates two update points.
- Embed image bytes in JSON: rejected because it bloats metadata responses and harms UI performance.
- Show only Unicode emoji as the preview: rejected because the specification requires sticker preview visibility.

## Decision 4: Standardize bundled default source assets to Discord-oriented square PNGs

**Decision**: Use square PNG source assets for every bundled default and target 320x320 pixels for static sticker source files where feasible. Because the bot sends files as Discord attachments/webhook uploads rather than registering Discord sticker objects, acceptance testing must record the actual observed Discord display size.

**Rationale**:
- Discord's static sticker guidance uses a 320x320 source canvas, which is a practical target for bundled sticker assets.
- Normalizing source files avoids adding runtime image-resizing logic or new image-processing dependencies.
- The current delivery path controls uploaded file bytes but not every Discord client display detail.

**Alternatives considered**:
- Keep all resources at 512x512 permanently: rejected because the feature explicitly asks to standardize toward Discord sticker size if possible.
- Resize images at send time: rejected because it adds processing overhead and a new failure path for a static asset problem.
- Use CSS-only sizing in the UI: rejected as insufficient because Discord message output also needs consistent source assets.

## Decision 5: Test through focused backend and UI regression coverage

**Decision**: Update existing tests that currently assert five defaults, add normalization and fallback loading coverage for the four new keys, and extend UI tests/fixtures to verify all new default entries and preview fallback behavior.

**Rationale**:
- The risk is catalog drift and accidental behavior expansion; focused tests catch both.
- Backend tests can run without Discord or external storage.
- UI tests can verify admin-visible requirements without full end-to-end setup.

**Alternatives considered**:
- Manual Discord-only validation: rejected because it does not protect future regressions.
- Broad end-to-end-only validation: rejected because it is slower and less precise for catalog and normalizer behavior.
