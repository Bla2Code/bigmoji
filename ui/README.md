# Bigmoji UI

React + TypeScript + Vite single-page app for the Bigmoji homepage and admin
mapping workflow.

## Local Development

```bash
npm install
npm run dev
```

Vite serves the app at `http://localhost:5173` and proxies `/api` to
`BIGMOJI_BACKEND_URL` or `http://localhost:8080`.

For local Discord OAuth through the UI origin, configure the backend with:

```text
UI_URL=http://localhost:5173
DISCORD_REDIRECT_URI=http://localhost:5173/api/auth/discord/callback
```

## Checks

```bash
npm run lint
npm run typecheck
npm test
npm run test:e2e
```

The e2e suite mocks backend API responses for browser-flow coverage. To smoke
test a running nginx UI container, set `PROXY_SMOKE_BASE_URL` and run the proxy
spec:

```bash
PROXY_SMOKE_BASE_URL=http://localhost:3000 npm run test:e2e -- proxy-smoke
```

## Docker

Build the standalone UI image:

```bash
docker build -t bigmoji-ui ./ui
```

Run it against a backend on the host:

```bash
docker run --rm \
  -p 3000:80 \
  -e BIGMOJI_BACKEND_URL=http://host.docker.internal:8080 \
  bigmoji-ui
```

The container serves the SPA and proxies browser `/api` calls to
`BIGMOJI_BACKEND_URL`. The health endpoint is:

```text
http://localhost:3000/healthz
```

For Docker-local OAuth, configure the backend with:

```text
UI_URL=http://localhost:3000
DISCORD_REDIRECT_URI=http://localhost:3000/api/auth/discord/callback
```

## Troubleshooting

- Sign-in returns to the backend origin: align `DISCORD_REDIRECT_URI` with the UI
  origin.
- `/api` returns nginx 404: verify `BIGMOJI_BACKEND_URL` is set and reachable.
- Session is lost after login: verify backend cookies are set for the UI origin.
- Selected guild returns 403: verify the Discord user can manage that server.
