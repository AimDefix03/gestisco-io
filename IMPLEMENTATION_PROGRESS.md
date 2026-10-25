# Implementation progress

Stato corrente del progetto. I resoconti dettagliati fino al 2026-10-24 (step 0.1-4.3 del master, ORG-1 - ORG-5, aggiornamenti Dependabot) sono in [docs/archive/IMPLEMENTATION_LOG_2026-10.md](docs/archive/IMPLEMENTATION_LOG_2026-10.md), copia integrale e invariata. Da P0.3 ogni step aggiunge una riga al registro; comandi, esiti numerici e limiti stanno nella descrizione della PR e nel messaggio di commit.

## Stato generale

- Current step: P1.1 categorie gerarchiche configurabili (in review)
- Next eligible step: P1.2 attributi e campi personalizzati
- Last completed step: P0 completata (#34, #35); storico master fino allo Step 4.3
- Last update: 2026-10-25
- Roadmap: [ROADMAP_PRODOTTO.md](docs/ROADMAP_PRODOTTO.md), fasi P0-P11 e gate G1-G7 ([ADR 0008](docs/DECISIONS/0008-product-direction-generalist-core-apparel-pilot.md)). Storico master: 32/48 step; nessuno dei due conteggi misura la prontezza alla vendita.
- Open P0: nessuno
- Open P1: F-17, F-18 (P9.1), F-19 (P9.2)
- Open P2: F-29 (P8), F-30 (P9.2)
- Open P3: nessuno
- PR in attesa per scelta: Dependabot #27 Apache POI 6, da rivalutare a inizio novembre o alla 6.0.1
- Main: `0e0df4b` dopo #35; 12 controlli obbligatori, job prod-like nel container Playwright

## Step

| Step | Stato | PR | Note |
|---|---|---|---|
| 0.1-4.3 master | COMPLETED | storico | Verifiche nel registro archiviato |
| ORG-1 - ORG-5 | COMPLETED | #13, #15, #16 | ORG-6 DEFERRED per decisione dell'utente del 2026-10-23 |
| P0.1 Direzione di prodotto | COMPLETED | #34 | ADR 0008 e roadmap di prodotto |
| P0.2 Benchmark e glossario | COMPLETED | #35 | WB-007 - WB-010, glossario in `DOMAIN_MODEL.md` |
| P0.3 Snellimento documentale | COMPLETED | #35 | Registro archiviato, stato compatto |
| P1.1 Categorie configurabili | IN_REVIEW | da aprire | V36, API categorie, filtri gerarchici, gestione da UI |
| P1.2 - P1.6 | PENDING | - | Gate G1 |
| P2 - P11 | PENDING | - | Vedere roadmap di prodotto |

## Decisioni approvate

- 2026-10-24, ADR 0008: gestionale generalista per il commercio al dettaglio, segmento pilota abbigliamento e calzature, istanza dedicata per cliente, prodotto completo e non beta; obiettivo dell'utente 2-3 mesi.
- 2026-10-24: autorizzazione permanente a commit, push di branch dedicate e PR per gli step della roadmap di prodotto; merge sempre all'utente.
- 2026-10-24: avanzamento autonomo tra step della stessa fase; stop obbligatorio a ogni gate, su decisioni dell'utente e su fallimenti non banali.
- 2026-10-24: TypeScript 7 rinviato a una valutazione entro gennaio 2027; OpenPDF fermo alla 2.0.5 finche il backend resta su Java 17.
- 2026-10-23: ORG-6 rinviato; prove di CI in container Playwright per eliminare l'installazione apt dei browser.
- 2026-10-18: zero approvazioni esterne obbligatorie per lo sviluppatore singolo; PR, 12 controlli, branch aggiornata e conversazioni risolte restano obbligatori.
- Restano valide: contratto operativo del master (parte A), Java 17, monolite modulare, PostgreSQL autorevole con Flyway, diritti riservati senza licenza open source, Giovanni De Filippo responsabile delle decisioni su stock, pagamenti e storico.

## Blocker

- Nessun blocker tecnico.
- Validazioni con operatore reale ancora da eseguire: WB-001, WB-002, WB-003 e i flussi 3.1 e 4.1 del master; per la nuova roadmap V1 prima di P2, V2 dopo G3, V3 dopo G4.
- Attivita esterne a cura dell'utente, percorso critico per P8-P10: intermediari per corrispettivi e SDI, commercialista, preventivi per penetration test, contratti e privacy.

## Rischi residui

- Tempi: stima di 38-57 settimane di lavoro effettivo contro l'obiettivo di 2-3 mesi; confronto a ogni fine fase.
- Varianti (P2): modifica strutturale su stock, ordini, acquisti, inventario e resi; migrazione da verificare su dati popolati.
- Esercizio non pronto per dati reali finche backup fuori sede, ripristino e alert (F-17, F-18, F-19) non sono chiusi in P9.
- Timeout Firefox/axe raro e diagnosticabile, causa non dimostrata.
- Pagamenti storici `UNRECONCILED` e ordini `UNRESOLVED` richiedono decisione umana su evidenze.
- OSS Index non attivo per mancanza di credenziali; fonte NVD del manutentore best-effort.
- Elenco completo dei rischi storici nel registro archiviato e in `docs/RISK_REGISTER.md`.

## Registro

Una riga per step o evento rilevante, dal piu recente.

| Data | Step | PR / commit | Esito |
|---|---|---|---|
| 2026-10-25 | P1.1 Categorie configurabili | da aprire | Backend 327/327 (+7: migrazione e controller categorie), frontend 213/213 (+4); verifica per mutazione sul controllo dei cicli; ambiente locale lento per memoria esaurita |
| 2026-10-24 | P0.3 Snellimento documentale | #35 | Registro storico archiviato integralmente; stato compatto nel formato del master |
| 2026-10-24 | P0.2 Benchmark e glossario | #35 | Schede WB-007 - WB-010 da Odoo 18.0 ed ERPNext; WB-008 e WB-009 in bozza |
| 2026-10-24 | P0.1 Direzione di prodotto | #34 | ADR 0008 e roadmap P0-P11 |
| 2026-10-24 | Ambiente di test frontend | #31 | Vitest 5, jsdom 30, jest-dom 7; npm audit a zero |
| 2026-10-24 | codeql-action v4, vite 8.3.5 | #33, #32 | Nessuna azione su Node 20 |
| 2026-10-23 | Test browser CI in container | #22 | Job prod-like da 30-55 a circa 4 minuti |
| 2026-10-21 | Chiusura ORG-5 | #15, #16 | CI e Security verdi su main |
