#!/usr/bin/env bash
set -euo pipefail
set -x

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
CA_CERT="${CA_CERT:-$ROOT_DIR/.certs/ca.crt}"

GATEWAY="http://localhost:8080"
KEYCLOAK="https://keycloak.local:8443"
REALM="knowledge"
USER="admin"
PASS="admin123"

COOKIE_JAR="$(mktemp)"
LOGIN_HTML="$(mktemp)"
LOGIN_HEADERS="$(mktemp)"
LOGIN_RESPONSE="$(mktemp)"

# 1) Demande d'auth via le gateway -> redirect vers Keycloak
AUTH_LOCATION=$(curl -sS -D - -o /dev/null -c "$COOKIE_JAR" \
  "$GATEWAY/oauth2/authorization/keycloak" \
  | awk -F': ' '/^Location:/ {print $2}' | tr -d '\r')

INITIAL_SESSION=$(awk '/\tSESSION\t/ {print $7}' "$COOKIE_JAR" | tail -n1)
echo "SESSION after /oauth2/authorization: ${INITIAL_SESSION:-<empty>}"

echo "Auth redirect: $AUTH_LOCATION"

# 2) Récupère la page de login Keycloak
curl -sS --cacert "$CA_CERT" -b "$COOKIE_JAR" -c "$COOKIE_JAR" \
  "$AUTH_LOCATION" -o "$LOGIN_HTML"

# Extract hidden fields + login button to replay the form.
declare -a FORM_ARGS=()
while IFS= read -r field; do
  name=${field%%=*}
  value=${field#*=}
  if [ -n "$name" ]; then
    FORM_ARGS+=(--data-urlencode "$name=$value")
  fi
done <<EOF
$(sed -n '/id="kc-form-login"/,/form/ s/.*<input[^>]*type="hidden"[^>]*name="\\([^"]*\\)"[^>]*value="\\([^"]*\\)".*/\\1=\\2/p' "$LOGIN_HTML")
EOF

LOGIN_BUTTON_VALUE=$(sed -n '/id="kc-form-login"/,/form/ s/.*name="login"[^>]*value="\\([^"]*\\)".*/\\1/p' "$LOGIN_HTML" | head -n1)
if [ -n "$LOGIN_BUTTON_VALUE" ]; then
  FORM_ARGS+=(--data-urlencode "login=$LOGIN_BUTTON_VALUE")
fi

# 3) Extrait l'action du formulaire de login
LOGIN_ACTION=$(sed -n '/id="kc-form-login"/,/form/ s/.*action="\([^"]*\)".*/\1/p' "$LOGIN_HTML" | head -n1)
LOGIN_ACTION=$(printf '%s' "$LOGIN_ACTION" | sed 's/&amp;/\&/g')

case "$LOGIN_ACTION" in
  http*) ;;
  /*) LOGIN_ACTION="$KEYCLOAK$LOGIN_ACTION" ;;
  *) LOGIN_ACTION="$KEYCLOAK/$LOGIN_ACTION" ;;
esac

echo "Login action: $LOGIN_ACTION"

# 4) Poste les identifiants
curl -sS --cacert "$CA_CERT" -b "$COOKIE_JAR" -c "$COOKIE_JAR" \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  ${FORM_ARGS[@]+"${FORM_ARGS[@]}"} \
  --data-urlencode "username=$USER" \
  --data-urlencode "password=$PASS" \
  -D "$LOGIN_HEADERS" -o "$LOGIN_RESPONSE" \
  "$LOGIN_ACTION"

# 5) Suivre le redirect de retour vers le gateway (callback)
LOGIN_STATUS=$(awk 'NR==1 {print $2}' "$LOGIN_HEADERS")
echo "Login status: $LOGIN_STATUS"
CALLBACK_URL=$(awk -F': ' 'tolower($1)=="location" {print $2}' "$LOGIN_HEADERS" | tr -d '\r' | tail -n1)
echo "Callback: $CALLBACK_URL"

if [ "$LOGIN_STATUS" != "302" ] || [ -z "$CALLBACK_URL" ]; then
  echo "Login failed: no redirect from Keycloak. Response body:" >&2
  echo "Response headers:" >&2
  sed -n '1,50p' "$LOGIN_HEADERS" >&2
  sed -n '1,200p' "$LOGIN_RESPONSE" >&2
  exit 1
fi

echo "Callback response headers:"
curl -sS -D - -o /dev/null -b "$COOKIE_JAR" -c "$COOKIE_JAR" \
  "$CALLBACK_URL"

# Show stored cookies for debugging.
echo "Cookie jar:"
sed -n '1,200p' "$COOKIE_JAR"

# Extract SESSION cookie value and call API explicitly with it.
SESSION_VALUE=$(awk '/\tSESSION\t/ {print $7}' "$COOKIE_JAR" | tail -n1)
echo "SESSION from jar: ${SESSION_VALUE:-<empty>}"
if [ -n "${INITIAL_SESSION:-}" ] && [ -n "${SESSION_VALUE:-}" ] && [ "$INITIAL_SESSION" != "$SESSION_VALUE" ]; then
  echo "SESSION changed between auth request and callback. OAuth2 state may be lost." >&2
fi

# 6) Appel API avec la session gateway
if [ -n "${SESSION_VALUE:-}" ]; then
  curl -i -H "Cookie: SESSION=$SESSION_VALUE" "$GATEWAY/api/users/me"
else
  curl -i -b "$COOKIE_JAR" "$GATEWAY/api/users/me"
fi
