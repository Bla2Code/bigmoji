import { expect, test } from "@playwright/test";
import { expectNoHorizontalOverflow, mockGuest } from "./helpers";

test.describe("homepage", () => {
  test("shows product purpose and demo on desktop", async ({ page }) => {
    await mockGuest(page);
    await page.setViewportSize({ width: 1280, height: 820 });

    await page.goto("/");

    await expect(page.getByRole("heading", { name: "Bigmoji" })).toBeVisible();
    await expect(page.getByText(/replace emoji-only Discord messages/i)).toBeVisible();
    await expect(page.getByRole("tab", { name: /upload/i })).toBeVisible();
    await expect(page.getByRole("tab", { name: /trigger/i })).toBeVisible();
    await expect(page.getByRole("tab", { name: /replace/i })).toBeVisible();
    await expect(page.getByRole("link", { name: /sign in with discord/i })).toBeVisible();
    expect(await expectNoHorizontalOverflow(page)).toBe(false);
  });

  test("keeps the demo and auth entry usable at 360px", async ({ page }) => {
    await mockGuest(page);
    await page.setViewportSize({ width: 360, height: 780 });

    await page.goto("/");

    await expect(page.getByRole("heading", { name: "Bigmoji" })).toBeVisible();
    await expect(page.getByRole("tab", { name: /upload/i })).toBeVisible();
    await expect(page.getByRole("link", { name: /sign in with discord/i })).toBeVisible();
    expect(await expectNoHorizontalOverflow(page)).toBe(false);
  });
});
