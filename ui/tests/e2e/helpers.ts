import type { Page, Route } from "@playwright/test";

export const testPrimaryGuild = {
  id: "123456789012345678",
  name: "Bigmoji Test Server",
};

export const testGuilds = [
  testPrimaryGuild,
  { id: "234567890123456789", name: "Launch Crew" },
];

export const testSession = {
  userId: "111111111111111111",
  username: "admin",
  globalName: "Admin",
  manageableGuilds: testGuilds,
  expiresAt: "2026-06-24T20:00:00Z",
};

export const testDefaults = [
  {
    emojiName: "😊",
    shortcodeName: "smile",
    description: "Smiling face",
  },
  {
    emojiName: "🔥",
    shortcodeName: "fire",
    description: "Hype response",
  },
];

export const testMappings = [
  {
    id: "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    guildId: testPrimaryGuild.id,
    emojiName: "😊",
    minioBucketName: "bigmoji-123456789012345678",
    minioObjectKey: "internal/smile.png",
    createdAt: "2026-06-24T16:00:00Z",
    updatedAt: "2026-06-24T16:00:00Z",
  },
];

export async function mockGuest(page: Page) {
  await page.route("**/api/auth/me", async (route) => {
    await route.fulfill({ status: 401, body: "" });
  });
}

export async function mockSession(page: Page, session = testSession) {
  await page.route("**/api/auth/me", async (route) => {
    await fulfillJson(route, session);
  });
}

export async function mockMappingData(page: Page) {
  const mappings = [...testMappings];

  await page.route("**/api/stickers/default", async (route) => {
    await fulfillJson(route, testDefaults);
  });

  await page.route(`**/api/mappings/${testPrimaryGuild.id}`, async (route) => {
    await fulfillJson(route, mappings);
  });

  await page.route("**/api/mappings", async (route) => {
    if (route.request().method() !== "POST") {
      await route.fallback();
      return;
    }

    const createdMapping = {
      id: "created-mapping-0000-0000-000000000000",
      guildId: testPrimaryGuild.id,
      emojiName: "🔥",
      minioBucketName: "bigmoji-hidden",
      minioObjectKey: "hidden/fire.png",
      createdAt: "2026-06-24T17:00:00Z",
      updatedAt: "2026-06-24T17:00:00Z",
    };

    mappings.push({
      ...createdMapping,
    });

    await fulfillJson(route, createdMapping, 201);
  });

  await page.route("**/api/mappings/*", async (route: Route) => {
    if (route.request().method() !== "DELETE") {
      await route.fallback();
      return;
    }

    const id = route.request().url().split("/").at(-1);
    const index = mappings.findIndex((mapping) => mapping.id === id);
    if (index >= 0) {
      mappings.splice(index, 1);
    }

    await route.fulfill({ status: 204, body: "" });
  });
}

export async function expectNoHorizontalOverflow(page: Page) {
  await page.waitForLoadState("networkidle");
  const hasOverflow = await page.evaluate(() => {
    return document.documentElement.scrollWidth > document.documentElement.clientWidth;
  });
  return hasOverflow;
}

async function fulfillJson(route: Route, json: unknown, status = 200) {
  await route.fulfill({
    status,
    contentType: "application/json",
    body: JSON.stringify(json),
  });
}
