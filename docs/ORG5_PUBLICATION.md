# ORG-5 - Pubblicazione Gestisco.IO

Aggiornamento: 2026-10-19. Stato: IN_PROGRESS, CI post-merge fallita per timeout Firefox/axe. Fonte locale: `/Users/aim_defix/Desktop/Gestisco.IO`. Destinazione: [AimDefix03/gestisco-io](https://github.com/AimDefix03/gestisco-io). I resoconti datati sotto conservano le decisioni e gli esiti precedenti, non sostituiscono questo stato corrente.

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
4. Protezione main: pull request obbligatoria con zero approvazioni esterne secondo la decisione esplicita del 18 ottobre, invalidazione delle eventuali review obsolete, conversazioni risolte, branch aggiornata, amministratori inclusi, niente force push o cancellazione. Dodici required checks associati ai nomi reali ricevuti da GitHub Actions, inclusi i singoli gate CI e le due lingue CodeQL.
5. Rilettura delle protezioni da API, commit e stato remoto documentati. Nessun merge automatico.

L'utente aveva indicato `AimDefix03` come revisore, coincidente con l'autore. Dopo aver chiarito che lavora da solo, il 18 ottobre ha autorizzato la rimozione del solo requisito di approvazione esterna; decisione registrata nella [PR #13](https://github.com/AimDefix03/gestisco-io/pull/13#issuecomment-6251038045). Non creare approvazioni fittizie, usare bypass o ridurre gli altri gate. Resta necessaria la revisione tecnica documentata e l'autorizzazione pertinente al merge; non si dichiara una revisione indipendente.

## Integrazione - 2026-10-19

- Head PR #13 `c43e68075128ef05e545a573a1a012e8124dc7cc`: [CI 39134914015](https://github.com/AimDefix03/gestisco-io/actions/runs/39134914015) e [Security 39134913936](https://github.com/AimDefix03/gestisco-io/actions/runs/39134913936) passate. Backend Java 17 320/320, frontend 209/209 in 42 file, PostgreSQL 105/105, browser 19/19 senza flaky; build e gate prod-like superati.
- Revisione tecnica del diff: modifiche circoscritte al ripristino focus, quattro regressioni deterministiche, assertion tastiera rafforzate e blocco CI dei flaky; nessuna modifica backend, schema o dipendenza. Master invariato. Nessuna approvazione umana indipendente dichiarata.
- PR #13 integrata senza bypass tramite merge commit `36ca5c75dc9de6726e2eb4b77ad4ebc6c6c50c31`. Branch della PR conservata, nessun force push, deploy o accesso ai database operativi. Main locale aggiornata solo fast-forward, con upstream `gestisco/main`.
- Nuovi gate sul merge: [CI 39304147758](https://github.com/AimDefix03/gestisco-io/actions/runs/39304147758) FALLITA e [Security 39304147694](https://github.com/AimDefix03/gestisco-io/actions/runs/39304147694) PASSATA. Dependency review sul push non applicabile, non contata come nuova prova. Backend 320/320, frontend 209/209 e PostgreSQL 105/105 passati; prod-like fallito e Quality gate skipped.
- E2E post-merge: 18 passati e uno flaky, verifica axe della pagina login su Firefox. Il primo tentativo supera navigazione e visibilita del titolo, poi resta nella prima evaluateExpression di AxeBuilder.analyze (iniezione di axe), fino al timeout globale di 60 secondi. Il retry passa. La ricorrenza e nello stesso stadio tecnico della precedente trace, su un test diverso; non dimostra ancora la causa. `failOnFlakyTests` ha correttamente reso rosso il gate. Nessun rerun usato per dichiarare risolto il problema, nessuna esclusione browser/regola o soglia allentata.
- Riproduzione temporanea macOS della login statica con stessa CSP: 20 esecuzioni senza tracing e 20 con trace/video, tutte completate, zero violazioni. Nessun accesso al backend o al database. Non e una riproduzione completa della CI Linux o del flusso autenticato. Artifact privati del job 118236773189 conservati fuori dal repository; non pubblicare trace grezze o configurazioni operative.
- Riproduzione ridotta nel container ufficiale `mcr.microsoft.com/playwright:v1.61.1-noble`, digest `sha256:5b8f294aff9041b7191c34a4bab3ac270157a28774d4b0660e9743297b697e48`: altre 20 esecuzioni con tracing/video passate, zero violazioni. Rete esterna disattivata, nessun database o credenziale; durata axe 463-740 ms. Non e l'ambiente completo GitHub Actions e non prova la risoluzione del timeout.
- Prossima azione: isolare il blocco in ambiente Linux e verificare una correzione motivata; non procedere a ORG-6 o 4.4. Restano gli avvisi di manutenzione Actions e ubuntu-latest.
- Le decisioni correnti prevalgono sui checkpoint storici sottostanti che riportano una review obbligatoria o pipeline pendenti.

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

## Verifica del candidato e rigore E2E - 2026-10-18

- Correzione pubblicata con commit `5e57d125ebb4617b43d4a7de4f4582b3faacdaf4`, PR #13. [CI 39013781946](https://github.com/AimDefix03/gestisco-io/actions/runs/39013781946) e [Security 39013781949](https://github.com/AimDefix03/gestisco-io/actions/runs/39013781949) verdi, inclusa Dependency review. Backend Java 17: 320 test, zero errori/skip; frontend: 209 test e build passati. PostgreSQL e backup/restore prod-like verificati dal runner; nessuna risorsa operativa locale coinvolta.
- Lettura dettagliata E2E: 18 passati al primo tentativo e uno Firefox flaky. Nella trace Firefox tutte le assertion sul cambio scheda e focus sono passate; ultima chiamata pendente e evaluateExpression con l'iniezione di axe, seguita dal timeout globale di 60 secondi. Il retry passa. Questo localizza il timeout ma non ne prova la causa: non equivale a una correzione del problema di stabilita dello strumento/browser.
- Hardening del gate: `failOnFlakyTests: Boolean(process.env.CI)` in Playwright. Retry mantenuto per acquisire diagnostica, ma un flaky produce exit non zero. Nessun browser escluso, assertion indebolita, pausa fissa o aumento del timeout. Fixture CLI sintetica senza browser: exit 0 prima della configurazione, exit 1 dopo sullo stesso test passato solo al retry; controllo stabile exit 0. La fixture deliberatamente intermittente resta nelle evidenze private, non nella suite applicativa.
- Typecheck E2E del candidato passato. I nuovi controlli remoti devono verificare il commit che include l'hardening; non si trasferisce automaticamente il verde di 5e57d12.
- Checkout Desktop: i tentativi aggiuntivi npm test/build sono rimasti senza output e sono stati interrotti, non dichiarati superati. Le prove locali riuscite sono nella copia temporanea; la CI verifica un checkout indipendente. Nessuna causa iCloud/sandbox viene presentata come dimostrata.
- Main riletta via API: dodici check associati a GitHub Actions, strict, una review, stale dismissal, conversation resolution, admins inclusi, force push e cancellazione vietati. PR REVIEW_REQUIRED/BLOCKED; nessun merge. CodeQL open alerts: zero al controllo, non garanzia di assenza di vulnerabilita.
- Restano avvisi upstream per Actions Node 20/setup-java v4 deprecati e migrazione ubuntu-latest, gia fuori dalla correzione circoscritta; non soppressi. Resta necessaria una revisione distinta dall'autore.
- Bundle `pr-candidate-5e57d12.bundle` in `/Users/aim_defix/GestiscoBackups/ORG5-PUBLIC-20261016`, verificato completo; cinque file della patch scansionati offline con Gitleaks, zero rilevazioni.

## Integrita

- `git fsck --full --strict` sul checkout autorevole: exit 0.
- Il repository non conteneva file LICENSE/COPYING/NOTICE nella cronologia ispezionata; nessuna licenza storica rimossa. Questa verifica non sostituisce l'inventario delle licenze delle dipendenze.
- Snapshot, scansioni, commit, push e integrazione della PR #13 conclusi; verifica post-merge richiesta prima della chiusura ORG-5.
- Rispetto alla prima pubblicazione e stata corretta la gestione del focus con test e hardening del gate Playwright. Nessuna dipendenza, migrazione o configurazione operativa modificata. Master da mantenere con SHA-256 `cb5fc5a663e4acc2b01c01d94556c0ef1d9cab62ede036426f90b70a521ffa78`.

## Preflight privato precedente

Il repository era stato inizialmente creato privato. Ruleset e protezione classica restituivano HTTP 403 con richiesta di upgrade o repository pubblico; Code Scanning non era abilitato. Lo step era stato fermato prima di commit/push, senza ridurre i gate o effettuare acquisti. Questa limitazione e stata affrontata con la successiva scelta esplicita del pubblico, non con un bypass.

Le prove locali precedenti del 16 ottobre restano distinte: Gitleaks v8.30.1 su mirror del bundle (25 commit, circa 5.32 MB) e snapshot (647 file, circa 3.02 MB), nessuna rilevazione. La prima scansione Docker del checkout aveva letto zero commit per un errore Git: non vale come esito positivo. Quelle scansioni non coprono automaticamente questo nuovo candidato.

Evidenze storiche private in `/Users/aim_defix/GestiscoBackups/ORG5-20261016`. Non pubblicare le copie di recovery o i file di configurazione locali. La disponibilita iCloud puo rallentare le letture: verificare gli hash prima della pubblicazione, non sostituire file mancanti con copie presunte equivalenti.

## Stato e beneficio

Organizzazione 4/6 (66.7%), applicazione 32/48 (66.7%): ORG-5 non ancora completato. Non e una misura di vendibilita. Caso reale: un collaboratore trova il prodotto web aggiornato e puo proporre modifiche attraverso controlli automatici e revisione, senza alterare direttamente main o confonderlo con il progetto universitario Swing.
