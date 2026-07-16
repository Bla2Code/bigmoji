# Research: Bigmoji Web UI

## Decision 1: Build the UI as React + TypeScript + Vite

**Decision**: Use React with TypeScript and Vite for a static single-page admin UI.

**Rationale**:
- The UI needs rich interactive states: homepage demo, Discord sign-in state, guild selection, upload progress, mapping list updates, and delete confirmation.
- React is a mainstream fit for this kind of interaction-heavy UI.
- Vite provides a lean dev server and production static build, which fits an independently deployed UI container.
- The official React docs recommend frameworks for larger React apps, but also document starting from scratch with build tools such as Vite when project constraints are not well served by full-stack frameworks.
- The official Vite docs include a React TypeScript template and production static asset build workflow.

**Alternatives considered**:
- **Java server-side UI (Thymeleaf/JSP/etc.)**: rejected because it couples the UI to the backend runtime and makes the "separate application integrated through API" requirement weaker.
- **Next.js/full-stack React**: rejected for initial scope because SSR/server functions are unnecessary; they would add another runtime service and more deployment surface.
- **Plain HTML/CSS/JS**: rejected because the admin flow has enough state and form behavior that typed components are safer and more maintainable.

**Sources**:
- React docs: https://react.dev/learn/creating-a-react-app
- Vite guide: https://vite.dev/guide/

## Decision 2: Serve production UI as static files through nginx

**Decision**: Use a multi-stage Docker image: build the UI with Node.js, then copy the compiled static assets into an nginx image.

**Rationale**:
- The production UI does not need a Node.js server.
- A static final image is smaller, simpler to operate, and aligns with the UI being a separate deployable application.
- nginx can serve the compiled assets and reverse-proxy `/api` to the existing backend.
- The official nginx Docker image supports hosting static content and environment-variable-based configuration templates.

**Alternatives considered**:
- **Run Vite preview in production**: rejected because it is a preview server, not the intended production serving model.
- **Run an Express/Node server**: rejected because no backend-for-frontend behavior is required.
- **Serve UI from the Java backend**: rejected because the feature explicitly requires separate app packaging and deployment.

**Sources**:
- Docker Node.js containerization guide: https://docs.docker.com/guides/nodejs/containerize/
- nginx official image: https://hub.docker.com/_/nginx

## Decision 3: Use nginx `/api` proxy for backend integration

**Decision**: The browser calls relative `/api/...` URLs. In Docker deployments, nginx proxies `/api` to `BIGMOJI_BACKEND_URL`.

**Rationale**:
- Keeps browser requests same-origin from the UI perspective.
- Avoids fragile cross-origin cookie/CORS setup for Discord OAuth session cookies.
- Lets the backend continue owning authentication and authorization.
- Supports local deployment as `http://localhost:3000` for UI with `/api` proxied to `http://localhost:8080`.

**Alternatives considered**:
- **Browser calls backend absolute URL directly**: rejected because credentialed cross-origin requests require stricter CORS and cookie attributes, which adds avoidable deployment complexity.
- **Duplicate auth in frontend**: rejected because the backend already owns Discord OAuth, sessions, and per-guild authorization.

## Decision 4: Keep client state lightweight

**Decision**: Use a small typed API client and React component state/hooks for the first version. Do not introduce Redux, MobX, or TanStack Query initially.

**Rationale**:
- The app has a small number of server interactions: session lookup, default stickers, list mappings, upload mapping, delete mapping, logout, install URL.
- Lightweight state is easier to understand and test.
- A data-fetching library can be added later if the admin UI grows into many screens or complex cache invalidation.

**Alternatives considered**:
- **Redux/global store**: rejected as unnecessary for the current scope.
- **TanStack Query**: useful, but deferred until the number of async resources justifies another dependency.

## Decision 5: Build the visual system with plain CSS and shared components

**Decision**: Use CSS variables, global CSS, and a small set of local shared components instead of adopting a full UI component library.

**Rationale**:
- The homepage needs a distinctive product demo, which would be constrained by a generic component kit.
- Plain CSS keeps the build simple and avoids another design abstraction.
- Shared components for buttons, modals, errors, loading states, and empty states are enough for consistency.
- lucide-react provides icon coverage without introducing a full design system.

**Alternatives considered**:
- **Tailwind CSS**: rejected for initial scope to keep configuration and generated class-heavy markup out of a small app.
- **Material UI/Ant Design/etc.**: rejected because these would make the homepage feel generic and add more dependency surface than needed.
