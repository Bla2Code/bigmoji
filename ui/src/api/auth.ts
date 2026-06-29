import { apiRequest, API_BASE_PATH } from "./client";
import type { CurrentSessionPayload, InstallUrlResponse } from "./types";

export function getSignInUrl() {
  return `${API_BASE_PATH}/auth/discord/login`;
}

export async function fetchCurrentSession() {
  return apiRequest<CurrentSessionPayload>("/auth/me");
}

export async function logout() {
  return apiRequest<void>("/auth/logout", { method: "POST" });
}

export async function fetchInstallUrl(guildId?: string) {
  const query = guildId ? `?guildId=${encodeURIComponent(guildId)}` : "";
  const response = await apiRequest<InstallUrlResponse>(
    `/auth/discord/install-url${query}`,
  );
  return response.url;
}
