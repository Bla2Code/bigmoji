# Feature Specification: Additional Default Emoji Stickers

**Feature Branch**: `008-add-default-stickers`

**Created**: 2026-06-29

**Status**: Draft

**Input**: User description: "используй workflow создай спецификацию на добавление новых дефолтных стикеров. новые стикеры тут face_with_bags_under_eyes.png, cry.png, open_mouth.png, pensive.png. используй имена стикеров на соответствующие эмоджи для автозамены. Делай по аналогии со старым функционалом - добавь только новое. Если возможно то нужно стандартизировать размер вывода стикеров - можно под стандартный размер Discord"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Replace newly supported emojis with default stickers (Priority: P1)

As a Discord user, I want four additional common emotion emojis to trigger bundled default stickers when a server has no custom mapping for them, so that the bot covers more everyday reactions without admin setup.

**Why this priority**: The feature's core value is expanding the default sticker catalog while preserving the existing replacement behavior.

**Independent Test**: Send one supported new emoji at a time and verify each one resolves to its matching default sticker name when no custom sticker mapping overrides it.

**Acceptance Scenarios**:

1. **Given** a message contains only 😢 or the `:cry:` shortcode, **When** default replacement runs and no custom mapping is available, **Then** the system sends the `cry` default sticker.
2. **Given** a message contains only 😮 or the `:open_mouth:` shortcode, **When** default replacement runs and no custom mapping is available, **Then** the system sends the `open_mouth` default sticker.
3. **Given** a message contains only 😔 or the `:pensive:` shortcode, **When** default replacement runs and no custom mapping is available, **Then** the system sends the `pensive` default sticker.
4. **Given** a message contains only 🫩 or the `:face_with_bags_under_eyes:` shortcode, **When** default replacement runs and no custom mapping is available, **Then** the system sends the `face_with_bags_under_eyes` default sticker.

---

### User Story 2 - Keep the existing default sticker set unchanged (Priority: P2)

As a maintainer, I want the new stickers added without changing the existing default mappings, so that current users see no regressions for already supported emojis.

**Why this priority**: The user explicitly requested an additive change by analogy with old functionality, so existing emoji-to-sticker behavior must remain stable.

**Independent Test**: Verify the old default mappings and unsupported inputs before and after the new catalog entries are added.

**Acceptance Scenarios**:

1. **Given** an existing supported default emoji such as 😊, ❤️, 🎉, 👍, or 🔥, **When** replacement runs, **Then** it continues to resolve to the same default sticker as before.
2. **Given** an unsupported emoji or mixed-content message, **When** replacement runs, **Then** the system keeps the same behavior it had before this feature.
3. **Given** a server-specific custom mapping exists for one of the newly supported emoji names, **When** replacement runs, **Then** the custom mapping remains preferred over the bundled default sticker according to existing precedence rules.

---

### User Story 3 - Show new defaults in the management UI (Priority: P3)

As a server admin, I want the management UI to show the newly bundled default emoji stickers alongside the existing defaults, so that I can see which emoji replacements are available out of the box.

**Why this priority**: The replacement behavior must be visible to admins; otherwise the backend catalog and UI catalog can drift and create confusion.

**Independent Test**: Open the default stickers area in the management UI and verify the four new emoji names and previews appear with the existing default set.

**Acceptance Scenarios**:

1. **Given** an admin opens the default stickers list, **When** the catalog loads, **Then** the UI shows `:cry:`, `:open_mouth:`, `:pensive:`, and `:face_with_bags_under_eyes:` entries.
2. **Given** the UI displays each new default sticker entry, **When** the admin scans the list, **Then** each entry has a visible sticker preview and the matching emoji name used for auto replacement.
3. **Given** the existing default stickers list already shows 😊, ❤️, 🎉, 👍, and 🔥 defaults, **When** the new entries are added, **Then** the existing UI entries remain visible and unchanged.

---

### User Story 4 - Present default stickers at a consistent Discord-oriented size (Priority: P4)

As a Discord user, I want bundled default stickers to appear at a consistent large sticker size, so that new and existing default stickers feel like one polished set.

**Why this priority**: Visual consistency improves the experience, but the primary deliverable is still the new default sticker mappings.

**Independent Test**: Compare all bundled default stickers in a supported Discord channel and verify they share a consistent square presentation with no visibly oversized, undersized, stretched, or cropped output.

**Acceptance Scenarios**:

1. **Given** any bundled default sticker is sent, **When** it appears in Discord, **Then** its output uses the same intended square presentation as the rest of the bundled default set.
2. **Given** the delivery surface supports Discord sticker source sizing, **When** bundled defaults are prepared for output, **Then** the target source canvas is 320x320 pixels.
3. **Given** the delivery surface cannot enforce Discord sticker source sizing, **When** acceptance testing is performed, **Then** the limitation and the actual observed output size are documented while preserving consistent square presentation across bundled defaults.

