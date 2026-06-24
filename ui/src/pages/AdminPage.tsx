import { ExternalLink, ShieldAlert } from "lucide-react";
import { useCallback, useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { getSignInUrl } from "../api/auth";
import { isApiRequestError, toApiError } from "../api/client";
import { deleteMapping, listDefaultStickers, listMappings } from "../api/mappings";
import type {
  ApiError,
  CurrentSession,
  DefaultSticker,
  StickerMapping,
} from "../api/types";
import { EmptyState } from "../components/EmptyState";
import { ErrorBanner } from "../components/ErrorBanner";
import { LoadingState } from "../components/LoadingState";
import { DefaultStickerList } from "../features/mappings/DefaultStickerList";
import { GuildSelector } from "../features/mappings/GuildSelector";
import { MappingList } from "../features/mappings/MappingList";
import { StickerUploadForm } from "../features/mappings/StickerUploadForm";
import "./AdminPage.css";
import "../features/mappings/mappings.css";

interface AdminPageProps {
  onSessionExpired: (error?: ApiError) => void;
  session: CurrentSession;
}

interface AdminDataState {
  defaults: DefaultSticker[];
  error?: ApiError;
  isLoading: boolean;
  mappings: StickerMapping[];
}

export function AdminPage({ onSessionExpired, session }: AdminPageProps) {
  const { guildId } = useParams();
  const [dataState, setDataState] = useState<AdminDataState>({
    defaults: [],
    isLoading: false,
    mappings: [],
  });

  const selectedGuild = useMemo(() => {
    if (session.status !== "authenticated" || !guildId) return undefined;
    return session.manageableGuilds.find((guild) => guild.id === guildId);
  }, [guildId, session]);

  const loadGuildData = useCallback(async () => {
    if (!selectedGuild) return;

    setDataState((current) => ({ ...current, error: undefined, isLoading: true }));

    try {
      const [defaults, mappings] = await Promise.all([
        listDefaultStickers(),
        listMappings(selectedGuild.id).catch((error: unknown) => {
          if (isApiRequestError(error) && error.apiError.kind === "notFound") {
            return [];
          }
          throw error;
        }),
      ]);

      setDataState({
        defaults,
        isLoading: false,
        mappings,
      });
    } catch (error) {
      const apiError = toApiError(error);
      if (apiError.kind === "unauthenticated") {
        onSessionExpired(apiError);
      }
      setDataState((current) => ({
        ...current,
        error: apiError,
        isLoading: false,
      }));
    }
  }, [onSessionExpired, selectedGuild]);

  useEffect(() => {
    void loadGuildData();
  }, [loadGuildData]);

  async function handleDelete(mapping: StickerMapping) {
    try {
      await deleteMapping(mapping.id);
    } catch (error) {
      if (!isApiRequestError(error) || error.apiError.kind !== "notFound") {
        const apiError = toApiError(error);
        if (apiError.kind === "unauthenticated") {
          onSessionExpired(apiError);
        }
        throw error;
      }
    }

    await loadGuildData();
  }

  if (session.status === "loading") {
    return (
      <div className="admin-page">
        <LoadingState label="Loading Discord session" />
      </div>
    );
  }

  if (session.status === "guest" || session.status === "expired") {
    return (
      <div className="admin-page">
        <EmptyState
          action={
            <a className="button button-primary" href={getSignInUrl()}>
              <ExternalLink aria-hidden="true" size={16} />
              Sign in with Discord
            </a>
          }
          message={
            session.status === "expired"
              ? "Your session expired. Sign in again to continue managing stickers."
              : "Sign in with Discord to select a server and manage Bigmoji mappings."
          }
          title={session.status === "expired" ? "Session expired" : "Sign in required"}
        />
      </div>
    );
  }

  if (session.status === "error") {
    return (
      <div className="admin-page">
        <ErrorBanner error={session.error} title="Could not load session" />
      </div>
    );
  }

  const hasInvalidGuild = Boolean(guildId && !selectedGuild);

  return (
    <div className="admin-page">
      <header className="admin-heading">
        <p className="eyebrow">Admin dashboard</p>
        <h1>Sticker mappings</h1>
        <p>
          Manage emoji-to-sticker behavior for the Discord servers returned by
          your authenticated session.
        </p>
      </header>

      <div className="admin-grid">
        <GuildSelector
          guilds={session.manageableGuilds}
          selectedGuildId={selectedGuild?.id}
        />

        <div className="admin-workspace">
          {hasInvalidGuild ? (
            <EmptyState
              action={
                <Link className="button button-secondary" to="/app">
                  Back to servers
                </Link>
              }
              message="This server was not included in your manageable Discord servers. Verify your permissions or pick another server."
              title="Access denied"
            />
          ) : null}

          {!guildId ? (
            <EmptyState
              message="Choose a server to upload stickers and manage custom emoji triggers."
              title="Select a server"
            />
          ) : null}

          {selectedGuild ? (
            <>
              {dataState.error ? (
                <ErrorBanner error={dataState.error} title="Could not load mappings" />
              ) : null}

              {dataState.isLoading ? (
                <LoadingState label="Loading sticker mappings" />
              ) : (
                <div className="admin-two-column">
                  <StickerUploadForm
                    guild={selectedGuild}
                    onSessionExpired={onSessionExpired}
                    onUploaded={loadGuildData}
                  />
                  <MappingList mappings={dataState.mappings} onDelete={handleDelete} />
                  <DefaultStickerList stickers={dataState.defaults} />
                </div>
              )}
            </>
          ) : null}

          {hasInvalidGuild ? (
            <div className="notice" role="note">
              <ShieldAlert aria-hidden="true" size={18} />
              <p>
                Upload and delete controls stay hidden until the selected server
                appears in the authenticated Discord session.
              </p>
            </div>
          ) : null}
        </div>
      </div>
    </div>
  );
}
