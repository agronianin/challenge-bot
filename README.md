# Challenge Bot

## Текущее состояние

- Есть Spring Boot приложение с базовой интеграцией Telegram Bot API.
- Есть PostgreSQL через Spring Data JPA.
- Миграции Liquibase запускаются отдельно, а не при старте приложения.
- Есть Dockerfile и Docker Compose для бота и базы данных.

Бизнес-логика challenge пока реализована не полностью. На текущем этапе задача репозитория — дать стабильную основу для деплоя и дальнейшей разработки.

## Требования

- Java 25
- Maven 3.9+
- Docker и Docker Compose

## Конфигурация

Создай `.env` на основе `.env.example` и заполни его реальными значениями.

Важно:

- `.env` должен храниться только локально на машине разработчика и на сервере, в репозиторий его коммитить нельзя;
- `.env.example` хранится в репозитории как шаблон без секретов;
- `DATABASE_URL` в `.env` используется для локального запуска через `mvn spring-boot:run`.
- В Docker Compose для контейнера бота он переопределяется на `jdbc:postgresql://postgres:5432/challenge`.
- Если `BOT_TOKEN` пустой или равен `replace_me`, приложение стартует, но Telegram polling не запускается.

Практический вариант хранения секретов для этого проекта:

- реальные значения хранить в локальном `.env`, который игнорируется Git;
- на сервере также хранить отдельный локальный `.env`;
- для резервного хранения пароля БД лучше использовать менеджер паролей или зашифрованную заметку, а не репозиторий.

## Локальный запуск

1. Поднять PostgreSQL:

```bash
docker compose up -d postgres
```

2. Применить миграции:

```bash
docker compose --profile tools run --rm migrate
```

3. Запустить приложение:

```bash
mvn spring-boot:run
```

Логи по умолчанию пишутся в `./logs/app.log`.

## Запуск в Docker

1. Поднять PostgreSQL:

```bash
docker compose up -d postgres
```

2. Применить миграции:

```bash
docker compose --profile tools run --rm migrate
```

3. Поднять бота:

```bash
docker compose up -d --build bot
```

## Деплой на Ubuntu сервер

Минимальный сценарий:

1. Скопировать проект на сервер.
2. Создать `.env` из `.env.example`.
3. Заполнить реальный `BOT_TOKEN`.
4. Поднять PostgreSQL:

```bash
docker compose up -d postgres
```

5. Применить миграции:

```bash
docker compose --profile tools run --rm migrate
```

6. Поднять бота:

```bash
docker compose up -d --build bot
```

## Обновление на новую версию

Если меняется только код приложения или миграции, а PostgreSQL уже работает, достаточно:

```bash
git pull
docker compose --profile tools run --rm migrate
docker compose up -d --build bot
```

Почему здесь нет `docker compose up -d postgres`:

- если PostgreSQL уже поднят и его конфигурацию менять не нужно, повторно трогать его не надо;
- если позже ты поменяешь образ PostgreSQL в `docker-compose.yml`, то отдельное обновление базы стоит делать осознанно и вручную, потому что смена версии PostgreSQL поверх старого volume может требовать отдельной процедуры миграции данных.

Данные базы хранятся в Docker volume `pgdata`.

## Миграции базы данных

- Changelog: `db/liquibase/changelog.xml`
- Миграции применяются вручную отдельной командой:

```bash
docker compose --profile tools run --rm migrate
```

- Предпросмотр SQL без применения:

```bash
docker compose --profile tools run --rm migrate updateSQL
```

- История примененных migration:

```bash
docker compose --profile tools run --rm migrate history
```

По умолчанию команда `docker compose --profile tools run --rm migrate` выводит логи прямо в терминал. Так как используется `--rm`, контейнер удаляется после завершения, поэтому постоянного места для просмотра этих логов после выполнения нет. Если нужно сохранить вывод, его лучше сразу перенаправить в файл или запускать без `--rm`.

## Текущие ограничения

- `CsvImporter` пока не реализован.
- `ExerciseSelector` пока не реализован.
- `DailyRunner` пока не реализован.
- `UpdateHandler` пока содержит только минимальный тестовый сценарий.
