# Эксплуатация и диагностика

## Health checks

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:3000/healthz
docker compose ps
docker compose logs -f bigmoji-app
```

Actuator публикует `health` и `info`. UI container имеет отдельный nginx endpoint `/healthz`.

## Диагностический поток listener

Для `com.bigmoji.discord` включён DEBUG. Ключевые события:

- `Message received` — JDA доставил событие;
- `Emoji detection result` — detector решил, является ли сообщение одиночным emoji;
- `Emoji normalization result` — нормализованный ключ;
- `Mapping lookup result` — найден ли custom/default asset;
- `Sticker loaded` — bytes доступны;
- `Sticker sent via webhook` — успешная отправка от имени автора;
- `WEBHOOK_FALLBACK` — отправка от имени бота;
- `RESOURCE_MISSING` — asset не удалось получить или отправить.

Логи содержат guild/channel/author context для корреляции, но production-сборщик следует настроить так, чтобы не хранить содержимое сообщений дольше необходимого.

## Типовые проблемы

### Бот не видит сообщения

- включите `MESSAGE_CONTENT` intent в Discord Developer Portal;
- проверьте, что bot token относится к тому же application;
- проверьте доступ к каналу и наличие `Message received` в логах.

### Emoji распознан, но не заменён

- найдите `Emoji detection result` и `Emoji normalization result`;
- проверьте `Mapping lookup result`;
- убедитесь, что сообщение состоит только из одного emoji;
- при ошибке БД/MinIO fallback намеренно не должен маскировать сбой.

### В Discord пришла ссылка вместо изображения

Текущая реализация должна отправлять `FileUpload`, а не presigned URL. Проверьте, что запущен актуальный image и в логах есть `Sticker loaded`/`Sticker sent`.

### Стикер отправляется от имени бота

Это fallback. Выдайте `MANAGE_WEBHOOKS`, проверьте, что канал является text channel, и найдите warning о permission или unknown webhook. Cache webhook хранится в памяти и сбрасывается при известных ошибках webhook.

### UI получает 401 после входа

- синхронизируйте `DISCORD_REDIRECT_URI` и `UI_URL`;
- вызывайте API с credentials/cookie;
- при HTTPS включите secure cookies;
- проверьте срок `AUTH_SESSION_TTL`.

### UI получает 403

Текущий Discord-пользователь должен быть owner либо иметь Administrator/Manage Server. Клиент не может расширить доступ, подменив `guildId`.

### UI возвращает nginx 404 на `/api`

Проверьте `BIGMOJI_BACKEND_URL` и доступность backend из сети UI container.

### Preview недоступен

Mapping остаётся управляемым. Проверьте наличие MinIO object, права пользователя на guild и backend-логи. API не должен раскрывать storage details в ответе.

## Резервное копирование

Согласованно сохраняйте PostgreSQL и MinIO: запись БД без объекта или объект без записи создают неполное состояние. Перед восстановлением остановите операции загрузки/удаления либо обеспечьте согласованный snapshot.

## Масштабирование

Cache и webhook cache находятся в памяти процесса. При нескольких backend replicas необходимо учитывать локальность cache, JDA connection и асинхронную обработку. Текущая архитектура ориентирована на один bot runtime; горизонтальное масштабирование требует отдельного проектного решения.
