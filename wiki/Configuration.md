# Конфигурация

Конфигурация читается Spring Boot из environment variables. `.env.example` содержит шаблон для локального запуска.

## Discord и OAuth

| Переменная | По умолчанию | Назначение |
|---|---|---|
| `DISCORD_TOKEN` | пусто | bot token |
| `DISCORD_CLIENT_ID` | пусто | OAuth client/application ID |
| `DISCORD_CLIENT_SECRET` | пусто | OAuth client secret |
| `DISCORD_REDIRECT_URI` | `http://localhost:8080/api/auth/discord/callback` | callback, должен совпадать с Developer Portal |
| `DISCORD_BOT_PERMISSIONS` | `125952` | permissions integer для install URL |
| `UI_URL` | `http://localhost:3000` | redirect после успешного входа |

Для UI за nginx рекомендуется callback на UI origin: `http://localhost:3000/api/auth/discord/callback`. Nginx передаст запрос backend.

## Сессия

| Переменная | По умолчанию | Назначение |
|---|---|---|
| `AUTH_SESSION_SECRET` | небезопасный dev default | ключ подписи session cookie; production требует секрет не короче 32 случайных байт |
| `AUTH_SESSION_TTL` | `PT8H` | TTL в ISO-8601 Duration |
| `AUTH_SECURE_COOKIES` | `false` | включить `true` за HTTPS |

Cookie `bigmoji_session` — signed, HttpOnly и SameSite=Lax. Статические API keys для admin API не поддерживаются.

## PostgreSQL

| Переменная | По умолчанию |
|---|---|
| `POSTGRES_URL` | `jdbc:postgresql://localhost:5432/bigmoji` |
| `POSTGRES_USER` | `bigmoji` |
| `POSTGRES_PASSWORD` | `bigmoji` |

JPA использует `ddl-auto: update`. Для production желательно заменить автоматическое изменение схемы управляемыми миграциями.

## MinIO

| Переменная | По умолчанию |
|---|---|
| `MINIO_ENDPOINT` | `http://localhost:9000` |
| `MINIO_ACCESS_KEY` | `minioadmin` |
| `MINIO_SECRET_KEY` | `minioadminpassword` |
| `MINIO_BUCKET_PREFIX` | `bigmoji-` |

Из backend-контейнера endpoint должен ссылаться на Compose service, а не на `localhost`. Метаданные находятся в PostgreSQL, бинарные файлы — в per-guild bucket MinIO.

## Асинхронная обработка

| Переменная | По умолчанию |
|---|---|
| `ASYNC_CORE_POOL_SIZE` | `10` |
| `ASYNC_MAX_POOL_SIZE` | `20` |

Увеличивайте значения только после наблюдения за очередью и задержкой обработки Discord events.

## UI container

| Переменная | Значение в Compose |
|---|---|
| `BIGMOJI_BACKEND_URL` | `http://bigmoji-app:8080` |

Переменная подставляется в nginx config при старте контейнера. Браузер обращается к относительному `/api`, а не к внутреннему container URL.

## Production checklist

- использовать HTTPS и `AUTH_SECURE_COOKIES=true`;
- заменить все пароли и access keys из примера;
- не передавать secrets в клиентский bundle;
- точно синхронизировать `DISCORD_REDIRECT_URI` с Discord Developer Portal;
- закрыть публичный доступ к PostgreSQL, MinIO API и MinIO Console;
- настроить резервное копирование PostgreSQL и MinIO;
- ограничить DEBUG-логи, если они создают избыточный объём.
