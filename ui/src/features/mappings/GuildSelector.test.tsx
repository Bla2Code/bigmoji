import { MemoryRouter } from "react-router-dom";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { guilds } from "../../test/fixtures";
import { GuildSelector } from "./GuildSelector";

describe("GuildSelector", () => {
  beforeEach(() => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(
        new Response(JSON.stringify({ url: "https://discord.com/install" }), {
          headers: { "Content-Type": "application/json" },
          status: 200,
        }),
      ),
    );
    vi.spyOn(window, "open").mockImplementation(() => null);
  });

  it("renders manageable guild links", () => {
    render(
      <MemoryRouter>
        <GuildSelector guilds={guilds} selectedGuildId={guilds[0]?.id} />
      </MemoryRouter>,
    );

    expect(screen.getByRole("link", { name: /bigmoji test server/i })).toHaveAttribute(
      "href",
      "/app/guilds/123456789012345678",
    );
  });

  it("loads install URL when no guilds are manageable", async () => {
    render(
      <MemoryRouter>
        <GuildSelector guilds={[]} />
      </MemoryRouter>,
    );

    await userEvent.click(screen.getByRole("button", { name: /add bigmoji/i }));

    const openMock = vi.mocked(window.open);
    expect(openMock).toHaveBeenCalledWith(
      "https://discord.com/install",
      "_self",
      "noopener",
    );
  });
});
