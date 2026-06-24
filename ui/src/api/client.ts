import type { ApiError } from "./types";

export const API_BASE_PATH = "/api";

export class ApiRequestError extends Error {
  readonly apiError: ApiError;

  constructor(apiError: ApiError) {
    super(apiError.message);
    this.name = "ApiRequestError";
    this.apiError = apiError;
  }
}

export function toApiError(error: unknown): ApiError {
  if (error instanceof ApiRequestError) {
    return error.apiError;
  }

  if (error instanceof TypeError) {
    return {
      kind: "backendUnavailable",
      message: "Bigmoji is not reachable. Check the backend or proxy and retry.",
    };
  }

  if (error instanceof Error) {
    return {
      kind: "unknown",
      message: error.message || "Something went wrong. Please retry.",
    };
  }

  return {
    kind: "unknown",
    message: "Something went wrong. Please retry.",
  };
}

export function isApiRequestError(error: unknown): error is ApiRequestError {
  return error instanceof ApiRequestError;
}

async function normalizeResponseError(response: Response): Promise<ApiError> {
  let payload: unknown;

  try {
    payload = await response.clone().json();
  } catch {
    payload = undefined;
  }

  const payloadRecord =
    payload && typeof payload === "object"
      ? (payload as Record<string, unknown>)
      : undefined;

  const message =
    typeof payloadRecord?.message === "string"
      ? payloadRecord.message
      : fallbackMessage(response.status);

  const fieldErrors =
    payloadRecord?.fieldErrors && typeof payloadRecord.fieldErrors === "object"
      ? (payloadRecord.fieldErrors as Record<string, string>)
      : undefined;

  return {
    kind: errorKindForStatus(response.status),
    message,
    status: response.status,
    fieldErrors,
  };
}

function errorKindForStatus(status: number): ApiError["kind"] {
  if (status === 400) return "validation";
  if (status === 401) return "unauthenticated";
  if (status === 403) return "forbidden";
  if (status === 404) return "notFound";
  if (status >= 500) return "backendUnavailable";
  return "unknown";
}

function fallbackMessage(status: number) {
  if (status === 400) return "Check the highlighted fields and retry.";
  if (status === 401) return "Your session has expired. Sign in again.";
  if (status === 403) {
    return "You do not have permission to manage this Discord server.";
  }
  if (status === 404) return "The requested Bigmoji resource was not found.";
  if (status >= 500) {
    return "Bigmoji is temporarily unavailable. Retry in a moment.";
  }
  return "Something went wrong. Please retry.";
}

export async function apiRequest<T>(
  path: string,
  init: RequestInit = {},
): Promise<T> {
  const bodyIsFormData =
    typeof FormData !== "undefined" && init.body instanceof FormData;
  const headers = new Headers(init.headers);

  if (!bodyIsFormData && init.body && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  try {
    const response = await fetch(`${API_BASE_PATH}${path}`, {
      ...init,
      credentials: "include",
      headers,
    });

    if (!response.ok) {
      throw new ApiRequestError(await normalizeResponseError(response));
    }

    if (response.status === 204) {
      return undefined as T;
    }

    const text = await response.text();
    return (text ? JSON.parse(text) : undefined) as T;
  } catch (error) {
    if (error instanceof ApiRequestError) {
      throw error;
    }

    throw new ApiRequestError(toApiError(error));
  }
}
