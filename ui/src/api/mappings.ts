import { apiRequest } from "./client";
import type { DefaultSticker, StickerMapping } from "./types";

export async function listDefaultStickers() {
  return apiRequest<DefaultSticker[]>("/stickers/default");
}

export async function listMappings(guildId: string) {
  return apiRequest<StickerMapping[]>(
    `/mappings/${encodeURIComponent(guildId)}`,
  );
}

export async function uploadMapping(input: {
  guildId: string;
  emojiName: string;
  file: File;
}) {
  const formData = new FormData();
  formData.set("guildId", input.guildId);
  formData.set("emojiName", input.emojiName);
  formData.set("file", input.file);

  return apiRequest<StickerMapping>("/mappings", {
    method: "POST",
    body: formData,
  });
}

export async function deleteMapping(mappingId: string) {
  return apiRequest<void>(`/mappings/${encodeURIComponent(mappingId)}`, {
    method: "DELETE",
  });
}
