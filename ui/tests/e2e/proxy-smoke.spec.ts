import { expect, test } from "@playwright/test";

const proxyBaseUrl = process.env.PROXY_SMOKE_BASE_URL;

test.describe("nginx proxy smoke", () => {
  test.skip(!proxyBaseUrl, "Set PROXY_SMOKE_BASE_URL to test a running UI container");

  test.use({ baseURL: proxyBaseUrl });

  test("serves the SPA and forwards /api requests", async ({ page, request }) => {
    await page.goto("/app/guilds/smoke");
    await expect(page.locator("#root")).toBeVisible();

    const response = await request.get("/api/auth/me");
    expect([200, 401, 403, 500, 503]).toContain(response.status());
    expect(response.status()).not.toBe(404);
  });
});