### Edge Cases

- What happens when an emoji is sent with a variation selector or skin-tone-like modifier that should not change the intended default mapping?
- How does the system handle the newly supported shortcode names if Discord clients display or normalize them differently?
- What happens when one of the new default sticker assets is missing, unreadable, or invalid at runtime?
- How does the system avoid changing behavior for messages that contain the new emoji plus additional text or additional emojis?
- How are new default stickers displayed if their source image dimensions differ from the final Discord-oriented output target?
- How does the UI handle a new default sticker preview if the asset cannot be loaded?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST add `cry` as a bundled default sticker key for the crying face emoji 😢 and the `:cry:` shortcode.
- **FR-002**: System MUST add `open_mouth` as a bundled default sticker key for the face with open mouth emoji 😮 and the `:open_mouth:` shortcode.
- **FR-003**: System MUST add `pensive` as a bundled default sticker key for the pensive face emoji 😔 and the `:pensive:` shortcode.
- **FR-004**: System MUST add `face_with_bags_under_eyes` as a bundled default sticker key for the face with bags under eyes emoji 🫩 and the `:face_with_bags_under_eyes:` shortcode.
- **FR-005**: System MUST make each new default sticker available anywhere the existing bundled defaults are listed, selected, previewed, or used as fallback replacement options.
- **FR-006**: System MUST preserve all existing default sticker keys and mappings for `smile`, `heart`, `party`, `thumbsup`, and `fire`.
- **FR-007**: System MUST preserve existing replacement precedence: server-specific custom stickers remain preferred over bundled defaults when both exist for the same emoji name.
- **FR-008**: System MUST NOT introduce replacement for unsupported emojis or mixed-content messages beyond the four explicitly added emoji names.
- **FR-009**: System MUST handle missing or invalid new sticker assets with the same user-safe behavior and maintainable diagnostics used for existing default stickers.
- **FR-010**: Management UI MUST display the four new default sticker entries in the same default stickers area as the existing bundled defaults.
- **FR-011**: Management UI MUST show the matching emoji shortcode-style names `:cry:`, `:open_mouth:`, `:pensive:`, and `:face_with_bags_under_eyes:` for the new default entries.
- **FR-012**: Management UI MUST show a preview for each new default sticker with the same visual treatment, loading behavior, and fallback behavior used for existing default sticker previews.
- **FR-013**: System MUST present bundled default stickers with a consistent square output size across the old and new default set.
- **FR-014**: System SHOULD use Discord's standard static sticker source canvas of 320x320 pixels for default sticker output when the delivery path can control the source dimensions.
- **FR-015**: If exact Discord sticker source sizing cannot be enforced in the current delivery path, the system MUST document the limitation during acceptance testing and still verify consistent square presentation across all bundled defaults.

### Key Entities *(include if feature involves data)*

- **Default Sticker Catalog Entry**: Represents one bundled default sticker with an emoji character, shortcode-style name, display label, and sticker asset.
- **Emoji Name Mapping**: Represents the normalized name used to connect an incoming emoji or shortcode to a default sticker key.
- **Default Sticker UI Entry**: Represents how a bundled default sticker appears to admins, including displayed shortcode-style name and preview state.
- **Sticker Output Presentation**: Represents the expected square visual size and aspect-ratio behavior for bundled default stickers when they are displayed to users.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of the four newly requested emoji mappings resolve to their corresponding default sticker during acceptance testing.
- **SC-002**: 100% of the five previously supported default mappings continue to resolve to the same sticker names after this feature is added.
- **SC-003**: 100% of the four newly requested default sticker entries appear in the management UI with the expected shortcode-style name and a visible preview during acceptance testing.
- **SC-004**: 0 unsupported or mixed-content message cases tested during regression verification produce a new unintended sticker replacement.
- **SC-005**: All bundled default stickers are verified to display as a consistent square sticker set, with no new sticker visibly stretched, cropped, or size-mismatched compared with existing defaults.
- **SC-006**: Acceptance evidence records whether exact 320x320 Discord source sizing was achieved or, if not achievable in the current delivery surface, records the observed output limitation.

## Assumptions

- The four PNG assets already provided under the default sticker asset set are the intended visual assets for this feature.
- The names `cry`, `open_mouth`, `pensive`, and `face_with_bags_under_eyes` are the authoritative default sticker keys for the new mappings.
- Existing default replacement rules remain authoritative: only messages that already qualify for emoji replacement are affected.
- Existing custom mappings continue to take precedence over bundled defaults.
- The management UI should consume the same default sticker catalog exposed for the existing default sticker list, rather than maintaining a conflicting separate list.
- Discord-oriented sticker sizing means a square sticker presentation, with 320x320 pixels as the target source canvas where the system can control source dimensions.
