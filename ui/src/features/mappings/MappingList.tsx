import { Trash2 } from "lucide-react";
import { useMemo, useState } from "react";
import { toApiError } from "../../api/client";
import type { ApiError, ServerEmojiPreview, StickerMapping } from "../../api/types";
import { Button } from "../../components/Button";
import { EmptyState } from "../../components/EmptyState";
import { ErrorBanner } from "../../components/ErrorBanner";
import { Modal } from "../../components/Modal";

interface MappingListProps {
  mappings: StickerMapping[];
  onDelete: (mapping: StickerMapping) => Promise<void>;
}

export function MappingList({ mappings, onDelete }: MappingListProps) {
  const [pendingDelete, setPendingDelete] = useState<StickerMapping | undefined>();
  const [deleteError, setDeleteError] = useState<ApiError | undefined>();
  const [failedAssets, setFailedAssets] = useState<Set<string>>(() => new Set());
  const [isDeleting, setIsDeleting] = useState(false);
  const groups = useMemo(() => groupMappings(mappings), [mappings]);

  function markAssetFailed(key: string) {
    setFailedAssets((current) => {
      if (current.has(key)) return current;
      const next = new Set(current);
      next.add(key);
      return next;
    });
  }

  async function confirmDelete() {
    if (!pendingDelete) return;

    setDeleteError(undefined);
    setIsDeleting(true);

    try {
      await onDelete(pendingDelete);
      setPendingDelete(undefined);
    } catch (error) {
      setDeleteError(toApiError(error));
    } finally {
      setIsDeleting(false);
    }
  }

  if (mappings.length === 0) {
    return (
      <EmptyState
        message="Upload a sticker and emoji trigger to create the first custom mapping for this server."
        title="No custom mappings"
      />
    );
  }

  return (
    <section className="mapping-section" aria-labelledby="custom-mappings-title">
      <div className="section-heading">
        <Trash2 aria-hidden="true" size={20} />
        <div>
          <h2 id="custom-mappings-title">Custom mappings</h2>
          <p>Mappings are grouped by emoji trigger for quick scanning.</p>
        </div>
      </div>
      <div className="mapping-groups">
        {groups.map((group) => {
          const emojiAssetKey = `emoji-${group.groupKey}`;
          const canShowEmoji =
            Boolean(group.emojiPreview?.available) &&
            Boolean(group.emojiPreview?.imageUrl) &&
            !failedAssets.has(emojiAssetKey);
          const fallbackEmoji =
            group.emojiPreview?.unicodeEmoji ?? group.fallbackTrigger;
          const hasUnicodeEmoji =
            Boolean(group.emojiPreview?.unicodeEmoji) ||
            /\p{Extended_Pictographic}/u.test(group.fallbackTrigger);

          return (
            <article className="mapping-group" key={group.groupKey}>
              <header>
                <span className="mapping-emoji" title={group.displayName}>
                  {canShowEmoji && group.emojiPreview?.imageUrl ? (
                    <img
                      alt={`Custom emoji ${group.displayName}`}
                      onError={() => markAssetFailed(emojiAssetKey)}
                      src={group.emojiPreview.imageUrl}
                    />
                  ) : (
                    <span
                      className={`mapping-emoji-fallback${hasUnicodeEmoji ? " mapping-emoji-unicode" : ""}`}
                    >
                      {fallbackEmoji}
                    </span>
                  )}
                </span>
                <div>
                  <h3>{group.displayName}</h3>
                  <p>
                    {group.mappings.length} custom sticker
                    {group.mappings.length === 1 ? "" : "s"}
                  </p>
                </div>
              </header>
              <ul>
                {group.mappings.map((mapping) => {
                  const stickerAssetKey = `sticker-${mapping.id}`;
                  const canShowSticker =
                    mapping.stickerPreviewState === "available" &&
                    Boolean(mapping.stickerPreviewUrl) &&
                    !failedAssets.has(stickerAssetKey);

                  return (
                    <li key={mapping.id}>
                      <span className="mapping-sticker-preview">
                        {canShowSticker && mapping.stickerPreviewUrl ? (
                          <img
                            alt={`Uploaded sticker for ${displayTrigger(mapping)}`}
                            onError={() => markAssetFailed(stickerAssetKey)}
                            src={mapping.stickerPreviewUrl}
                          />
                        ) : (
                          <span>Preview unavailable</span>
                        )}
                      </span>
                      <div className="mapping-row-copy">
                        <strong>Sticker for {displayTrigger(mapping)}</strong>
                        <span>
                          Mapping {mapping.id.slice(0, 8)} - Created{" "}
                          {formatDate(mapping.createdAt)}
                        </span>
                      </div>
                      <Button
                        icon={<Trash2 aria-hidden="true" size={16} />}
                        onClick={() => {
                          setDeleteError(undefined);
                          setPendingDelete(mapping);
                        }}
                        variant="danger"
                      >
                        Delete
                      </Button>
                    </li>
                  );
                })}
              </ul>
            </article>
          );
        })}
      </div>

      <Modal
        isOpen={Boolean(pendingDelete)}
        onClose={() => setPendingDelete(undefined)}
        title="Delete mapping?"
      >
        <div className="delete-confirmation">
          <p>
            This removes the custom mapping for{" "}
            {pendingDelete ? displayTrigger(pendingDelete) : "this trigger"}. The
            default sticker behavior remains available.
          </p>
          <ErrorBanner error={deleteError} title="Delete failed" />
          <div className="modal-actions">
            <Button onClick={() => setPendingDelete(undefined)} variant="secondary">
              Cancel
            </Button>
            <Button
              icon={<Trash2 aria-hidden="true" size={16} />}
              isLoading={isDeleting}
              onClick={confirmDelete}
              variant="danger"
            >
              Delete mapping
            </Button>
          </div>
        </div>
      </Modal>
    </section>
  );
}

interface MappingGroup {
  displayName: string;
  emojiPreview?: ServerEmojiPreview;
  fallbackTrigger: string;
  groupKey: string;
  mappings: StickerMapping[];
}

function groupMappings(mappings: StickerMapping[]) {
  const grouped = new Map<string, MappingGroup>();

  for (const mapping of mappings) {
    const groupKey = groupKeyFor(mapping);
    const existing = grouped.get(groupKey);
    if (existing) {
      existing.mappings.push(mapping);
    } else {
      grouped.set(groupKey, {
        displayName: displayTrigger(mapping),
        emojiPreview: mapping.emojiPreview,
        fallbackTrigger: displayTrigger(mapping),
        groupKey,
        mappings: [mapping],
      });
    }
  }

  return [...grouped.values()].sort((left, right) =>
    left.displayName.localeCompare(right.displayName),
  );
}

function groupKeyFor(mapping: StickerMapping) {
  if (mapping.emojiPreview?.id) {
    return `custom-${mapping.emojiPreview.id}`;
  }

  return mapping.emojiPreview?.shortcode ?? mapping.emojiName;
}

function displayTrigger(mapping: StickerMapping) {
  return (
    mapping.emojiPreview?.shortcode ?? mapping.emojiPreview?.name ?? mapping.emojiName
  );
}

function formatDate(value: string) {
  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return "recently";
  }

  return new Intl.DateTimeFormat(undefined, {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(date);
}
