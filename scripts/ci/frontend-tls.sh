#!/bin/sh
# Certificato autofirmato per gli stack di verifica in HTTPS (ADR 0009). Valido 2 giorni per
# 127.0.0.1 e localhost, generato a ogni run nella cartella dei secret e cancellato con essa.
# Va incluso con `.` dai runner: esporta le variabili lette da docker-compose.e2e-tls.yml.

frontend_tls_prepare() {
  frontend_tls_dir=$1
  openssl req -x509 -newkey rsa:2048 -nodes -days 2 \
    -subj "/CN=gestionale-verifica-locale" \
    -addext "subjectAltName=IP:127.0.0.1,DNS:localhost" \
    -addext "basicConstraints=critical,CA:TRUE" \
    -keyout "$frontend_tls_dir/frontend-tls.key" \
    -out "$frontend_tls_dir/frontend-tls.crt" \
    >/dev/null 2>&1
  export GESTIONALE_FRONTEND_TLS_CERTIFICATE_FILE="$frontend_tls_dir/frontend-tls.crt"
  export GESTIONALE_FRONTEND_TLS_KEY_FILE="$frontend_tls_dir/frontend-tls.key"
  export GESTIONALE_FRONTEND_CONTAINER_PORT=8443
  # curl dei controlli di sicurezza verifica il certificato invece di disattivare la verifica con -k.
  export CURL_CA_BUNDLE="$frontend_tls_dir/frontend-tls.crt"
  # Solo i browser Playwright accettano il certificato autofirmato.
  export PLAYWRIGHT_TLS_SELF_SIGNED=true
}
