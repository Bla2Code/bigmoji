# Feature Specification: Display Custom Assets

**Feature Branch**: `007-display-custom-assets`

**Created**: 2026-06-28

**Status**: Draft

**Input**: User description: "Display server custom emojis and uploaded stickers in custom mapping views instead of text-only placeholders."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Recognize Server Custom Emoji Triggers (Priority: P1)

An authenticated Discord server admin opens the custom mapping list and can identify each server custom emoji trigger by its actual visual emoji image and readable name, rather than only seeing a shortcode-style text label.

**Why this priority**: The mapping list is hard to scan when custom emoji triggers are shown as plain text placeholders. Admins need to confirm that the correct server emoji is connected to the correct sticker.

**Independent Test**: Can be fully tested by opening a server that has at least one custom emoji mapping and verifying that the mapping group shows the emoji image and readable emoji name for that selected server.

**Acceptance Scenarios**:

1. **Given** an authenticated admin selects a server with a custom emoji mapping, **When** the custom mappings list loads, **Then** the mapped custom emoji appears as a visual emoji image with its readable name or shortcode nearby.
2. **Given** a server has multiple custom emoji mappings, **When** the admin scans the list, **Then** each mapping group can be distinguished by the visual emoji and name without relying on internal mapping identifiers.
3. **Given** a mapping uses a standard Unicode emoji or a text trigger instead of a server custom emoji, **When** the mapping appears, **Then** the trigger is still displayed accurately in a readable fallback form.

---

### User Story 2 - Preview Uploaded Stickers in Mappings (Priority: P1)

An authenticated Discord server admin can see the actual uploaded sticker image for every custom mapping, both immediately after upload and when returning to the mapping list later.

**Why this priority**: Admins cannot verify a mapping if the list only shows that a custom sticker exists. The uploaded sticker preview is the confirmation that the correct image was attached.

**Independent Test**: Can be fully tested by uploading a valid sticker for a custom emoji, confirming the new mapping appears with the sticker preview, refreshing the view, and confirming the same preview remains visible.

**Acceptance Scenarios**:

1. **Given** an admin uploads a valid sticker for a selected server and emoji trigger, **When** the upload succeeds, **Then** the mapping list shows the uploaded sticker preview in the relevant mapping group.
2. **Given** an admin opens a server that already has uploaded sticker mappings, **When** the mappings load, **Then** every available uploaded sticker appears as a preview image with enough surrounding context to identify its emoji trigger.
3. **Given** multiple stickers are mapped to the same emoji trigger, **When** the admin expands or scans that trigger group, **Then** the UI shows each sticker preview and the total sticker count for the group.

---

### User Story 3 - Handle Missing or Slow Assets Clearly (Priority: P2)

An authenticated Discord server admin can still manage mappings when an emoji image or sticker preview is unavailable, slow to load, deleted, or inaccessible.

**Why this priority**: Visual assets may fail independently from mapping data. The management view must remain trustworthy and usable instead of showing broken images or ambiguous blank areas.

**Independent Test**: Can be fully tested by viewing mappings where one custom emoji image or sticker preview is unavailable and verifying that the list shows a clear fallback while preserving management actions.

**Acceptance Scenarios**:

1. **Given** a custom emoji image cannot be loaded, **When** the mapping appears, **Then** the UI shows a readable emoji name or shortcode fallback and marks the visual preview as unavailable.
2. **Given** an uploaded sticker preview cannot be loaded, **When** the mapping appears, **Then** the UI shows a clear unavailable-state placeholder and still lets an authorized admin delete the mapping.
3. **Given** asset loading is slow, **When** the admin opens the mapping list, **Then** the UI shows stable loading states that do not shift, overlap, or hide mapping controls.

### Edge Cases

