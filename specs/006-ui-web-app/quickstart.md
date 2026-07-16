# Quickstart: Bigmoji Web UI

## Recommended Technical Choice

Use a separate React + TypeScript + Vite app in `ui/`, packaged as a Docker image that serves static files with nginx and proxies `/api` to the existing Bigmoji backend.

This keeps the UI simple:
- no Java template UI;
- no Node.js server in production;
- no direct database or MinIO access;
- no duplicate Discord auth logic in the frontend.

## Prerequisites

- Node.js 22.12+ for local UI development
- npm
- Docker and Docker Compose
- Existing Bigmoji backend running locally or in Docker

## Backend Configuration for Local UI

When the UI runs at `http://localhost:3000`, configure the backend with:

```text
UI_URL=http://localhost:3000
DISCORD_REDIRECT_URI=http://localhost:3000/api/auth/discord/callback
```

The `/api/auth/discord/callback` request reaches the UI container first, then nginx proxies it to the backend.

## Local Development

From the repository root:

```bash
cd ui
npm install
npm run dev
```

The Vite dev server should run at:

```text
http://localhost:5173
```

For local development, configure Vite to proxy `/api` to the backend:

```text
http://localhost:8080
```

## Production Build

```bash
cd ui
npm run build
npm run preview
```

Expected output:
- `dist/` contains the compiled static UI assets.

## Docker Build

```bash
docker build -t bigmoji-ui ./ui
```

## Docker Run Against Local Backend

If backend runs on the host at `http://localhost:8080`, start the UI container with:

```bash
docker run --rm \
  -p 3000:80 \
  -e BIGMOJI_BACKEND_URL=http://host.docker.internal:8080 \
  bigmoji-ui
```

Open:

```text
http://localhost:3000
```

Verify the API proxy:

```bash
curl http://localhost:3000/api/auth/me
```

Expected unauthenticated response:
- `401 Unauthorized` from backend, not an nginx 404.

## Docker Compose Shape

The eventual compose service should look like this:

```yaml
services:
  ui:
    build:
      context: ./ui
    ports:
      - "3000:80"
    environment:
      BIGMOJI_BACKEND_URL: http://backend:8080
    depends_on:
      - backend
```

The backend service should keep `UI_URL` and `DISCORD_REDIRECT_URI` aligned with the public UI URL.

## Expected UI Smoke Test

1. Open `http://localhost:3000`.
2. Confirm homepage shows:
   - sticker upload step;
   - emoji trigger step;
   - Discord-like replacement result;
   - sign-in/register panel.
3. Click sign in.
4. Complete Discord OAuth.
5. Confirm `/api/auth/me` returns the user and manageable guilds.
6. Select a guild.
7. Upload a sticker image and emoji trigger.
8. Confirm the mapping appears in the list.
9. Delete the mapping after confirmation.

## Test Commands

```bash
cd ui
npm run lint
npm run typecheck
npm test
npm run test:e2e
```

## Troubleshooting

- **Sign-in returns to backend instead of UI**: verify backend `DISCORD_REDIRECT_URI` uses the UI origin and `/api/auth/discord/callback`.
- **API calls return nginx 404**: verify `BIGMOJI_BACKEND_URL` is set and nginx has a `/api` proxy location.
- **API returns 401 after login**: verify cookies are set for the UI origin and browser requests include credentials.
- **API returns 403 for selected guild**: verify the signed-in Discord user owns the server, is Administrator, or has Manage Server permission.
- **Docker UI cannot reach backend on host**: use `host.docker.internal` on Docker Desktop or put UI and backend in the same Compose network.
