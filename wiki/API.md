# REST API

Base path: `/api`. Интерактивное описание доступно в Swagger UI: `/swagger-ui.html`.

## Авторизация

Защищённые endpoints используют HttpOnly cookie `bigmoji_session`. Для mapping-запросов backend проверяет, что guild входит в `manageableGuilds` текущей сессии.

| Код | Значение |
|---|---|
| `401` | cookie отсутствует, повреждена или истекла |
| `403` | пользователь вошёл, но не вправе управлять guild |
| `404` | ресурс или маппинг не найден |

## Auth endpoints

| Метод | Endpoint | Назначение |
|---|---|---|
| `GET` | `/api/auth/discord/login` | redirect на Discord OAuth2 |
| `GET` | `/api/auth/discord/callback?code=...&state=...` | проверка state, создание session, redirect в UI |
| `GET` | `/api/auth/me` | текущий пользователь и manageable guilds |
| `POST` | `/api/auth/logout` | очистить session cookie |
| `GET` | `/api/auth/discord/install-url?guildId=...` | получить URL установки бота |

## Sticker endpoints

### Список пользовательских маппингов

```http
GET /api/mappings/{guildId}
```

Пример preview-capable ответа:

```json
[
  {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "guildId": "123456789012345678",
    "emojiName": ":party_blob:",
    "isDefault": false,
    "createdAt": "2026-06-24T16:00:00Z",
    "updatedAt": "2026-06-24T16:00:00Z",
    "stickerPreviewUrl": "/api/mappings/a1b2c3d4-e5f6-7890-abcd-ef1234567890/preview",
    "stickerPreviewState": "available",
    "emojiPreview": {
      "id": "987654321098765432",
      "name": "party_blob",
      "shortcode": ":party_blob:",
      "imageUrl": "https://cdn.discordapp.com/emojis/987654321098765432.png",
      "animated": false,
      "available": true
    }
  }
]
```

Для Unicode/text trigger поле `emojiPreview` может отсутствовать. Недоступный sticker сохраняется в выдаче со `stickerPreviewState: "unavailable"`.

### Загрузка

```http
POST /api/mappings
Content-Type: multipart/form-data
```

Поля: `guildId` — numeric Discord snowflake, `emojiName` — trigger, `file` — PNG/JPEG/WEBP до 512 КБ. Успех: `201` и объект в той же форме, что у list endpoint.

```bash
curl -X POST http://localhost:8080/api/mappings \
  -b cookies.txt \
  -F 'guildId=123456789012345678' \
  -F 'emojiName=😊' \
  -F 'file=@sticker.png'
```

### Preview файла

```http
GET /api/mappings/{id}/preview
```

Backend проверяет права на guild и возвращает bytes с `image/png`, `image/jpeg`, `image/webp` либо `application/octet-stream`.

### Удаление

```http
DELETE /api/mappings/{id}
```

Успех: `204 No Content`.

### Встроенные стикеры

```http
GET /api/stickers/default
```

Возвращает metadata встроенного набора. Этот endpoint не включает пользовательские маппинги.

## Health

```http
GET /actuator/health
```

Endpoint не требует session cookie.

## Ошибки

Контроллеры возвращают единый объект с `timestamp`, `status`, `error`, `message` и `path`. UI должен преобразовывать ошибки в понятные состояния и не показывать stack trace, MinIO credentials, bucket или object key.
