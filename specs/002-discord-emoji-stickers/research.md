# Research: Discord Emoji-to-Sticker Bot

## Decision: JDA Version and Emoji Detection

**Context**: Need to determine the correct JDA version compatible with Java 25 and Spring Boot 4.0.6, and the approach for detecting single-emoji messages.

**Decision**: Use JDA 5.x (latest stable) with Java 25 compatibility. For emoji detection, use a two-pass approach:
1. First pass: Check if message matches a single Unicode emoji using regex with Unicode property escapes
2. Second pass: Check if message matches a single Discord shortcode emoji (e.g., `:smile:`)

**Rationale**: JDA 5.x is the actively maintained version with modern Java support. Unicode emoji detection requires handling surrogate pairs and variation selectors. Discord shortcodes follow the `:name:` pattern.

**Alternatives considered**:
- Using a third-party emoji library (e.g., emoji-java) — rejected to minimize dependencies; regex is sufficient
- Using JDA's built-in emote detection — rejected as we need Unicode emoji, not server custom emotes

---

## Decision: MinIO Bucket Strategy

**Context**: Need to determine how to organize sticker images in MinIO storage.

**Decision**: Use per-guild buckets with naming pattern `bigmoji-{guildId}`. Each bucket is created automatically on first sticker upload for that guild. Object keys are UUIDs with original file extension (e.g., `a1b2c3d4-e5f6-7890-abcd-ef1234567890.png`).

**Rationale**: Per-guild buckets provide natural isolation and simplify cleanup if a server removes the bot. UUID keys prevent filename collisions.

**Alternatives considered**:
- Single bucket with guildId prefix in object key — rejected as it complicates bucket lifecycle management
- Shared bucket with flat structure — rejected due to lack of isolation

---

## Decision: Presigned URL Strategy for Sticker Delivery

**Context**: Bot needs to send sticker images to Discord. Discord's official sticker API requires pre-uploading stickers to the server, which is complex. Alternative is sending images as message attachments.

**Decision**: Generate presigned URLs from MinIO (valid for 5 minutes) and send images as regular message attachments using `channel.sendFiles()`. After sending, delete the original user message.

**Rationale**: Simpler implementation, achieves the goal of "big emoji replacement". Presigned URLs avoid downloading the image to the bot server — Discord fetches directly from MinIO.

**Alternatives considered**:
- Download image from MinIO to bot, then upload to Discord — rejected as it adds latency and memory overhead
- Use Discord's official sticker API — rejected as too complex for initial release

---

## Decision: In-Memory Caching Strategy

**Context**: Spec requires loading all mappings into memory on startup for fast processing.

**Decision**: Use a `ConcurrentHashMap<String, List<StickerMapping>>` where key is `{guildId}:{emojiName}` and value is list of mappings for that emoji. Cache is populated on startup via `ApplicationReadyEvent` and refreshed on API write operations (upload/delete).

**Rationale**: Simple, thread-safe, sufficient for the expected scale (100 servers, reasonable number of mappings per server). No need for external cache like Redis at this scale.

**Alternatives considered**:
- Query database on each message — rejected as it adds latency and may miss the 3-second Discord window
- Use Caffeine/Guava cache — rejected as overkill; direct ConcurrentHashMap is simpler and sufficient

---

## Decision: API Key Authentication

**Context**: Spec requires authenticating REST API requests. OAuth2 is complex for initial release.

**Decision**: Use static API key passed via `X-API-Key` header. Validated through a Spring MVC `HandlerInterceptor`. API key is configured via environment variable. Single API key for all servers (server admin responsibility to protect it).

**Rationale**: Simplest authentication mechanism, sufficient for initial release. Can be upgraded to OAuth2 later without changing API contract.

**Alternatives considered**:
- Discord OAuth2 flow — rejected as too complex for initial release
- JWT tokens — rejected as unnecessary complexity for a single-key scenario
- Per-guild API keys — rejected as overkill for initial release

---

## Decision: Default Sticker Distribution

**Context**: Bot needs pre-installed default stickers for popular emojis.

**Decision**: Embed default sticker images in `src/main/resources/default-stickers/`. On first startup (or when a guild has no mappings), upload these images to the guild's MinIO bucket and create default mapping entries in the database.

**Rationale**: Self-contained deployment, no external dependencies for default stickers. Images are uploaded to MinIO so they can be served via presigned URLs consistently with custom stickers.

**Alternatives considered**:
- Download default stickers from a CDN on startup — rejected as it adds external dependency
- Hardcode presigned URLs to a shared bucket — rejected as it complicates per-guild isolation

---

## Decision: Async Event Processing

**Context**: Bot must handle multiple concurrent messages efficiently and respond within Discord's 3-second window.

**Decision**: Use Spring's `@Async` with a configurable thread pool (`ThreadPoolTaskExecutor`) for message event processing. Pool size configured via environment variables (default: 10 threads, max 20).

**Rationale**: Prevents blocking the JDA event dispatch thread. Configurable pool allows tuning based on server load.

**Alternatives considered**:
- Process events synchronously — rejected as it would block and cause delays under load
- Use virtual threads (Java 21+) — considered but rejected as thread pool provides better control and monitoring

---

## Decision: Docker Compose Service Architecture

**Context**: Need to define the deployment architecture.

**Decision**: Three services in docker-compose.yml:
1. `postgres` — PostgreSQL latest, with persistent volume
2. `minio` — MinIO latest, with persistent volume and default bucket creation via entrypoint script
3. `bigmoji-app` — Built from Dockerfile (multi-stage: Gradle build → JVM runtime)

**Rationale**: Standard three-tier architecture. MinIO entrypoint script creates the initial bucket structure. Multi-stage Dockerfile keeps the runtime image small.

**Alternatives considered**:
- Use external PostgreSQL/MinIO — rejected as docker-compose should be self-contained for easy deployment
- Single container with embedded database — rejected as PostgreSQL is required for production reliability
