# ORG-2 - Indipendenza web/Swing e recuperabilita Git

Data: 2026-10-13. Stato: COMPLETED. Indipendenza tecnica verificata, cronologia controllata e recuperabilita del bundle provata. Nessuna separazione definitiva eseguita.

## Perimetro

Repository reale: `/Users/aim_defix/Desktop/Gestionale.Negozio.Computer `, con spazio finale. Branch `miglioramenti-gestionale`, HEAD `b077f7afe42b08c7976713d14552cbd58e9107a2`. Le due modifiche documentali di ORG-1 sono state preservate. Nessun sorgente applicativo, dipendenza, migrazione o configurazione operativa modificato.

Lo step verifica l'indipendenza tecnica e identifica lo storico; non esegue la separazione delle cartelle, non certifica parita funzionale completa e non autorizza cancellazioni. Non sono stati eseguiti commit, push, tag, merge, deploy o operazioni sui database/volumi operativi.

## Prova di indipendenza

Archivio acquisito in sola lettura da GitHub per il commit esatto `b077f7afe42b08c7976713d14552cbd58e9107a2`, non da un branch mobile. SHA-256 dell'archivio ricevuto: `13f80c6a44597e5e4d5642eaabd02571f6de0dc271442c954253340351b0e85b`.

Directory temporanea: `/private/tmp/gestisco-org2-Ge0gB1`. Non e una cartella definitiva ne un backup durevole.

- `web-only`: contiene web, script, configurazioni e documentazione versionati, ma NON contiene la directory radice `src/` o `pom-legacy.xml`.
- `legacy-only`: contiene soltanto `src/` e `pom-legacy.xml` dello stesso commit, senza `web/`, database operativi o file `.dat` locali.
- Nessuna `.git` inizializzata nelle copie sorgenti di verifica. Successivamente e stato creato un clone bare temporaneo dal bundle reale per provarne il recupero, non un repository artificiale nella copia sanificata. Nessun `target` o `node_modules` originale riutilizzato; dipendenze installate dalla cache locale.
- I 722 blob delle copie sorgenti sono stati confrontati con gli hash Git del commit nel clone recuperato: 722 corrispondenze, zero differenze. Tre assertion confermano l'assenza di src/pom legacy nella copia web e di web nella copia legacy. Verificatore: `verify-export.mjs` nella directory temporanea.

### Evidenze statiche

- `pom.xml` e un aggregatore con il solo modulo `web/backend`; `web/backend/pom.xml` usa il parent Spring Boot senza parent locale.
- Il Dockerfile backend copia solo pom e sorgenti del backend, oltre al resolver dei segreti da `scripts/security`. Il Dockerfile frontend usa il proprio contesto. Non copiano il legacy Swing.
- Workflow CI e Security compilano backend/frontend e richiamano script web; nessun comando di build Swing richiesto.
- Ricerca nei sorgenti web, script, workflow, deploy e Compose: nessun import dei package legacy `model`, `service`, `repository`, `utils`, `factory`, `strategy`, `command`, `decorator`, `ui`; nessun riferimento a `javax.swing`, `pom-legacy.xml` o JAR legacy necessario al web.
- `PdfReportWriter` usa `java.awt.Color` per OpenPDF. E una classe della piattaforma Java per il colore, non una dipendenza dal progetto Swing: non va eliminata con una sostituzione indiscriminata.
- Le occorrenze di `legacy` nelle migrazioni, nell'idempotenza e nei dati storici web non indicano codice Swing e devono essere preservate.
- Il JAR backend prodotto non contiene i package di root del legacy ne FlatLaf, secondo l'ispezione delle entry ZIP.
- La suite backend comprende controlli contrattuali che leggono script, workflow e documentazione dalla root: separare il prodotto NON significa copiare soltanto la directory `web/`.

### Test eseguiti

Toolchain locale: Maven 3.9.16, Java 23.0.1 con compilazione `release 17`, Node 26.8.1. JDK 17 non installato localmente al controllo; questo step non costituisce una nuova prova runtime su Java 17.

