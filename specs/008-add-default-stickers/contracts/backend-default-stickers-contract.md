# Contract: Default Stickers Backend API

**Consumer**: `ui/` React admin application and backend replacement tests  
**Provider**: Bigmoji backend  
**Scope**: Bundled default sticker catalog metadata and previews  
**Credentials**: Browser requests include backend session cookies according to existing `/api/**` session rules

## List Default Stickers

```http
GET /api/stickers/default
```

### Response 200

The endpoint returns all bundled default sticker entries. After this feature, the response contains 9 entries.

```json
[
  {
    "emojiName": "😢",
    "shortcodeName": "cry",
    "description": "Crying face",
    "stickerPreviewUrl": "/api/stickers/default/cry/preview",
    "stickerPreviewState": "available"
  },
  {
    "emojiName": "😮",
    "shortcodeName": "open_mouth",
    "description": "Face with open mouth",
    "stickerPreviewUrl": "/api/stickers/default/open_mouth/preview",
    "stickerPreviewState": "available"
  },
  {
    "emojiName": "😔",
    "shortcodeName": "pensive",
    "description": "Pensive face",
    "stickerPreviewUrl": "/api/stickers/default/pensive/preview",
    "stickerPreviewState": "available"
  },
  {
    "emojiName": "🫩",
    "shortcodeName": "face_with_bags_under_eyes",
    "description": "Face with bags under eyes",
    "stickerPreviewUrl": "/api/stickers/default/face_with_bags_under_eyes/preview",
    "stickerPreviewState": "available"
  }
]
```

### Required Fields

| Field | Required | Notes |
|-------|----------|-------|
| `emojiName` | Yes | Unicode emoji identity for the default sticker |
| `shortcodeName` | Yes | Canonical lookup key without surrounding colons |
| `description` | Yes | Human-readable display label |
| `stickerPreviewState` | Yes | `available` or `unavailable` |
| `stickerPreviewUrl` | No | Present only when preview bytes can be served |

### Required Catalog Entries

The response must include these new entries:

| Emoji | shortcodeName | Expected shortcode display |
|-------|---------------|----------------------------|
| 😢 | `cry` | `:cry:` |
| 😮 | `open_mouth` | `:open_mouth:` |
| 😔 | `pensive` | `:pensive:` |
| 🫩 | `face_with_bags_under_eyes` | `:face_with_bags_under_eyes:` |

The response must continue to include the existing `smile`, `heart`, `party`, `thumbsup`, and `fire` entries unchanged.

## Preview Bundled Default Sticker

```http
GET /api/stickers/default/{shortcodeName}/preview
```

### Path Rules

- `shortcodeName` must match an existing default catalog entry.
- Path traversal, raw file paths, and unknown names must not be used to resolve resources.
- Unknown `shortcodeName` values return `404`.

### Response 200

- Body is the bundled PNG bytes for the requested default sticker.
- `Content-Type` is `image/png`.
- Response must not expose classpath or filesystem details.

### Error Handling

| Backend Result | Required Behavior |
|----------------|-------------------|
| Resource exists | Return `200 image/png` with bytes |
| Known catalog entry but missing/unreadable resource | Return a non-success response suitable for image fallback and log an actionable warning |
| Unknown `shortcodeName` | Return `404` |
| Unauthenticated request | Follow existing `/api/**` session behavior |

## Replacement Contract

- Normalized `cry`, `open_mouth`, `pensive`, and `face_with_bags_under_eyes` keys must resolve to bundled fallback assets when no custom mapping exists.
- Existing custom mappings remain preferred over bundled defaults.
- Existing `smile`, `heart`, `party`, `thumbsup`, and `fire` mappings keep their prior emoji names, shortcode names, descriptions, and fallback behavior.
- Unsupported inputs and mixed-content messages remain outside replacement scope.

## Test Expectations

Backend tests must verify:

- default catalog size is 9;
- all four new entries are present with expected names and descriptions;
- `fallbackFor` loads each new key from bundled resources;
- Unicode 😢, 😮, 😔, and 🫩 normalize to the expected keys;
- old default keys still normalize and load;
- `GET /api/stickers/default` returns preview metadata for new entries;
- preview endpoint returns PNG bytes for a known default;
- unknown preview names do not resolve to arbitrary resources.
