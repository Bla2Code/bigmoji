import type {
  CurrentSessionPayload,
  DefaultSticker,
  ManageableGuild,
  StickerMapping,
} from "../api/types";

export const primaryGuild: ManageableGuild = {
  id: "123456789012345678",
  name: "Bigmoji Test Server",
};

export const secondaryGuild: ManageableGuild = {
  id: "234567890123456789",
  name: "Launch Crew",
};

export const guilds: ManageableGuild[] = [primaryGuild, secondaryGuild];

export const authenticatedSession: CurrentSessionPayload = {
  userId: "111111111111111111",
  username: "admin",
  globalName: "Admin",
  manageableGuilds: guilds,
  expiresAt: "2026-06-24T20:00:00Z",
};

export const defaultStickers: DefaultSticker[] = [
  {
    emojiName: "😊",
    shortcodeName: "smile",
    description: "Smiling face",
    stickerPreviewUrl: "/api/stickers/default/smile/preview",
    stickerPreviewState: "available",
  },
  {
    emojiName: "❤️",
    shortcodeName: "heart",
    description: "Red heart",
    stickerPreviewUrl: "/api/stickers/default/heart/preview",
    stickerPreviewState: "available",
  },
  {
    emojiName: "🎉",
    shortcodeName: "party",
    description: "Party popper",
    stickerPreviewUrl: "/api/stickers/default/party/preview",
    stickerPreviewState: "available",
  },
  {
    emojiName: "👍",
    shortcodeName: "thumbsup",
    description: "Thumbs up",
    stickerPreviewUrl: "/api/stickers/default/thumbsup/preview",
    stickerPreviewState: "available",
  },
  {
    emojiName: "🔥",
    shortcodeName: "fire",
    description: "Fire",
    stickerPreviewUrl: "/api/stickers/default/fire/preview",
    stickerPreviewState: "available",
  },
  {
    emojiName: "😢",
    shortcodeName: "cry",
    description: "Crying face",
    stickerPreviewUrl: "/api/stickers/default/cry/preview",
    stickerPreviewState: "available",
  },
  {
    emojiName: "😮",
    shortcodeName: "open_mouth",
    description: "Face with open mouth",
    stickerPreviewUrl: "/api/stickers/default/open_mouth/preview",
    stickerPreviewState: "available",
  },
  {
    emojiName: "😔",
    shortcodeName: "pensive",
    description: "Pensive face",
    stickerPreviewUrl: "/api/stickers/default/pensive/preview",
    stickerPreviewState: "available",
  },
  {
    emojiName: "🫩",
    shortcodeName: "face_with_bags_under_eyes",
    description: "Face with bags under eyes",
    stickerPreviewUrl: "/api/stickers/default/face_with_bags_under_eyes/preview",
    stickerPreviewState: "available",
  },
];

export const customMappings: StickerMapping[] = [
  {
    id: "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    guildId: primaryGuild.id,
    emojiName: ":party_blob:",
    isDefault: false,
    createdAt: "2026-06-24T16:00:00Z",
    updatedAt: "2026-06-24T16:00:00Z",
    stickerPreviewUrl: "/api/mappings/a1b2c3d4-e5f6-7890-abcd-ef1234567890/preview",
    stickerPreviewState: "available",
    emojiPreview: {
      id: "987654321098765432",
      name: "party_blob",
      shortcode: ":party_blob:",
      imageUrl: "https://cdn.discordapp.com/emojis/987654321098765432.png",
      animated: false,
      available: true,
    },
  },
  {
    id: "b1b2c3d4-e5f6-7890-abcd-ef1234567890",
    guildId: primaryGuild.id,
    emojiName: ":party_blob:",
    isDefault: false,
    createdAt: "2026-06-24T16:10:00Z",
    updatedAt: "2026-06-24T16:10:00Z",
    stickerPreviewUrl: "/api/mappings/b1b2c3d4-e5f6-7890-abcd-ef1234567890/preview",
    stickerPreviewState: "available",
    emojiPreview: {
      id: "987654321098765432",
      name: "party_blob",
      shortcode: ":party_blob:",
      imageUrl: "https://cdn.discordapp.com/emojis/987654321098765432.png",
      animated: false,
      available: true,
    },
  },
  {
    id: "c1b2c3d4-e5f6-7890-abcd-ef1234567890",
    guildId: primaryGuild.id,
    emojiName: "😊",
    isDefault: false,
    createdAt: "2026-06-24T16:20:00Z",
    updatedAt: "2026-06-24T16:20:00Z",
    stickerPreviewUrl: "/api/mappings/c1b2c3d4-e5f6-7890-abcd-ef1234567890/preview",
    stickerPreviewState: "available",
  },
  {
    id: "d1b2c3d4-e5f6-7890-abcd-ef1234567890",
    guildId: primaryGuild.id,
    emojiName: ":deleted_blob:",
    isDefault: false,
    createdAt: "2026-06-24T16:30:00Z",
    updatedAt: "2026-06-24T16:30:00Z",
    stickerPreviewState: "unavailable",
    emojiPreview: {
      id: "876543210987654321",
      name: "deleted_blob",
      shortcode: ":deleted_blob:",
      animated: false,
      available: false,
    },
  },
];

export function jsonResponse(body: unknown, init: ResponseInit = {}) {
  return new Response(JSON.stringify(body), {
    status: 200,
    headers: { "Content-Type": "application/json" },
    ...init,
  });
}
