# Запуск и эксплуатация

Для Docker нужны Docker Engine и Compose; для локальной Java-сборки — JDK 25 и Maven Wrapper. Перед работой читай [AGENTS.md](AGENTS.md), при восстановлении БД — [HANDOFF.md](../HANDOFF.md).

## Конфигурация

Все команды ниже выполняются из корня проекта. Создание файлов — только при первой настройке; существующий `runtime.env` не перезаписывай:

```bash
mkdir -p .local
chmod 700 .local
cp -n .env.example .local/runtime.env
chmod 600 .local/runtime.env
# Заполни runtime.env реальными значениями до запуска.
```

Секреты и серверные адреса храни по [карте приватных данных](../docs/PRIVATE-DATA.md). `scripts/compose.sh` передает `.local/runtime.env` и для подстановок Compose, и для окружения контейнеров. Используй его вместо голого `docker compose`: последний не подхватит этот файл для подстановок автоматически. `DB_NAME` применяется к PostgreSQL и обоим JDBC URL.

Локальный Java-запуск не читает env-файл автоматически: передавай переменные из `.local/runtime.env` через окружение или настройки IDE. Логи по умолчанию — `.local/logs`.

## Первый запуск с чистой БД

```bash
./scripts/start-db.sh
./scripts/run-migrations.sh
./scripts/init-config.sh
./scripts/restart-bot.sh
./scripts/compose.sh ps
./scripts/compose.sh logs --tail=100 bot
```

Liquibase применяется отдельно; Hibernate только проверяет схему (`ddl-auto: validate`). При ошибке миграций не запускай бота. `init-config.sh` собирает образ `challenge-bot:local` и копирует defaults в `.local/config/`; если каталог уже существует, скрипт завершается без перезаписи. Для обновления defaults сравни их с сохраненными настройками вручную.

Compose монтирует `.local/config/` только для чтения, `.local/logs/` для записи и `.local/datasets/exercises/videos/` в `/app/videos:ro`. Подготовка и перенос набора — [datasets/README.md](../datasets/README.md). После изменения runtime-конфигов выполни `./scripts/compose.sh restart bot`; при изменении env или Compose пересоздай контейнер через `restart-bot.sh`.

Для первого администратора напиши боту `/start`, затем выполни в БД со своим числовым Telegram user ID:

```sql
UPDATE tg_user SET role = 'ADMIN' WHERE tg_id = 123456789;
```

Снова отправь `/start`, заверши первичную настройку и импортируй CSV через администрирование. Добавь бота в тестовую группу с правами публикации и удаления его сообщений.

## Проверки и обновление

```bash
./mvnw clean test
# После запуска отдельной PostgreSQL и миграций, с переменными тестового окружения:
./mvnw spring-boot:run
```

При обновлении получи проверенную версию кода и сохрани резервные копии БД/конфигов, затем:

```bash
./scripts/run-migrations.sh
./scripts/restart-bot.sh
```

Dockerfile собирает с `-DskipTests`, поэтому сборка образа не заменяет тесты. Обновление major-версии PostgreSQL — отдельная операция с проверкой совместимости данных.

## Эксплуатационные ограничения

- Бот использует long polling: домен и входящий HTTP endpoint не нужны. Не запускай два экземпляра с одним токеном. Пустой токен или `replace_me` отключает только polling: scheduler и доступ к БД остаются активными.
- PostgreSQL `5432` и JDWP `5005` публикуются на `127.0.0.1`. Для удаленной отладки используй SSH-туннель; реальные адреса — только в `.local/servers.md`.
- PostgreSQL хранится в именованном volume `pgdata` (имя зависит от Compose project). Не меняй имя проекта при переносе без учета volume; `down -v` удаляет данные. Автоперезапуск настроен, но reboot и восстановление дампа нужно проверить на целевом сервере.
- Сохраняй дампы в `.local/backups/` и защищенную копию вне VPS. Git не резервирует локальные данные.
- `.local/logs/application-info.log` и `application-debug.log` ротируются; debug содержит SQL и bind-параметры. Перед передачей очищай приватные данные. `.local/config/logback-spring.xml` переопределяет встроенный файл.
- Не публикуй вывод `compose config` с подставленными секретами; для проверки синтаксиса используй `./scripts/compose.sh config --quiet`.
