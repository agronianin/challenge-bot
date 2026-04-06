FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/challenge-bot-0.1.0-SNAPSHOT.jar /app/app.jar
COPY src/main/resources/application.yml /app/default-config/application.yml
COPY src/main/resources/logback-spring.xml /app/default-config/logback-spring.xml
COPY src/main/resources/messages /app/default-config/messages
COPY docker/entrypoint.sh /app/entrypoint.sh
RUN mkdir -p /app/logs /app/external-config && chmod +x /app/entrypoint.sh
ENTRYPOINT ["/app/entrypoint.sh"]
