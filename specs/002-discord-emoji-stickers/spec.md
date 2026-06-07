# Feature Specification: Discord Emoji-to-Sticker Bot

**Feature Branch**: `002-discord-emoji-stickers`

**Created**: 2026-06-03

**Status**: Draft

**Input**: User description: "project Bigmoji - Discord bot that replaces emojis with large stickers. Backend-only application with REST API for UI integration. WHAT: Bot connects to Discord server via token and reads all messages in channels where it has permissions. When a user sends a message containing exactly one emoji (Unicode or :smile: in text) and nothing else, the bot deletes that message and sends a sticker from the server's sticker collection that matches that emoji. If the message contains multiple emojis, regular text, or emojis inside text - the bot ignores it. Mapping between emoji and sticker is configured by the server admin via REST API: upload a large sticker image to the bot, assign it a name matching the emoji (e.g., smile for 😊). Bot supports multiple stickers per emoji (random selection). Pre-installed default set for popular emojis (😊, ❤️, 🎉, 👍, 🔥). Each Discord server owner has their own independent sticker mappings (isolated by guildId). REST API must allow: upload new sticker image with emoji and guildId, get all mappings for a server, delete a mapping. API must verify requests come from authorized user (server admin) via Discord OAuth2 or static API key (simple option for start). Bot stores all mappings in database and loads into memory on startup for fast processing. WHY: Discord users want to express emotions with large bright stickers instead of small emojis, similar to Nitro. Bot automates replacement, allowing each server to have its unique sticker set. Deleting original message avoids duplication and keeps chat clean."

## Clarifications

### Session 2026-06-07

- Q: Should local fallback stickers be used when database lookup fails or times out? → A: Use local fallback only when DB lookup succeeds and no mapping exists; on DB failure the bot does not replace the message.
- Q: Should mapping API responses include fallback defaults or only persisted mappings? → A: `GET /mappings` returns only database (custom) mappings; fallback defaults are runtime-only and not listed.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Single Emoji Message Auto-Replacement (Priority: P1)

A user sends a message in a Discord channel containing only a single emoji (either a Unicode emoji like 😊 or a text-based emoji like :smile:). The bot detects this, deletes the original message, and posts a matching large sticker from the server's configured sticker collection in the same channel.

**Why this priority**: This is the core functionality and primary value proposition of the bot. Without this, the bot serves no purpose.

**Independent Test**: Can be fully tested by sending a single-emoji message in a channel and verifying the original message is deleted and a matching sticker appears in its place.

**Acceptance Scenarios**:

1. **Given** a user sends a message containing only the emoji 😊, **When** the bot processes the message, **Then** the original message is deleted and a sticker mapped to 😊 is posted in the same channel
2. **Given** a user sends a message containing only the text :smile:, **When** the bot processes the message, **Then** the original message is deleted and a sticker mapped to "smile" is posted in the same channel
3. **Given** multiple stickers are mapped to the same emoji, **When** the bot responds to a single-emoji message, **Then** one of the mapped stickers is selected at random
4. **Given** no sticker is mapped to the sent emoji, **When** the bot processes the message, **Then** the original message is left unchanged (not deleted)

---

### User Story 2 - Multi-Emoji and Text Message Ignored (Priority: P1)

A user sends a message containing multiple emojis, regular text with emojis, or emojis embedded within text. The bot correctly identifies this as not matching the single-emoji pattern and takes no action.

**Why this priority**: Prevents false positives and ensures the bot only acts on the intended use case. Critical for user experience and chat cleanliness.

**Independent Test**: Can be fully tested by sending messages with multiple emojis, text with emojis, and mixed content, then verifying the bot does not delete or respond to any of them.

**Acceptance Scenarios**:

1. **Given** a user sends a message with two emojis (e.g., "😊❤️"), **When** the bot processes the message, **Then** the message is left unchanged and no sticker is posted
2. **Given** a user sends a message with text and an emoji (e.g., "Hello 😊"), **When** the bot processes the message, **Then** the message is left unchanged and no sticker is posted
3. **Given** a user sends a message with an emoji embedded in text (e.g., "I am 😊 today"), **When** the bot processes the message, **Then** the message is left unchanged and no sticker is posted

