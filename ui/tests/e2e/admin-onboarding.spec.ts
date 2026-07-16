import { expect, test } from "@playwright/test";
import { mockSession, mockGuest, testSession } from "./helpers";

test.describe("admin onboarding", () => {
  test("prompts guests to sign in", async ({ page }) => {
    await mockGuest(page);

    await page.goto("/app");

    await expect(page.getByRole("heading", { name: /sign in required/i })).toBeVisible();
    await expect(page.getByRole("link", { name: /sign in with discord/i })).toHaveAttribute(
      "href",
      "/api/auth/discord/login",
    );
  });

  test("shows manageable servers for authenticated admins", async ({ page }) => {
    await mockSession(page);

    await page.goto("/app");

    await expect(page.locator(".user-chip", { hasText: "Admin" })).toBeVisible();
    await expect(page.getByRole("heading", { name: /servers/i })).toBeVisible();
    await expect(page.getByRole("link", { name: /Bigmoji Test Server/i })).toBeVisible();
  });

  test("shows no-guild install guidance", async ({ page }) => {
    await mockSession(page, { ...testSession, manageableGuilds: [] });
    await page.route("**/api/auth/discord/install-url", async (route) => {
      await route.fulfill({
        contentType: "application/json",
        body: JSON.stringify({ url: "https://discord.com/oauth2/install" }),
      });
    });

    await page.goto("/app");

    await expect(page.getByRole("heading", { name: /no manageable servers/i })).toBeVisible();
    await expect(page.getByRole("button", { name: /add bigmoji to discord/i })).toBeVisible();
  });

  test("moves protected 401 responses to expired-session state", async ({ page }) => {
    await mockSession(page);
    await page.route("**/api/stickers/default", async (route) => {
      await route.fulfill({ status: 401, body: "" });
    });
    await page.route("**/api/mappings/*", async (route) => {
      await route.fulfill({
        contentType: "application/json",
        body: JSON.stringify([]),
      });
    });

    await page.goto("/app/guilds/123456789012345678");

    await expect(page.getByRole("heading", { name: /session expired/i })).toBeVisible();
  });
});
