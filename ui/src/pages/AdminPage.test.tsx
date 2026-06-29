import { MemoryRouter, Route, Routes } from "react-router-dom";
import { render, screen, waitFor } from "@testing-library/react";
import { ApiRequestError } from "../api/client";
import { authenticatedSession, customMappings, defaultStickers } from "../test/fixtures";
import type { CurrentSession } from "../api/types";
import { AdminPage } from "./AdminPage";

const apiMocks = vi.hoisted(() => ({
  deleteMapping: vi.fn(),
  listDefaultStickers: vi.fn(),
  listMappings: vi.fn(),
  uploadMapping: vi.fn(),
}));

vi.mock("../api/mappings", () => apiMocks);

function renderAdminPage(path: string, session: CurrentSession) {
  const onSessionExpired = vi.fn();

  render(
    <MemoryRouter initialEntries={[path]}>
      <Routes>
        <Route
          path="/app"
          element={<AdminPage onSessionExpired={onSessionExpired} session={session} />}
        />
        <Route
          path="/app/guilds/:guildId"
          element={<AdminPage onSessionExpired={onSessionExpired} session={session} />}
        />
      </Routes>
    </MemoryRouter>,
  );

  return { onSessionExpired };
}

describe("AdminPage", () => {
  beforeEach(() => {
    apiMocks.listDefaultStickers.mockResolvedValue(defaultStickers);
    apiMocks.listMappings.mockResolvedValue(customMappings);
    apiMocks.deleteMapping.mockResolvedValue(undefined);
    apiMocks.uploadMapping.mockResolvedValue(customMappings[0]);
  });

  it("prompts guests to sign in", () => {
    renderAdminPage("/app", { status: "guest" });

    expect(screen.getByRole("heading", { name: /sign in required/i })).toBeVisible();
    expect(screen.getByRole("link", { name: /sign in with discord/i })).toHaveAttribute(
      "href",
      "/api/auth/discord/login",
    );
  });

  it("shows server selection before a guild is selected", () => {
    renderAdminPage("/app", { status: "authenticated", ...authenticatedSession });

    expect(screen.getByRole("heading", { name: /servers/i })).toBeVisible();
    expect(screen.getByRole("heading", { name: /select a server/i })).toBeVisible();
    expect(screen.getByRole("link", { name: /bigmoji test server/i })).toBeVisible();
  });

  it("loads defaults and custom mappings for selected guilds", async () => {
    renderAdminPage("/app/guilds/123456789012345678", {
      status: "authenticated",
      ...authenticatedSession,
    });

    await waitFor(() => {
      expect(apiMocks.listDefaultStickers).toHaveBeenCalledTimes(1);
      expect(apiMocks.listMappings).toHaveBeenCalledWith("123456789012345678");
    });

    expect(screen.getByRole("heading", { name: /upload custom sticker/i })).toBeVisible();
    expect(screen.getByRole("heading", { name: /default stickers/i })).toBeVisible();
    expect(screen.getByText("2 custom stickers")).toBeVisible();
  });

  it("hides upload controls for unauthorized guild ids", () => {
    renderAdminPage("/app/guilds/not-authorized", {
      status: "authenticated",
      ...authenticatedSession,
    });

    expect(screen.getByRole("heading", { name: /access denied/i })).toBeVisible();
    expect(screen.queryByRole("button", { name: /upload mapping/i })).not.toBeInTheDocument();
  });

  it("marks the session expired when protected data returns 401", async () => {
    apiMocks.listDefaultStickers.mockRejectedValueOnce(
      new ApiRequestError({
        kind: "unauthenticated",
        message: "Sign in again",
        status: 401,
      }),
    );

    const { onSessionExpired } = renderAdminPage("/app/guilds/123456789012345678", {
      status: "authenticated",
      ...authenticatedSession,
    });

    await waitFor(() => {
      expect(onSessionExpired).toHaveBeenCalledWith(
        expect.objectContaining({ kind: "unauthenticated" }),
      );
    });
  });
});
