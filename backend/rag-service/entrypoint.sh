#!/bin/sh
set -e
if [ -n "$JWT_SECRET_FILE" ] && [ -f "$JWT_SECRET_FILE" ]; then
  export JWT_SECRET=$(cat "$JWT_SECRET_FILE")
fi
exec java $JAVA_OPTS -jar app.jar
