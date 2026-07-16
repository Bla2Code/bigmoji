import { ExternalLink, Server } from "lucide-react";
import { useState } from "react";
import { Link } from "react-router-dom";
import { fetchInstallUrl } from "../../api/auth";
import { toApiError } from "../../api/client";
import type { ApiError, ManageableGuild } from "../../api/types";
import { Button } from "../../components/Button";
import { EmptyState } from "../../components/EmptyState";
import { ErrorBanner } from "../../components/ErrorBanner";

interface GuildSelectorProps {
  guilds: ManageableGuild[];
  selectedGuildId?: string;
}

export function GuildSelector({ guilds, selectedGuildId }: GuildSelectorProps) {
  const [installError, setInstallError] = useState<ApiError | undefined>();
  const [isLoadingInstallUrl, setIsLoadingInstallUrl] = useState(false);

  async function openInstallUrl() {
    setInstallError(undefined);
    setIsLoadingInstallUrl(true);

    try {
      window.open(await fetchInstallUrl(selectedGuildId), "_self", "noopener");
    } catch (error) {
      setInstallError(toApiError(error));
    } finally {
      setIsLoadingInstallUrl(false);
    }
  }

  if (guilds.length === 0) {
    return (
      <div className="guild-selector">
        <EmptyState
          action={
            <Button
              icon={<ExternalLink aria-hidden="true" size={16} />}
              isLoading={isLoadingInstallUrl}
              onClick={openInstallUrl}
            >
              Add Bigmoji to Discord
            </Button>
          }
          message="Discord did not return a server where you can manage Bigmoji. Install the bot or check your server permissions."
          title="No manageable servers"
        />
        <ErrorBanner error={installError} title="Could not open install URL" />
      </div>
    );
  }

  return (
    <section className="guild-selector" aria-labelledby="guild-selector-title">
      <div className="section-heading">
        <Server aria-hidden="true" size={20} />
        <div>
          <h2 id="guild-selector-title">Servers</h2>
          <p>Select a Discord server to manage custom Bigmoji mappings.</p>
        </div>
      </div>
      <div className="guild-grid">
        {guilds.map((guild) => (
          <Link
            aria-current={guild.id === selectedGuildId ? "page" : undefined}
            className="guild-link"
            key={guild.id}
            to={`/app/guilds/${guild.id}`}
          >
            <span>{guild.name}</span>
            <small>{guild.id}</small>
          </Link>
        ))}
      </div>
    </section>
  );
}
