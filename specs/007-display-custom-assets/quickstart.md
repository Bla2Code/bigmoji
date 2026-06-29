# Quickstart: Display Custom Assets

## Goal

Verify that the authenticated custom mapping view displays:
- server custom emoji images and readable names;
- uploaded sticker preview images;
- clear fallback states for unavailable emoji or sticker previews;
- delete controls that remain usable even when previews fail.

## Prerequisites

- Java 21
- Node.js 22.12+ and npm
- Docker/MinIO/PostgreSQL setup used by the existing backend
- Discord bot configured for a test guild that contains at least one custom emoji
- Existing UI dependencies installed with `npm install` in `ui/`

## Backend Verification

From the repository root:

```bash
./gradlew test
```

Focused tests to add/run during implementation:

```bash
./gradlew test --tests '*StickerMappingControllerContractTest'
./gradlew test --tests '*StickerMappingControllerIntegrationTest'
./gradlew test --tests '*StickerMappingServiceTest'
```

Expected backend behavior:
- `GET /api/mappings/{guildId}` returns preview-capable mapping payloads for authorized guilds.
- `POST /api/mappings` returns the same preview-capable shape for the newly uploaded mapping.
- Missing sticker preview URL generation does not remove the mapping from the response.
- Unauthorized users do not receive preview URLs or custom emoji metadata.

## UI Verification

From `ui/`:

```bash
npm run lint
npm run typecheck
npm test
```

Focused tests to add/run during implementation:

```bash
npx vitest run MappingList mappings --coverage.enabled=false
```

Use full `npm test` for the coverage-gated suite.

Expected UI behavior:
- mapping groups show custom emoji image and readable name when available;
- standard Unicode emoji and text triggers still render readably;
- uploaded sticker preview images render for each mapping;
- unavailable preview states show a clear placeholder instead of a broken image;
- delete confirmation works for mappings with and without preview images.

## Manual Smoke Test

1. Start the backend with PostgreSQL, MinIO, Discord bot credentials, and a test guild.
2. Start the UI.
3. Sign in as a Discord user authorized to manage the test guild.
4. Open `/app/guilds/{guildId}`.
5. Upload a sticker for a server custom emoji trigger.
6. Confirm the custom mappings list shows:
   - the server custom emoji image;
   - the readable emoji name or shortcode;
   - the uploaded sticker preview image;
   - the correct custom sticker count.
7. Refresh the page.
8. Confirm the same previews still appear after reload.
9. Delete the mapping and confirm it disappears.

## Fallback Smoke Test

Use mocked API data or a controlled test fixture where:
- `emojiPreview.available` is false or `imageUrl` is missing;
- `stickerPreviewState` is `unavailable`;
- at least one mapping has a standard Unicode emoji trigger.

Verify:
- no broken image icon appears;
- fallback text is readable;
- delete action is still reachable;
- layout remains stable at desktop width and 360px mobile width.

## E2E/Responsive Check

From `ui/`:

```bash
npm run test:e2e -- mapping-management.spec.ts
```

If browser dependencies are missing:

```bash
npx playwright install
```

Expected result:
- admin mapping flow passes for preview-capable payloads;
- responsive mapping list has no horizontal scrolling or overlapping content at 360px width.