- **Deleted server emoji**: If a mapped server emoji no longer exists or is no longer accessible, the mapping remains visible with a fallback label and can still be deleted by an authorized admin.
- **Deleted or unavailable uploaded sticker**: If an uploaded sticker preview is missing, the mapping shows an unavailable sticker state instead of a broken image.
- **Animated custom emoji**: If a server custom emoji is animated, the admin can still identify it visually or through a readable fallback if animation preview is unavailable.
- **Duplicate emoji names**: If different server emoji share the same display name, the mapping list provides enough visual or contextual distinction for the selected server.
- **Large mapping lists**: If a server has many mappings, previews must not make the list difficult to scan or cause excessive layout jumps while loading.
- **Mobile layout**: Emoji previews, sticker previews, labels, counts, and delete controls must not overlap or require horizontal scrolling on common mobile widths.
- **Unauthorized server access**: Users must not see custom emoji or sticker previews for servers they are not authorized to manage.
- **Fresh upload state**: A newly uploaded sticker should appear in the list with the same preview treatment as mappings loaded from existing data.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST show a visual preview for a server custom emoji trigger whenever that preview is available for the selected server.
- **FR-002**: System MUST show a readable custom emoji name or shortcode near the visual preview so admins can identify the trigger even when the image is small.
- **FR-003**: System MUST preserve accurate readable display for standard Unicode emoji and text-based triggers that do not have a server custom emoji image.
- **FR-004**: System MUST show the actual uploaded sticker preview for each custom mapping whenever the sticker preview is available.
- **FR-005**: System MUST show newly uploaded stickers in the mapping list with the same preview behavior as existing mappings after a successful upload.
- **FR-006**: System MUST show existing uploaded sticker previews after an admin leaves and later reopens or refreshes the mapping view.
- **FR-007**: System MUST represent multiple uploaded stickers mapped to the same emoji trigger as a grouped set that includes visual previews and the sticker count.
- **FR-008**: System MUST provide clear loading, unavailable, and fallback states for custom emoji previews and uploaded sticker previews.
- **FR-009**: System MUST NOT show broken image icons, empty visual boxes, or misleading placeholder artwork when an asset cannot be displayed.
- **FR-010**: System MUST keep mapping controls, labels, counts, emoji previews, and sticker previews visually stable and non-overlapping across supported desktop and mobile widths.
- **FR-011**: System MUST make visual previews accessible by providing readable names or descriptions for admins using assistive technology.
- **FR-012**: System MUST scope custom emoji and uploaded sticker previews to the currently selected server and the authenticated admin's authorized server list.
- **FR-013**: System MUST keep authorized management actions available for mappings whose emoji or sticker preview is unavailable.
- **FR-014**: System MUST use human-recognizable emoji and sticker information as the primary mapping content; internal mapping identifiers MUST NOT be the primary way admins distinguish mappings.
- **FR-015**: System MUST keep this feature limited to the management display experience and MUST NOT change the rules for replacing emoji messages in Discord.

### Key Entities

- **Authenticated Admin**: A Discord user with an active session who is authorized to manage one or more Discord servers.
- **Selected Server**: The Discord server whose custom mappings, custom emoji, and uploaded stickers are currently being viewed.
- **Server Custom Emoji**: A custom emoji belonging to the selected server, represented by a visual asset and a readable name or shortcode.
- **Uploaded Sticker**: A sticker image uploaded by an admin and connected to an emoji trigger for the selected server.
- **Sticker Mapping**: The relationship between one emoji trigger and one or more uploaded stickers for a selected server.
- **Asset Preview State**: The visible state of an emoji or sticker preview, including loading, available, unavailable, and readable fallback states.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of custom mappings with available server custom emoji previews display the emoji image and readable emoji name in the mapping list.
- **SC-002**: 100% of custom mappings with available uploaded sticker previews display the sticker image after upload and after reopening the mapping list.
- **SC-003**: In a test server with at least 20 custom mappings, an admin can identify the intended emoji-to-sticker pair for a mapping within 10 seconds.
- **SC-004**: 100% of tested missing, deleted, or unavailable emoji and sticker asset cases show a clear fallback state instead of a broken image or empty box.
- **SC-005**: The mapping list remains usable without horizontal scrolling or overlapping content at common mobile widths down to 360px.
- **SC-006**: Asset loading states remain visually stable, with no mapping card changing height by more than one row of content after previews load.
- **SC-007**: 100% of tested authorized delete actions remain reachable for mappings whose emoji preview or sticker preview is unavailable.

## Assumptions

- The existing management experience remains the source of truth for authentication, server selection, upload success, deletion, and mapping data.
- Server custom emoji metadata and uploaded sticker preview references are available to the management experience for authorized servers.
- This feature covers the custom mapping management view shown to authenticated admins; public homepage demos and Discord message replacement behavior are out of scope.
- If a visual asset cannot be displayed, a readable fallback is preferable to hiding the mapping.
- Existing upload validation continues to determine which sticker files are accepted.
