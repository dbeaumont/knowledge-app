#!/usr/bin/env sh
set -eu

# Generates the local root CA and the JVM truststore used by services.

ROOT_CA_DIR=${ROOT_CA_DIR:-.certs}
ROOT_CA_KEY=${ROOT_CA_KEY:-$ROOT_CA_DIR/ca.key}
ROOT_CA_CERT=${ROOT_CA_CERT:-$ROOT_CA_DIR/ca.crt}
ROOT_CA_DAYS=${ROOT_CA_DAYS:-3650}
ROOT_CA_TRUSTSTORE=${ROOT_CA_TRUSTSTORE:-$ROOT_CA_DIR/truststore.p12}
ROOT_CA_TRUSTSTORE_PASS=${ROOT_CA_TRUSTSTORE_PASS:-changeit}

mkdir -p "$ROOT_CA_DIR"

if [ ! -f "$ROOT_CA_KEY" ]; then
  openssl genrsa -out "$ROOT_CA_KEY" 4096
fi

if [ ! -f "$ROOT_CA_CERT" ]; then
  openssl req -x509 -new -nodes -key "$ROOT_CA_KEY" -sha256 -days "$ROOT_CA_DAYS" \
    -subj "/C=FR/O=Local Dev/OU=AI Knowledge Workspace/CN=Local Root CA" \
    -out "$ROOT_CA_CERT"
fi

if [ ! -f "$ROOT_CA_TRUSTSTORE" ]; then
  if ! command -v keytool >/dev/null 2>&1; then
    echo "keytool not found. Install a JDK to generate $ROOT_CA_TRUSTSTORE." >&2
    exit 1
  fi
  keytool -importcert -noprompt \
    -alias local-root-ca \
    -file "$ROOT_CA_CERT" \
    -keystore "$ROOT_CA_TRUSTSTORE" \
    -storetype PKCS12 \
    -storepass "$ROOT_CA_TRUSTSTORE_PASS"
fi

echo "CA ready: $ROOT_CA_CERT"
echo "Truststore ready: $ROOT_CA_TRUSTSTORE"
