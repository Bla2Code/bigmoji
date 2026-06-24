import { Trash2 } from "lucide-react";
import { useMemo, useState } from "react";
import { toApiError } from "../../api/client";
import type { ApiError, StickerMapping } from "../../api/types";
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
  const [isDeleting, setIsDeleting] = useState(false);
  const groups = useMemo(() => groupMappings(mappings), [mappings]);

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
        {groups.map(([emojiName, group]) => (
          <article className="mapping-group" key={emojiName}>
            <header>
              <span className="mapping-emoji" aria-hidden="true">
                {emojiName}
              </span>
              <div>
                <h3>{emojiName}</h3>
                <p>
                  {group.length} custom sticker{group.length === 1 ? "" : "s"}
                </p>
              </div>
            </header>
            <ul>
              {group.map((mapping) => (
                <li key={mapping.id}>
                  <div>
                    <strong>Mapping {mapping.id.slice(0, 8)}</strong>
                    <span>Created {formatDate(mapping.createdAt)}</span>
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
              ))}
            </ul>
          </article>
        ))}
      </div>

      <Modal
        isOpen={Boolean(pendingDelete)}
        onClose={() => setPendingDelete(undefined)}
        title="Delete mapping?"
      >
        <div className="delete-confirmation">
          <p>
            This removes the custom mapping for {pendingDelete?.emojiName}. The
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

function groupMappings(mappings: StickerMapping[]) {
  const grouped = new Map<string, StickerMapping[]>();

  for (const mapping of mappings) {
    const group = grouped.get(mapping.emojiName) ?? [];
    group.push(mapping);
    grouped.set(mapping.emojiName, group);
  }

  return [...grouped.entries()].sort(([left], [right]) =>
    left.localeCompare(right),
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
