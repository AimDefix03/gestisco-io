# ORG-5 - Pubblicazione Gestisco.IO

Data: 2026-10-16. Stato: IN_PROGRESS. Fonte locale: `/Users/aim_defix/Desktop/Gestisco.IO`. Destinazione: [AimDefix03/gestisco-io](https://github.com/AimDefix03/gestisco-io).

## Decisioni autorizzate

- Pubblicazione del prodotto web con cronologia preservata, senza riscrivere la provenienza Swing.
- Dopo il preflight privato, passaggio al pubblico autorizzato esplicitamente. API GitHub al nuovo preflight: PUBLIC, repository vuoto, nessuna branch effettiva; endpoint rulesets accessibile e lista vuota.
- Diritti riservati, senza licenza open source: scelta esplicita dell'utente, recepita in README e NOTICE. Le condizioni delle dipendenze e dei contenuti di terzi non cambiano. Un repository pubblico resta tecnicamente copiabile e soggetto ai termini GitHub.
- Commit della separazione e pubblicazione iniziale nel nuovo repository vuoto. Nessun force push, cancellazione di ref, riscrittura della cronologia o pubblicazione automatica di tutte le branch locali.
- Nessun acquisto, deploy, modifica dei database operativi, avvio ORG-6 o Step 4.4.

## Baseline e provenienza

Il punto di partenza e `b077f7afe42b08c7976713d14552cbd58e9107a2`, branch locale `miglioramenti-gestionale`. Il consolidamento include le rimozioni intenzionali ORG-3 di 82 file Swing sotto src/ e di pom-legacy.xml, oltre ai documenti ORG-3/4/5. La storia completa resta conservata localmente e nel bundle privato ORG-2; sul nuovo remote si pubblica soltanto la linea del prodotto, non tutte le ref di archivio.

Il remote dedicato e `gestisco`, per non confonderlo con gli snapshot `refs/remotes/origin/*` ereditati dal vecchio progetto. Il primo push ha inizializzato main con la baseline web verificata: questa eccezione di bootstrap non autorizza push diretti successivi. La main locale storica non e stata spostata.

Il vecchio repository e la [PR #2](https://github.com/AimDefix03/gestionale-negozio-computer/pull/2) restano invariati, con PR aperta e draft come traccia storica. Lo sviluppo web prosegue nel nuovo repository. Chiusura della PR o archiviazione del legacy richiedono autorizzazione separata, non sono implicite in ORG-5.

## Gate e revisione

Prima di chiudere ORG-5 devono risultare verificati sul nuovo repository:

1. Snapshot completo e cronologia pubblicata senza rilevazioni Gitleaks, con controllo dei file locali esclusi.
2. CI su Java 17, frontend, PostgreSQL e stack prod-like; Security con audit dipendenze, Gitleaks e CodeQL.
3. Dependency review su una PR effettiva, poiche quel job non esegue la revisione delle dipendenze sul push iniziale.
4. Protezione main: almeno una approvazione, invalidazione delle review obsolete, conversazioni risolte, branch aggiornata, amministratori inclusi, niente force push o cancellazione. Required checks associati ai nomi reali ricevuti da GitHub Actions, inclusi i singoli gate CI e le due lingue CodeQL.
5. Rilettura delle protezioni da API, commit e stato remoto documentati. Nessun merge automatico.

L'utente ha indicato `AimDefix03` come revisore, ma e l'account autore delle modifiche. GitHub non consente di approvare la propria PR: i merge successivi richiedono un revisore distinto. Non creare approvazioni fittizie e non togliere il requisito per sbloccare un merge. Una PR con test verdi ma senza review rimane in attesa.

## Pubblicazione e verifiche - 2026-10-16

- Baseline pubblicata su main: [`cabc12f52e0a6adeb5712babeb78a5b462562e0e`](https://github.com/AimDefix03/gestisco-io/commit/cabc12f52e0a6adeb5712babeb78a5b462562e0e). 102 file interessati dalla separazione e documentazione; nessun cambiamento applicativo in web/, scripts/, .github/, pom.xml o master.
- 649 file del commit confrontati byte per byte con il candidato: corrispondenza completa. Nessun target, dist, node_modules, secret, backup, dump o .dat tracciato. Checkout pulito dopo il commit.
- Gitleaks v8.30.1 senza rete: candidato circa 3.03 MB e cronologia esatta pubblicata (23 commit, circa 3.65 MB), nessuna rilevazione. Ripetuta anche la scansione del bundle completo con 25 commit pre-baseline: nessuna rilevazione. La differenza dipende dalle tre revisioni di archivio non raggiungibili dalla linea pubblicata, preservate privatamente; nessuna ref storica eliminata.
- Copia temporanea del candidato: `mvn -B -o verify`, 320 test passati, zero errori o skip, packaging riuscito. Java locale 23.0.1 con release 17; il runtime Java 17 viene verificato dalla CI remota.
- Frontend: `npm ci --offline`, `npm test` 205/205 in 42 file, `npm run build` e `npm run typecheck:e2e` passati. L'audit offline non sostituisce la scansione remota aggiornata.
- Main protetta, riletta tramite API: branch aggiornata obbligatoria, una review, invalidazione review obsolete, conversazioni risolte, amministratori inclusi, force push e cancellazione disabilitati. Dodici required checks associati a GitHub Actions app ID 15368, nomi confrontati con i check-run reali.
- Pipeline baseline avviate: [CI 38925595841](https://github.com/AimDefix03/gestisco-io/actions/runs/38925595841) e [Security 38925596067](https://github.com/AimDefix03/gestisco-io/actions/runs/38925596067). Esito completo ancora da acquisire in questo checkpoint; Dependency review sul push e skipped e non viene contato come passato.
- Branch `codex/org5-publication-verification` per documentare gli esiti e verificare una PR effettiva. Nessun merge o bypass della review. Il requisito del revisore distinto resta aperto.
- Evidenze e backup privati: `/Users/aim_defix/GestiscoBackups/ORG5-PUBLIC-20261016`, incluso bundle della baseline. Nessuna credenziale o dato operativo copiati nel repository.

## Ripresa e correzione del gate - 2026-10-17

- La CI baseline 38925595841 ha fallito il test Chromium di navigazione da tastiera anche al retry: 18 E2E passati, uno fallito. Quality gate skipped, non superato. La Security baseline 38925596067 e passata, con Dependency review non applicabile al push.
- PR [#13](https://github.com/AimDefix03/gestisco-io/pull/13), head iniziale `e85fd267518e4a77743673f18368742c98004779`: CI 38926093331 passata, ma questo non annulla il difetto intermittente precedente. Security 38926093388 inizialmente fallita per Dependency graph non abilitato, non per una vulnerabilita rilevata. Dopo l'abilitazione confermata dall'utente, rerun dei job falliti concluso con successo il 17 ottobre, inclusa Dependency review.
- Diagnosi verificata in AccessibleDialog: il ripristino differito puo sovrascrivere il focus su una nuova destinazione o su un altro dialog; il selettore di fallback combinato sceglie per ordine DOM e puo preferire Aggiorna alla scheda attiva. Anche il replay degli effetti in StrictMode lasciava un timer obsoleto.
- Quattro nuovi test deterministici falliti prima della patch e passati dopo. La patch conserva il nuovo focus intenzionale, annulla il timer obsoleto al replay, preserva il controllo di origine e preferisce esplicitamente la scheda attiva. Restano coperti Escape, focus trap e ripristino normale.
- E2E rafforzato: dopo la conferma deve risultare focalizzata la scheda Magazzino senza chiamare focus dal test; ArrowLeft deve attivare e focalizzare Catalogo. Nessuna attesa fissa, assertion rimossa o test disabilitato.
- Candidato locale: frontend 209/209 in 42 file, build TypeScript/Vite e typecheck E2E passati. Backend e migrazioni invariati; nessuna nuova verifica backend locale dichiarata. Le prove browser e CI/Security sul commit della correzione restano da acquisire.
- Modifiche limitate al dialog condiviso, ai relativi test unitari/E2E e a questo checkpoint. Nessuna dipendenza, configurazione operativa, database o master modificati. Pubblicazione soltanto nella branch della PR, senza merge o riduzione delle protezioni.
- Stop: ORG-5 resta IN_PROGRESS finche gli esiti sul candidato aggiornato non sono verificati. Anche con controlli verdi la PR richiede un revisore distinto dall'autore.

## Integrita

- `git fsck --full --strict` sul checkout autorevole: exit 0.
- Il repository non conteneva file LICENSE/COPYING/NOTICE nella cronologia ispezionata; nessuna licenza storica rimossa. Questa verifica non sostituisce l'inventario delle licenze delle dipendenze.
- Snapshot, scansioni, commit e push conclusi; chiusura delle pipeline e verifica PR ancora necessarie prima di completare ORG-5.
- Nessun codice applicativo, dipendenza, migrazione o configurazione operativa modificato. Master da mantenere con SHA-256 `cb5fc5a663e4acc2b01c01d94556c0ef1d9cab62ede036426f90b70a521ffa78`.

## Preflight privato precedente

Il repository era stato inizialmente creato privato. Ruleset e protezione classica restituivano HTTP 403 con richiesta di upgrade o repository pubblico; Code Scanning non era abilitato. Lo step era stato fermato prima di commit/push, senza ridurre i gate o effettuare acquisti. Questa limitazione e stata affrontata con la successiva scelta esplicita del pubblico, non con un bypass.

Le prove locali precedenti del 16 ottobre restano distinte: Gitleaks v8.30.1 su mirror del bundle (25 commit, circa 5.32 MB) e snapshot (647 file, circa 3.02 MB), nessuna rilevazione. La prima scansione Docker del checkout aveva letto zero commit per un errore Git: non vale come esito positivo. Quelle scansioni non coprono automaticamente questo nuovo candidato.

Evidenze storiche private in `/Users/aim_defix/GestiscoBackups/ORG5-20261016`. Non pubblicare le copie di recovery o i file di configurazione locali. La disponibilita iCloud puo rallentare le letture: verificare gli hash prima della pubblicazione, non sostituire file mancanti con copie presunte equivalenti.

## Stato e beneficio

Organizzazione 4/6 (66.7%), applicazione 32/48 (66.7%): ORG-5 non ancora completato. Non e una misura di vendibilita. Caso reale: un collaboratore trova il prodotto web aggiornato e puo proporre modifiche attraverso controlli automatici e revisione, senza alterare direttamente main o confonderlo con il progetto universitario Swing.
