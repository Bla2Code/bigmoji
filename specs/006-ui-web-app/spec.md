# Feature Specification: Bigmoji Web UI

**Feature Branch**: `006-ui-web-app`

**Created**: 2026-06-24

**Status**: Draft

**Input**: User description: "теперь нам нужно сделать UI для нашего бота. Важно сделать это как отдельное приложение интегрированное через API и упаковонное в docker. UI должен иметь красивую главную страницу на которой понятно что делает бот. Пример: видно как пользователь загружает свои стикеры, далее он вводит эмоджи и как они заменяются стикерами, и после сбоку или внизу форма регистрации или авторизации"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Understand Bigmoji From the Homepage (Priority: P1)

A new visitor opens the Bigmoji web UI and immediately understands that Bigmoji lets Discord server admins upload custom stickers, bind them to emoji triggers, and have the bot replace emoji-only messages with large stickers in chat.

**Why this priority**: The homepage is the first product touchpoint and must explain the bot's value before asking users to authenticate or configure anything.

**Independent Test**: Can be fully tested by opening the unauthenticated homepage and verifying that the first viewport shows the Bigmoji purpose, sticker upload step, emoji input step, replacement result, and an authentication or registration entry point.

**Acceptance Scenarios**:

1. **Given** a visitor opens the UI without an active session, **When** the homepage loads, **Then** the visitor sees a polished first screen that clearly communicates "upload stickers, map emojis, replace emoji messages in Discord"
2. **Given** a visitor scans the visual demo, **When** they follow the sequence, **Then** they can identify the sticker upload step, the emoji trigger step, and the resulting sticker replacement in a Discord-like chat preview
3. **Given** the visitor is unauthenticated, **When** they view the first screen on desktop, **Then** the authentication or registration form is visible beside the product demo without requiring navigation
4. **Given** the visitor is unauthenticated, **When** they view the first screen on a mobile device, **Then** the authentication or registration form remains visible near the demo without overlapping or hiding the core message

---

### User Story 2 - Start Admin Onboarding (Priority: P1)

A Discord server admin uses the homepage authentication or registration area to start onboarding, sign in with their Discord identity, and return to the UI with their manageable servers available.

**Why this priority**: The UI cannot manage stickers safely unless the admin is authenticated and authorized for a Discord server.

**Independent Test**: Can be fully tested by starting authentication from the homepage, completing the Discord identity flow, and verifying that the UI shows the authenticated user and their manageable servers.

**Acceptance Scenarios**:

1. **Given** an unauthenticated visitor, **When** they choose to sign in or register from the homepage form, **Then** the UI starts the approved Discord-based onboarding flow
2. **Given** authentication succeeds, **When** the admin returns to the UI, **Then** the UI shows their current session state and a list of Discord servers they are allowed to manage
3. **Given** authentication fails or is cancelled, **When** the visitor returns to the UI, **Then** they see a clear, non-technical message and can retry from the same entry point
4. **Given** an authenticated user has no manageable Discord servers, **When** they reach the UI, **Then** the UI explains that no eligible servers are available and offers the bot installation path if available

---

### User Story 3 - Manage Emoji-to-Sticker Mappings (Priority: P2)

An authenticated server admin selects a Discord server, uploads one or more sticker images, assigns each sticker to an emoji trigger, reviews existing mappings, and removes mappings they no longer want.

**Why this priority**: The homepage creates intent, but the product value is completed when admins can configure the stickers used by the bot.

**Independent Test**: Can be fully tested by signing in as an authorized admin, selecting one manageable server, uploading a valid sticker for an emoji, confirming it appears in the mapping list, and deleting it.

**Acceptance Scenarios**:

