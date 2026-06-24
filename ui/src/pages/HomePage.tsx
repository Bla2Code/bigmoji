import type { CurrentSession } from "../api/types";
import { AuthPanel } from "../features/auth/AuthPanel";
import { HomepageDemo } from "../features/homepage-demo/HomepageDemo";
import "./HomePage.css";

interface HomePageProps {
  onRetrySession?: () => Promise<void>;
  session: CurrentSession;
}

export function HomePage({ onRetrySession, session }: HomePageProps) {
  return (
    <div className="home-page">
      <section className="home-hero" aria-labelledby="home-title">
        <div className="hero-copy">
          <p className="eyebrow">Discord sticker automation</p>
          <h1 id="home-title">Bigmoji</h1>
          <p className="hero-lede">
            Upload server stickers, map them to emoji, and let Bigmoji replace
            emoji-only Discord messages with large sticker reactions.
          </p>
        </div>
        <HomepageDemo />
        <AuthPanel onRetrySession={onRetrySession} session={session} />
      </section>
      <section className="home-next" aria-label="Bigmoji workflow">
        <div>
          <strong>Upload</strong>
          <span>Transparent PNG stickers stay server-scoped.</span>
        </div>
        <div>
          <strong>Map</strong>
          <span>Admins choose the emoji trigger per Discord server.</span>
        </div>
        <div>
          <strong>Replace</strong>
          <span>Emoji-only messages become the mapped sticker.</span>
        </div>
      </section>
    </div>
  );
}
