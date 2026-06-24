#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
image_name="${IMAGE_NAME:-bigmoji-ui-smoke}"
port="${BIGMOJI_UI_SMOKE_PORT:-3000}"
backend_url="${BIGMOJI_BACKEND_URL:-http://host.docker.internal:8080}"

docker build -t "${image_name}" "${repo_root}/ui"

container_id="$(
  docker run -d --rm \
    -p "${port}:80" \
    -e BIGMOJI_BACKEND_URL="${backend_url}" \
    "${image_name}"
)"

cleanup() {
  docker stop "${container_id}" >/dev/null
}
trap cleanup EXIT

for _ in {1..30}; do
  if curl -fsS "http://localhost:${port}/healthz" >/dev/null; then
    break
  fi
  sleep 1
done

curl -fsS "http://localhost:${port}/healthz" >/dev/null

status="$(
  curl -sS -o /tmp/bigmoji-ui-proxy-smoke.out \
    -w "%{http_code}" \
    "http://localhost:${port}/api/auth/me" || true
)"

if [[ "${status}" == "404" || "${status}" == "000" ]]; then
  echo "Expected /api/auth/me to reach backend proxy, got HTTP ${status}" >&2
  exit 1
fi

echo "UI smoke passed on http://localhost:${port}; /api/auth/me returned ${status}."
