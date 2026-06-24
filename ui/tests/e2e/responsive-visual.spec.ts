import { expect, test } from "@playwright/test";
import { expectNoHorizontalOverflow, mockGuest } from "./helpers";

test.describe("responsive visual checks", () => {
  test("captures desktop homepage", async ({ page }, testInfo) => {
    await mockGuest(page);
    await page.setViewportSize({ width: 1366, height: 860 });
    await page.goto("/");

    await page.screenshot({
      fullPage: true,
      path: testInfo.outputPath("homepage-desktop.png"),
    });

    expect(await expectNoHorizontalOverflow(page)).toBe(false);
  });

  test("captures 360px homepage", async ({ page }, testInfo) => {
    await mockGuest(page);
    await page.setViewportSize({ width: 360, height: 780 });
    await page.goto("/");

    await page.screenshot({
      fullPage: true,
      path: testInfo.outputPath("homepage-mobile.png"),
    });

    expect(await expectNoHorizontalOverflow(page)).toBe(false);
  });
});
