# Быстрый старт

## Требования

- Docker и Docker Compose;
- Discord Application и bot token;
- для локальной backend-разработки — Java 21;
- для локальной UI-разработки — Node.js 22.12+ и npm.

## 1. Discord Application

В [Discord Developer Portal](https://discord.com/developers/applications):

1. Создайте application и bot.
2. Включите privileged intent `MESSAGE_CONTENT`.
3. Подготовьте client ID, client secret и bot token.
4. Зарегистрируйте OAuth callback `http://localhost:3000/api/auth/discord/callback` для полного Compose-запуска.
5. При установке боту нужны Send Messages, Manage Messages, Read Message History, Attach Files, Embed Links и Manage Webhooks.

## 2. Переменные окружения

```bash
cp .env.example .env
```

Обязательно замените как минимум:

```dotenv
DISCORD_TOKEN=...
DISCORD_CLIENT_ID=...
DISCORD_CLIENT_SECRET=...
DISCORD_REDIRECT_URI=http://localhost:3000/api/auth/discord/callback
UI_URL=http://localhost:3000
AUTH_SESSION_SECRET=<случайная строка не короче 32 байт>
POSTGRES_PASSWORD=<надёжный пароль>
MINIO_SECRET_KEY=<надёжный пароль>
```

Не коммитьте `.env`.

## 3. Запуск

Backend с инфраструктурой:

```bash
docker compose up -d postgres minio bigmoji-app
```

Полный стек с отдельным UI profile:

```bash
docker compose --profile ui up -d --build
```

Адреса после запуска:

| Сервис | URL |
|---|---|
| UI | http://localhost:3000 |
| Backend | http://localhost:8080 |
| Health | http://localhost:8080/actuator/health |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| MinIO Console | http://localhost:9001 |

Проверка:

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:3000/healthz
```

## 4. Первый вход и установка бота

1. Откройте `http://localhost:3000`.
2. Нажмите вход через Discord.
3. Выберите guild из списка доступных для управления.
4. Если бот ещё не установлен, используйте предложенную install-ссылку.
5. Загрузите PNG/JPEG/WEBP до 512 КБ и укажите emoji trigger.
6. Отправьте в Discord сообщение, содержащее только этот emoji.

## Локальный запуск backend

```bash
docker compose up -d postgres minio
./gradlew bootRun
```

При запуске приложения на host установите `MINIO_ENDPOINT=http://localhost:9000` и `POSTGRES_URL=jdbc:postgresql://localhost:5432/bigmoji`.

## Локальный запуск UI

```bash
cd ui
npm install
npm run dev
```

Vite доступен на `http://localhost:5173` и проксирует `/api` на backend `http://localhost:8080`.
