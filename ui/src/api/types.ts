export type SessionStatus = "loading" | "guest" | "authenticated" | "expired" | "error";

export interface ManageableGuild {
  id: string;
  name: string;
}

export interface CurrentSessionPayload {
  userId: string;
  username: string;
  globalName?: string;
  manageableGuilds: ManageableGuild[];
  expiresAt: string;
}

export type CurrentSession =
  | { status: "loading" }
  | { status: "guest" }
  | { status: "expired"; error?: ApiError }
  | ({ status: "authenticated" } & CurrentSessionPayload)
  | { status: "error"; error: ApiError };

export type StickerPreviewState = "available" | "unavailable";

export interface ServerEmojiPreview {
  id?: string;
  name: string;
  shortcode: string;
  imageUrl?: string;
  animated: boolean;
  available: boolean;
}

export interface StickerMapping {
  id: string;
  guildId: string;
  emojiName: string;
  isDefault: boolean;
  createdAt: string;
  updatedAt: string;
  stickerPreviewUrl?: string;
  stickerPreviewState: StickerPreviewState;
  emojiPreview?: ServerEmojiPreview;
}

export interface DefaultSticker {
  emojiName: string;
  shortcodeName: string;
  description: string;
  stickerPreviewUrl?: string;
  stickerPreviewState: StickerPreviewState;
}

export type StickerUploadStatus =
  | "idle"
  | "validating"
  | "uploading"
  | "success"
  | "error";

export interface StickerUploadDraft {
  guildId: string;
  emojiName: string;
  file?: File;
  previewUrl?: string;
  status: StickerUploadStatus;
  errorMessage?: string;
}

export type ApiErrorKind =
  | "validation"
  | "unauthenticated"
  | "forbidden"
  | "notFound"
  | "backendUnavailable"
  | "unknown";

export interface ApiError {
  kind: ApiErrorKind;
  message: string;
  status?: number;
  fieldErrors?: Record<string, string>;
}

export interface InstallUrlResponse {
  url: string;
}
