#!/bin/sh
set -eu

REPEATS=${PLAYWRIGHT_FIREFOX_DIAGNOSTIC_REPEATS:-0}
case "$REPEATS" in
  0) exit 0 ;;
  30|100) ;;
  *) printf 'Numero di ripetizioni Firefox non valido: usare 0, 30 o 100.\n' >&2; exit 2 ;;
esac

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)
cd "$ROOT_DIR/web/frontend"
printf 'Diagnostica Firefox: %s ripetizioni, nessun retry, stop al primo errore.\n' "$REPEATS"
DEBUG=pw:browser npx --no-install playwright test e2e/accessibility.spec.ts \
  --project=firefox \
  --grep='^.*la pagina di accesso non presenta violazioni axe critiche o serie$' \
  --repeat-each="$REPEATS" --retries=0 --max-failures=1 \
  --reporter=line --trace=on --output=test-results/firefox-diagnostic
