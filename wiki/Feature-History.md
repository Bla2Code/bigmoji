# История функций spec-kit

Wiki собрана из последовательных спецификаций проекта и сверена с текущим кодом. Файлы spec-kit формально имеют статус `Draft`, поэтому этот раздел описывает эволюцию требований, а не релизные гарантии.

## 002 — Discord Emoji-to-Sticker Bot

Базовый продукт:

- обработка сообщения из одного emoji;
- guild-isolated custom mappings;
- PostgreSQL и MinIO;
- in-memory lookup cache;
- встроенные 😊, ❤️, 🎉, 👍 и 🔥;
- upload/list/delete REST API;
- Discord OAuth2, signed session и проверка Manage Server.

Ключевое правило: custom mapping имеет приоритет; fallback используется только при успешном поиске без результата, но не при storage/database failure.

## 003 — Emoji Listener Logging

Добавлена наблюдаемость получения и обработки Discord events без изменения бизнес-решений. Контекст логов позволяет отличить отсутствие event от фильтрации или ошибки дальнейшей обработки.

## 004 — Default Sticker Replacement

Исправлено фактическое использование ресурсов `src/main/resources/default-stickers/` и добавлены decision states: mapped, unmapped, no emoji, missing resource.

## 005 — Sticker Delivery and Author Name

Вместо отправки MinIO URL backend загружает bytes и посылает Discord attachment. Webhook использует guild display name и avatar исходного автора. Текст `БОТ` к username не добавляется: Discord отображает стандартный bot badge сам. При проблемах с webhook выполняется fallback от имени бота.

## 006 — Web UI

Появилось отдельное React/TypeScript/Vite приложение:

- публичная landing page с демонстрацией продукта;
- Discord onboarding;
- guild selector;
- upload/list/delete management;
- адаптивные и доступные UI states;
- отдельный Docker image с nginx proxy к `/api`.

## 007 — Display Custom Assets

Management UI стал показывать реальные preview:

- server custom emoji image и readable name;
- загруженный sticker image;
- группировку нескольких stickers на trigger;
- устойчивые loading/unavailable states;
- same-origin защищённый endpoint `/api/mappings/{id}/preview`.

Эта функция меняет только административное отображение и не меняет правила замены сообщений в Discord.

## Источники

- `specs/002-discord-emoji-stickers/`
- `specs/003-emoji-listener-logging/`
- `specs/004-default-sticker-replacement/`
- `specs/005-fix-sticker-delivery/`
- `specs/006-ui-web-app/`
- `specs/007-display-custom-assets/`
