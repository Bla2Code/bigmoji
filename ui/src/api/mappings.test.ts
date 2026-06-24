import {
  deleteMapping,
  listDefaultStickers,
  listMappings,
  uploadMapping,
} from "./mappings";
import { customMappings, defaultStickers, jsonResponse } from "../test/fixtures";

describe("mapping API", () => {
  beforeEach(() => {
    vi.stubGlobal("fetch", vi.fn());
  });

  it("loads default stickers with credentials", async () => {
    vi.mocked(fetch).mockResolvedValueOnce(jsonResponse(defaultStickers));

    await expect(listDefaultStickers()).resolves.toEqual(defaultStickers);
    expect(fetch).toHaveBeenCalledWith(
      "/api/stickers/default",
      expect.objectContaining({ credentials: "include" }),
    );
  });

  it("loads custom mappings for an encoded guild id", async () => {
    vi.mocked(fetch).mockResolvedValueOnce(jsonResponse(customMappings));

    await expect(listMappings("guild with space")).resolves.toEqual(customMappings);
    expect(fetch).toHaveBeenCalledWith(
      "/api/mappings/guild%20with%20space",
      expect.objectContaining({ credentials: "include" }),
    );
  });

  it("uploads multipart form data without forcing json content type", async () => {
    const file = new File(["demo"], "demo.png", { type: "image/png" });
    vi.mocked(fetch).mockResolvedValueOnce(jsonResponse(customMappings[0], { status: 201 }));

    await uploadMapping({ emojiName: "🔥", file, guildId: "123" });

    const call = vi.mocked(fetch).mock.calls[0];
    expect(call).toBeDefined();
    const init = call?.[1];
    expect(init).toMatchObject({ credentials: "include", method: "POST" });
    expect(init?.body).toBeInstanceOf(FormData);
    expect(init?.headers).toBeInstanceOf(Headers);
    expect((init?.headers as Headers).has("Content-Type")).toBe(false);
  });

  it("deletes mappings and accepts empty 204 responses", async () => {
    vi.mocked(fetch).mockResolvedValueOnce(new Response(null, { status: 204 }));

    await expect(deleteMapping("mapping/id")).resolves.toBeUndefined();
    expect(fetch).toHaveBeenCalledWith(
      "/api/mappings/mapping%2Fid",
      expect.objectContaining({ method: "DELETE" }),
    );
  });

  it.each([
    [400, "validation"],
    [401, "unauthenticated"],
    [403, "forbidden"],
    [404, "notFound"],
    [503, "backendUnavailable"],
  ] as const)("normalizes %i responses to %s", async (status, kind) => {
    vi.mocked(fetch).mockResolvedValueOnce(
      jsonResponse({ message: "Backend said no" }, { status }),
    );

    try {
      await listMappings("123");
      throw new Error("Expected listMappings to reject");
    } catch (error) {
      const apiError = (error as {
        apiError?: { kind?: unknown; status?: unknown };
      }).apiError;
      expect(apiError?.kind).toBe(kind);
      expect(apiError?.status).toBe(status);
    }
  });
});
