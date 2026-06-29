import { MemoryRouter } from "react-router-dom";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { jsonResponse, authenticatedSession, customMappings } from "./test/fixtures";
import App from "./App";
import { MappingList } from "./features/mappings/MappingList";

describe("application accessibility", () => {
  beforeEach(() => {
    vi.stubGlobal("fetch", vi.fn());
  });

  it("exposes navigation, auth state, and form labels to assistive tech", async () => {
    vi.mocked(fetch).mockResolvedValueOnce(jsonResponse(authenticatedSession));

    render(
      <MemoryRouter initialEntries={["/"]}>
        <App />
      </MemoryRouter>,
    );

    await waitFor(() => {
      expect(screen.getByRole("navigation", { name: /main navigation/i })).toBeVisible();
    });
    expect(screen.getByRole("link", { name: /^dashboard$/i })).toBeVisible();
    expect(screen.getAllByText("Admin")[0]).toBeVisible();
  });

  it("moves focus into delete confirmation dialogs", async () => {
    render(<MappingList mappings={customMappings.slice(0, 1)} onDelete={vi.fn()} />);

    const trigger = screen.getByRole("button", { name: /^delete$/i });
    trigger.focus();
    await userEvent.click(trigger);

    expect(screen.getByRole("dialog", { name: /delete mapping/i })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /close dialog/i })).toHaveFocus();

    await userEvent.keyboard("{Escape}");
    expect(trigger).toHaveFocus();
  });
});
