# Challenge Bot

Telegram-бот для групповых фитнес-челленджей: общие задания в группе, персональная нагрузка в личке. MVP не завершен.

Стек: Java 25, Spring Boot, Spring Data JPA, PostgreSQL, Pengrad Telegram Bot API, Liquibase и Docker Compose. Точные версии — в `pom.xml`, `.mvn/wrapper/maven-wrapper.properties` и `docker-compose.yml`.

## Документация по задаче

- [AGENTS.md](AGENTS.md) — общие правила и маршрутизация к частным инструкциям.
- [Promt.md](Promt.md) — поведение и продуктовые требования.
- [Status.md](Status.md) — готовность, ограничения и ближайшие задачи.
- [scripts/README.md](scripts/README.md) — конфигурация, запуск, сборка и обновление.
- [db/liquibase/AGENTS.md](db/liquibase/AGENTS.md) — правила изменения и проверки схемы.
- [datasets/README.md](datasets/README.md) — формат CSV, импорт и работа с приватным набором упражнений.
- [docs/PRIVATE-DATA.md](docs/PRIVATE-DATA.md) — где хранить секреты, адреса серверов, данные и резервные копии.
- [docs/GIT.md](docs/GIT.md) — ветки, рабочие копии и Pull Request.
- [HANDOFF.md](HANDOFF.md) — восстановление после потери VPS.

Все приватные материалы находятся в `.local/` и передаются отдельно от Git. В репозитории остаются код, общие инструкции и обезличенные шаблоны.
