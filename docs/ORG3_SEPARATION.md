# ORG-3 - Censimento e separazione locale

Data preflight: 2026-10-13. Aggiornamento: 2026-10-14. Stato corrente: COMPLETED per la separazione locale dei sorgenti. Recupero database isolato precedente conservato, nessun cutover operativo. Risultati finali e test nelle nuove cartelle: [verifica ORG-3](ORG3_LOCAL_VERIFICATION.md). Le sezioni di inventario e blocco sotto conservano le evidenze del preflight originale, non lo stato attuale.

## Perimetro e fonte autorevole

Repository originale: `/Users/aim_defix/Desktop/Gestionale.Negozio.Computer `, incluso spazio finale. Branch `miglioramenti-gestionale`, HEAD `b077f7afe42b08c7976713d14552cbd58e9107a2`. Modifiche pregresse a progress e report ORG-1/ORG-2 preservate.

Destinazioni previste al preflight, allora entrambe assenti; create e verificate il 2026-10-14:

- `/Users/aim_defix/Desktop/Gestisco.IO`: linea web.
- `/Users/aim_defix/Desktop/GestionaleNegozioComputer`: snapshot Swing senza web.

L'originale deve rimanere recuperabile, non essere rinominato o cancellato. Portfolio e copie di review sono fuori perimetro. Non e stata introdotta alcuna nuova funzionalita, modifica applicativa o migrazione. Nessun valore di credenziale estratto o mostrato nel rapporto.

## Dati locali non versionati

Inventario dei file ignorati, esclusi artifact ricostruibili, node_modules e file IDE:

| Percorso relativo | Byte | Classificazione |
|---|---:|---|
| audit-log.dat | 2197 | Dati Swing storici, conservare |
| movimenti-magazzino.dat | 728 | Dati Swing storici, conservare |
| ordini.dat | 639 | Dati Swing storici, conservare |
| prodotti.dat | 1125 | Dati Swing storici, conservare |
| ruoli.dat | 138 | Dati account legacy, riservati |
| utenti.dat | 142 | Dati account legacy, riservati |
| backups/gestionale_gestionale_20260707_165425.dump | 39415 | Dump preesistente; non backup aggiornato del database corrente |

Copia privata: `/Users/aim_defix/GestiscoBackups/ORG3-20261013-6HLddy/local-data/`. Directory 0700, file copiati 0600. Manifest SHA-256 separato fuori Git. Controllo sorgente prima/dopo e copia: nessuna modifica ai file originali. La copia protegge i byte, non certifica compatibilita semantica, migrazione o restore. Non deserializzati i file `.dat`, non caricato il dump.

Nessun `.env.docker`, file `.env` operativo o secret locale rilevato nell'inventario del repository; presenti solo esempi versionati. Cache, target e node_modules non copiati.

## Docker: inventario in sola lettura

Due stack gestionali, tutti e sei i container fermi al controllo:

| Project Compose | Container | Porte host configurate | Volume PostgreSQL esistente |
|---|---|---|---|
| gestionale-step43-demo | gestionale-step43-demo-{frontend,backend,postgres} | 8181, 8180, 5543 | gestionale-step43-demo_gestionale-prodlike-postgres-data |
| gestionalenegoziocomputer | gestionale-prodlike-{frontend,backend,postgres} | 8081, 8080, 5433 | gestionalenegoziocomputer_gestionale-prodlike-postgres-data |

Le porte sono configurazione dei container fermi, non servizi dichiarati raggiungibili. Le label Compose puntano ancora al repository originale. Nessun volume montato dal task, nessuna query al database, nessun avvio o ricreazione di container. Il solo volume presente non prova che tutti i dati siano integri o recuperabili.

Un container PostgreSQL di un altro progetto occupa 5432 ed e stato escluso da ogni intervento. Non avviare il Compose base con i default senza verificare conflitti.

## Blocco rilevato

I mount del backend/PostgreSQL demo richiedono sei file sotto `/tmp/gestionale-step43-demo/secrets`: password bootstrap database, migrator, runtime, backup, restore e bootstrap account. La directory `/tmp/gestionale-step43-demo` non esiste piu. La loro assenza e verificata sul filesystem; il rapporto non deduce chi o cosa li abbia rimossi.

Ricerca mirata di `.env.docker`, file `*step43*` e file secret noti in GestiscoBackups, workspace codex_portfolio_sources e Documents/REVIEW: nessuna copia trovata. Non e una ricerca esaustiva dell'host e non esclude un secret manager o una copia privata dell'utente.

Non e quindi dimostrato che lo stack demo possa essere riavviato con la stessa configurazione. Non significa che i dati PostgreSQL siano spariti. Il vecchio stack, fermo anch'esso, non e stato usato come sostituto e non e stato tentato alcun riuso delle sue credenziali.

