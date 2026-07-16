# Data Model: Bigmoji Sticker Mappings

## Entities

### StickerMapping

Represents the association between an emoji and a sticker image for a specific Discord server.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | UUID | PK, NOT NULL | Unique identifier for the mapping |
| `guildId` | String (VARCHAR 32) | NOT NULL, INDEX | Discord server (guild) identifier |
| `emojiName` | String (VARCHAR 64) | NOT NULL, INDEX | Emoji identifier (Unicode char or shortcode name like "smile") |
| `minioBucketName` | String (VARCHAR 128) | NOT NULL | MinIO bucket name (format: `bigmoji-{guildId}`) |
| `minioObjectKey` | String (VARCHAR 256) | NOT NULL | MinIO object key (format: `{uuid}.{ext}`) |
| `createdAt` | Instant | NOT NULL | Creation timestamp |
| `updatedAt` | Instant | NOT NULL | Last update timestamp |

**Indexes**:
- Composite index on `(guildId, emojiName)` for fast lookup during message processing
- Index on `guildId` for API listing operations

**Validation Rules**:
- `guildId` must be a valid Discord snowflake (numeric string)
- `emojiName` must be non-empty
- `minioBucketName` must follow MinIO naming conventions
- `minioObjectKey` must be a valid UUID with file extension

**State Transitions**: N/A (simple CRUD entity)

**Relationships**:
- Many-to-one with Guild (implicit, no separate Guild entity needed)
- No direct relationship to MinIO — bucket/object key are stored as strings

## Database Schema (PostgreSQL)

```sql
CREATE TABLE sticker_mapping (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    guild_id VARCHAR(32) NOT NULL,
    emoji_name VARCHAR(64) NOT NULL,
    minio_bucket_name VARCHAR(128) NOT NULL,
    minio_object_key VARCHAR(256) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_sticker_mapping_guild_emoji ON sticker_mapping(guild_id, emoji_name);
CREATE INDEX idx_sticker_mapping_guild ON sticker_mapping(guild_id);
```

## JPA Entity Mapping

```java
@Entity
@Table(name = "sticker_mapping", indexes = {
    @Index(name = "idx_guild_emoji", columnList = "guildId, emojiName"),
    @Index(name = "idx_guild", columnList = "guildId")
})
public class StickerMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "guild_id", nullable = false, length = 32)
    private String guildId;

    @Column(name = "emoji_name", nullable = false, length = 64)
    private String emojiName;

    @Column(name = "minio_bucket_name", nullable = false, length = 128)
    private String minioBucketName;

    @Column(name = "minio_object_key", nullable = false, length = 256)
    private String minioObjectKey;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
```

## Runtime-Only Fallback Catalog (Non-Persistent)

### LocalFallbackSticker

Represents a packaged fallback asset loaded from application resources and used only when no DB mapping exists for `{guildId, emojiName}`.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `emojiName` | String | NOT NULL | Supported fallback emoji key |
| `resourcePath` | String | NOT NULL | Classpath location of fallback image |
| `mediaType` | String | NOT NULL | MIME type for Discord attachment upload |

**Persistence Rule**:
- This structure is in-memory only and has no PostgreSQL table.

## Runtime Auth Session (Non-Persistent)

### AuthSession

Represents the signed server session issued after Discord OAuth2 login.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `userId` | String | NOT NULL | Discord user snowflake |
| `username` | String | NOT NULL | Discord username |
| `globalName` | String | Nullable | Discord display name |
| `manageableGuilds` | List<AuthorizedGuild> | NOT NULL | Guilds the user can manage according to Discord OAuth2 guild data |
| `expiresAt` | Instant | NOT NULL | Server session expiration |

### AuthorizedGuild

Represents a Discord guild available to the signed-in user in the admin UI.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | String | NOT NULL | Discord guild snowflake |
| `name` | String | NOT NULL | Discord guild display name |

**Authorization Rule**:
- A user can manage mappings for a guild only when `manageableGuilds` contains the requested `guildId`.
- `manageableGuilds` is derived from Discord OAuth2 `guilds` data and includes guilds where the user is owner, Administrator, or has Manage Server permission.
- Auth sessions are signed HttpOnly cookies and have no PostgreSQL table.

## Spring Data JPA Repository

```java
public interface StickerMappingRepository extends JpaRepository<StickerMapping, UUID> {
    
    List<StickerMapping> findByGuildIdAndEmojiName(String guildId, String emojiName);
    
    List<StickerMapping> findByGuildId(String guildId);
    
    void deleteByGuildIdAndEmojiName(String guildId, String emojiName);
    
    boolean existsByGuildId(String guildId);
}
```
