import { Sparkles } from "lucide-react";
import type { DefaultSticker } from "../../api/types";
import { EmptyState } from "../../components/EmptyState";

interface DefaultStickerListProps {
  stickers: DefaultSticker[];
}

export function DefaultStickerList({ stickers }: DefaultStickerListProps) {
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
        {stickers.map((sticker) => (
          <article className="default-sticker" key={sticker.shortcodeName}>
            <span className="default-emoji" aria-hidden="true">
              {sticker.emojiName}
            </span>
            <div>
              <h3>:{sticker.shortcodeName}:</h3>
              <p>{sticker.description}</p>
            </div>
          </article>
        ))}
      </div>
    </section>
  );
}
