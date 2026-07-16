# Bigmoji

[![Java 21](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot 4](https://img.shields.io/badge/Spring_Boot-4.0-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![React 19](https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=black)](https://react.dev/)
[![License](https://img.shields.io/github/license/Bla2Code/bigmoji)](LICENSE)

Turn a single emoji message into a large, custom image in Discord.

Bigmoji watches server text channels for messages containing exactly one emoji. If the emoji has a mapping, Bigmoji replaces the original message with the mapped image. Server administrators manage mappings through a web interface, while PostgreSQL stores metadata and MinIO stores image files.

> Bigmoji is self-hosted. You need a Discord application and a machine capable of running Docker Compose.

## Table of contents

- [Features](#features)
- [How it works](#how-it-works)
- [Requirements](#requirements)
- [Discord setup](#discord-setup)
- [Quick start with Docker Compose](#quick-start-with-docker-compose)
- [Start using Bigmoji](#start-using-bigmoji)
- [Configuration](#configuration)
- [Local development](#local-development)
- [Architecture](#architecture)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)

## Features

- Per-server emoji-to-image mappings
- Unicode, shortcode, and custom Discord emoji triggers
- Multiple images per trigger with random selection
- Nine built-in fallback stickers
- Discord OAuth2 administration with server permission checks
- Web UI for uploading, previewing, and deleting mappings
- Webhook delivery using the original author's display name and avatar
- Direct bot-message fallback when webhooks are unavailable
- PostgreSQL metadata storage and MinIO object storage
- Docker images for the Java backend and React frontend
- Health endpoints, Swagger UI, and diagnostic logging

## How it works

1. A member sends a message containing one emoji and no other text.
2. Bigmoji normalizes the emoji and looks for a mapping belonging to that Discord server.
3. If there is no custom mapping, Bigmoji checks its built-in stickers.
4. Bigmoji posts the selected image in the same channel, preferably through a webhook using the member's display name and avatar.
5. After the image is sent, Bigmoji deletes the original emoji message.

Messages containing text, multiple emoji, direct messages, bot messages, and emoji without a mapping are left unchanged.

## Requirements

For the recommended Docker setup:

- Docker Engine with Docker Compose v2
- A Discord account that can create applications
- `Manage Server` or `Administrator` on the Discord server where the bot will be installed

For development outside Docker:

- Java 21
- Node.js 22.12 or newer and npm
- PostgreSQL 17
- MinIO

## Discord setup

### 1. Create the Discord application and bot

1. Open the [Discord Developer Portal](https://discord.com/developers/applications) and select **New Application**.
2. Give the application a name, for example `Bigmoji`.
3. Open **Bot** in the application sidebar and select **Add Bot** if Discord has not created one automatically.
4. Under **Privileged Gateway Intents**, enable **Message Content Intent**. Bigmoji needs it to read and recognize emoji messages.
5. Reset or copy the bot token and store it as `DISCORD_TOKEN`. Treat this token as a password and never commit it.
6. Open **OAuth2** and copy the **Client ID** and **Client Secret** for `DISCORD_CLIENT_ID` and `DISCORD_CLIENT_SECRET`.

### 2. Register the OAuth2 callback

In **OAuth2 > General > Redirects**, add the callback used by your deployment:

```text
http://localhost:3000/api/auth/discord/callback
```

This is the correct callback for the full local Docker Compose stack. In production, replace the origin with your public HTTPS URL. The Developer Portal value must exactly match `DISCORD_REDIRECT_URI`, including the scheme, host, port, and path.

### 3. Configure bot permissions

Give the bot the following permissions on every channel where Bigmoji should work:

| Permission | Why it is needed |
| --- | --- |
| View Channel | Receive events from the channel |
| Send Messages | Send the replacement image when webhook delivery is unavailable |
| Manage Messages | Delete the original emoji message |
| Embed Links | Support normal rich-message delivery |
| Attach Files | Upload the mapped image |
| Read Message History | Work reliably with messages in the channel |
| Manage Webhooks | Create and use a channel webhook with the original author's name and avatar |

The recommended Discord permissions integer is `536996864`. Set it before generating an install link:

```dotenv
DISCORD_BOT_PERMISSIONS=536996864
```

The project's default value is `536996864`, including **Manage Webhooks** so replacement images retain the original author's name and avatar. If you override this value, keep **Manage Webhooks** enabled or Bigmoji will fall back to its own identity.

For tighter security, grant these permissions only in the categories or text channels where Bigmoji is intended to operate. A channel-level deny overrides a server role allow, so check the channel's **Permissions** page if the bot can work in one channel but not another. Administrator permission is not required or recommended for the bot.

### 4. Add the bot to a server or channel

After Bigmoji is running:

1. Open the web UI and sign in with Discord.
2. Select a server you own or can manage.
3. Use the install action shown by Bigmoji and choose that server in Discord's authorization screen.
4. Authorize the requested permissions.
5. To limit the bot to specific channels, open **Server Settings > Roles > Bigmoji** and remove broad channel access, then open each allowed channel's **Edit Channel > Permissions** page and grant the Bigmoji role the permissions listed above.

You can also build an installation URL manually in **OAuth2 > URL Generator**: select the `bot` scope, select the permissions from the table, copy the generated URL, and open it in a browser.

## Quick start with Docker Compose

### 1. Clone and configure

```bash
git clone https://github.com/Bla2Code/bigmoji.git
cd bigmoji
cp .env.example .env
```

Edit `.env` and provide at least the following values:

```dotenv
DISCORD_TOKEN=your_bot_token
DISCORD_CLIENT_ID=your_application_id
DISCORD_CLIENT_SECRET=your_client_secret
DISCORD_REDIRECT_URI=http://localhost:3000/api/auth/discord/callback
DISCORD_BOT_PERMISSIONS=536996864

AUTH_SESSION_SECRET=replace_with_at_least_32_random_bytes
UI_URL=http://localhost:3000

POSTGRES_URL=jdbc:postgresql://postgres:5432/bigmoji
POSTGRES_USER=bigmoji
POSTGRES_PASSWORD=replace_with_a_strong_password

MINIO_ENDPOINT=http://minio:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=replace_with_a_strong_password
MINIO_BUCKET_PREFIX=bigmoji-
```

Generate a suitable session secret with, for example:

```bash
openssl rand -base64 32
```

Do not commit `.env`. The repository ignores it by default.

> The `postgres` and `minio` hostnames above are Docker Compose service names. Use `localhost` instead only when the backend itself runs directly on your host.

### 2. Start the complete stack

```bash
docker compose --profile ui up -d --build
```

Check the services:

```bash
docker compose ps
curl http://localhost:8080/actuator/health
curl http://localhost:3000/healthz
```

| Service | URL |
| --- | --- |
| Bigmoji UI | <http://localhost:3000> |
| Backend API | <http://localhost:8080> |
| Health check | <http://localhost:8080/actuator/health> |
| Swagger UI | <http://localhost:8080/swagger-ui.html> |
| MinIO Console | <http://localhost:9001> |

To stop the stack:

```bash
docker compose --profile ui down
```

## Start using Bigmoji

1. Open <http://localhost:3000>.
2. Sign in with Discord. Bigmoji only lists servers where you are the owner or have `Manage Server`/`Administrator`.
3. Select a server and install the bot if it is not already present.
4. Upload a PNG, JPEG, or WebP image no larger than 512 KB.
5. Enter an emoji trigger, such as `🔥`, `:fire:`, or a custom server emoji.
6. In an allowed Discord text channel, send only that emoji.

If several images use the same normalized trigger, Bigmoji chooses one at random. Custom mappings take precedence over built-in stickers.

## Configuration

Bigmoji reads configuration from environment variables. See [`.env.example`](.env.example) for a copyable template.

| Variable | Default | Description |
| --- | --- | --- |
| `DISCORD_TOKEN` | empty | Discord bot token |
| `DISCORD_CLIENT_ID` | empty | Discord application/client ID |
| `DISCORD_CLIENT_SECRET` | empty | Discord OAuth2 client secret |
| `DISCORD_REDIRECT_URI` | `http://localhost:8080/api/auth/discord/callback` | Exact registered OAuth2 callback |
| `DISCORD_BOT_PERMISSIONS` | `536996864` | Permissions integer used by the generated bot install URL, including Manage Webhooks |
| `UI_URL` | `http://localhost:3000` | Redirect destination after sign-in |
| `AUTH_SESSION_SECRET` | insecure development value | Signing secret; use at least 32 random bytes in production |
| `AUTH_SESSION_TTL` | `PT8H` | Session lifetime as an ISO-8601 duration |
| `AUTH_SECURE_COOKIES` | `false` | Set to `true` behind production HTTPS |
| `POSTGRES_URL` | `jdbc:postgresql://localhost:5432/bigmoji` | JDBC connection URL |
| `POSTGRES_USER` | `bigmoji` | PostgreSQL user |
| `POSTGRES_PASSWORD` | `bigmoji` | PostgreSQL password |
| `MINIO_ENDPOINT` | `http://localhost:9000` | MinIO API endpoint |
| `MINIO_ACCESS_KEY` | `minioadmin` | MinIO access key |
| `MINIO_SECRET_KEY` | `minioadminpassword` | MinIO secret key |
| `MINIO_BUCKET_PREFIX` | `bigmoji-` | Prefix for per-server buckets |
| `ASYNC_CORE_POOL_SIZE` | `10` | Core Discord event worker count |
| `ASYNC_MAX_POOL_SIZE` | `20` | Maximum Discord event worker count |

For production, use HTTPS, set `AUTH_SECURE_COOKIES=true`, rotate every example credential, keep PostgreSQL and MinIO off the public network, and back up both data stores together.

## Local development

Start only the infrastructure:

```bash
docker compose up -d postgres minio
```

Set host-local endpoints in `.env` or your shell, then run the backend:

```dotenv
POSTGRES_URL=jdbc:postgresql://localhost:5432/bigmoji
MINIO_ENDPOINT=http://localhost:9000
```

```bash
./gradlew bootRun
```

Run the UI in another terminal:

```bash
cd ui
npm ci
npm run dev
```

Vite serves the UI at <http://localhost:5173> and proxies `/api` to the backend. For this setup, register and configure `http://localhost:5173/api/auth/discord/callback`, and set `UI_URL=http://localhost:5173`.

### Checks

```bash
./gradlew test
./gradlew spotlessCheck

cd ui
npm run lint
npm run typecheck
npm test
npm run build
```

End-to-end tests are available through `npm run test:e2e` after Playwright's browser dependencies are installed.

## Architecture

```text
Discord Gateway
      |
      v
Spring Boot backend ---- PostgreSQL (mapping metadata)
      |        |
      |        +-------- MinIO (image files)
      |
      +----------------- Discord webhooks / message API
      ^
      |
React UI behind nginx -- Discord OAuth2
```

The backend uses JDA for Discord events, Spring MVC for the management API, and an in-memory mapping cache. The frontend is a React and TypeScript single-page application served by nginx in the container image.

Additional project documentation is available in [`wiki/`](wiki/) and feature specifications are stored in [`specs/`](specs/).

## Troubleshooting

### The bot does not react to messages

- Confirm **Message Content Intent** is enabled in the Discord Developer Portal.
- Confirm the token belongs to the same Discord application.
- Check that the bot can **View Channel** and **Read Message History**.
- Inspect the backend logs with `docker compose logs -f bigmoji-app`.

### The image appears under the bot's name

This is the expected fallback when Bigmoji cannot create or use a webhook. Grant **Manage Webhooks** in that channel and use `DISCORD_BOT_PERMISSIONS=536996864` for future installations.

### The original emoji message remains

Grant **Manage Messages** in the affected channel. Also verify that the uploaded asset is available: Bigmoji only deletes the source message after successfully sending the image.

### Discord OAuth2 reports an invalid redirect URI

Make sure `DISCORD_REDIRECT_URI` exactly matches an entry in **Developer Portal > OAuth2 > General > Redirects**. Use the UI origin (`:3000` for Docker or `:5173` for Vite), not the backend origin, when the UI proxy handles `/api`.

### Docker cannot connect to PostgreSQL or MinIO

When the backend runs in Compose, use `postgres` and `minio` as hostnames. When it runs directly on your machine, use `localhost`.

## Contributing

Issues and pull requests are welcome. Before opening a pull request:

1. Keep the change focused and add tests where appropriate.
2. Run the backend and UI checks listed above.
3. Update the README or wiki when configuration or behavior changes.
4. Describe the user impact and validation performed in the pull request.

## License

Bigmoji is available under the [Apache License 2.0](LICENSE).