1. **Given** an authenticated admin selects a manageable server, **When** they upload a valid sticker and choose an emoji trigger, **Then** the UI creates the mapping for that server and shows it in the server's mapping list
2. **Given** a server already has mappings, **When** the admin opens that server in the UI, **Then** they see the current custom mappings grouped or presented clearly enough to find a specific emoji
3. **Given** multiple stickers can be associated with the same emoji, **When** the admin views that emoji, **Then** the UI makes it clear that any of the associated stickers may be used by the bot
4. **Given** an admin deletes a mapping, **When** the deletion is confirmed, **Then** the mapping disappears from the UI and is no longer presented as active for that server

---

### User Story 4 - Deploy UI Independently (Priority: P3)

An operator deploys the web UI as an independent containerized application that connects to an existing Bigmoji backend through its public management interface, without bundling the Discord bot runtime into the UI service.

**Why this priority**: The user explicitly requires the UI to be a separate application integrated through the API and packaged for Docker-based deployment.

**Independent Test**: Can be fully tested by starting the UI container with runtime configuration for the backend URL, opening the UI, and confirming it can read session state and perform admin flows through the backend interface.

**Acceptance Scenarios**:

1. **Given** the Bigmoji backend is running and reachable, **When** the UI container starts with valid runtime configuration, **Then** the UI serves the homepage and connects to backend management capabilities
2. **Given** the backend URL changes between environments, **When** the operator changes runtime configuration without rebuilding the backend, **Then** the same UI application can target the new backend
3. **Given** the backend is unavailable, **When** a visitor opens the UI or an admin performs an action, **Then** the UI shows a clear unavailable-state message instead of exposing technical failure details

### Edge Cases

- **Expired session**: If the admin session expires while using the UI, the next protected action should prompt the admin to sign in again and preserve enough context to continue.
- **Unauthorized guild access**: If a user attempts to access a guild they cannot manage, the UI should show an access-denied state and must not present upload or delete controls for that guild.
- **Bot not installed**: If a manageable server does not yet have the bot installed, the UI should guide the admin to the installation path before asking them to configure stickers for that server.
- **Invalid sticker upload**: If a file is too large, corrupted, or not an allowed image type, the UI should explain the problem before or after submission and allow the admin to choose another file.
- **Emoji ambiguity**: If an emoji has variants, modifiers, or a shortcode equivalent, the UI should display the selected trigger clearly so admins understand exactly what will be replaced.
- **Mobile layout**: The demo, auth area, and management forms must not overlap or require horizontal scrolling on common mobile widths.
- **Slow backend**: If the backend is slow, the UI should show loading, retry, and disabled-action states so admins do not submit duplicate uploads by accident.
- **Large mapping list**: If a server has many mappings, the UI should remain searchable or scannable enough for admins to find a specific emoji without reviewing every item manually.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST provide a separate web UI application for Bigmoji that can be deployed independently from the Discord bot runtime.
- **FR-002**: System MUST integrate with the existing Bigmoji management backend only through its public management interface; the UI MUST NOT require direct database access, object storage access, or bot token access.
- **FR-003**: System MUST be packageable as a Docker-compatible container image with runtime configuration for the backend base URL and public UI URL.
- **FR-004**: System MUST provide a polished unauthenticated homepage that clearly explains what the bot does within the first viewport.
- **FR-005**: Homepage MUST visually demonstrate the core workflow: admin uploads sticker images, admin assigns emoji triggers, Discord user sends an emoji, and Bigmoji replaces it with a large sticker.
- **FR-006**: Homepage MUST show an authentication or registration entry point beside the demo on wider screens and near the demo on narrower screens.
- **FR-007**: System MUST start the approved Discord-based sign-in or onboarding flow from the homepage entry point.
- **FR-008**: System MUST show current authentication state after sign-in, including the authenticated Discord user and the Discord servers they are authorized to manage.
- **FR-009**: System MUST prevent unauthenticated users from accessing sticker management screens and prompt them to sign in instead.
- **FR-010**: System MUST prevent users from managing servers outside their authorized server list.
- **FR-011**: System MUST allow an authenticated admin to select one manageable Discord server before viewing or changing mappings.
- **FR-012**: System MUST allow an authenticated admin to upload a sticker image and assign it to a specific emoji trigger for the selected server.
- **FR-013**: System MUST show existing custom emoji-to-sticker mappings for the selected server.
- **FR-014**: System MUST allow an authenticated admin to delete an existing mapping after an explicit confirmation.
- **FR-015**: System MUST distinguish custom server mappings from any default stickers that are globally available to new servers.
- **FR-016**: System MUST show clear loading, success, validation error, authorization error, and backend unavailable states for all user-facing operations.
- **FR-017**: System MUST support responsive desktop and mobile layouts for the homepage, authentication entry point, and core mapping management flows.
- **FR-018**: System MUST meet basic accessibility expectations for a web UI, including keyboard navigation, meaningful labels, visible focus states, and screen-reader-friendly form errors.
- **FR-019**: System MUST avoid exposing raw backend errors, storage URLs, bot tokens, session secrets, stack traces, or internal identifiers that are not needed by the user.
- **FR-020**: System MUST provide deployment documentation sufficient for an operator to run the UI container against an existing Bigmoji backend in a local or hosted environment.

