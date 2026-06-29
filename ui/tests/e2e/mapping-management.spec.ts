import { expect, test } from "@playwright/test";
import { mockMappingData, mockSession, testSession } from "./helpers";

test.describe("mapping management", () => {
  test("uploads and deletes custom mappings", async ({ page }) => {
    await mockSession(page);
    await mockMappingData(page);

    await page.goto("/app/guilds/123456789012345678");

    await expect(
      page.getByRole("heading", { name: /upload custom sticker/i }),
    ).toBeVisible();

    await page.getByLabel(/emoji trigger/i).fill("🔥");
    await page.getByLabel(/sticker image/i).setInputFiles({
      name: "fire.png",
      mimeType: "image/png",
      buffer: Buffer.from("fake-png"),
    });
    await page.getByRole("button", { name: /upload mapping/i }).click();

    await expect(page.getByText("Mapping created")).toBeVisible();
    await expect(page.getByText("🔥").first()).toBeVisible();
    await expect(
      page.getByRole("img", { name: /uploaded sticker for 🔥/i }),
    ).toBeVisible();

    await page
      .getByRole("button", { name: /^delete$/i })
      .first()
      .click();
    await expect(page.getByRole("dialog", { name: /delete mapping/i })).toBeVisible();
    await page.getByRole("button", { name: /delete mapping/i }).click();

    await expect(page.getByRole("dialog", { name: /delete mapping/i })).toBeHidden();
  });

  test("blocks guilds outside the authenticated session", async ({ page }) => {
    await mockSession(page, {
      ...testSession,
      manageableGuilds: [{ id: "authorized", name: "Authorized Server" }],
    });

    await page.goto("/app/guilds/not-authorized");

    await expect(page.getByRole("heading", { name: /access denied/i })).toBeVisible();
    await expect(page.getByRole("button", { name: /upload mapping/i })).toHaveCount(0);
  });

  test("renders preview fallbacks without mobile overflow", async ({ page }) => {
    await page.setViewportSize({ width: 360, height: 760 });
    await mockSession(page);
    await mockMappingData(page);

    await page.goto("/app/guilds/123456789012345678");

    await expect(
      page.getByRole("img", { name: /custom emoji :party_blob:/i }),
    ).toBeVisible();
    await expect(page.getByText(/preview unavailable/i)).toBeVisible();

    const hasOverflow = await page.evaluate(() => {
      return (
        document.documentElement.scrollWidth > document.documentElement.clientWidth
      );
    });
    expect(hasOverflow).toBe(false);
  });
});
