import { AlertTriangle } from "lucide-react";
import type { ApiError } from "../api/types";

interface ErrorBannerProps {
  error?: ApiError | string;
  title?: string;
}

export function ErrorBanner({ error, title = "Action needed" }: ErrorBannerProps) {
  if (!error) return null;

  const message = typeof error === "string" ? error : error.message;

  return (
    <div className="notice notice-error" role="alert">
      <AlertTriangle aria-hidden="true" size={18} />
      <div>
        <strong>{title}</strong>
        <p>{message}</p>
      </div>
    </div>
  );
}
