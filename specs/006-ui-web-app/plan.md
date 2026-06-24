# Implementation Plan: Bigmoji Web UI

**Branch**: `006-ui-web-app` | **Date**: 2026-06-24 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/006-ui-web-app/spec.md`

## Summary

Build a separate Docker-packaged web UI for Bigmoji. The UI will be a small React + TypeScript single-page application built with Vite, served as static files by nginx, and integrated with the existing Java/Spring backend through the public management API. In production the UI container will reverse-proxy `/api` to the backend so the browser uses same-origin API calls and Discord OAuth/session cookies remain simple.

The plan intentionally avoids a Java-rendered UI and avoids a full-stack React framework because this feature only needs a public homepage, Discord sign-in entry, server selection, and sticker mapping management over an existing backend API.

## Technical Context

**Language/Version**: TypeScript 5.x, React 19.x, Node.js 22.12+ for local/build tooling

**Primary Dependencies**: React, Vite, React Router, lucide-react, Vitest, Testing Library, Playwright, nginx Docker image

**Storage**: N/A for UI persistence; backend remains source of truth for auth sessions, guild access, mappings, default stickers, and uploads

**Testing**: Vitest + Testing Library for unit/component tests; Playwright for end-to-end homepage/admin flow and responsive screenshot checks

**Target Platform**: Browser-based web app served from a Linux Docker container

**Project Type**: Separate frontend SPA consuming existing backend API

**Performance Goals**:
- Homepage first meaningful render under 2 seconds on a typical broadband connection
- Built assets small enough for first load under 250 KB gzip for app JavaScript/CSS before optional image assets
- Management API UI actions show loading feedback within 100 ms
- Core views remain responsive at 360px mobile width and desktop widths

**Constraints**:
- UI MUST be deployable independently from the backend and Discord bot runtime
- UI MUST NOT access database, MinIO, bot token, Discord client secret, or backend internals directly
- Browser API calls MUST use relative `/api` paths through the UI container proxy in Docker deployments
- OAuth callback for Docker/local UI SHOULD use the UI origin, e.g. `http://localhost:3000/api/auth/discord/callback`, and be proxied to backend
- Session cookies MUST be handled by the backend; frontend only sends credentialed requests and reacts to 401/403 states
- Homepage must include a visually clear demo of upload -> emoji trigger -> Discord-like sticker replacement
- No server-side rendering or Java template engine for the UI in this feature

**Scale/Scope**:
- Public homepage
- Auth/onboarding entry using existing Discord OAuth backend endpoints
- Authenticated admin shell with manageable guild selection
- Sticker upload, mapping list, delete mapping, default stickers display
- Dockerfile, nginx proxy template, `.dockerignore`, and quickstart documentation
- No billing, analytics, team roles, local account system, or theme builder

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| **I. Code Quality - Linting & Formatting** | PASS | Use TypeScript strict mode, ESLint, and Prettier for UI files |
| **I. Code Quality - Code Reviews** | PASS | Standard PR workflow applies; UI screenshots included for visual changes |
| **I. Code Quality - Simplicity First** | PASS | Static SPA + nginx proxy avoids Java template UI, SSR, BFF, Redux, and extra runtime service |
| **I. Code Quality - Documentation** | PASS | Quickstart documents local dev, Docker run, backend env requirements, and API proxy behavior |
| **I. Code Quality - Error Handling** | PASS | Central API client maps backend failures to clear UI states; no raw stack traces exposed |
| **II. Testing - 80% Coverage** | PASS | Unit/component coverage for API client, auth state, upload validation, and key UI states |
| **II. Testing - Critical Paths 100%** | PASS | Auth gating, guild authorization UI behavior, upload/delete flows, and API error handling covered |
| **II. Testing - Test Independence** | PASS | Component tests use mocked API responses; Playwright uses controlled backend stubs or local backend |
| **III. UX Consistency** | PASS | Shared layout, form, button, empty-state, loading, and error components across homepage/admin |
| **III. Accessibility** | PASS | Keyboard navigation, labels, focus states, and screen-reader-visible errors required |
| **IV. Performance - API p95** | PASS | UI does not add backend latency; uses relative proxy and minimal client-side work |
| **IV. Performance - Asset Optimization** | PASS | Vite production build, code splitting where helpful, optimized demo assets, lazy non-critical media |
| **IV. Performance - Resource Efficiency** | PASS | Final image serves static files from nginx; no Node process in production |

## Project Structure

