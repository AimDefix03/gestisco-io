# ORG-4 - Verifica identita documentale Gestisco.IO

Data: 2026-10-15. Stato: COMPLETED, con evidenza finale di installazione nel manifest privato `completion.json`. Una copia di staging da sola non prova l'installazione.

## Ambito

Solo identita e organizzazione documentale del checkout web `/Users/aim_defix/Desktop/Gestisco.IO`. Nessuna nuova funzione, modifica UI, migrazione o pubblicazione. La decisione autorizzata e recepita nell'[addendum](GESTISCO_IDENTITY_ADDENDUM.md); il master non viene riscritto.

## Modifiche

| File | Prima | Dopo |
|---|---|---|
| README.md | Identita del gestionale computer e presentazione precedente | Gestisco.IO, flussi reali con limiti, stack, verifiche, provenienza e stato di pubblicazione |
| AGENTS.md | Priorita delle fonti non allineata ad A.7 | Gerarchia master, root web, scope, stop e comandi coerenti |
| IMPLEMENTATION_PROGRESS.md | Prossimo ORG-4 | Esito ORG-4 e prossimo ORG-5, roadmap applicativa invariata |
| docs/PRODUCT_SCOPE.md | Vecchia stima di maturita e visione didattica | Identita nuova, scope congelato e nessun indice di prontezza inventato |
| docs/ARCHITECTURE.md | Albero con Swing nella root, versioni e bootstrap obsoleti | Albero web separato, versioni dai POM, bootstrap applicativo distinto dalle fixture test |
| docs/ROADMAP.md | Stima di maturita 95-97% | Conteggi distinti e pausa applicativa dopo 4.3 |
| docs/REPOSITORY_ORGANIZATION.md | ORG-3 corrente | ORG-4 corrente; inventario storico conservato |
| docs/LOCAL_LAYOUT.md | Nome documentale ancora da decidere | Nome recepito; destinazione GitHub e runtime ancora da gestire |
| docs/DEPLOYMENT.md | Esempi senza preflight della nuova cartella e bootstrap descritto in modo assoluto | Preflight per ambienti e volumi; distinzione tra default operativi e configurazione test |
| docs/GESTISCO_IDENTITY_ADDENDUM.md | Assente | Decisioni autorizzate, compatibilita e criteri di accettazione |
| docs/ORG4_VERIFICATION.md | Assente | Questo rapporto |

## Verifiche

- Snapshot iniziale: 645 file esistenti tracciati o non ignorati. Confronto SHA-256 prima della copia passato su tutti i 645: nessuna modifica concorrente rilevata, master compreso.
- 59 collegamenti Markdown locali nei documenti coinvolti risolti correttamente. Controllate destinazioni file, non anchor Markdown o URL esterni.
- Coerenza positiva di identita, scope, stop, fonte primaria e prossimo step; presenza dei quattro script npm e dei due runner citati nel README; modulo Maven web-only, versioni Boot/Modulith e default bootstrap confrontati con i file reali.
- Cinque casi negativi in memoria rifiutati correttamente: titolo errato, scope alterato, avanzamento automatico, master rimosso dalle istruzioni e link locale mancante. Nessun file del progetto alterato per questi casi.
- `git diff --check` senza errori. Controllati anche whitespace e marker di conflitto dei candidati; `git diff --no-index --check` fra snapshot e candidati non segnala whitespace (exit 1 indica le differenze presenti, non un test applicativo fallito).
- Installazione limitata a nove documenti esistenti e due nuovi, con controllo degli hash immediatamente prima della sostituzione e backup preventivo. Il controllo finale confronta gli 11 documenti con i candidati e i 636 file fuori scope con il manifest iniziale, oltre a HEAD, branch, ref, remote, indice e insieme delle 83 rimozioni legacy.
- Il primo controllo seriale e stato interrotto durante lente letture del Desktop; il confronto integrale ripetuto con otto letture concorrenti limitate e passato. Nessun controllo saltato per accelerare il risultato.

Comandi del verificatore privato: `env UV_THREADPOOL_SIZE=8 node verify.mjs` e, dopo la copia, `env UV_THREADPOOL_SIZE=8 node verify.mjs --installed`. I risultati completi sono in `candidate-results.json` e `installed-results.json` nel backup ORG-4; `completion.json` attesta la chiusura dopo l'ultimo controllo. Il verificatore e uno strumento documentale, non una suite applicativa.

SHA-256 del master invariato: `cb5fc5a663e4acc2b01c01d94556c0ef1d9cab62ede036426f90b70a521ffa78`.

## Database e sicurezza

Nessuna migrazione, query sui dati operativi, rotazione di secret o modifica di Docker. Nessuna credenziale aggiunta ai documenti. Non e una nuova scansione Gitleaks, SCA, pentest o prova di conformita. I finding F-17, F-18, F-19, F-29 e F-30 restano aperti secondo il progress.

## Git e limiti

Branch `miglioramenti-gestionale`, HEAD iniziale `b077f7afe42b08c7976713d14552cbd58e9107a2`. Nessun commit, push, merge, remote o modifica ai riferimenti Git prevista. Le 83 rimozioni legacy preesistenti nel checkout web sono intenzionali e non vengono annullate.

Le suite Maven, Vitest, PostgreSQL, Playwright e le build non sono rieseguite per questo step documentale. Le prove nelle cartelle definitive restano quelle del [rapporto ORG-3](ORG3_LOCAL_VERIFICATION.md), datate 2026-10-14. Nessuna verifica operatore, browser, DNS, marchio, disponibilita repository o validazione legale.

Le evidenze private sono conservate in `/Users/aim_defix/GestiscoBackups/ORG4-20261015`: snapshot dei documenti, manifest SHA-256 e verificatore. Non sostituiscono backup off-site. Nessun dato o secret va allegato a una review pubblica. I manifest considerano file tracciati e non ignorati; non costituiscono una nuova verifica dei volumi Docker o dei backup di dati, sui quali ORG-4 non opera.

## Avanzamento e beneficio

Organizzazione 4/6 (66.7%), roadmap applicativa 32/48 (66.7%) invariata. Non e un indice di vendibilita. Prossimo ORG-5, solo su autorizzazione; Step 4.4 non iniziato.

Caso reale: un collaboratore trova il web nella cartella corretta, comprende quali workflow esistono e quali sono futuri, e non avvia i vecchi volumi pensando che il cambio di nome abbia migrato l'ambiente.
