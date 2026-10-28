# Implementation progress

Stato corrente del progetto. I resoconti dettagliati fino al 2026-10-24 (step 0.1-4.3 del master, ORG-1 - ORG-5, aggiornamenti Dependabot) sono in [docs/archive/IMPLEMENTATION_LOG_2026-10.md](docs/archive/IMPLEMENTATION_LOG_2026-10.md), copia integrale e invariata. Da P0.3 ogni step aggiunge una riga al registro; comandi, esiti numerici e limiti stanno nella descrizione della PR e nel messaggio di commit.

## Stato generale

- Current step: P1.7 unita di misura configurabili, in review; ultimo requisito del gate G1
- Next eligible step: chiusura del gate G1 dopo l'integrazione di P1.7, poi P2.1 griglie e attributi di variante, con la validazione V1 in parallelo
- Last completed step: P1.2 ridotto Attributi di prodotto (#46, `8159791`)
- Last update: 2026-10-28
- Roadmap: [ROADMAP_PRODOTTO.md](docs/ROADMAP_PRODOTTO.md), fasi P0-P11 e gate G1-G7 ([ADR 0008](docs/DECISIONS/0008-product-direction-generalist-core-apparel-pilot.md)). Storico master: 32/48 step; nessuno dei due conteggi misura la prontezza alla vendita.
- Open P0: nessuno
- Open P1: F-17, F-18 (P9.1), F-19 (P9.2)
- Open P2: F-29 (P8), F-30 (P9.2)
- Open P3: nessuno
- PR in attesa per scelta: Dependabot #27 Apache POI 6, da rivalutare a inizio novembre o alla 6.0.1
- Main: `8159791` dopo #46; 12 controlli obbligatori verdi dopo il merge, job prod-like nel container Playwright
- Dependabot 2026-10-27: patch Maven #37, #38, #39 raggruppate in #43

## Step

| Step | Stato | PR | Note |
|---|---|---|---|
| 0.1-4.3 master | COMPLETED | storico | Verifiche nel registro archiviato |
| ORG-1 - ORG-5 | COMPLETED | #13, #15, #16 | ORG-6 DEFERRED per decisione dell'utente del 2026-10-23 |
| P0.1 Direzione di prodotto | COMPLETED | #34 | ADR 0008 e roadmap di prodotto |
| P0.2 Benchmark e glossario | COMPLETED | #35 | WB-007 - WB-010, glossario in `DOMAIN_MODEL.md` |
| P0.3 Snellimento documentale | COMPLETED | #35 | Registro archiviato, stato compatto |
| P1.1 Categorie configurabili | COMPLETED | #36 | V36, API categorie, filtri gerarchici, gestione da UI |
| P1.3 Codici a barre e unita di misura | COMPLETED | #40 | V37, verifica EAN, ricerca per codice |
| P1.4 IVA per prodotto e per riga | COMPLETED | #41 | V38, aliquote, riepilogo IVA per documento |
| P1.5 Indirizzi per pagina | COMPLETED | #44 | Router su History API, dettagli ordini, acquisti e inventari |
| P1.6 Sessione persistente sicura | COMPLETED | #45 | ADR 0009, cookie `HttpOnly` e CSRF double-submit, stack di verifica in HTTPS |
| P1.2 ridotto Attributi di prodotto | COMPLETED | #46 | V39, attributi testuali e a elenco con ambito di categoria, filtri ed export; campi su clienti e fornitori in P6.4 |
| P1.7 Unita di misura configurabili | IN REVIEW | - | V40, unita in tabella gestite da UI, pezzo come unita base |
| Gate G1 | IN REVIEW | - | Criteri verificati il 2026-10-28 su `8159791`; resta P1.7 |
| P2 - P11 | PENDING | - | Vedere roadmap di prodotto |

## Decisioni approvate

- 2026-10-28, revisione del gate G1: le unita di misura diventano configurabili con lo step P1.7 prima di chiudere il gate; la validazione V1 con negozi di abbigliamento si svolge in parallelo a P2.1 e se ne tiene conto prima di P2.2 (modello dati delle varianti); i testi visibili dell'interfaccia passano da "Gestionale Computer" a Gestisco.IO, senza toccare package, database, volumi e variabili `GESTIONALE_*`; confermate le scelte di P1.2 (ambito di categoria con sottocategorie, codice e tipo immutabili, disattivazione invece di cancellazione).
- 2026-10-27, P1.6 (ADR 0009): CSRF double-submit di Spring Security, cookie di sessione fino alla chiusura del browser, header `X-Session-Token` rimosso del tutto; stack E2E e prod-like serviti in HTTPS con certificato autofirmato per runner, perche WebKit rifiuta i cookie `Secure` su HTTP.
- 2026-10-26, P1.4: prezzi IVA inclusa; scorporo una volta per aliquota con arrotondamento `HALF_UP` al centesimo, provvisorio fino alla conferma del consulente in P8; sconto per riga spostato a P3.2.
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
| 2026-10-28 | P1.7 Unita di misura configurabili | PR P1.7 | Backend 364/364 (+4: API unita, migrazione V40), verifica per mutazione sul rifiuto delle unita disattivate; frontend 248/248 (+5); E2E locale 23/23 in HTTPS con il nuovo `units-of-measure.spec.ts` |
| 2026-10-28 | Revisione gate G1 | `8159791` | #43-#46 integrate, CI su main verde (12 controlli); criteri soddisfatti tranne le unita di misura fisse, da cui P1.7 |
| 2026-10-28 | P1.2 ridotto Attributi di prodotto | #46 | Backend 360/360 (+5: API e validazione attributi, filtri, colonne di export in CSV/Excel/PDF), verifica per mutazione sulla validazione degli elenchi; frontend 243/243 (+6); E2E locale 22/22 in HTTPS con il nuovo `product-attributes.spec.ts` |
| 2026-10-27 | P1.6 Sessione persistente sicura | #45 | Backend 355/355 (+8 `BrowserSessionCookieSecurityTest`), frontend 237/237 (+3), E2E locale 21/21 in HTTPS su Chromium, Firefox e WebKit; verifiche per mutazione su CSRF e rotazione del token; R-024 mitigato |
| 2026-10-27 | P1.5 Indirizzi per pagina | #44 | Frontend 234/234 (+18: indirizzi, cronologia, controllo bozze sul tasto indietro, sincronizzazione dei dettagli), verifica per mutazione sul controllo bozze; E2E locale 21/21 sullo stack isolato con il nuovo `navigation.spec.ts` |
| 2026-10-27 | Patch Maven (Dependabot #37, #38, #39) | #43 | Spring Boot 4.1.2, surefire 3.6.1, Modulith 2.1.2; backend 347/347 |
| 2026-10-26 | Checkout fuori da iCloud Drive | #42 | Copia integrale con `.git` in `/Users/aim_defix/Developer/Gestisco.IO` (64 ref identiche, fsck pulito); build locale dalla nuova cartella: frontend 213/213, backend 341/341; copia sulla Scrivania conservata come riserva |
| 2026-10-26 | P1.4 IVA per prodotto e per riga | #41 | Backend 347/347 (+6: calcolo per aliquota con prova su 500 casi casuali, fattura mista 22/10/esente, snapshot IVA, API aliquote, migrazione V38 sulle fixture storiche); verifica per mutazione sull'aliquota della riga; frontend 216/216 (+3) |
| 2026-10-26 | P1.3 Codici a barre e unita di misura | #40 | Backend +14 test (validatore EAN, API, migrazione V37), verifica per mutazione sulla cifra di controllo; frontend 213/213 |
| 2026-10-25 | P1.1 Categorie configurabili | #36 | Backend 327/327 (+7: migrazione e controller categorie), frontend 213/213 (+4); verifica per mutazione sul controllo dei cicli; ambiente locale lento per memoria esaurita |
| 2026-10-24 | P0.3 Snellimento documentale | #35 | Registro storico archiviato integralmente; stato compatto nel formato del master |
| 2026-10-24 | P0.2 Benchmark e glossario | #35 | Schede WB-007 - WB-010 da Odoo 18.0 ed ERPNext; WB-008 e WB-009 in bozza |
| 2026-10-24 | P0.1 Direzione di prodotto | #34 | ADR 0008 e roadmap P0-P11 |
| 2026-10-24 | Ambiente di test frontend | #31 | Vitest 5, jsdom 30, jest-dom 7; npm audit a zero |
| 2026-10-24 | codeql-action v4, vite 8.3.5 | #33, #32 | Nessuna azione su Node 20 |
| 2026-10-23 | Test browser CI in container | #22 | Job prod-like da 30-55 a circa 4 minuti |
| 2026-10-21 | Chiusura ORG-5 | #15, #16 | CI e Security verdi su main |