La stop condition di ORG-1 richiede di fermarsi prima della copia dei progetti se dati o configurazioni necessari non sono recuperabili e verificati. Applicata senza indebolire il requisito. Generare password nuove non recupererebbe automaticamente quelle del database esistente.

## Strategia proposta al preflight e successivamente eseguita

- Derivare le nuove copie dal bundle ORG-2 verificato, preservando le branch locali non pubblicate e i documenti non committati.
- Web: conservare web, build root, script, deploy, workflow, master e documentazione necessari ai test; escludere src legacy e pom-legacy.
- Swing: conservare src e pom-legacy dello snapshot verificato e istruzioni dedicate. Non importare dati operativi automaticamente.
- Nessun push verso il vecchio repository dalle nuove copie; destinazione GitHub e commit della separazione trattati esplicitamente negli step organizzativi approvati.
- Rivalidare tutte le copie con hash e build/test nelle destinazioni effettive prima di cambiare la fonte di lavoro autorevole.
- Non cambiare project Compose, nomi dei volumi o configurazione dei servizi per effetto della rinomina della cartella. Nessun avvio dai nuovi percorsi finche ambiente e dati non sono deliberatamente associati.

## Verifiche e limiti del preflight storico

- Stato Git e HEAD originali ricontrollati; nessun sorgente applicativo modificato.
- `git bundle verify` sul backup ORG-2: PASS, storia completa senza prerequisiti, sei ref piu HEAD. Nessun nuovo clone effettuato.
- Inventario Docker: ps, inspect selettivo di label/mount/porte, volume ls. Nessuna stampa di `.Config.Env` o credenziali.
- Presenza percorsi secret: NEGATIVA, blocco confermato.
- Copia dei sette file ignorati con confronto SHA-256: PASS, 7/7, 44.384 byte, sorgenti invariati. Manifest `local-data-manifest.json` nella directory privata di backup. I file non sono allegabili a review pubbliche.
- Build/test nelle nuove destinazioni: NON ESEGUITI, destinazioni non create. Gli esiti ORG-2 restano prove precedenti e non chiudono ORG-3.
- Nessuna nuova verifica di restore, GUI, browser, migrazioni, sicurezza remota o build Docker.

## Accettazione

- [x] Fonte, HEAD e modifiche locali censiti.
- [x] Bundle Git nuovamente verificato.
- [x] Dati locali e risorse Docker censiti senza modifiche operative.
- [x] Recupero database e nuove credenziali sulla copia isolata verificati il 2026-10-14; vecchie credenziali non recuperate, nessun cutover del runtime.
- [x] Due progetti separati con cronologia e provenienza preservate: 25 commit e sei ref in ciascuna copia.
- [x] Build/test passati nelle nuove destinazioni e handoff dei percorsi in LOCAL_LAYOUT.md.

## Ripresa storica prima della separazione

Aggiornamento 2026-10-14: l'utente ha autorizzato il percorso isolato descritto sotto, ora eseguito. [Rapporto di recupero](ORG3_DATABASE_RECOVERY.md). Il blocco per l'accesso al database su copia e superato; restano separazione sorgenti, build nelle nuove cartelle e associazione esplicita dell'ambiente prima di avviare la UI. Le password degli utenti dell'app restano invariate.

Prima scelta: recuperare i file secret e la configurazione da una copia privata o secret manager, senza incollarne il contenuto in chat o committarlo. In assenza, concordare un recupero non distruttivo su copia isolata, preceduto da backup del volume e da una procedura verificata. Non eseguire restore in-place, reset account o rotazioni automatiche in questo step.

Una separazione esclusivamente dei sorgenti puo essere valutata solo con una decisione esplicita che lasci aperto il recupero operativo: non presentarla come migrazione dell'ambiente completa.

Beneficio reale: il controllo evita di riordinare il codice ignorando la mancanza dei file che permettono al gestionale di collegarsi al database dopo un riavvio.

Avanzamento al preflight: organizzazione 2/6 (33.3%, non pesato); roadmap applicativa 32/48 (66.7%).

## Chiusura - 2026-10-14

Separazione locale verificata: web in `/Users/aim_defix/Desktop/Gestisco.IO`, Swing in `/Users/aim_defix/Desktop/GestionaleNegozioComputer`. Originale e dati conservati. Backend 320 test, frontend 205 test e build, legacy 41 test e JAR passati nelle destinazioni definitive. Nessun codice applicativo o migrazione modificato. Nessun commit/push/merge o modifica agli stack operativi. Le rimozioni Git nelle nuove copie sono intenzionali e non committate; i file sono nel backup privato oltre che nella storia.

Report completo: ORG3_LOCAL_VERIFICATION.md. Fonte web per le prossime attivita: Gestisco.IO. Organizzazione 3/6 (50%); roadmap applicativa 32/48 (66.7%), invariata. ORG-4 e Step 4.4 non iniziati; avvio web sulla copia database recuperata ancora da verificare.
