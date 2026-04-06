#!/usr/bin/env sh
set -eu

JAVA_ARGS=""
if [ -f /app/external-config/logback-spring.xml ]; then
  JAVA_ARGS="$JAVA_ARGS -Dlogging.config=/app/external-config/logback-spring.xml"
fi

exec java $JAVA_ARGS -jar /app/app.jar