---

### User Story 3 - Server Admin Manages Sticker Mappings via REST API (Priority: P2)

A server administrator uses the REST API to upload new sticker images, associate them with specific emojis, view existing mappings for their server, and delete mappings they no longer want.

**Why this priority**: Enables server customization, which is a key differentiator. Each server having its own unique sticker set is core to the product vision.

**Independent Test**: Can be fully tested by making REST API calls to upload a sticker, retrieve mappings, and delete a mapping, then verifying the database reflects the changes.

**Acceptance Scenarios**:

1. **Given** an authenticated server admin, **When** they upload a sticker image with an associated emoji and guildId, **Then** the sticker is stored and mapped to that emoji for that server
2. **Given** an authenticated server admin, **When** they request all mappings for their server, **Then** they receive a list of all emoji-to-sticker mappings for that guild
3. **Given** an authenticated server admin, **When** they delete a specific mapping, **Then** that mapping is removed and the sticker is no longer used for that emoji
4. **Given** an unauthenticated or unauthorized user, **When** they attempt any management API call, **Then** the request is rejected with an appropriate error

---

### User Story 4 - Local Fallback Stickers for First Use (Priority: P3)

When a server first adds the bot, a local fallback set of sticker placeholders for popular emojis (😊, ❤️, 🎉, 👍, 🔥) is available without any configuration and is used only when no database mapping exists for that emoji in that guild.

**Why this priority**: Provides immediate value and reduces friction for new users. Not critical for MVP since admins can upload their own, but improves onboarding.

**Independent Test**: Can be fully tested by adding the bot to a new server and sending single-emoji messages for each fallback emoji, verifying placeholders are posted only when no custom mapping exists in the database for that emoji.

**Acceptance Scenarios**:

1. **Given** a server has just added the bot and no custom mappings exist, **When** a user sends 😊, **Then** a local fallback sticker placeholder for 😊 is posted
2. **Given** an admin uploads one or more custom stickers for 😊, **When** a user sends 😊, **Then** only custom database-mapped stickers are eligible for random selection and fallback placeholders are not used

---

### Edge Cases

- **Bot permissions**: How does the bot behave if it lacks permission to delete messages in a channel? The bot should skip processing and not attempt deletion.
- **Sticker upload failures**: How does the system handle a failed sticker upload (e.g., invalid image format, file too large)? The API should return a clear error message.
- **Database lookup failures**: If mapping lookup fails or times out, the bot should not use local fallback stickers and should leave the original message unchanged.
- **Discord API rate limits**: How does the bot handle Discord rate limiting when deleting messages and posting stickers in rapid succession? The bot should queue or delay actions appropriately.
- **Emoji variant sequences**: How does the bot handle emoji with skin tone modifiers, gender variants, or flag sequences? These should be treated as distinct emojis requiring their own mappings.
- **Bot message loop**: How does the bot avoid processing its own sticker messages? The bot should ignore messages sent by itself.
- **Concurrent messages**: How does the bot handle multiple single-emoji messages sent simultaneously? Each should be processed independently without race conditions.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST connect to Discord servers using a bot token and receive real-time message events from channels where it has read and send permissions
- **FR-002**: System MUST detect messages that contain exactly one emoji (Unicode or text-based like :smile:) and no other content (no text, no additional emojis, no whitespace-only content)
- **FR-003**: System MUST delete the original user message when a single-emoji match is found and a corresponding sticker mapping exists
- **FR-004**: System MUST post a sticker from the server's sticker collection that matches the detected emoji in the same channel as the deleted message
- **FR-005**: System MUST randomly select one sticker when multiple stickers are mapped to the same emoji
- **FR-006**: System MUST ignore messages that contain multiple emojis, text with emojis, or emojis embedded within text
- **FR-007**: System MUST ignore messages sent by the bot itself to prevent processing loops
- **FR-008**: System MUST provide a REST API endpoint to upload a sticker image file and associate it with a specific emoji for a specific guildId
- **FR-009**: System MUST provide a REST API endpoint to retrieve all emoji-to-sticker mappings for a given guildId
- **FR-010**: System MUST provide a REST API endpoint to delete a specific emoji-to-sticker mapping for a given guildId
- **FR-011**: System MUST authenticate REST API requests using either Discord OAuth2 or a static API key, verifying the requester is an authorized server admin
- **FR-012**: System MUST store all sticker mappings in a persistent database
- **FR-013**: System MUST load all sticker mappings into memory on startup for fast message processing
- **FR-014**: System MUST include a local fallback sticker placeholder set for popular emojis (😊, ❤️, 🎉, 👍, 🔥) packaged with the application resources and MUST NOT persist this fallback set in the database
- **FR-015**: System MUST isolate sticker mappings by guildId so each Discord server has independent mappings
- **FR-016**: System MUST first use database mappings for the detected emoji and guildId; if none exist, system MUST use local fallback placeholders when available for that emoji, otherwise leave the original message unchanged
- **FR-017**: System MUST respect Discord channel permissions and only process messages in channels where it has both read and send permissions
- **FR-018**: System MUST validate uploaded sticker images for acceptable format and size before storing
- **FR-019**: System MUST NOT use local fallback placeholders when database lookup fails or times out; in this case the original message MUST remain unchanged
- **FR-020**: System MUST return only persisted database mappings in the mapping retrieval API; local fallback placeholders are runtime-only and excluded from API response payloads

