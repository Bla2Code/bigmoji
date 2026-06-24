import { Navigate, Route, Routes, useNavigate } from "react-router-dom";
import { AppShell } from "./components/AppShell";
import { useSession } from "./features/auth/useSession";
import { HomePage } from "./pages/HomePage";
import { AdminPage } from "./pages/AdminPage";

export default function App() {
  const navigate = useNavigate();
  const sessionState = useSession();

  async function handleLogout() {
    await sessionState.signOut();
    void navigate("/");
  }

  return (
    <AppShell onLogout={handleLogout} session={sessionState.session}>
      <Routes>
        <Route
          path="/"
          element={
            <HomePage onRetrySession={sessionState.refresh} session={sessionState.session} />
          }
        />
        <Route
          path="/app"
          element={
            <AdminPage onSessionExpired={sessionState.markExpired} session={sessionState.session} />
          }
        />
        <Route
          path="/app/guilds/:guildId"
          element={
            <AdminPage onSessionExpired={sessionState.markExpired} session={sessionState.session} />
          }
        />
        <Route path="*" element={<Navigate replace to="/" />} />
      </Routes>
    </AppShell>
  );
}
