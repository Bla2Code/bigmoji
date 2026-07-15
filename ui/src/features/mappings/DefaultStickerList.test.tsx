import { fireEvent, render, screen } from "@testing-library/react";
import { defaultStickers } from "../../test/fixtures";
import { DefaultStickerList } from "./DefaultStickerList";

describe("DefaultStickerList", () => {
  it("renders all default stickers separately", () => {
    render(<DefaultStickerList stickers={defaultStickers} />);

    expect(screen.getByRole("heading", { name: /default stickers/i })).toBeVisible();
    expect(screen.getByText(":smile:")).toBeVisible();
    expect(screen.getByText(":heart:")).toBeVisible();
    expect(screen.getByText(":party:")).toBeVisible();
    expect(screen.getByText(":thumbsup:")).toBeVisible();
    expect(screen.getByText(":fire:")).toBeVisible();
    expect(screen.getByText(":cry:")).toBeVisible();
    expect(screen.getByText(":open_mouth:")).toBeVisible();
    expect(screen.getByText(":pensive:")).toBeVisible();
    expect(screen.getByText(":face_with_bags_under_eyes:")).toBeVisible();
    expect(screen.getByText("😢")).toBeVisible();
  });

  it("renders available sticker preview images", () => {
    render(<DefaultStickerList stickers={defaultStickers} />);

    expect(screen.getByRole("img", { name: "Default sticker :cry:" })).toHaveAttribute(
      "src",
      "/api/stickers/default/cry/preview",
    );
  });

  it("shows fallback text when preview metadata is unavailable", () => {
    render(
      <DefaultStickerList
        stickers={[
          {
            emojiName: "😢",
            shortcodeName: "cry",
            description: "Crying face",
            stickerPreviewState: "unavailable",
          },
        ]}
      />,
    );

    expect(screen.getByText("Preview unavailable")).toBeVisible();
    expect(
      screen.queryByRole("img", { name: /default sticker :cry:/i }),
    ).not.toBeInTheDocument();
  });

  it("hides broken preview images after a browser load error", () => {
    render(<DefaultStickerList stickers={defaultStickers.slice(0, 1)} />);

    fireEvent.error(screen.getByRole("img", { name: "Default sticker :smile:" }));

    expect(screen.getByText("Preview unavailable")).toBeVisible();
    expect(
      screen.queryByRole("img", { name: "Default sticker :smile:" }),
    ).not.toBeInTheDocument();
  });

  it("shows an empty state when defaults are unavailable", () => {
    render(<DefaultStickerList stickers={[]} />);

    expect(screen.getByRole("heading", { name: /no default stickers/i })).toBeVisible();
  });
});
