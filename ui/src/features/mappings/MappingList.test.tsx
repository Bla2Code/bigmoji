import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { customMappings } from "../../test/fixtures";
import { MappingList } from "./MappingList";

describe("MappingList", () => {
  it("groups mappings by custom emoji and hides storage internals", () => {
    render(<MappingList mappings={customMappings} onDelete={vi.fn()} />);

    expect(screen.getByRole("heading", { name: ":party_blob:" })).toBeVisible();
    expect(screen.getByText("2 custom stickers")).toBeInTheDocument();
    expect(screen.queryByText(/bigmoji-123456789012345678/i)).not.toBeInTheDocument();
    expect(screen.queryByText(/internal\/smile/i)).not.toBeInTheDocument();
  });

  it("renders custom emoji and sticker preview images when available", () => {
    render(<MappingList mappings={customMappings.slice(0, 2)} onDelete={vi.fn()} />);

    expect(
      screen.getByRole("img", { name: /custom emoji :party_blob:/i }),
    ).toHaveAttribute("src", expect.stringContaining("cdn.discordapp.com"));
    expect(
      screen.getAllByRole("img", { name: /uploaded sticker for :party_blob:/i }),
    ).toHaveLength(2);
  });

  it("keeps Unicode triggers readable when no custom emoji preview exists", () => {
    render(<MappingList mappings={[customMappings[2]!]} onDelete={vi.fn()} />);

    expect(screen.getByRole("heading", { name: "😊" })).toBeVisible();
    expect(screen.getByRole("img", { name: /uploaded sticker for 😊/i })).toBeVisible();
  });

  it("renders standard Discord shortcode as Unicode with its code", () => {
    render(
      <MappingList
        mappings={[
          {
            ...customMappings[2]!,
            emojiName: ":innocent:",
            emojiPreview: {
              name: "innocent",
              shortcode: ":innocent:",
              animated: false,
              available: true,
              unicodeEmoji: "😇",
            },
          },
        ]}
        onDelete={vi.fn()}
      />,
    );

    expect(screen.getByText("😇")).toBeVisible();
    expect(screen.getByRole("heading", { name: ":innocent:" })).toBeVisible();
  });

  it("shows unavailable placeholders instead of broken preview images", () => {
    render(<MappingList mappings={[customMappings[3]!]} onDelete={vi.fn()} />);

    expect(screen.getByRole("heading", { name: ":deleted_blob:" })).toBeVisible();
    expect(screen.getByText(/preview unavailable/i)).toBeVisible();
    expect(screen.queryByRole("img")).not.toBeInTheDocument();
  });

  it("switches to fallback UI when preview images fail to load", () => {
    render(<MappingList mappings={customMappings.slice(0, 1)} onDelete={vi.fn()} />);

    fireEvent.error(screen.getByRole("img", { name: /custom emoji :party_blob:/i }));
    fireEvent.error(
      screen.getByRole("img", { name: /uploaded sticker for :party_blob:/i }),
    );

    expect(
      screen.queryByRole("img", { name: /custom emoji :party_blob:/i }),
    ).not.toBeInTheDocument();
    expect(screen.getByText(/preview unavailable/i)).toBeVisible();
  });

  it("requires confirmation before deleting a mapping", async () => {
    const onDelete = vi.fn().mockResolvedValue(undefined);
    render(<MappingList mappings={customMappings.slice(0, 1)} onDelete={onDelete} />);

    await userEvent.click(screen.getByRole("button", { name: /^delete$/i }));

    expect(onDelete).not.toHaveBeenCalled();
    expect(screen.getByRole("dialog", { name: /delete mapping/i })).toBeInTheDocument();

    await userEvent.click(screen.getByRole("button", { name: /delete mapping/i }));

    await waitFor(() => {
      expect(onDelete).toHaveBeenCalledWith(customMappings[0]);
    });
  });

  it("keeps delete confirmation usable when previews are unavailable", async () => {
    const onDelete = vi.fn().mockResolvedValue(undefined);
    render(<MappingList mappings={[customMappings[3]!]} onDelete={onDelete} />);

    await userEvent.click(screen.getByRole("button", { name: /^delete$/i }));

    expect(screen.getByRole("dialog", { name: /delete mapping/i })).toHaveTextContent(
      ":deleted_blob:",
    );

    await userEvent.click(screen.getByRole("button", { name: /delete mapping/i }));

    await waitFor(() => {
      expect(onDelete).toHaveBeenCalledWith(customMappings[3]);
    });
  });
});
