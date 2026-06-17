# Feature Specification: Fix Sticker Delivery and Bot Name Replacement

**Feature Branch**: `feature/005-fix-sticker-delivery`

**Created**: 2026-06-10

**Status**: Draft

**Input**: User description: "Ошибка в замене эмоджи на стикеры — вместо изображения приходит ссылка на minio, а нужно послать картинку через API Discord. Не работает замена имени — имя должно быть автора с припиской БОТ."

## Clarifications

### Session 2026-06-17

- Q: Which author name must be used for webhook username replacement? → A: Use the original guild display name exactly as shown in Discord; rely on Discord's standard bot badge instead of appending textual `БОТ` to the webhook username.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Send sticker as image file instead of URL (Priority: P1)

As a Discord user, I want the bot to send the actual sticker image when an emoji is detected, not a Minio presigned URL, so that the sticker appears as a proper image in the chat.

**Why this priority**: The core feature is completely broken — users see raw Minio URLs instead of sticker images. This is the primary user-facing bug.

**Independent Test**: Send a message with a supported emoji and verify that the bot responds with an actual image file attachment, not a URL string.

**Acceptance Scenarios**:

1. **Given** a user sends a message containing a supported default emoji, **When** the listener processes the message, **Then** the bot sends the corresponding sticker as an image file attachment (not a URL) in the same channel.
2. **Given** a user sends a message with a mapped custom sticker, **When** the listener processes the message, **Then** the bot sends the sticker image file from Minio as an attachment.
3. **Given** the Minio presigned URL expires or is unreachable, **When** the listener attempts to download the file, **Then** the system logs an error and does not crash.

---

### User Story 2 - Send sticker as the original message author with Discord bot badge (Priority: P2)

As a Discord user, I want the sticker response to appear as if it was sent by me (the original message author) with Discord's standard bot badge, so that the sticker feels like a personal reaction rather than a bot response.

**Why this priority**: The bot currently sends messages under its own name "Bigmoji БОТ", which breaks the illusion of emoji-to-sticker replacement. Users expect the sticker to appear as their own reaction.

**Independent Test**: Send a message with a supported emoji as user "Alice" and verify the sticker appears from "Alice" with Discord's standard bot badge via webhook.

**Acceptance Scenarios**:

1. **Given** a user named "Alice" sends a message with a supported emoji, **When** the listener processes the message, **Then** the sticker is sent via webhook with username "Alice", Discord's standard bot badge, and the original user's avatar.
2. **Given** a guild member whose visible nickname is "Ne_Tort" and whose account username may differ or be normalized, **When** the listener processes a supported emoji message, **Then** the webhook username is "Ne_Tort" with Discord's standard bot badge and not a lowercased or prefixed variant such as ".ne_tort БОТ".
3. **Given** a user with a special character name sends a message, **When** the listener processes the message, **Then** the webhook username is sanitized to comply with Discord's username rules (max 32 chars, no @/#) while preserving allowed casing and underscores.
4. **Given** the webhook creation fails (e.g., missing permissions), **When** the listener processes the message, **Then** the system falls back to sending the sticker under the bot's own name and logs a warning.

---

### User Story 3 - Preserve existing behavior for non-target messages (Priority: P3)

As a product owner, I want the listener to keep existing behavior for unsupported inputs while fixing the sticker delivery and name replacement, so that only intended scenarios change.

**Why this priority**: The fix should not introduce regressions in unrelated message handling.

**Independent Test**: Re-run current listener regression checks for supported and unsupported message inputs and confirm no unrelated behavior changes.

**Acceptance Scenarios**:

1. **Given** a message previously ignored by listener rules (e.g., bot message, no emoji), **When** the updated listener receives it, **Then** it remains ignored under the same rules.
2. **Given** a message with unsupported emoji content, **When** the updated listener processes it, **Then** no incorrect sticker is sent.

### Edge Cases

- What happens when the Minio file download fails (network error, file deleted)?
- How does the system handle very large sticker files that exceed Discord's file size limit (8MB for non-nitro)?
- What happens when the original author's name is longer than 32 characters (Discord webhook username limit)?
- How does the system handle author names containing `@` or `#` characters that Discord may interpret specially?
- How does the system avoid adding textual `БОТ` when Discord already renders the webhook message with the standard bot badge?
- What happens when the bot lacks `MANAGE_WEBHOOKS` permission in the channel?
- How are duplicate emoji occurrences in one message handled to avoid unintended duplicate sticker sends?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST download the sticker file from Minio using the presigned URL before sending it to Discord.
- **FR-002**: System MUST send the sticker as a file attachment using Discord's `sendFiles()` API, not as a text message containing a URL.
- **FR-003**: System MUST create or reuse a Discord webhook in the target channel to send the sticker under the original author's name with Discord's standard bot badge.
- **FR-004**: System MUST set the webhook username to `<original_guild_display_name>`, sanitized only as required to comply with Discord's username rules (max 32 characters, no `@` or `#`) while preserving the original casing, underscores, and other allowed characters.
- **FR-004a**: System MUST use the guild member effective display name (`event.getMember().getEffectiveName()` when available) for webhook username replacement; it MUST NOT use a normalized account username when guild display name is available.
- **FR-004b**: System MUST NOT append textual `БОТ` to the webhook username because Discord renders the standard bot badge automatically. The final visible result for nickname `Ne_Tort` MUST be username `Ne_Tort` plus the Discord bot badge, not `Ne_Tort БОТ` plus the badge.
- **FR-005**: System MUST set the webhook avatar URL to the original author's avatar URL.
- **FR-006**: System MUST fall back to sending the sticker under the bot's own name if webhook creation or execution fails, logging a warning.
- **FR-007**: System MUST handle Minio download failures gracefully by logging an error and not crashing the application.
- **FR-008**: System MUST delete the original emoji message after successful sticker delivery (existing behavior preserved).
- **FR-009**: System MUST keep existing listener behavior unchanged for message flows outside sticker replacement scope.

### Key Entities *(include if feature involves data)*

- **Sticker File**: Binary image data downloaded from Minio, sent as a Discord file attachment.
- **Discord Webhook**: Channel-level webhook used to impersonate the original message author when sending the sticker.
- **Webhook Username**: Sanitized version of the original guild display name without textual `БОТ`, max 32 characters.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of messages containing supported emojis result in the corresponding sticker being sent as an image file attachment (not a URL).
- **SC-002**: 100% of sticker responses appear under the original guild display name with only Discord's standard bot badge when webhook permissions are available.
- **SC-003**: System gracefully falls back to bot name when webhook creation fails, with no message loss.
- **SC-004**: Regression verification confirms no unintended behavior changes for unsupported inputs compared with baseline listener behavior.

## Assumptions

- The bot has `MANAGE_WEBHOOKS` permission in channels where sticker replacement is expected.
- Minio presigned URLs are valid for at least 5 minutes (current expiry is 300 seconds).
- Sticker files are within Discord's file size limit (8MB for standard users).
- The original message author's guild display name is accessible via `event.getMember().getEffectiveName()` for guild messages; `event.getAuthor().getName()` is only a fallback when member context is unavailable.
- Webhook creation is idempotent — the system should reuse existing webhooks when possible to avoid cluttering the channel's webhook list.