| Copia | Comando | Esito |
|---|---|---|
| web-only, root | `mvn -B -o verify` nel sandbox | Fallito: 320 test, 3 errori Actuator per `SocketException: Operation not permitted` sul bind delle porte locali |
| web-only, root | `mvn -B -o verify` fuori sandbox | PASS: 320 test, 0 failure/error/skip, packaging Spring Boot riuscito |
| web-only/web/frontend | `npm ci --offline` | PASS: installazione da lockfile/cache; non presentata come nuova scansione online vulnerabilita |
| web-only/web/frontend | `npm test` | PASS: 205 test in 42 file |
| web-only/web/frontend | `npm run build` | PASS: TypeScript e build Vite |
| web-only/web/frontend | `npm run typecheck:e2e` | PASS: typecheck, non esecuzione browser |
| legacy-only | `mvn -B -o -f pom-legacy.xml verify` | PASS: 41 test, 0 failure/error/skip, JAR eseguibile generato |

Log nella directory temporanea: `backend.log`, `backend-unrestricted.log`, `legacy.log`, `npm-ci.log`, `frontend-tests.log`, `frontend-build.log`.

Non rieseguiti: PostgreSQL/Testcontainers, suite browser, build/avvio Docker, scansione dipendenze, GUI Swing o validazione con operatore. Non e stata modificata alcuna migrazione. Gli esiti remoti del consolidamento precedente restano evidenze distinte.

## Snapshot Swing da conservare

Scelta tecnica: contenuti `src/` e `pom-legacy.xml` della baseline pubblicata `9f9e23ab1089f640efe7d88a91debba24ac0884a`.

- `git diff --exit-code 9f9e23a HEAD -- src pom-legacy.xml`: exit 0, contenuti invariati rispetto alla copia appena testata.
- Tree `src/`: `c44219a280430335505c4c21ab6065b95286ac40`; blob `pom-legacy.xml`: `36572563d4de060d325248b6ff34edbd86175f0c`.
- Versione attuale: 82 file sotto `src/` (69 sorgenti applicativi, 13 file test); `main` ne contiene soltanto 33.
- Confronto `main..HEAD` sul perimetro legacy: 63 file modificati/aggiunti includendo il pom dedicato. La versione piu recente comprende ordini, magazzino, audit, documenti simulati e UI modernizzata che non vanno persi scegliendo automaticamente il vecchio `main`.
- JAR verificato: `legacy-only/target/gestionale-negozio-computer-legacy-1.0.0.jar`, SHA-256 `414aeba70c1ce0d55508539e0ac0f718d3f6d21e22d3c16f28d7f4fcc48ed126`. Il checksum identifica questo artifact, non promette build bit-per-bit riproducibili.

La scelta non crea un tag o una release. La cronologia precedente va preservata; i dati `.dat` necessari richiedono ancora classificazione e un backup distinto. Nessun dato personale e incluso nell'archivio sorgenti di prova.

## Cronologia: evidenze e recupero verificato

Riferimenti locali identificati:

| Ref | Commit |
|---|---|
| main e origin/main | `6da79514b5f354ea730be255d05c36855477b0e6` |
| miglioramenti-gestionale e origin/miglioramenti-gestionale | `b077f7afe42b08c7976713d14552cbd58e9107a2` |
| baseline-pre-publication-20260727 | `6da3f076a307d6a9872a191e80934daacde41d36` |
| miglioramenti-gestionale-pre-publication | `6da3f076a307d6a9872a191e80934daacde41d36` |

`git rev-list --count --branches --not --remotes` restituisce 3. Commit non raggiungibili dai riferimenti remoti locali:

1. `6da3f076a307d6a9872a191e80934daacde41d36`: docs: record baseline verification status.
2. `7eb650fc7002148c692ed3c99f07c72462836928`: test: align CI artifact assertion with baseline metadata.
3. `bd06256cf57dd5b97de75caeff749892f55eb8b7`: chore: establish reviewed project baseline.

Il progress precedente documenta che queste branch furono conservate intenzionalmente prima della pubblicazione. La presenza di contenuti simili sul remoto non rende sacrificabili i commit locali. Un clone del solo remoto non ne garantirebbe la conservazione.

