import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { HomepageDemo } from "./HomepageDemo";

describe("HomepageDemo", () => {
  it("renders upload, emoji trigger, and replacement steps", async () => {
    render(<HomepageDemo />);

    expect(screen.getByLabelText(/sticker upload preview/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/discord replacement preview/i)).toBeInTheDocument();
    expect(screen.getByRole("tab", { name: /upload/i })).toHaveAttribute(
      "aria-selected",
      "true",
    );

    await userEvent.click(screen.getByRole("tab", { name: /trigger/i }));
    expect(screen.getByRole("tab", { name: /trigger/i })).toHaveAttribute(
      "aria-selected",
      "true",
    );

    await userEvent.click(screen.getByRole("tab", { name: /replace/i }));
    expect(screen.getByText(/Bigmoji posts it large/i)).toBeInTheDocument();
  });

  it("announces reduced motion mode", () => {
    vi.mocked(window.matchMedia).mockReturnValueOnce({
      matches: true,
      media: "(prefers-reduced-motion: reduce)",
      onchange: null,
      addEventListener: vi.fn(),
      removeEventListener: vi.fn(),
      addListener: vi.fn(),
      removeListener: vi.fn(),
      dispatchEvent: vi.fn(),
    });

    render(<HomepageDemo />);

    expect(screen.getByText(/Demo animation is reduced/i)).toBeInTheDocument();
  });
});
