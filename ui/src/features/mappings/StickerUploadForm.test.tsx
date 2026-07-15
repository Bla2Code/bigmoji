import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { ApiRequestError } from "../../api/client";
import { primaryGuild } from "../../test/fixtures";
import { StickerUploadForm } from "./StickerUploadForm";

const uploadMappingMock = vi.hoisted(() => vi.fn());

vi.mock("../../api/mappings", () => ({
  uploadMapping: uploadMappingMock,
}));

function renderForm() {
  const onUploaded = vi.fn().mockResolvedValue(undefined);
  const onSessionExpired = vi.fn();

  render(
    <StickerUploadForm
      guild={primaryGuild}
      onSessionExpired={onSessionExpired}
      onUploaded={onUploaded}
    />,
  );

  return { onSessionExpired, onUploaded };
}

describe("StickerUploadForm", () => {
  it("documents Discord custom emoji mention input", () => {
    renderForm();

    expect(screen.getByLabelText(/emoji trigger/i)).toHaveAttribute(
      "placeholder",
      "🔥, :fire:, or <:custom:123456789>",
    );
  });

  it("validates required emoji and file inputs", async () => {
    renderForm();

    await userEvent.click(screen.getByRole("button", { name: /upload mapping/i }));

    expect(screen.getByText(/choose an emoji trigger/i)).toBeInTheDocument();
    expect(screen.getByText(/choose a sticker image/i)).toBeInTheDocument();
    expect(uploadMappingMock).not.toHaveBeenCalled();
  });

  it("shows a preview and uploads valid sticker mappings", async () => {
    uploadMappingMock.mockResolvedValueOnce({ id: "created" });
    const { onUploaded } = renderForm();
    const file = new File(["demo"], "demo.png", { type: "image/png" });

    await userEvent.type(screen.getByLabelText(/emoji trigger/i), "🔥");
    await userEvent.upload(screen.getByLabelText(/sticker image/i), file);

    expect(screen.getByAltText(/selected sticker preview/i)).toBeInTheDocument();

    await userEvent.click(screen.getByRole("button", { name: /upload mapping/i }));

    await waitFor(() => {
      expect(uploadMappingMock).toHaveBeenCalledWith({
        emojiName: "🔥",
        file,
        guildId: primaryGuild.id,
      });
      expect(onUploaded).toHaveBeenCalledTimes(1);
    });
  });

  it("surfaces upload failures and marks expired sessions", async () => {
    uploadMappingMock.mockRejectedValueOnce(
      new ApiRequestError({
        kind: "unauthenticated",
        message: "Sign in again",
        status: 401,
      }),
    );
    const { onSessionExpired } = renderForm();
    const file = new File(["demo"], "demo.png", { type: "image/png" });

    await userEvent.type(screen.getByLabelText(/emoji trigger/i), "🔥");
    await userEvent.upload(screen.getByLabelText(/sticker image/i), file);
    await userEvent.click(screen.getByRole("button", { name: /upload mapping/i }));

    await waitFor(() => {
      expect(onSessionExpired).toHaveBeenCalledTimes(1);
    });
    expect(screen.getByText(/sign in again/i)).toBeInTheDocument();
  });
});