`git fsck --full`, esportazione locale HEAD e bundle `--all` hanno inizialmente incontrato attese prolungate nella lettura degli oggetti locali. I tentativi iniziali bloccati sono stati terminati senza modificare oggetti o ref; i tentativi sostitutivi fuori sandbox sono infine terminati correttamente. Il controllo dei file aperti aveva osservato entrambi in lettura su `.git/objects/59/7ae438a20a69eac1a17fd82d92960fdd366c19`.

Il conteggio degli attributi macOS `dataless` e passato dai 1.404 osservati in ORG-1 a 406 durante ORG-2; questo non dimostra integrita o piena disponibilita. L'oggetto in attesa non mostrava piu `dataless` al controllo: la sola assenza dell'icona nuvola non basta. Non e stata dimostrata corruzione e nessun oggetto e stato riparato, sovrascritto o eliminato.

Esiti conclusivi:

- `git fsck --full` sul repository reale: exit 0, nessun oggetto mancante o corrotto. Segnala soltanto il dangling tree `25b1357382dc754cd770f70aa447f77033afcfa2` e il dangling blob vuoto `e69de29bb2d1d6434b8b29ae775ad8c2e48c5391`. Sono oggetti non raggiungibili dalle ref, non corruzione; conservati nell'originale, nessun prune eseguito.
- `git bundle create ... --all`: completato. `git bundle verify`: archivio con storia completa, sei ref piu HEAD, senza prerequisiti esterni.
- Clone mirror dal bundle in `recovery.git`: riuscito; `git fsck --full --strict`: exit 0, senza segnalazioni. Le sei ref corrispondono esattamente all'inventario originale; 25 commit raggiungibili, compresi i tre locali non pubblicati.
- Copia durevole locale fuori dal Desktop sincronizzato: `/Users/aim_defix/GestiscoBackups/ORG2-20261013/history.bundle`. Directory di backup creata con permessi 700. Bundle ricontrollato dopo la copia.
- SHA-256 del bundle temporaneo e della copia conservata: `a52fc8a2bb709c73f5fefcf1c67f71d609abb3f44cbb8be8a409ede64f000efc`.

Il bundle protegge gli oggetti raggiungibili dalle ref, non file ignorati/non committati, configurazioni, reflog, oggetti dangling o database. Non e un backup off-site o un backup del gestionale completo. I documenti organizzativi non committati vengono conservati separatamente accanto al bundle; dati e configurazioni richiedono il censimento previsto prima degli spostamenti.

## Accettazione e ripresa

- [x] Build e suite standard web senza sorgenti Swing.
- [x] Build e suite Swing senza web e dati locali.
- [x] Snapshot legacy identificato con hash e confronto.
- [x] Riferimenti e commit locali non pubblicati censiti.
- [x] Lettura e verifica completa degli oggetti Git tramite fsck.
- [x] Bundle completo creato, verificato, recuperato in clone separato e conservato fuori dalla directory temporanea.

ORG-2 e COMPLETED; ORG-3 non iniziato. Prima degli spostamenti rivalidare HEAD, stato locale e riferimenti, preservare i documenti non committati e censire dati/configurazioni/volumi necessari. Usare la cronologia verificata come fonte di recupero, senza sostituire l'originale con un clone del solo remoto. Il filesystem sincronizzato puo introdurre nuove attese o evizioni: non trattare il successo odierno come garanzia permanente.

La dismissione definitiva di Swing richiede inoltre i controlli operativi di `LEGACY_SWING.md`, inclusi eventuali dati storici e parita dei flussi. Questa prova tecnica autorizza una futura separazione verificata, non la cancellazione della vecchia applicazione.

Beneficio reale: il futuro Gestisco.IO puo essere compilato senza trascinarsi l'app desktop; lo storico corretto e identificato e i commit non pubblicati non verranno persi durante il riordino.

Avanzamento: 2/6 milestone organizzative concluse (33.3%, conteggio non pesato). Roadmap applicativa invariata a 32/48 (66.7%). Nessuna stima di prontezza commerciale.
