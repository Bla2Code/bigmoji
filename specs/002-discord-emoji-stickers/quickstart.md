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
   API_KEY=your_secret_api_key_here
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
   export API_KEY=your_secret_api_key_here
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

### Upload a sticker:
```bash
curl -X POST http://localhost:8080/api/mappings \
  -H "X-API-Key: your_secret_api_key_here" \
  -F "emojiName=😊" \
  -F "guildId=123456789012345678" \
  -F "file=@/path/to/sticker.png"
```

### List mappings:
```bash
curl http://localhost:8080/api/mappings/123456789012345678 \
  -H "X-API-Key: your_secret_api_key_here"
```

### Delete a mapping:
```bash
curl -X DELETE http://localhost:8080/api/mappings/{mapping-id} \
  -H "X-API-Key: your_secret_api_key_here"
```

### Get default stickers:
```bash
curl http://localhost:8080/api/stickers/default \
  -H "X-API-Key: your_secret_api_key_here"
```

## Discord Bot Setup

1. Create a new application at https://discord.com/developers/applications
2. Create a bot under the application
3. Enable the following intents:
   - **MESSAGE_CONTENT** (Privileged)
   - **GUILD_MESSAGES**
4. Generate bot token and set as `DISCORD_TOKEN`
5. Invite bot to your server using OAuth2 URL generator with scopes: `bot`
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
- **API returns 401**: Verify `X-API-Key` header matches the `API_KEY` environment variable

## Operational Notes

- Validate API auth by calling `/api/mappings/{guildId}` with and without `X-API-Key`.
- Validate default sticker metadata via `/api/stickers/default`.
- For production, rotate `API_KEY` and restrict API ingress to trusted admins.

