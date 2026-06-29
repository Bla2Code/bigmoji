import { ExternalLink, LogIn, RefreshCw } from "lucide-react";
import { Link } from "react-router-dom";
import { getSignInUrl } from "../../api/auth";
import type { CurrentSession } from "../../api/types";
import { Button } from "../../components/Button";
import { ErrorBanner } from "../../components/ErrorBanner";
import { LoadingState } from "../../components/LoadingState";

interface AuthPanelProps {
  onRetrySession?: () => Promise<void>;
  session: CurrentSession;
}

export function AuthPanel({ onRetrySession, session }: AuthPanelProps) {
  if (session.status === "loading") {
    return (
      <aside className="auth-panel" aria-label="Discord sign-in">
        <LoadingState label="Checking Discord session" />
      </aside>
    );
  }

  if (session.status === "authenticated") {
    const firstGuild = session.manageableGuilds[0];
    const dashboardTarget = firstGuild ? `/app/guilds/${firstGuild.id}` : "/app";

    return (
      <aside className="auth-panel" aria-label="Discord sign-in">
        <div>
          <p className="eyebrow">Signed in</p>
          <h2>{session.globalName || session.username}</h2>
          <p>
            {session.manageableGuilds.length
              ? `${session.manageableGuilds.length} manageable server${
                  session.manageableGuilds.length === 1 ? "" : "s"
                } ready.`
              : "No manageable servers were returned by Discord."}
          </p>
        </div>
        <Link className="button button-primary" to={dashboardTarget}>
          Open dashboard
        </Link>
      </aside>
    );
  }

  const hasError = session.status === "error" || session.status === "expired";

  return (
    <aside className="auth-panel" aria-label="Discord sign-in">
      <div>
        <p className="eyebrow">Admin setup</p>
        <h2>Connect Discord</h2>
        <p>
          Sign in with Discord to pick a server, upload stickers, and map them
          to emoji triggers.
        </p>
      </div>
      {hasError ? (
        <ErrorBanner
          error={
            session.status === "expired"
              ? session.error ??
                "Your Discord session expired. Sign in again to continue."
              : session.error
          }
          title={session.status === "expired" ? "Session expired" : "Backend unavailable"}
        />
      ) : null}
      <div className="auth-actions">
        <a className="button button-primary" href={getSignInUrl()}>
          <LogIn aria-hidden="true" size={18} />
          Sign in with Discord
        </a>
        <a className="button button-secondary" href={getSignInUrl()}>
          <ExternalLink aria-hidden="true" size={18} />
          Register server
        </a>
        {hasError && onRetrySession ? (
          <Button
            icon={<RefreshCw aria-hidden="true" size={16} />}
            onClick={() => void onRetrySession()}
            variant="ghost"
          >
            Retry
          </Button>
        ) : null}
      </div>
    </aside>
  );
}
