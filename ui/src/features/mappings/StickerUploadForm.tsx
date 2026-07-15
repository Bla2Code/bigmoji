import { ImagePlus, Upload } from "lucide-react";
import { useEffect, useId, useState } from "react";
import type { FormEvent } from "react";
import { uploadMapping } from "../../api/mappings";
import { toApiError } from "../../api/client";
import type { ApiError, ManageableGuild } from "../../api/types";
import { Button } from "../../components/Button";
import { ErrorBanner } from "../../components/ErrorBanner";

const MAX_FILE_SIZE_BYTES = 512 * 1024;
const ALLOWED_IMAGE_TYPES = ["image/png", "image/jpeg", "image/webp", "image/gif"];

interface StickerUploadFormProps {
  guild: ManageableGuild;
  onSessionExpired: (error?: ApiError) => void;
  onUploaded: () => Promise<void>;
}

interface FieldErrors {
  emojiName?: string;
  file?: string;
}

export function StickerUploadForm({
  guild,
  onSessionExpired,
  onUploaded,
}: StickerUploadFormProps) {
  const emojiId = useId();
  const fileId = useId();
  const [emojiName, setEmojiName] = useState("");
  const [file, setFile] = useState<File | undefined>();
  const [previewUrl, setPreviewUrl] = useState<string | undefined>();
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [formError, setFormError] = useState<ApiError | undefined>();
  const [status, setStatus] = useState<"idle" | "uploading" | "success">("idle");

  useEffect(() => {
    return () => {
      if (previewUrl) {
        URL.revokeObjectURL(previewUrl);
      }
    };
  }, [previewUrl]);

  function handleFileChange(nextFile?: File) {
    if (previewUrl) {
      URL.revokeObjectURL(previewUrl);
    }

    setFile(nextFile);
    setPreviewUrl(nextFile ? URL.createObjectURL(nextFile) : undefined);
    setFieldErrors((current) => ({ ...current, file: undefined }));
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setFormError(undefined);
    setStatus("idle");

    const errors = validateForm(emojiName, file);
    setFieldErrors(errors);

    if (Object.keys(errors).length > 0 || !file) {
      return;
    }

    setStatus("uploading");

    try {
      await uploadMapping({
        emojiName: emojiName.trim(),
        file,
        guildId: guild.id,
      });
      setStatus("success");
      setEmojiName("");
      handleFileChange(undefined);
      await onUploaded();
    } catch (error) {
      const apiError = toApiError(error);
      if (apiError.kind === "unauthenticated") {
        onSessionExpired(apiError);
      }
      setFormError(apiError);
      setStatus("idle");
    }
  }

  return (
    <section className="mapping-section" aria-labelledby="upload-title">
      <div className="section-heading">
        <ImagePlus aria-hidden="true" size={20} />
        <div>
          <h2 id="upload-title">Upload custom sticker</h2>
          <p>{guild.name}</p>
        </div>
      </div>
      <form className="upload-form" onSubmit={handleSubmit}>
        <div className="field">
          <label htmlFor={emojiId}>Emoji trigger</label>
          <input
            aria-describedby={fieldErrors.emojiName ? `${emojiId}-error` : undefined}
            aria-invalid={Boolean(fieldErrors.emojiName)}
            id={emojiId}
            name="emojiName"
            onChange={(event) => {
              setEmojiName(event.target.value);
              setFieldErrors((current) => ({ ...current, emojiName: undefined }));
            }}
            placeholder="🔥, :fire:, or <:custom:123456789>"
            value={emojiName}
          />
          {fieldErrors.emojiName ? (
            <span className="field-error" id={`${emojiId}-error`} role="alert">
              {fieldErrors.emojiName}
            </span>
          ) : null}
        </div>

        <div className="field">
          <label htmlFor={fileId}>Sticker image</label>
          <input
            accept={ALLOWED_IMAGE_TYPES.join(",")}
            aria-describedby={fieldErrors.file ? `${fileId}-error` : undefined}
            aria-invalid={Boolean(fieldErrors.file)}
            id={fileId}
            name="file"
            onChange={(event) => handleFileChange(event.target.files?.[0])}
            type="file"
          />
          {fieldErrors.file ? (
            <span className="field-error" id={`${fileId}-error`} role="alert">
              {fieldErrors.file}
            </span>
          ) : null}
        </div>

        {previewUrl ? (
          <figure className="upload-preview">
            <img alt="Selected sticker preview" src={previewUrl} />
            <figcaption>{file?.name}</figcaption>
          </figure>
        ) : null}

        <ErrorBanner error={formError} title="Upload failed" />

        {status === "success" ? (
          <p className="success-message" role="status">
            Sticker mapping uploaded.
          </p>
        ) : null}

        <Button
          icon={<Upload aria-hidden="true" size={16} />}
          isLoading={status === "uploading"}
          type="submit"
        >
          Upload mapping
        </Button>
      </form>
    </section>
  );
}

function validateForm(emojiName: string, file?: File) {
  const errors: FieldErrors = {};

  if (!emojiName.trim()) {
    errors.emojiName = "Choose an emoji trigger.";
  }

  if (!file) {
    errors.file = "Choose a sticker image.";
  } else if (!ALLOWED_IMAGE_TYPES.includes(file.type)) {
    errors.file = "Use PNG, JPEG, WebP, or GIF.";
  } else if (file.size > MAX_FILE_SIZE_BYTES) {
    errors.file = "Use an image under 512 KB.";
  }

  return errors;
}
