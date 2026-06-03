# Implementation Plan: Discord Emoji-to-Sticker Bot (Bigmoji)

**Branch**: `002-discord-emoji-stickers` | **Date**: 2026-06-03 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/002-discord-emoji-stickers/spec.md`

## Summary

Build a Discord bot (Bigmoji) that detects single-emoji messages in Discord channels, deletes them, and posts a matching large sticker image from a per-server configurable collection. The bot is a Spring Boot 4.0.6 application using JDA for Discord integration, PostgreSQL for mapping storage, MinIO for image storage, and exposes a REST API for sticker management.

## Technical Context

**Language/Version**: Java 25

**Primary Dependencies**: Spring Boot 4.0.6, JDA (Java Discord API), Hibernate/JPA with Spring Data JPA, MinIO Java SDK, Spring Boot Actuator, SpringDoc OpenAPI 3 (Swagger UI)

**Storage**: PostgreSQL (relational data), MinIO (S3-compatible object storage for sticker images)

**Testing**: JUnit 5, Mockito, Testcontainers (PostgreSQL + MinIO)

**Target Platform**: Linux server (Docker container)

**Project Type**: Web-service + Discord bot (backend-only)

**Performance Goals**: 
- Message processing within 2 seconds (SC-001)
- API responses within 200ms at p95 (Constitution IV)
- Support 100 concurrent Discord servers (SC-005)
- Discord interaction response within 3-second window (Constitution IV)

**Constraints**:
- Must respect Discord rate limits for message deletion and sending
- Sticker images sent as attachments (not Discord's official sticker API) for simplicity
- Single emoji detection must be accurate (99% per SC-002, SC-003)
- Memory-loaded mappings for fast lookup on startup

**Scale/Scope**: 
- 100 concurrent Discord servers
- Multiple stickers per emoji (random selection)
- Per-guild isolated mappings
- Default sticker set for 5 popular emojis

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| **I. Code Quality - Linting & Formatting** | ✅ PASS | Configure Spotless plugin for Java formatting (google-java-format) |
| **I. Code Quality - Code Reviews** | ✅ PASS | Standard PR workflow |
| **I. Code Quality - Simplicity First** | ✅ PASS | YAGNI: No OAuth2 initially, API key only. No Discord sticker API, use attachments. |
| **I. Code Quality - Documentation** | ✅ PASS | OpenAPI/Swagger for REST API, Javadoc for public interfaces |
| **I. Code Quality - Error Handling** | ✅ PASS | Explicit error handling with clear messages, no silent failures |
| **II. Testing - 80% Coverage** | ✅ PASS | JUnit 5 + Mockito for unit, Testcontainers for integration |
| **II. Testing - Critical Paths 100%** | ✅ PASS | Auth interceptor, emoji detection, mapping lookup, MinIO upload |
| **II. Testing - Test Independence** | ✅ PASS | Each test self-contained with Testcontainers |
| **III. UX Consistency** | ✅ PASS | Consistent error responses, clear API error messages |
| **IV. Performance - 200ms API p95** | ✅ PASS | In-memory mapping cache, async event processing |
| **IV. Performance - 3s Discord window** | ✅ PASS | @Async listener with thread pool, presigned URLs for fast retrieval |
| **IV. Performance - Resource Efficiency** | ✅ PASS | Connection pooling, presigned URL caching |

## Project Structure

### Documentation (this feature)

```text
specs/002-discord-emoji-stickers/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
└── tasks.md             # Phase 2 output
```

### Source Code (repository root)

```text
bigmoji/
├── build.gradle.kts
├── settings.gradle.kts
├── Dockerfile
├── docker-compose.yml
├── .env.example
├── src/
│   ├── main/
│   │   ├── java/com/bigmoji/
│   │   │   ├── BigmojiApplication.java
│   │   │   ├── config/
│   │   │   │   ├── JdaConfig.java
│   │   │   │   ├── MinioConfig.java
│   │   │   │   ├── ApiKeyInterceptor.java
│   │   │   │   └── AsyncConfig.java
│   │   │   ├── discord/
│   │   │   │   ├── EmojiMessageListener.java
│   │   │   │   └── StickerSenderService.java
│   │   │   ├── emoji/
│   │   │   │   ├── EmojiDetector.java
│   │   │   │   └── EmojiNormalizer.java
│   │   │   ├── sticker/
│   │   │   │   ├── StickerMappingService.java
│   │   │   │   ├── StickerMappingCache.java
│   │   │   │   └── DefaultStickerInitializer.java
│   │   │   ├── api/
│   │   │   │   ├── StickerMappingController.java
│   │   │   │   ├── dto/
│   │   │   │   │   ├── MappingResponse.java
│   │   │   │   │   ├── MappingUploadRequest.java
│   │   │   │   │   └── ErrorResponse.java
│   │   │   │   └── exception/
│   │   │   │       └── GlobalExceptionHandler.java
│   │   │   ├── storage/
│   │   │   │   └── MinioStorageService.java
│   │   │   └── domain/
│   │   │       ├── entity/
│   │   │       │   └── StickerMapping.java
│   │   │       └── repository/
│   │   │           └── StickerMappingRepository.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-docker.yml
│   │       └── default-stickers/
│   │           ├── smile.png
│   │           ├── heart.png
│   │           ├── party.png
│   │           ├── thumbsup.png
│   │           └── fire.png
│   └── test/
│       ├── java/com/bigmoji/
│       │   ├── discord/
│       │   │   └── EmojiMessageListenerTest.java
│       │   ├── emoji/
│       │   │   └── EmojiDetectorTest.java
│       │   ├── sticker/
│       │   │   └── StickerMappingServiceTest.java
│       │   ├── api/
│       │   │   └── StickerMappingControllerTest.java
│       │   └── storage/
│       │       └── MinioStorageServiceTest.java
│       └── resources/
│           └── test-sticker.png
└── gradle/
    └── wrapper/
