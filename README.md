# Challenge Bot (Spring Boot)

## What is ready
- Spring Boot skeleton with Telegram bot wiring.
- JPA entities and repositories.
- Liquibase changelog for PostgreSQL.
- CSV importer and selection logic placeholders.

## Requirements
- Java 25 (latest supported by Spring Boot 4.0.x)
- Maven 3.9+

## Environment
`.env` is already present; use `.env.example` as a reference.

```
BOT_TOKEN=replace_me
BOT_USERNAME=challenge_bot
CHAT_ID=replace_me

DB_NAME=challenge
DB_USER=challenge
DB_PASSWORD=challenge
DATABASE_URL=jdbc:postgresql://postgres:5432/challenge

TIMEZONE=Europe/Moscow
POST_TIME=08:00
EXERCISES_PER_DAY=3
GROUPS_PER_DAY=2
REPS_GROWTH_PERCENT=0.05
REPS_ROUND_MODE=ceil
LOG_LEVEL=INFO
```

## Liquibase
Liquibase changelog: `db/liquibase/changelog.xml`
Run Liquibase separately (example):

```
liquibase \
  --url="jdbc:postgresql://localhost:5432/challenge" \
  --username=challenge \
  --password=challenge \
  --changeLogFile=db/liquibase/changelog.xml \
  update
```

## Run locally
```
mvn spring-boot:run
```

## Run with Docker
```
docker compose up -d postgres
# run liquibase here
mvn spring-boot:run
```

## CSV format
```
id,name,group,base_reps,comment,video_path
```

## External PostgreSQL access
Docker exposes port `5432` and listens on all interfaces.
If you have an existing volume, `POSTGRES_INITDB_ARGS` won't reapply; recreate the volume if needed.
On Ubuntu with UFW, allow access from your IP only:

```
sudo ufw allow from <your_ip> to any port 5432 proto tcp
sudo ufw status
```

## TODO
- Implement selection logic in `ExerciseSelector`.
- Implement CSV import in `CsvImporter`.
- Implement daily scheduler flow in `DailyRunner`.
- Implement command parsing in `UpdateHandler`.
