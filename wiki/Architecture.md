# Архитектура

Bigmoji состоит из четырёх runtime-сервисов: backend/Discord bot, web UI, PostgreSQL и MinIO.

```mermaid
flowchart LR
    User["Пользователь Discord"] --> Discord["Discord API"]
    Admin["Администратор guild"] --> UI["React UI / nginx"]
    UI -->|"/api, session cookie"| App["Spring Boot + JDA"]
    Discord <-->|"events, webhooks, attachments"| App
    App --> DB[("PostgreSQL")]
    App --> Storage[("MinIO")]
```

## Backend

Основные пакеты:

| Пакет | Назначение |
|---|---|
| `api` | REST-контроллеры, DTO и единый формат ошибок |
| `auth` | Discord OAuth2, signed cookie session и guild authorization |
| `discord` | JDA listener, webhook-отправка, custom emoji metadata |
| `emoji` | распознавание и нормализация emoji |
| `sticker` | выбор маппинга, fallback-каталог и cache |
| `storage` | загрузка и чтение файлов из MinIO |
| `domain` | JPA entity и repository |
| `config` | JDA, MinIO, async executor, MVC и health |

`EmojiMessageListener` обрабатывает события асинхронно через `messageExecutor`. Поиск выполняется по ключу `{guildId, normalizedEmoji}`. Пользовательские маппинги имеют приоритет над встроенными.

## Поток замены

```mermaid
flowchart TD
    A["MessageReceivedEvent"] --> B{"Автор — бот?"}
    B -->|"да"| Ignore["Игнорировать"]
    B -->|"нет"| C{"Ровно один emoji?"}
    C -->|"нет"| Ignore
    C -->|"да"| D{"Сообщение из guild?"}
    D -->|"нет"| Ignore
    D -->|"да"| E["Нормализация"]
    E --> F["Поиск custom mapping"]
    F --> G{"Найден?"}
    G -->|"нет"| H["Поиск default sticker"]
    G -->|"да"| I["Загрузить bytes из MinIO"]
    H --> J{"Fallback найден?"}
    J -->|"нет"| Ignore
    J -->|"да"| K["Прочитать classpath resource"]
    I --> L["Отправить через webhook"]
    K --> L
    L --> M{"Webhook доступен?"}
    M -->|"нет"| N["Отправить от имени бота"]
    M -->|"да"| O["Имя и avatar автора"]
    N --> P["Удалить исходное сообщение"]
    O --> P
```

## Данные

Таблица `sticker_mapping` хранит:

| Поле | Тип | Назначение |
|---|---|---|
| `id` | UUID | идентификатор маппинга |
| `guild_id` | VARCHAR(32) | Discord guild |
| `emoji_name` | VARCHAR(64) | Unicode emoji, shortcode или custom emoji |
| `minio_bucket_name` | VARCHAR(128) | bucket вида `bigmoji-{guildId}` |
| `minio_object_key` | VARCHAR(256) | UUID и расширение файла |
| `created_at`, `updated_at` | timestamp | аудит времени |

Индексы по `(guild_id, emoji_name)` и `guild_id` ускоряют обработку сообщений и выдачу списка. Встроенные стикеры и auth session в БД не сохраняются.

## Web UI

React-приложение использует маршруты `/`, `/app` и `/app/guilds/:guildId`. В production nginx раздаёт SPA и проксирует `/api/` на `BIGMOJI_BACKEND_URL`, поэтому OAuth cookie остаётся same-origin для браузера. UI не имеет прямого доступа к PostgreSQL, MinIO или bot token.

## Авторизация

Discord OAuth2 запрашивает `identify guilds`. Backend формирует подписанную HttpOnly cookie `bigmoji_session`, содержащую пользователя, срок действия и список управляемых guild. Каждый API-запрос повторно проверяется по этому списку; переданный клиентом `guildId` сам по себе не считается доверенным.
