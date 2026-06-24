import { LogOut, Sparkles } from "lucide-react";
import type { ReactNode } from "react";
import { Link, NavLink } from "react-router-dom";
import type { CurrentSession } from "../api/types";
import { Button } from "./Button";

interface AppShellProps {
  children: ReactNode;
  onLogout: () => void;
  session: CurrentSession;
}

export function AppShell({ children, onLogout, session }: AppShellProps) {
  const displayName =
    session.status === "authenticated"
      ? session.globalName || session.username
      : undefined;

  return (
    <div className="app-shell">
      <header className="site-header">
        <Link className="brand" to="/">
          <Sparkles aria-hidden="true" size={24} />
          <span>Bigmoji</span>
        </Link>
        <nav aria-label="Main navigation">
          <NavLink to="/">Home</NavLink>
          <NavLink to="/app">Dashboard</NavLink>
        </nav>
        <div className="header-session">
          {displayName ? (
            <>
              <span className="user-chip">{displayName}</span>
              <Button
                aria-label="Log out"
                icon={<LogOut aria-hidden="true" size={16} />}
                onClick={onLogout}
                variant="ghost"
              >
                Logout
              </Button>
            </>
          ) : (
            <span className="user-chip user-chip-muted">Guest</span>
          )}
        </div>
      </header>
      <main>{children}</main>
    </div>
  );
}
