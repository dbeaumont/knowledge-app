#!/bin/sh
set -e

TRUSTSTORE_PATH=$(printf '%s' "$JAVA_OPTS" | sed -n 's/.*trustStore=\([^ ]*\).*/\1/p')
if [ -n "$TRUSTSTORE_PATH" ]; then
  echo "Truststore: $TRUSTSTORE_PATH"
  ls -l "$TRUSTSTORE_PATH" || true
  if command -v keytool >/dev/null 2>&1; then
    keytool -list -keystore "$TRUSTSTORE_PATH" -storetype PKCS12 -storepass "${ROOT_CA_TRUSTSTORE_PASS:-changeit}" || true
  else
    echo "keytool not available; skipping truststore check."
  fi
else
  echo "Truststore not configured in JAVA_OPTS."
fi

exec java $JAVA_OPTS -jar app.jar