### Documentation (this feature)

```text
specs/006-ui-web-app/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── backend-api-usage.md
│   └── ui-route-contract.md
└── tasks.md             # Phase 2 output, created by /speckit.tasks
```

### Source Code (repository root)

```text
bigmoji/
├── src/                 # Existing Java/Spring backend remains API provider; configure UI_URL/OAuth redirect for UI origin
├── build.gradle.kts
├── Dockerfile           # Existing backend image
├── docker-compose.yml   # May add optional UI service later
└── ui/
    ├── Dockerfile
    ├── nginx.conf.template
    ├── .dockerignore
    ├── package.json
    ├── package-lock.json
    ├── index.html
    ├── vite.config.ts
    ├── tsconfig.json
    ├── eslint.config.js
    ├── playwright.config.ts
    ├── src/
    │   ├── main.tsx
    │   ├── App.tsx
    │   ├── api/
    │   │   ├── client.ts
    │   │   ├── auth.ts
    │   │   └── mappings.ts
    │   ├── components/
    │   │   ├── AppShell.tsx
    │   │   ├── Button.tsx
    │   │   ├── EmptyState.tsx
    │   │   ├── ErrorBanner.tsx
    │   │   ├── LoadingState.tsx
    │   │   └── Modal.tsx
    │   ├── pages/
    │   │   ├── HomePage.tsx
    │   │   └── AdminPage.tsx
    │   ├── features/
    │   │   ├── homepage-demo/
    │   │   │   ├── HomepageDemo.tsx
    │   │   │   └── demoData.ts
    │   │   ├── auth/
    │   │   │   ├── AuthPanel.tsx
    │   │   │   └── useSession.ts
    │   │   └── mappings/
    │   │       ├── GuildSelector.tsx
    │   │       ├── StickerUploadForm.tsx
    │   │       ├── MappingList.tsx
    │   │       └── DefaultStickerList.tsx
    │   ├── styles/
    │   │   ├── tokens.css
    │   │   └── global.css
    │   └── test/
    │       ├── setup.ts
    │       └── fixtures.ts
    ├── tests/
    │   └── e2e/
    │       ├── homepage.spec.ts
    │       └── admin-flow.spec.ts
    └── public/
        └── demo-stickers/
```

**Structure Decision**: Add a separate `ui/` application at repository root. The existing Java backend stays the API provider. The UI is independently built and deployed but can be composed with the backend in Docker for local or hosted environments.

## Post-Design Constitution Check Re-Evaluation

*Re-checked after Phase 1 design completion.*

| Principle | Status | Notes |
|-----------|--------|-------|
| **I. Code Quality - Linting & Formatting** | PASS | TypeScript strict, ESLint, Prettier, and CI scripts defined in UI app |
| **I. Code Quality - Code Reviews** | PASS | PR evidence includes test output and Playwright screenshots for UI changes |
| **I. Code Quality - Simplicity First** | PASS | React SPA with direct API client keeps surface small; no SSR, Java template layer, global state framework, or database access |
| **I. Code Quality - Documentation** | PASS | `quickstart.md` and UI README cover local dev, Docker run, and backend OAuth redirect setup |
| **I. Code Quality - Error Handling** | PASS | API contract defines normalized UI handling for 400/401/403/404/5xx and network failures |
| **II. Testing - 80% Coverage** | PASS | Coverage target applies to `ui/src`; e2e validates critical user journeys |
| **II. Testing - Critical Paths 100%** | PASS | Auth state, authorization gating, upload validation, delete confirmation, and error states are explicitly covered |
| **II. Testing - Test Independence** | PASS | Mock service fixtures isolate component tests; e2e can run against deterministic backend stubs |
| **III. UX Consistency** | PASS | Shared components and style tokens keep homepage and admin flows visually consistent |
| **III. Accessibility** | PASS | Route contract requires focus handling, labels, keyboard access, and visible validation messages |
| **IV. Performance - Response Time** | PASS | UI uses same-origin proxy and avoids client-heavy state machinery |
| **IV. Performance - Asset Optimization** | PASS | Static assets are built by Vite and served by nginx with cache headers; demo assets are optimized and lazy loaded if non-critical |
| **IV. Performance - Monitoring** | PASS | UI container exposes nginx health endpoint; backend health remains `/actuator/health` |

## Complexity Tracking

> No constitution violations identified. The chosen architecture is the simplest option that still satisfies a separate Dockerized UI integrated through the backend API.
