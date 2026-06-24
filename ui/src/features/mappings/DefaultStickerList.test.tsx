import { render, screen } from "@testing-library/react";
import { defaultStickers } from "../../test/fixtures";
import { DefaultStickerList } from "./DefaultStickerList";

describe("DefaultStickerList", () => {
  it("renders default stickers separately", () => {
    render(<DefaultStickerList stickers={defaultStickers} />);

    expect(screen.getByRole("heading", { name: /default stickers/i })).toBeVisible();
    expect(screen.getByText(":smile:")).toBeVisible();
  });

  it("shows an empty state when defaults are unavailable", () => {
    render(<DefaultStickerList stickers={[]} />);

    expect(screen.getByRole("heading", { name: /no default stickers/i })).toBeVisible();
  });
});
