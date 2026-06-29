import { MemoryRouter } from "react-router-dom";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { authenticatedSession } from "../../test/fixtures";
import type { CurrentSession } from "../../api/types";
import { AuthPanel } from "./AuthPanel";

function renderPanel(session: CurrentSession, onRetrySession = vi.fn()) {
  render(
    <MemoryRouter>
      <AuthPanel onRetrySession={onRetrySession} session={session} />
    </MemoryRouter>,
  );
  return { onRetrySession };
}

describe("AuthPanel", () => {
  it("shows Discord sign-in and register entry points for guests", () => {
    renderPanel({ status: "guest" });

    expect(screen.getByRole("link", { name: /sign in with discord/i })).toHaveAttribute(
      "href",
      "/api/auth/discord/login",
    );
    expect(screen.getByRole("link", { name: /register server/i })).toHaveAttribute(
      "href",
      "/api/auth/discord/login",
    );
  });

  it("shows authenticated dashboard CTA", () => {
    renderPanel({ status: "authenticated", ...authenticatedSession });

    expect(screen.getByRole("link", { name: /open dashboard/i })).toHaveAttribute(
      "href",
      "/app/guilds/123456789012345678",
    );
  });

  it("shows retry action for backend errors", async () => {
    const { onRetrySession } = renderPanel({
      status: "error",
      error: {
        kind: "backendUnavailable",
        message: "Backend unavailable",
      },
    });

    await userEvent.click(screen.getByRole("button", { name: /retry/i }));

    expect(onRetrySession).toHaveBeenCalledTimes(1);
  });
});
