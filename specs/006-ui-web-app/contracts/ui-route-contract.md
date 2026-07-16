# Contract: UI Routes and User Flows

The UI is a single-page application. Routes are browser routes served by the UI container; unknown browser routes should fall back to `index.html`.

## Routes

### `/`

**Purpose**: Public homepage and onboarding entry.

**Visible to**: Everyone.

**Required Content**:
- Bigmoji product signal in the first viewport.
- Visual demo of upload -> emoji trigger -> sticker replacement.
- Sign-in/register panel connected to Discord sign-in.
- Mobile layout where demo and auth panel remain visible without overlap.

**Actions**:
- "Sign in with Discord" navigates to `/api/auth/discord/login`.
- If already authenticated, "Open dashboard" navigates to `/app`.

### `/app`

**Purpose**: Authenticated admin entry.

**Visible to**: Authenticated users.

**Required Content**:
- Current user summary.
- Manageable guild selector.
- Empty state when no manageable guilds exist.
- Bot install guidance when available.

**Guards**:
- Guest users are redirected or prompted to sign in.
- 401 from session check returns user to guest state.

### `/app/guilds/:guildId`

**Purpose**: Manage sticker mappings for one guild.

**Visible to**: Authenticated users whose session includes `guildId`.

**Required Content**:
- Selected guild name.
- Sticker upload form with file preview and emoji trigger input.
- Existing custom mappings list.
- Default sticker list shown separately from custom mappings.
- Delete confirmation for each custom mapping.
- Loading, empty, validation, forbidden, and backend unavailable states.

**Guards**:
- If `guildId` is not in `manageableGuilds`, show access denied and no upload/delete controls.

## Layout and Accessibility Rules

- Header/navigation must expose current auth state and logout when authenticated.
- Primary actions must be reachable by keyboard.
- Form controls must have visible labels.
- Form errors must be associated with controls and announced to assistive technology.
- Focus must move into modal dialogs and return to the triggering control when closed.
- Reduced-motion preference must disable or minimize homepage demo animations.
- At 360px viewport width, content must not require horizontal scrolling.

## Critical User Flow Contracts

### Homepage comprehension flow

1. User opens `/`.
2. First viewport shows product name/purpose, demo, and sign-in/register area.
3. User can identify upload, emoji trigger, and replacement result without reading external documentation.

### Authenticated setup flow

1. User clicks sign in on `/`.
2. Browser navigates through Discord OAuth via backend.
3. User returns to UI.
4. UI calls `/api/auth/me`.
5. UI shows manageable guilds or no-guild empty state.

### Mapping management flow

1. User opens `/app/guilds/:guildId`.
2. UI verifies session and guild authorization from `/api/auth/me`.
3. UI loads default stickers and custom mappings.
4. User uploads image + emoji trigger.
5. UI refreshes mapping list after success.
6. User deletes a mapping after confirmation.
7. UI refreshes or updates mapping list after delete.
