# Organizzazione repository: Gestisco.IO

## Stato corrente - 2026-10-19

ORG-3 concluso: la fonte web per le prossime attivita e `/Users/aim_defix/Desktop/Gestisco.IO`; Swing e separato in `/Users/aim_defix/Desktop/GestionaleNegozioComputer`. L'originale con i punti e lo spazio finale resta conservato per rollback. Al termine della separazione entrambe le copie preservavano 25 commit e sei ref, senza remote attivo. ORG-5 ha poi consolidato e pubblicato la sola linea web con `cabc12f`; la copia Swing resta invariata. Vedere [verifica finale ORG-3](ORG3_LOCAL_VERIFICATION.md) e [layout corrente](LOCAL_LAYOUT.md).

ORG-4 concluso: identita documentale Gestisco.IO, istruzioni e descrizione del prodotto allineate. [Addendum](GESTISCO_IDENTITY_ADDENDUM.md) e [verifica](ORG4_VERIFICATION.md) distinguono le decisioni organizzative dallo scope funzionale, che resta invariato. I documenti allora non committati sono ora inclusi nella baseline ORG-5.

Organizzazione: 5/6 milestone (83.3%). Roadmap applicativa: 32/48 (66.7%), invariata. ORG-5 concluso il 21 ottobre: repository `AimDefix03/gestisco-io` pubblico a diritti riservati, PR #13 e #15 integrate, CI e Security verdi su main `b68c5b8`. Main locale aggiornata solo fast-forward e associata a gestisco/main. Il 18 ottobre l'utente ha autorizzato zero approvazioni esterne obbligatorie per lavorare da solo; PR, dodici check, branch aggiornata, conversazioni risolte e protezioni amministratori restano attivi. Esiti nel [rapporto ORG-5](ORG5_PUBLICATION.md). ORG-6 non iniziato. Le sezioni seguenti mantengono l'inventario e le decisioni storiche, incluso lo stato dei percorsi al momento del preflight. Questi conteggi non misurano la prontezza alla vendita.

## ORG-1 - Inventario verificato

Data: 2026-10-13. Ambito: ricognizione in sola lettura, seguita da questo report e dall'aggiornamento di IMPLEMENTATION_PROGRESS.md. Nessun codice modificato, progetto spostato, repository creato o operazione Git di scrittura effettuata.

Decisione di direzione dell'utente: mantenere la versione Swing come storico con il nome attuale e separare il prodotto web con identita Gestisco.IO. Il nome non implica che multi-tenancy, abbonamenti o prontezza commerciale siano gia implementati. MASTER_SOURCE_AGENTE_AI.md resta invariato e vincolante; eventuali variazioni di perimetro richiedono un addendum approvato.

## Fonti e cartelle

| Percorso | Evidenza e ruolo | Trattamento |
|---|---|---|
| `/Users/aim_defix/Desktop/Gestionale.Negozio.Computer ` | Repository reale del gestionale; il nome termina con uno spazio | Fonte autorevole per la separazione, non rinominata |
| `/Users/aim_defix/Desktop/PortfolioGiovanniDeFilippo` | Repository portfolio indipendente | Escluso dalla migrazione del gestionale |
| `/Users/aim_defix/Desktop/codex_portfolio_sources` | Workspace della chat, vecchi sorgenti portfolio e staging; nessun repository Git proprio o superiore rilevato | Non e il checkout autorevole del gestionale |
| `/Users/aim_defix/Desktop/VerificaProgetto1.0` | Copia di review senza Git | Non promossa a sorgente; uguaglianza con HEAD non verificata |
| `/private/tmp/gestionale-consolidamento-20261011.E2FiVh` | Copia temporanea usata per precedenti verifiche | Non e un backup durevole |
| `/Users/aim_defix/Desktop/GestionaleNegozioComputer` | Assente al controllo | Non ricreata |
| `/Users/aim_defix/Desktop/Gestisco.IO` | Assente al controllo | Destinazione futura proposta, non creata |

Nel workspace della chat sono presenti 36 directory `.step-*`, senza `.git` al primo livello. Non sono state confrontate byte per byte con HEAD: non possono essere dichiarate duplicati eliminabili. Anche gli archivi di review sul Desktop restano copie storiche non validate in questo step.

## Git e GitHub

