# ORG-3 - Verifica delle cartelle separate

Data: 2026-10-14. Stato: COMPLETED per la separazione locale. Questo rapporto integra lo storico del preflight in ORG3_SEPARATION.md; non modifica le evidenze precedenti.

## Risultato locale

| Cartella | Contenuti | Stato Git |
|---|---|---|
| `/Users/aim_defix/Desktop/Gestisco.IO` | Web Spring Boot/React, PostgreSQL/Flyway, scripts, deploy, workflow e documenti | Branch miglioramenti-gestionale, separazione non committata |
| `/Users/aim_defix/Desktop/GestionaleNegozioComputer` | 82 file src Swing, pom-legacy.xml e istruzioni dedicate | Branch miglioramenti-gestionale, separazione non committata |
| `/Users/aim_defix/Desktop/Gestionale.Negozio.Computer ` | Originale completo, inclusi dati locali | Conservato per rollback; solo documenti di stato aggiornati al termine |

La cartella web mantiene 639 dei 722 file tracciati della baseline; 83 file legacy sono esclusi dal checkout. La cartella Swing mantiene 88 file tracciati della baseline; 634 file sono esclusi dal checkout. Questi conteggi non includono i nuovi report organizzativi o gli artifact ricostruibili dei test.

I file esclusi sono spostati dalle sole copie nuove in un archivio privato, non cancellati dall'originale. Non sono copiati dati `.dat`, dump, secret, ambiente Docker locale, file IDE, target o node_modules preesistenti. I nuovi artifact di build sono generati dai comandi di verifica e restano ignorati da Git.

## Provenienza e integrita

- Bundle: `/Users/aim_defix/GestiscoBackups/ORG2-20261013/history.bundle`, SHA-256 `a52fc8a2bb709c73f5fefcf1c67f71d609abb3f44cbb8be8a409ede64f000efc`.
- Baseline: `b077f7afe42b08c7976713d14552cbd58e9107a2`.
- Clone indipendente in entrambe le destinazioni; nessun oggetto Git condiviso tramite alternates.
- Tutte le sei ref e i 25 commit raggiungibili preservati, inclusi tre commit locali non pubblicati. `git fsck --full --strict` passato in entrambe le copie.
- I file rimasti nelle copie sono confrontati con i checksum del contenuto originale; quelli archiviati con i blob della baseline Git.
- Il primo controllo dell'archivio legacy ha rilevato la differenza attesa tra IMPLEMENTATION_PROGRESS.md committato e il documento locale aggiornato. Il verificatore e stato corretto per distinguere baseline e working tree; la seconda verifica completa e passata. Il documento locale aggiornato e salvato separatamente e presente nella copia web.
- Nessun Java, TypeScript, test, dipendenza, migrazione, script operativo o configurazione Docker modificato. Il master resta identico nelle due copie e nell'originale.

## Test nelle destinazioni definitive

| Destinazione | Comando | Esito |
|---|---|---|
| Gestisco.IO, root | `mvn -B -o verify` | PASS: 320 test, zero errori/fallimenti/skipped, JAR backend generato |
| Gestisco.IO/web/frontend | `npm ci --offline` | PASS: dipendenze ripristinate dalla cache, lockfile invariato |
| Gestisco.IO/web/frontend | `npm test` | PASS: 205 test in 42 file |
| Gestisco.IO/web/frontend | `npm run build` | PASS: TypeScript e build Vite |
| Gestisco.IO/web/frontend | `npm run typecheck:e2e` | PASS: controllo tipi, non esecuzione browser |
| GestionaleNegozioComputer | `mvn -B -o -f pom-legacy.xml verify` | PASS: 41 test, zero errori/fallimenti/skipped, JAR Swing generato |

Toolchain effettiva: Maven 3.9.16, Java 23.0.1 con compilazione release 17, Node 26.8.1. Non e una nuova esecuzione dei test su runtime Java 17. Suite Maven aggregate dai report XML Surefire; conteggi frontend dal report Vitest. I comandi sono stati eseguiti nelle destinazioni definitive, non in una copia temporanea.

Verifica aggiuntiva del JAR backend: assenza di package della root legacy e di FlatLaf. Confronto dei sorgenti conservati e degli archivi con baseline/manifest, master invariato, sette file dati originali invariati e `git diff --check` senza errori. Non sono aggiunti test applicativi perche il comportamento e il codice non cambiano; sono rieseguite le suite esistenti.

Le letture iniziali nel filesystem Desktop hanno allungato le verifiche. Rimangono warning di API Java deprecate e dipendenza npm whatwg-encoding deprecata; nessuna dipendenza aggiornata in questo step. L'installazione offline non sostituisce una nuova scansione online delle vulnerabilita.

## Git e pubblicazione

Nessun commit, tag, push, merge, repository GitHub o branch protection creato o modificato. Nessun remote configurato nelle nuove copie. Le ref `refs/remotes/origin/*` conservano soltanto la provenienza della baseline e non rappresentano una connessione remota attiva.

Le rimozioni mostrate da Git sono intenzionali e non ancora committate. La storia completa contiene ancora entrambe le linee: separazione del checkout non significa riscrittura della storia. La presentazione pubblica sara affrontata in ORG-4/ORG-5 con autorizzazione separata.

## Database e limiti

Nessuna nuova migrazione e nessuna operazione sul database in ORG-3 separazione sorgenti. Il recupero precedente resta separato e documentato in ORG3_DATABASE_RECOVERY.md. Nessun frontend/backend ricollegato alla copia recuperata, nessun reset account, nessun cutover.

Controllo finale di soli metadati `docker inspect`: org3-recovery-20261014-cmqt8-postgres, gestionale-step43-demo-postgres e gestionale-prodlike-postgres risultano tutti `exited`. Nessuna query, mount o modifica a container/volumi in questo step.

Le build non certificano parita completa Swing/web, migrazione dei `.dat`, UI/login, E2E browser, nuova scansione remota o prontezza produttiva. I backup restano locali, non cifrati/off-site. Il Desktop puo essere sincronizzato da iCloud: la cartella presente non sostituisce un backup verificato.

## Evidenze e ripresa

Manifest, file esclusi, documenti originali e log: `/Users/aim_defix/GestiscoBackups/SEPARATION-20261014`. Il backup e privato e non va condiviso integralmente con agenti o servizi esterni. I file operativi restano nell'originale e nei backup gia verificati, fuori dalle nuove copie sorgenti.

Per riprendere il prodotto usare `/Users/aim_defix/Desktop/Gestisco.IO`, leggere AGENTS.md, IMPLEMENTATION_PROGRESS.md e docs/LOCAL_LAYOUT.md. L'originale rimane disponibile per rollback, senza cancellazioni o reset automatici.

Beneficio reale: una modifica futura al magazzino o agli ordini avviene nel progetto web corretto, senza rischiare di lavorare sulla vecchia interfaccia Swing o di perdere dati e cronologia durante il riordino.

Avanzamento: organizzazione 3/6 (50%, conteggio non pesato); roadmap applicativa 32/48 (66.7%), invariata. Prossimo step eleggibile ORG-4, solo dopo nuova autorizzazione. Non iniziati ORG-4, ORG-5, ORG-6 o Step 4.4.
