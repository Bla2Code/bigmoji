import { useCallback, useEffect, useState } from "react";
import { fetchCurrentSession, logout } from "../../api/auth";
import { isApiRequestError, toApiError } from "../../api/client";
import type { ApiError, CurrentSession } from "../../api/types";

export interface UseSessionResult {
  session: CurrentSession;
  refresh: () => Promise<void>;
  signOut: () => Promise<void>;
  markExpired: (error?: ApiError) => void;
}

export function useSession(): UseSessionResult {
  const [session, setSession] = useState<CurrentSession>({ status: "loading" });

  const refresh = useCallback(async () => {
    setSession((current) =>
      current.status === "authenticated" ? current : { status: "loading" },
    );

    try {
      const payload = await fetchCurrentSession();
      setSession({ status: "authenticated", ...payload });
    } catch (error) {
      const apiError = toApiError(error);
      setSession(
        apiError.kind === "unauthenticated"
          ? { status: "guest" }
          : { status: "error", error: apiError },
      );
    }
  }, []);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  const signOut = useCallback(async () => {
    try {
      await logout();
    } catch (error) {
      if (!isApiRequestError(error) || error.apiError.kind !== "unauthenticated") {
        setSession({ status: "error", error: toApiError(error) });
        return;
      }
    }

    setSession({ status: "guest" });
  }, []);

  const markExpired = useCallback((error?: ApiError) => {
    setSession({ status: "expired", error });
  }, []);

  return { session, refresh, signOut, markExpired };
}
