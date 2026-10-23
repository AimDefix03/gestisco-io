#!/bin/sh
set -eu

# Esegue un comando nell'immagine ufficiale Playwright della stessa versione di @playwright/test,
# con browser e librerie di sistema gia installati. Evita `playwright install --with-deps`, che sui
# runner GitHub ha impiegato fino a 57 minuti per lo scaricamento apt da un mirror lento.
# Il container usa la rete dell'host per raggiungere lo stack, porta le etichette della run per
# il cleanup e riceve le variabili d'ambiente solo per nome, senza valori sulla riga di comando.

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)
. "$ROOT_DIR/scripts/ci/docker-run-safety.sh"

if [ "$#" -lt 3 ]; then
  printf 'Uso: %s <progetto-compose> <run-id> <comando> [argomenti...]\n' "$0" >&2
  exit 2
fi

PROJECT_NAME=$1
RUN_ID=$2
shift 2
docker_run_validate_project_name "$PROJECT_NAME" gestionale-prodlike-
docker_run_validate_id "$RUN_ID"

PLAYWRIGHT_PACKAGE="$ROOT_DIR/web/frontend/node_modules/@playwright/test/package.json"
if [ ! -f "$PLAYWRIGHT_PACKAGE" ]; then
  printf 'Pacchetto @playwright/test non installato: eseguire npm ci in web/frontend.\n' >&2
  exit 1
fi
PLAYWRIGHT_VERSION=$(node -p 'require(process.argv[1]).version' "$PLAYWRIGHT_PACKAGE")
case "$PLAYWRIGHT_VERSION" in
  *[!0-9.]*|.*|*.|*..*|'')
    printf 'Versione Playwright non valida: %s\n' "$PLAYWRIGHT_VERSION" >&2
    exit 1
    ;;
esac

IMAGE="mcr.microsoft.com/playwright:v$PLAYWRIGHT_VERSION-noble"
printf 'Playwright nel container %s\n' "$IMAGE"

exec docker run --rm --init --ipc=host --network host \
  --label "$DOCKER_RUN_PROJECT_LABEL=$PROJECT_NAME" \
  --label "$DOCKER_RUN_ID_LABEL=$RUN_ID" \
  --user "$(id -u):$(id -g)" \
  --env HOME=/tmp \
  --env CI \
  --env PLAYWRIGHT_BASE_URL \
  --env E2E_USERNAME \
  --env E2E_PASSWORD \
  --env GESTIONALE_BOOTSTRAP_SUPER_ADMIN_USERNAME \
  --env GESTIONALE_BOOTSTRAP_SUPER_ADMIN_PASSWORD \
  --env PLAYWRIGHT_FIREFOX_DIAGNOSTIC_REPEATS \
  --volume "$ROOT_DIR:$ROOT_DIR" \
  --workdir "$PWD" \
  "$IMAGE" "$@"