- Remote gestionale: https://github.com/AimDefix03/gestionale-negozio-computer.git
- Branch attiva: `miglioramenti-gestionale`.
- HEAD locale e origin: `b077f7afe42b08c7976713d14552cbd58e9107a2`.
- Stato iniziale pulito, inclusi diff staged e unstaged. Questo non certifica il backup dei file ignorati.
- `main` locale e remota: `6da79514b5f354ea730be255d05c36855477b0e6`.
- Branch locali aggiuntive: `baseline-pre-publication-20260727` e `miglioramenti-gestionale-pre-publication`, entrambe mostrate a `6da3f07`. Unicita dei loro contenuti e copertura remota da verificare prima della separazione.
- Nessun tag; un solo worktree registrato, quello reale.
- Repository pubblico, non archiviato, default branch `main`, descrizione GitHub assente.
- [PR #2](https://github.com/AimDefix03/gestionale-negozio-computer/pull/2): aperta e draft, 702 file modificati, base `main`, head `miglioramenti-gestionale`. Nessun merge effettuato; la destinazione delle modifiche va decisa durante la separazione.
- Protezione `main`: branch aggiornata obbligatoria, una approvazione, amministratori inclusi, force push e cancellazione disabilitati. Check richiesti: Backend Spring Boot, Frontend React, Database migrations, Quality gate. Il workflow Security non figura tra questi quattro check obbligatori: aspetto da rivalutare per il nuovo repository.

Ultimi workflow osservati su `b077f7a`, entrambi SUCCESS:

- [CI 38304720795](https://github.com/AimDefix03/gestionale-negozio-computer/actions/runs/38304720795).
- [Security 38304720775](https://github.com/AimDefix03/gestionale-negozio-computer/actions/runs/38304720775).

Sono evidenze remote del consolidamento precedente, non test rieseguiti per ORG-1. Nessuna nuova scansione segreti o dipendenze eseguita in questo step.

## Disponibilita locale: punto da risolvere

Durante l'inventario, 1.404 file sotto `.git` risultavano marcati `dataless`; anche README.md e pom.xml presentavano inizialmente attributi `compressed,dataless`. Alcune letture Git hanno richiesto tempo prima di completarsi. Il conteggio e puntuale e puo cambiare quando macOS scarica i file.

Non e prova di corruzione, ma impedisce di considerare una semplice copia della cartella un backup autonomo gia verificato. Non sono stati eseguiti `git fsck`, un bundle verificato o un confronto integrale delle copie.

Prima degli spostamenti: rendere disponibile la cronologia e verificarla, oppure predisporre una copia controllata dal remote conservando separatamente branch locali e dati non pubblicati. Non copiare alla cieca `.git`. Per la futura cartella sul Desktop occorre evitare l'evizione automatica; valutare Mantieni scaricato o un checkout fuori dalle directory sincronizzate, senza cambiare la destinazione concordata unilateralmente.

## Confine Swing/web: evidenza preliminare

Il pom.xml principale e un aggregatore con il solo modulo `web/backend`. Esiste `pom-legacy.xml` e la documentazione `docs/LEGACY_SWING.md` descrive build e test separati del legacy. Questo e un indizio statico di separazione, non una prova completa di indipendenza.

File tracciati: 722, di cui `src/` 82, `web/backend/` 384, `web/frontend/` 143, `web/postgres/` 2, `scripts/` 49, `docs/` 36, `.github/` 3. Il controllo dei percorsi tracciati non ha trovato target, out, node_modules, backup, `.dat` o `.env` non di esempio. Non sostituisce una scansione del contenuto per segreti.

Nel repository esistono file locali ignorati e dati `.dat`. Non sono stati letti, copiati o eliminati. Prima di archiviare Swing va stabilito quali siano ancora necessari e se i relativi dati siano gia migrati/verificati. `main` non e automaticamente lo snapshot legacy piu completo: va scelto in base a storia, funzionalita e test.

Database, volumi Docker, credenziali e backup operativi non sono stati inventariati in questo step. Nessuna rinomina automatica di volumi, database, package Java o configurazioni.

## Portfolio: lavoro separato da preservare

Remote: https://github.com/AimDefix03/portfolio-giovanni-de-filippo.git, branch `main` con upstream `origin/main`.

Modifiche preesistenti a sei file: package-lock.json, package.json, src/App.tsx, src/styles/globals.css, tsconfig.json e vite.config.ts. Sei voci non tracciate: Hero.txt, components.json, src/components/MacbookTerminalIntro.tsx, src/components/TerminalIntro.tsx, src/components/ui/ e src/lib/. Le directory possono contenere piu file. Nessuno di questi contenuti e stato modificato o incluso nella separazione.

## Sequenza organizzativa e accettazione

Ogni milestone richiede una nuova autorizzazione a procedere. La roadmap funzionale resta a 32/48; lo Step 4.4 non parte automaticamente.

| Stato | Step | Attivita e dipendenze | Criterio di accettazione |
|---|---|---|---|
| [x] | ORG-1 | Inventario prima di qualunque spostamento | Fonti, branch, copie, rischi e limiti documentati; nessun cambiamento applicativo |
| [x] | ORG-2 | Build web/Swing indipendenti e snapshot legacy verificati; fsck e recupero bundle completi passati | Tutte le sei ref preservate, inclusi tre commit locali; bundle conservato fuori da Desktop/temp |
| [x] | ORG-3 | Recupero isolato e separazione locale conclusi; 25 commit e sei ref in ciascuna copia | Backend 320, frontend 205, Swing 41 test e build passati nelle nuove destinazioni; nessun cutover implicito |
| [x] | ORG-4 | Identita documentale Gestisco.IO allineata dopo ORG-3 | README, AGENTS, progress e addendum sulle decisioni approvate concordano su root, comandi, scope e limiti; master invariato; verifica documentale dedicata |
| [x] | ORG-5 | Repository pubblico autorizzato, diritti riservati; gate verdi su main `b68c5b8` | Repository nuovo con CI/Security verdi, branch protetta e storia/provenienza chiara; nessun merge o archivio implicito |
| [ ] | ORG-6 | Dopo ORG-5: handoff e prova di ripresa | Avvio e verifiche riproducibili seguendo i documenti; nuova chat agganciabile alla root corretta con prossimo step univoco |

Destinazione verificata ORG-5: `https://github.com/AimDefix03/gestisco-io`, proprietario `AimDefix03`. La creazione e il successivo passaggio al pubblico sono autorizzati; questo non implica che i gate siano gia passati.

## Stop condition

- Fermarsi prima della copia se cronologia, branch locali o dati necessari non sono recuperabili e verificati.
- Fermarsi in caso di modifiche concorrenti che rendano obsoleto l'inventario; rileggerle senza sovrascriverle.
- Fermarsi se emergono dipendenze produttive dal legacy: risolverle con una attivita circoscritta e test prima dell'archiviazione.
- Nessun commit, push, tag, merge, eliminazione o operazione remota senza autorizzazione pertinente.
- Non pubblicare dati `.dat`, dump, segreti o configurazioni locali. Non usare un export di review come sostituto di un backup.
- Non considerare superati test, restore o controlli di integrita non eseguiti.

## Esito ORG-1

Completato l'inventario; ORG-2 non iniziato. Solo questo report e IMPLEMENTATION_PROGRESS.md vengono aggiornati. Nessuna migrazione database, build o test applicativo nuovo. Verifiche: stato/diff Git, refs, remote, worktree, file tracciati, attributi filesystem e interrogazioni GitHub in lettura.

Avanzamento organizzativo: 1/6 (16.7%, non pesato). Avanzamento della roadmap applicativa: 32/48 (66.7%), invariato. Queste percentuali non rappresentano la prontezza alla vendita.

Caso d'uso reale: durante una modifica futura sapremo quale checkout contiene il prodotto corrente, evitando di pubblicare una copia vecchia o perdere lavoro e dati nella separazione dal progetto universitario.

## Aggiornamento ORG-2

Vedere [verifica ORG-2](ORG2_VERIFICATION.md): backend 320/320 e frontend 205/205 con build senza legacy; Swing 41/41 con JAR senza web. Snapshot da conservare individuato nella baseline `9f9e23a`, identico al legacy attuale e piu completo di main. Tre commit delle branch pre-publication non sono raggiungibili dai riferimenti remoti locali.

La verifica integrale Git e il bundle sono conclusi: fsck originale exit 0 (due oggetti dangling conservati), clone del bundle con fsck strict verde e tutte le sei ref presenti, 25 commit raggiungibili. Backup in `/Users/aim_defix/GestiscoBackups/ORG2-20261013/history.bundle`; checksum e limiti nel report. Il bundle non include dati ignorati o modifiche non committate, che richiedono conservazione separata. Lo step e COMPLETED; la separazione non inizia. Nessun dato, sorgente o branch e stato spostato o eliminato. Milestone completate 2/6 (33.3%); roadmap applicativa 32/48 (66.7%) invariata.

## Preflight ORG-3

Autorizzato il 2026-10-13; vedere [inventario e blocco della separazione](ORG3_SEPARATION.md). I volumi PostgreSQL dei due stack gestionali esistono, ma i file secret dello stack demo erano sotto `/tmp` e non sono piu presenti nel percorso montato. Il piano impone lo stop prima della copia quando la configurazione necessaria non e recuperabile e verificata. Nessuna destinazione creata, nessuna separazione dichiarata completa. Sorgenti e dati originali restano in posizione; sei file Swing e un dump storico sono conservati separatamente con checksum. Organizzazione 2/6 (33.3%), applicazione 32/48 (66.7%), entrambe invariate.

## Recupero autorizzato - 2026-10-14

Il blocco delle credenziali e stato affrontato con backup e recupero su un nuovo volume, non modificando quello originale. Vedere [recupero database](ORG3_DATABASE_RECOVERY.md): 1.482 file originali invariati, 25 tabelle conservate, 35 migrazioni riuscite, 12 controlli passati e riavvio/accesso runtime verificati. Nuove credenziali soltanto sulla copia, nessun reset degli account applicativi. Copia lasciata ferma, senza rete o porte pubblicate; frontend/backend non avviati. ORG-3 passa a PARTIAL, non COMPLETED: cartelle ancora non separate. Percentuali invariate. Il vecchio stack non e stato riparato in-place e nessun cutover e autorizzato implicitamente.
