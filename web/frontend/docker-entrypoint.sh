#!/bin/sh
set -eu

NGINX_LOGIN_RATE=${NGINX_LOGIN_RATE:-10r/m}
NGINX_LOGIN_BURST=${NGINX_LOGIN_BURST:-10}
NGINX_LOGIN_RETRY_AFTER_SECONDS=${NGINX_LOGIN_RETRY_AFTER_SECONDS:-60}
NGINX_REGISTER_RATE=${NGINX_REGISTER_RATE:-5r/m}
NGINX_REGISTER_BURST=${NGINX_REGISTER_BURST:-5}
NGINX_REGISTER_RETRY_AFTER_SECONDS=${NGINX_REGISTER_RETRY_AFTER_SECONDS:-60}

for value in "$NGINX_LOGIN_RATE" "$NGINX_REGISTER_RATE"; do
  if ! printf '%s\n' "$value" | grep -Eq '^[1-9][0-9]*r/[smh]$'; then
    printf 'Configurazione rate limit Nginx non valida.\n' >&2
    exit 78
  fi
done

for value in \
  "$NGINX_LOGIN_BURST" \
  "$NGINX_LOGIN_RETRY_AFTER_SECONDS" \
  "$NGINX_REGISTER_BURST" \
  "$NGINX_REGISTER_RETRY_AFTER_SECONDS"
do
  if ! printf '%s\n' "$value" | grep -Eq '^[1-9][0-9]*$'; then
    printf 'Configurazione numerica Nginx non valida.\n' >&2
    exit 78
  fi
done

export \
  NGINX_LOGIN_RATE \
  NGINX_LOGIN_BURST \
  NGINX_LOGIN_RETRY_AFTER_SECONDS \
  NGINX_REGISTER_RATE \
  NGINX_REGISTER_BURST \
  NGINX_REGISTER_RETRY_AFTER_SECONDS

mkdir -p \
  /tmp/nginx-client-body \
  /tmp/nginx-proxy \
  /tmp/nginx-fastcgi \
  /tmp/nginx-uwsgi \
  /tmp/nginx-scgi

envsubst '${NGINX_LOGIN_RATE} ${NGINX_LOGIN_BURST} ${NGINX_LOGIN_RETRY_AFTER_SECONDS} ${NGINX_REGISTER_RATE} ${NGINX_REGISTER_BURST} ${NGINX_REGISTER_RETRY_AFTER_SECONDS}' \
  < /etc/nginx/templates/default.conf.template \
  > /tmp/default.conf

# HTTPS opzionale sulla porta 8443 (ADR 0009: i cookie Secure richiedono TLS). La porta 8080 resta per
# l'healthcheck interno; in modalita TLS il compose pubblica soltanto la 8443.
NGINX_TLS_CERTIFICATE_FILE=${NGINX_TLS_CERTIFICATE_FILE:-}
NGINX_TLS_CERTIFICATE_KEY_FILE=${NGINX_TLS_CERTIFICATE_KEY_FILE:-}
if [ -n "$NGINX_TLS_CERTIFICATE_FILE" ] || [ -n "$NGINX_TLS_CERTIFICATE_KEY_FILE" ]; then
  for file in "$NGINX_TLS_CERTIFICATE_FILE" "$NGINX_TLS_CERTIFICATE_KEY_FILE"; do
    case "$file" in
      /*) ;;
      *)
        printf 'Certificato e chiave TLS richiedono entrambi un percorso assoluto.\n' >&2
        exit 78
        ;;
    esac
    if [ ! -r "$file" ]; then
      printf 'File TLS non leggibile: %s\n' "$file" >&2
      exit 78
    fi
  done
  awk -v certificate="$NGINX_TLS_CERTIFICATE_FILE" -v key="$NGINX_TLS_CERTIFICATE_KEY_FILE" '
    { print }
    $0 ~ /^    listen 8080;$/ {
      print "    listen 8443 ssl;"
      print "    ssl_certificate " certificate ";"
      print "    ssl_certificate_key " key ";"
      print "    ssl_protocols TLSv1.2 TLSv1.3;"
      print "    ssl_session_cache off;"
    }
  ' /tmp/default.conf > /tmp/default-tls.conf
  if ! grep -q '^    listen 8443 ssl;$' /tmp/default-tls.conf; then
    printf 'Listener TLS non applicato alla configurazione Nginx.\n' >&2
    exit 78
  fi
  mv /tmp/default-tls.conf /tmp/default.conf
fi

exec nginx -c /etc/nginx/nginx.conf -g 'daemon off;'
