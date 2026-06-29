# Research: Display Custom Assets

## Decision 1: Extend mapping responses with display-oriented preview fields

**Decision**: Add safe display fields to mapping responses for sticker preview and emoji display metadata. The UI should render these fields and stop using storage identifiers as meaningful display data.

**Rationale**:
- The current mapping response exposes `minioBucketName` and `minioObjectKey`, but those are implementation details and are not useful to admins.
- The current UI already avoids showing storage internals; adding display fields lets it show real assets without breaking the existing delete/list/upload flow.
- Keeping the data on the existing list/upload mapping responses means a newly uploaded sticker can appear with the same preview behavior as mappings loaded after refresh.

**Alternatives considered**:
- **Let UI build MinIO URLs from bucket/object key**: rejected because it leaks storage topology, couples the browser to MinIO, and bypasses backend authorization.
- **Add a separate preview lookup per mapping**: rejected for initial scope because it adds N+1 browser calls and more loading states for the same mapping list.
- **Embed image bytes in JSON**: rejected because it would bloat responses and hurt mapping list performance.

## Decision 2: Use same-origin backend sticker preview endpoints

**Decision**: Generate a browser-safe relative `stickerPreviewUrl` such as `/api/mappings/{id}/preview` for each mapping. The backend preview endpoint verifies the session and guild access before streaming sticker bytes from storage.

**Rationale**:
- A same-origin `/api` URL works through the existing Vite/nginx proxy and does not depend on browser access to an internal MinIO hostname.
- The browser includes normal same-origin session cookies for image requests.
- The API response stays small because it returns only the preview endpoint URL, not image bytes.
- Missing sticker objects can still be represented by image load failure in the UI without hiding the mapping.

**Alternatives considered**:
- **Direct MinIO presigned URLs**: rejected because deployments often configure MinIO with an internal endpoint that backend containers can reach but browsers cannot.
- **Make buckets public**: rejected because uploaded stickers are guild-scoped admin data.
- **Persist generated preview URLs**: rejected because URLs expire and should be created on demand.

## Decision 3: Resolve server custom emoji metadata from the selected guild context

**Decision**: Resolve emoji display metadata from the Discord guild available to the bot for the selected, authorized guild. Return a compact display object when a mapping trigger corresponds to a server custom emoji.

**Rationale**:
- The feature is specifically about custom emojis from the server, so the selected guild is the correct scope.
- JDA is already part of the backend and the bot has guild context; adding display metadata there avoids giving the UI direct Discord API responsibilities.
- If the emoji no longer exists or is inaccessible, the backend can still return the mapping with a readable fallback label.

**Alternatives considered**:
- **Call Discord APIs directly from the UI**: rejected because the browser should not hold bot credentials and auth scopes.
- **Persist custom emoji metadata in the mapping table**: rejected for initial scope because metadata can become stale and the feature only needs current display.
- **Require admins to upload emoji thumbnails manually**: rejected because it adds friction and does not solve existing mappings.

## Decision 4: Treat preview availability as display state, not mapping validity

**Decision**: Keep mappings visible and manageable even when an emoji image or sticker preview is unavailable. The UI renders an explicit fallback state and keeps delete controls available.

**Rationale**:
- A missing asset does not mean the mapping record should disappear.
- Admins need a way to clean up broken mappings.
- This matches the spec requirement to avoid broken images, empty boxes, or misleading placeholders.

**Alternatives considered**:
- **Hide mappings with missing assets**: rejected because it prevents cleanup and makes data loss look worse than it is.
- **Block the whole mapping list when one preview fails**: rejected because asset failures are per-item display issues.

## Decision 5: Keep preview layout local, stable, and accessible in `MappingList`

**Decision**: Add preview-specific view models and CSS inside the existing mapping feature. Use fixed preview dimensions, object-fit containment, loading/error states, and readable labels.

**Rationale**:
- The screenshot problem is isolated to the custom mapping list.
- Existing shared components already cover buttons, modals, empty states, loading, and errors.
- Stable preview cells prevent layout shifts on slow image loads and protect mobile layout.
- Accessible labels ensure the preview is useful to keyboard and screen-reader users, not only sighted users.

**Alternatives considered**:
- **Introduce a new card framework or UI library**: rejected as unnecessary for a focused mapping list fix.
- **Move previews into a modal-only detail view**: rejected because admins need quick scanning directly in the list.