```

**Structure Decision**: Single-project Spring Boot application with modular package structure. Discord listener, emoji detection, sticker management, REST API, and storage are separated into distinct packages.

## Post-Design Constitution Check Re-Evaluation

*Re-checked after Phase 1 design completion.*

| Principle | Status | Notes |
|-----------|--------|-------|
| **I. Code Quality - Linting & Formatting** | ✅ PASS | Spotless + google-java-format configured in build.gradle.kts |
| **I. Code Quality - Code Reviews** | ✅ PASS | Standard PR workflow |
| **I. Code Quality - Simplicity First** | ✅ PASS | YAGNI applied: API key auth only, attachment-based stickers, ConcurrentHashMap cache |
| **I. Code Quality - Documentation** | ✅ PASS | OpenAPI/Swagger auto-generated, Javadoc for public interfaces, API contract documented |
| **I. Code Quality - Error Handling** | ✅ PASS | GlobalExceptionHandler with consistent error format, no silent failures |
| **II. Testing - 80% Coverage** | ✅ PASS | Unit tests per service, integration tests with Testcontainers (PostgreSQL + MinIO) |
| **II. Testing - Critical Paths 100%** | ✅ PASS | Auth interceptor, emoji detection regex, mapping cache lookup, MinIO upload/download |
| **II. Testing - Test Independence** | ✅ PASS | Testcontainers provides isolated DB/MinIO per test class |
| **III. UX Consistency** | ✅ PASS | Consistent JSON error format, clear messages, no raw stack traces exposed |
| **IV. Performance - 200ms API p95** | ✅ PASS | In-memory ConcurrentHashMap for reads, connection pooling for DB/MinIO |
| **IV. Performance - 3s Discord window** | ✅ PASS | @Async with ThreadPoolTaskExecutor, presigned URLs (no download step) |
| **IV. Performance - Resource Efficiency** | ✅ PASS | HikariCP connection pool, MinIO client singleton, stable memory footprint |
| **IV. Performance - Monitoring** | ✅ PASS | Spring Boot Actuator /actuator/health, structured JSON logs to stdout |

## Complexity Tracking

> No constitution violations identified. All principles satisfied with chosen architecture.