### Key Entities

- **Visitor**: A person viewing the unauthenticated homepage to understand what Bigmoji does and decide whether to start onboarding.
- **Authenticated Admin**: A Discord user with an active session who can manage one or more Discord servers.
- **Manageable Server**: A Discord server the authenticated admin is authorized to configure through Bigmoji.
- **Sticker Mapping**: A relationship between an emoji trigger and one or more sticker images for a specific Discord server.
- **Sticker Upload**: A user-selected image file and associated emoji trigger submitted by an admin for a selected server.
- **Auth Session**: The current authenticated state used by the UI to show user identity, server access, and protected actions.
- **Homepage Demo Step**: A presentational state in the homepage walkthrough that explains upload, emoji mapping, and replacement behavior without requiring real configuration.
- **Deployment Configuration**: Runtime values that tell the UI where the Bigmoji backend is located and what public URL the UI uses during authentication redirects.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: At least 90% of first-time visitors in a usability test can describe Bigmoji's core purpose within 15 seconds of seeing the homepage.
- **SC-002**: At least 90% of first-time visitors can identify the three homepage demo stages: sticker upload, emoji trigger entry, and sticker replacement result.
- **SC-003**: An unauthenticated visitor can start sign-in or registration from the first screen in 1 click on desktop and mobile.
- **SC-004**: An authenticated admin can select a server, upload one valid sticker, assign it to an emoji, and see it in the mapping list within 3 minutes without external documentation.
- **SC-005**: 100% of protected management actions are unavailable to unauthenticated users and unavailable for servers outside the authenticated user's manageable server list.
- **SC-006**: The homepage and core management views remain usable without horizontal scrolling at common mobile widths down to 360px.
- **SC-007**: Users receive clear, actionable feedback for invalid uploads, expired sessions, authorization failures, and backend unavailability in 100% of tested error scenarios.
- **SC-008**: An operator can run the UI as an independent containerized application against an existing Bigmoji backend without rebuilding or redeploying the bot service.

## Assumptions

- "Registration or authorization" means the homepage must provide an onboarding entry point; the existing Discord-based identity flow remains the primary authentication path unless a later feature explicitly introduces local accounts.
- The backend remains the source of truth for authentication, authorization, manageable servers, sticker mappings, default stickers, and upload validation.
- The UI will not store Discord bot credentials, database credentials, or object storage credentials.
- The homepage demo may use illustrative sample stickers, emoji, and chat states; it does not need to create real mappings until the admin signs in.
- The initial UI scope includes the public homepage, sign-in/onboarding entry, authenticated server selection, upload/list/delete mapping flows, and deployment packaging.
- Advanced analytics, billing, team roles beyond Discord server authorization, and custom visual theme builders are out of scope for this feature.
