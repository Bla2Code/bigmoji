import { MemoryRouter } from "react-router-dom";
import { render, screen } from "@testing-library/react";
import { authenticatedSession } from "../test/fixtures";
import { HomePage } from "./HomePage";

describe("HomePage", () => {
  it("shows product purpose, demo, and guest CTA", () => {
    render(
      <MemoryRouter>
        <HomePage session={{ status: "guest" }} />
      </MemoryRouter>,
    );

    expect(screen.getByRole("heading", { name: "Bigmoji" })).toBeInTheDocument();
    expect(screen.getByText(/replace emoji-only Discord messages/i)).toBeInTheDocument();
    expect(screen.getByRole("tab", { name: /upload/i })).toBeInTheDocument();
    expect(screen.getByRole("tab", { name: /trigger/i })).toBeInTheDocument();
    expect(screen.getByRole("tab", { name: /replace/i })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: /sign in with discord/i })).toBeVisible();
  });

  it("shows dashboard CTA when already authenticated", () => {
    render(
      <MemoryRouter>
        <HomePage session={{ status: "authenticated", ...authenticatedSession }} />
      </MemoryRouter>,
    );

    expect(screen.getByRole("link", { name: /open dashboard/i })).toHaveAttribute(
      "href",
      "/app/guilds/123456789012345678",
    );
  });
});
