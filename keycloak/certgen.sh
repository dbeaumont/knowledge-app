#!/usr/bin/env sh
set -eu
set -x

# Root CA used for all local HTTPS endpoints.
ROOT_CA_DIR=${ROOT_CA_DIR:-.certs}
ROOT_CA_KEY=${ROOT_CA_KEY:-$ROOT_CA_DIR/ca.key}
ROOT_CA_CERT=${ROOT_CA_CERT:-$ROOT_CA_DIR/ca.crt}
ROOT_CA_TRUSTSTORE=${ROOT_CA_TRUSTSTORE:-$ROOT_CA_DIR/truststore.p12}
ROOT_CA_TRUSTSTORE_PASS=${ROOT_CA_TRUSTSTORE_PASS:-changeit}

# Server cert specific to Keycloak (signed by the root CA).
# Keycloak must have a keystore to expose HTTPS.
KEYCLOAK_HOST=${KEYCLOAK_HOST:-keycloak.local}
KEYCLOAK_CERT_DIR=${KEYCLOAK_CERT_DIR:-.certs/keycloak}
KEYCLOAK_KEY=${KEYCLOAK_KEY:-$KEYCLOAK_CERT_DIR/keycloak.key}
KEYCLOAK_CSR=${KEYCLOAK_CSR:-$KEYCLOAK_CERT_DIR/keycloak.csr}
KEYCLOAK_CERT=${KEYCLOAK_CERT:-$KEYCLOAK_CERT_DIR/keycloak.crt}
KEYSTORE_FILE=${KEYSTORE_FILE:-$KEYCLOAK_CERT_DIR/keystore.p12}
KEYSTORE_PASS=${KEYSTORE_PASS:-changeit}
KEYCLOAK_CERT_DAYS=${KEYCLOAK_CERT_DAYS:-825}

if [ ! -f "$ROOT_CA_KEY" ] || [ ! -f "$ROOT_CA_CERT" ]; then
  echo "Root CA not found at $ROOT_CA_DIR. Run 'make ca-root' first." >&2
  exit 1
fi

# Keep files under .certs/ to avoid polluting the repo root.
mkdir -p "$KEYCLOAK_CERT_DIR"

if [ ! -f "$KEYCLOAK_KEY" ]; then
  openssl genrsa -out "$KEYCLOAK_KEY" 2048
fi

if [ ! -f "$KEYCLOAK_CSR" ]; then
  openssl req -new -key "$KEYCLOAK_KEY" \
    -subj "/C=FR/O=Local Dev/OU=AI Knowledge Workspace/CN=$KEYCLOAK_HOST" \
    -out "$KEYCLOAK_CSR"
fi

# SubjectAltName is required by browsers and JVMs.
SAN_CONFIG=$(mktemp)
cat > "$SAN_CONFIG" <<EOF
[v3_req]
subjectAltName=DNS:$KEYCLOAK_HOST,DNS:keycloak,DNS:localhost,IP:127.0.0.1
EOF

if [ ! -f "$KEYCLOAK_CERT" ]; then
  openssl x509 -req -in "$KEYCLOAK_CSR" \
    -CA "$ROOT_CA_CERT" -CAkey "$ROOT_CA_KEY" -CAcreateserial \
    -out "$KEYCLOAK_CERT" -days "$KEYCLOAK_CERT_DAYS" -sha256 \
    -extfile "$SAN_CONFIG" -extensions v3_req
fi

rm -f "$SAN_CONFIG"

if [ ! -f "$KEYSTORE_FILE" ]; then
  openssl pkcs12 -export \
    -inkey "$KEYCLOAK_KEY" \
    -in "$KEYCLOAK_CERT" \
    -certfile "$ROOT_CA_CERT" \
    -out "$KEYSTORE_FILE" \
    -password "pass:$KEYSTORE_PASS"
fi

# Truststore used by JVM services to trust the local CA.
# Without it, backend services cannot validate Keycloak's HTTPS certificate.
if [ -f "$ROOT_CA_TRUSTSTORE" ]; then
  echo "Truststore already present: $ROOT_CA_TRUSTSTORE"
else
  if ! command -v keytool >/dev/null 2>&1; then
    echo "Truststore missing and keytool not available." >&2
    echo "Run 'make ca-root' on the host to generate $ROOT_CA_TRUSTSTORE." >&2
    exit 1
  fi
  keytool -importcert -noprompt \
    -alias local-root-ca \
    -file "$ROOT_CA_CERT" \
    -keystore "$ROOT_CA_TRUSTSTORE" \
    -storetype PKCS12 \
    -storepass "$ROOT_CA_TRUSTSTORE_PASS"
fi

echo "Keycloak cert ready: $KEYCLOAK_CERT"
echo "Keycloak keystore ready: $KEYSTORE_FILE"
echo "Truststore ready: $ROOT_CA_TRUSTSTORE"