### Key Entities

- **Sticker Mapping**: Represents the association between an emoji (Unicode character or text name) and a sticker image file, scoped to a specific guildId. Includes: emoji identifier, sticker file reference, guildId, creation timestamp.
- **Sticker Image**: The actual image file uploaded by a server admin to be used as a sticker response. Includes: file data, format, size, unique identifier.
- **Guild Configuration**: Represents a Discord server's sticker settings, including which database mappings are active for that guild. Includes: guildId, admin identifiers.
- **Local Fallback Sticker**: Represents a packaged application resource (not persisted in database) used as a runtime placeholder only when a guild has no database mapping for a supported emoji.
- **API Credential**: Represents authentication credentials for REST API access. Includes: credential type (OAuth2 token or API key), associated user/guild, permissions level, expiration (if applicable).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can send a single emoji and see a matching sticker posted within 2 seconds of sending the message, 95% of the time
- **SC-002**: Bot correctly identifies and processes single-emoji messages with 99% accuracy (no false positives on multi-emoji or text+emoji messages)
- **SC-003**: Bot correctly ignores non-single-emoji messages with 99% accuracy (no false negatives on messages that should be ignored)
- **SC-004**: Server admins can upload a new sticker and have it available for use within 5 seconds of upload completion
- **SC-005**: System supports 100 concurrent Discord servers with active emoji-to-sticker replacement without message processing delays exceeding 3 seconds
- **SC-006**: 90% of new server admins can successfully upload and configure at least one custom sticker within 3 minutes of adding the bot, without external documentation

## Assumptions

- Users have stable internet connectivity and Discord client applications that support stickers (desktop, mobile, web)
- The bot will be added to servers by users with "Manage Server" or equivalent administrative permissions
- Discord's API and rate limits remain stable and within documented specifications
- Sticker images uploaded by admins comply with Discord's sticker requirements (format, dimensions, file size)
- The bot operates within a single Discord application/bot account
- Local fallback sticker set images are provided as part of the initial application deployment
- REST API consumers are either a separate frontend UI application or server admins using API tools directly
- Static API key authentication is sufficient for initial release; full Discord OAuth2 flow can be implemented later
- Database of choice supports concurrent read/write operations and can scale with the number of servers and mappings
- The bot does not need to support custom Discord emotes (server-specific animated/static emotes) in the initial scope — only standard Unicode emojis and text-based emoji shortcodes
