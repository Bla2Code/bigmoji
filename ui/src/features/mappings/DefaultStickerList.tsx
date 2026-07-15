import { useState } from "react";
import { Sparkles } from "lucide-react";
import type { DefaultSticker } from "../../api/types";
import { EmptyState } from "../../components/EmptyState";

interface DefaultStickerListProps {
  stickers: DefaultSticker[];
}

export function DefaultStickerList({ stickers }: DefaultStickerListProps) {
  const [failedPreviews, setFailedPreviews] = useState<Set<string>>(() => new Set());

  if (stickers.length === 0) {
    return (
      <EmptyState
        message="The backend did not return any built-in fallback stickers."
        title="No default stickers"
      />
    );
  }

  return (
    <section className="mapping-section" aria-labelledby="default-stickers-title">
      <div className="section-heading">
        <Sparkles aria-hidden="true" size={20} />
        <div>
          <h2 id="default-stickers-title">Default stickers</h2>
          <p>Built-in fallbacks are shown separately from server custom mappings.</p>
        </div>
      </div>
      <div className="default-sticker-grid">
        {stickers.map((sticker) => {
          const shortcode = `:${sticker.shortcodeName}:`;
          const showPreview =
            sticker.stickerPreviewState === "available" &&
            Boolean(sticker.stickerPreviewUrl) &&
            !failedPreviews.has(sticker.shortcodeName);

          return (
            <article className="default-sticker" key={sticker.shortcodeName}>
              <div className="default-sticker-preview">
                {showPreview ? (
                  <img
                    alt={`Default sticker ${shortcode}`}
                    onError={() => {
                      setFailedPreviews((current) => {
                        const next = new Set(current);
                        next.add(sticker.shortcodeName);
                        return next;
                      });
                    }}
                    src={sticker.stickerPreviewUrl}
                  />
                ) : (
                  <span>
                    <span className="default-preview-emoji" aria-hidden="true">
                      {sticker.emojiName}
                    </span>
                    Preview unavailable
                  </span>
                )}
              </div>
              <div className="default-sticker-copy">
                <h3>
                  <span className="default-sticker-emoji" aria-hidden="true">
                    {sticker.emojiName}
                  </span>
                  {shortcode}
                </h3>
                <p>{sticker.description}</p>
              </div>
            </article>
          );
        })}
      </div>
    </section>
  );
}
