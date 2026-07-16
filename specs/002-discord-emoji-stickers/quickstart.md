# Quickstart: Bigmoji Discord Bot

## Prerequisites

- Java 25 JDK installed
- Docker and Docker Compose installed
- A Discord bot token (create at https://discord.com/developers/applications)

## Running with Docker Compose (Recommended)

1. **Clone the repository**:
   ```bash
   git clone <repo-url>
   cd bigmoji
   ```

2. **Create `.env` file**:
   ```bash
   cp .env.example .env
   ```
   Edit `.env` and set:
   ```
   DISCORD_TOKEN=your_discord_bot_token_here
   DISCORD_CLIENT_ID=your_discord_application_client_id
   DISCORD_CLIENT_SECRET=your_discord_application_client_secret
   DISCORD_REDIRECT_URI=http://localhost:8080/api/auth/discord/callback
   AUTH_SESSION_SECRET=replace_with_at_least_32_random_bytes
   UI_URL=http://localhost:3000
   POSTGRES_USER=bigmoji
   POSTGRES_PASSWORD=your_secure_password
   MINIO_ACCESS_KEY=minioadmin
   MINIO_SECRET_KEY=minioadminpassword
   ```

3. **Start services**:
   ```bash
   docker-compose up -d
   ```

4. **Verify health**:
   ```bash
   curl http://localhost:8080/actuator/health
   ```
   Expected response: `{"status":"UP"}`

5. **Access Swagger UI**:
   Open http://localhost:8080/swagger-ui.html in your browser

## Running Locally (Development)

1. **Start dependencies**:
   ```bash
   docker-compose up -d postgres minio
   ```

2. **Set environment variables**:
   ```bash
   export DISCORD_TOKEN=your_discord_bot_token_here
   export DISCORD_CLIENT_ID=your_discord_application_client_id
   export DISCORD_CLIENT_SECRET=your_discord_application_client_secret
   export DISCORD_REDIRECT_URI=http://localhost:8080/api/auth/discord/callback
   export AUTH_SESSION_SECRET=replace_with_at_least_32_random_bytes
   export UI_URL=http://localhost:3000
   export POSTGRES_URL=jdbc:postgresql://localhost:5432/bigmoji
   export POSTGRES_USER=bigmoji
   export POSTGRES_PASSWORD=your_secure_password
   export MINIO_ENDPOINT=localhost:9000
   export MINIO_ACCESS_KEY=minioadmin
   export MINIO_SECRET_KEY=minioadminpassword
   ```

3. **Build and run**:
   ```bash
   ./gradlew bootRun
   ```

## Testing the API

### Sign in with Discord:
Open this URL in a browser and complete Discord login:
```text
http://localhost:8080/api/auth/discord/login
```

After callback, reuse the issued `bigmoji_session` cookie for API calls. With curl, store cookies in `cookies.txt` after completing the browser flow or by using a browser/API client that can follow the redirect.

### Current session:
```bash
curl http://localhost:8080/api/auth/me \
  -b cookies.txt
```

### Upload a sticker:
```bash
curl -X POST http://localhost:8080/api/mappings \
  -b cookies.txt \
  -F "emojiName=😊" \
  -F "guildId=123456789012345678" \
  -F "file=@/path/to/sticker.png"
```

### List mappings (persisted custom mappings only):
```bash
curl http://localhost:8080/api/mappings/123456789012345678 \
  -b cookies.txt
```

### Delete a mapping:
```bash
curl -X DELETE http://localhost:8080/api/mappings/{mapping-id} \
  -b cookies.txt
```

### Get default stickers:
```bash
curl http://localhost:8080/api/stickers/default \
  -b cookies.txt
```

## Discord Bot Setup

1. Create a new application at https://discord.com/developers/applications
2. Create a bot under the application
3. Enable the following intents:
   - **MESSAGE_CONTENT** (Privileged)
   - **GUILD_MESSAGES**
4. Generate bot token and set as `DISCORD_TOKEN`
5. Invite bot to your server using `/api/auth/discord/install-url?guildId={guildId}` or the Discord OAuth2 URL generator with scope: `bot`
6. Bot permissions required:
   - **Send Messages**
   - **Manage Messages** (to delete messages)
   - **Read Message History**
   - **Attach Files**
   - **Embed Links**

## Troubleshooting

- **Bot not responding to messages**: Check that MESSAGE_CONTENT intent is enabled and bot has proper permissions in the channel
- **MinIO connection errors**: Verify `MINIO_ENDPOINT` is accessible from the container network
- **Database connection errors**: Check PostgreSQL is running and credentials match
- **API returns 401**: Sign in again through `/api/auth/discord/login`; the session cookie may be missing, invalid, or expired
- **API returns 403**: Verify the signed-in Discord user owns the guild, is Administrator, or has Manage Server permission

## Operational Notes

- Validate API auth by calling `/api/auth/me` with and without the `bigmoji_session` cookie.
- Validate guild authorization by calling `/api/mappings/{guildId}` for a guild the signed-in user can manage and one they cannot manage.
- Validate fallback sticker metadata via `/api/stickers/default`.
- For production, set a strong `AUTH_SESSION_SECRET`, enable secure cookies behind HTTPS, and configure Discord OAuth2 redirect URLs exactly in the Discord developer portal.
