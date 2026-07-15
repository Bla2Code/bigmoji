# Разработка и тестирование

## Структура репозитория

```text
src/main/java/com/bigmoji/   backend и Discord bot
src/main/resources/         application.yml и default stickers
src/test/                   backend tests
ui/                         React/TypeScript приложение
specs/                      spec-kit: spec, plan, research, model, contracts, tasks
docker-compose.yml          PostgreSQL, MinIO, backend и UI profile
```

## Backend

```bash
./gradlew bootRun
./gradlew test
./gradlew spotlessApply
```

Точечные наборы:

```bash
./gradlew test --tests '*EmojiMessageListenerTest'
./gradlew test --tests '*StickerMappingControllerContractTest'
./gradlew test --tests '*StickerMappingControllerIntegrationTest'
./gradlew test --tests '*StickerMappingServiceTest'
./gradlew test --tests '*WebhookUsernameSanitizerTest'
```

Backend использует Java toolchain 21. Форматирование Java выполняет Spotless с Google Java Format.

## UI

```bash
cd ui
npm install
npm run lint
npm run typecheck
npm test
npm run test:e2e
npm run build
```

Vitest покрывает компоненты и API layer, Playwright — onboarding, mapping management, homepage, proxy smoke и responsive behavior.

## Проверка полного сценария

1. Запустите PostgreSQL, MinIO, backend и UI.
2. Войдите через Discord пользователем с Manage Server.
3. Загрузите mapping для Unicode и custom emoji.
4. Проверьте preview сразу после upload и после refresh.
5. Отправьте одиночный emoji в Discord.
6. Убедитесь, что пришло вложение от display name автора со стандартным bot badge.
7. Уберите `MANAGE_WEBHOOKS` и проверьте fallback от имени бота.
8. Проверьте, что текст, несколько emoji и unsupported emoji остаются без изменений.
9. Удалите mapping и проверьте исчезновение из UI и API.

## Работа со spec-kit

Каждая функция в `specs/<NNN-name>/` обычно содержит:

- `spec.md` — сценарии и requirements;
- `plan.md` — technical context и структура реализации;
- `research.md` — принятые решения;
- `data-model.md` — данные и state transitions;
- `contracts/` — API/UI/logging contracts;
- `quickstart.md` — проверка функции;
- `tasks.md` — implementation checklist.

При расхождении ранней спецификации и текущего кода фиксируйте решение в новой spec/clarification и обновляйте Wiki. Для инструкций запуска текущие `build.gradle.kts`, `docker-compose.yml`, `.env.example` и `application.yml` являются источником истины.
