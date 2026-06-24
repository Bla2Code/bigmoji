import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { jsonResponse, authenticatedSession } from "../../test/fixtures";
import { useSession } from "./useSession";

function SessionHarness() {
  const session = useSession();

  return (
    <div>
      <span data-testid="status">{session.session.status}</span>
      <button onClick={() => void session.refresh()}>refresh</button>
      <button onClick={() => void session.signOut()}>logout</button>
      <button onClick={() => session.markExpired()}>expire</button>
    </div>
  );
}

describe("useSession", () => {
  beforeEach(() => {
    vi.stubGlobal("fetch", vi.fn());
  });

  it("loads authenticated session state", async () => {
    vi.mocked(fetch).mockResolvedValueOnce(jsonResponse(authenticatedSession));

    render(<SessionHarness />);

    await expect(screen.findByTestId("status")).resolves.toHaveTextContent(
      "authenticated",
    );
  });

  it("maps initial 401 to guest state", async () => {
    vi.mocked(fetch).mockResolvedValueOnce(new Response(null, { status: 401 }));

    render(<SessionHarness />);

    await expect(screen.findByTestId("status")).resolves.toHaveTextContent("guest");
  });

  it("logs out through the backend and clears local session", async () => {
    vi.mocked(fetch)
      .mockResolvedValueOnce(jsonResponse(authenticatedSession))
      .mockResolvedValueOnce(new Response(null, { status: 204 }));

    render(<SessionHarness />);
    await screen.findByText("authenticated");

    await userEvent.click(screen.getByRole("button", { name: "logout" }));

    await waitFor(() => {
      expect(screen.getByTestId("status")).toHaveTextContent("guest");
    });
  });

  it("supports explicit expired-session transitions", async () => {
    vi.mocked(fetch).mockResolvedValueOnce(jsonResponse(authenticatedSession));

    render(<SessionHarness />);
    await screen.findByText("authenticated");

    await userEvent.click(screen.getByRole("button", { name: "expire" }));

    expect(screen.getByTestId("status")).toHaveTextContent("expired");
  });
});
