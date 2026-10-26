# Gestisco.IO - Roadmap di prodotto fino alla vendibilita

Stato: approvata dall'utente il 2026-10-24 con [ADR 0008](DECISIONS/0008-product-direction-generalist-core-apparel-pilot.md). E la roadmap operativa del prodotto; il contratto operativo del master (parte A), le invarianti e i gate di qualita restano vincolanti. Lo stato puntuale e in `IMPLEMENTATION_PROGRESS.md`.

## 1. Direzione

- **Prodotto:** gestionale generalista per attivita commerciali al dettaglio, adattabile per configurazione e moduli attivabili, senza fork del codice per cliente.
- **Primo segmento pilota:** negozi di abbigliamento e calzature. Il segmento decide l'ordine dei lavori, non chiude il prodotto: ogni funzione e progettata in forma generica (per esempio "varianti" e non "taglie").
- **Mercato:** Italia. Vendibile significa utilizzabile come software principale del negozio, quindi con adempimenti fiscali e privacy coperti tramite provider e professionisti, mai dichiarati conformi senza validazione.
- **Erogazione iniziale:** istanza dedicata e gestita per ciascun cliente (deployment dedicato previsto da ADR 0005). Il SaaS multi-tenant condiviso resta la direzione di scala, da attivare solo con piu clienti (fase P11).

## 2. Regole che restano valide

- Metodo del master: step piccoli, test-first, PostgreSQL autorevole, nessun controllo allentato, un solo step per volta con autorizzazione, `AUTO_ADVANCE: false`.
- Invarianti gia costruite: ledger di magazzino e pagamenti, idempotenza, ownership per ID, audit, permessi lato server.
- Java 17, monolite modulare, React/TypeScript. Eccezioni solo con decisione registrata.
- Ogni modifica strutturale passa da migrazioni Flyway nuove e testate su database popolato.

## 3. Punto di partenza (2026-10-24)

Presente e verificato: autenticazione e ruoli, catalogo, clienti e fornitori, vendita assistita e ordini con riserva ed evasione, incassi, resi e rimborsi con riconciliazione, ordini fornitore con ricezione e costo medio, ledger di magazzino, inventario fisico con approvazione, documenti simulati non fiscali, report con export CSV/Excel/PDF, audit, CI con 12 gate.

Limiti che bloccano la vendita:

| Area | Oggi |
|---|---|
| Catalogo | Categorie fisse nel codice (`HARDWARE`, `SOFTWARE`), una sola giacenza per prodotto, nessun codice a barre, nessuna unita di misura |
| IVA | Una sola aliquota per azienda, nessuna aliquota per prodotto o riga |
| Vendita | Solo flusso ordine (bozza, conferma, evasione); nessuna cassa |
| Documenti | Simulati e non stampabili |
| Frontend | Nessun indirizzo per pagina; ricaricare la pagina richiede un nuovo login |
| Fiscale | Nessun corrispettivo telematico, nessuna fattura elettronica |
| Esercizio | Backup solo locale, nessun alert (F-17, F-18, F-19 aperti) |

## 4. Fasi e gate

| Fase | Tema | Gate di uscita | Lavoro stimato* |
|---|---|---|---|
| P0 | Decisione, scope e conoscenza del dominio | Direzione approvata | 1 settimana |
| P1 | Catalogo configurabile e frontend da prodotto | G1 Catalogo generalista | 3-5 settimane |
| P2 | Varianti (modello e SKU) | G2 Varianti end-to-end | 6-9 settimane |
| P3 | Cassa e vendita al banco | G3 Demo vendibile | 4-6 settimane |
| P4 | Prezzi, stagioni e saldi | - | 2-3 settimane |
| P5 | Acquisti stagionali e magazzino | - | 3-4 settimane |
| P6 | Documenti, clienti e comunicazioni | G4 Pilota affiancato | 3-5 settimane |
| P7 | Dati: import, report, prestazioni | - | 3-4 settimane |
| P8 | Fiscale tramite intermediario | G5 Fiscale validato | 5-8 settimane + tempi esterni |
| P9 | Produzione: esercizio, sicurezza, privacy | G6 Produzione | 5-7 settimane + pentest esterno |
| P10 | Commercializzazione | G7 Vendibile | 3-5 settimane |
| P11 | Scala (dopo i primi clienti) | - | per domanda |

\* Settimane di lavoro effettivo a tempo pieno di uno sviluppatore con agente AI, incluse verifiche e documentazione. Totale P0-P10: circa 38-57 settimane, quindi 9-13 mesi a tempo pieno; a tempo parziale il calendario si allunga in proporzione. Le stime sono indicative e vanno riviste a fine di ogni fase. Obiettivo dichiarato dall'utente: prodotto vendibile e completo, non beta, in 2-3 mesi. Lo scarto con le stime si riduce solo con lavoro a tempo pieno, parallelizzazione dei pezzi indipendenti, processo snello (P0.3) e avvio immediato delle attivita esterne (intermediario fiscale, consulenti, penetration test), che sono il percorso critico.

Validazione con persone reali, senza attendere la vendita:

- **V1** prima di P2: due o tre conversazioni brevi con chi lavora in un negozio di abbigliamento o calzature (taglie, cambi, saldi, cartellini, riassortimenti).
- **V2** dopo G3: dimostrazione guidata a un negoziante.
- **V3** dopo G4: pilota affiancato, in cui il negozio usa Gestisco.IO per magazzino e vendite mantenendo il proprio registratore telematico. Non richiede la fase fiscale.

## 5. Step

Formato: obiettivo, contenuto, criteri di accettazione, dimensione (S fino a 3 giorni, M fino a 8, L fino a 15; L va diviso in piu PR).

### P0 - Decisione e dominio

**P0.1 Decisione di direzione (S).** ADR 0008: nucleo generalista, pilota abbigliamento, erogazione per istanza dedicata. Aggiornamento di `PRODUCT_SCOPE.md` e `ROADMAP.md`; questa roadmap diventa il riferimento operativo.
Accettazione: decisione approvata e registrata; nessun step precedente perso (tabella di tracciabilita alla sezione 7).

**P0.2 Benchmark e glossario di dominio (S).** Scheda in `WORKFLOW_BENCHMARKS.md` per vendita al banco, cambi, saldi, varianti e acquisti stagionali; glossario modello, variante, SKU, griglia, collezione.
Accettazione: per ogni flusso, regole di dominio, cosa non fare e test di accettazione.

**P0.3 Snellimento documentale (S).** `IMPLEMENTATION_PROGRESS.md` ridotto a stato corrente e una riga per step; dettagli nelle PR e nella storia Git; resoconti storici archiviati senza perdita.
Accettazione: stato leggibile in una schermata; nessuna evidenza cancellata.

### P1 - Catalogo configurabile e frontend da prodotto

Ordine di esecuzione rivisto con l'utente il 2026-10-25: P1.1, P1.3, P1.4, P1.5, P1.6, poi P1.2 ridotto (attributi di prodotto testuali e a elenco) prima del gate G1. I campi personalizzati su clienti e fornitori passano a P6.4.

**P1.1 Categorie gerarchiche configurabili (M).** Tabelle categoria con padre, migrazione di `HARDWARE`/`SOFTWARE` come voci iniziali, gestione da UI con permessi.
Accettazione: nessuna categoria fissa nel codice; prodotti esistenti migrati; test di migrazione su database popolato.

**P1.2 Attributi e campi personalizzati (M).** Definizione di attributi per categoria (testo, numero, elenco) e campi personalizzati su clienti e fornitori, con validazione lato server.
Accettazione: un attributo definito da UI e usabile in filtri ed export senza modifiche al codice.

**P1.3 Unita di misura e codici a barre (S).** Unita di misura per prodotto; EAN/UPC o codice interno con unicita garantita dal database e controllo della cifra di verifica.
Accettazione: ricerca per codice a barre; duplicati rifiutati dal database.

**P1.4 IVA per prodotto e per riga (M).** Tabella aliquote con natura di esenzione; aliquota sul prodotto copiata sulla riga d'ordine; totali per aliquota; sconto per riga. Documenti simulati con riepilogo IVA multiplo.
Accettazione: ordine con righe a 22% e 10% e una esente produce imponibili e imposte corretti; arrotondamenti testati; storico invariato.

**P1.5 Indirizzi per pagina (M).** Router con indirizzi per sezione e dettaglio, tasto indietro e link diretti, mantenendo bozze e controllo delle modifiche non salvate.
Accettazione: ogni pagina ha un indirizzo; E2E di navigazione e ricarica.

**P1.6 Sessione persistente sicura (M).** Nuova ADR che aggiorna ADR 0004: sessione in cookie `HttpOnly`, `Secure`, `SameSite=Strict` con protezione CSRF; il token non e piu accessibile a JavaScript.
Accettazione: ricaricare la pagina non richiede il login; test CSRF negativi; R-024 chiuso.

Gate **G1**: catalogo senza valori fissi nel codice, IVA per riga, indirizzi e sessione persistenti, tutti i gate CI verdi.

### P2 - Varianti

Modifica strutturale piu ampia: separa **modello** (descrizione, categoria, prezzo base) e **variante/SKU** (combinazione di attributi di variante, codice a barre, giacenza). Ogni prodotto esistente diventa un modello con una sola variante.

**P2.1 Griglie e attributi di variante (M).** Griglie configurabili (XS-XXL, 35-46, mezze taglie, bambino) e colori; associazione griglia-categoria.

**P2.2 Modello dati e migrazione (L).** Tabelle modello e variante, migrazione non distruttiva con preflight, compatibilita delle API esistenti durante la transizione.
Accettazione: upgrade da database popolato senza perdita; conteggi e valori di magazzino identici prima e dopo.

**P2.3 Giacenze e ledger per variante (L).** Movimenti, riserve, saldo iniziale, rettifiche e inventario fisico per SKU; riconciliazione aggiornata.
Accettazione: invarianti del ledger e test di concorrenza PostgreSQL verdi per variante.

**P2.4 Ordini, resi e documenti per variante (L).** Righe d'ordine e resi su SKU con snapshot di taglia e colore.

**P2.5 Acquisti e ricezioni per variante (M).**

**P2.6 Interfaccia a matrice (L).** Inserimento modello con griglia taglie x colori, giacenze a matrice, ricerca per modello o codice a barre.
Accettazione: creare un modello con 2 colori e 8 taglie in meno di due minuti; E2E completo.

Gate **G2**: ciclo acquisto, magazzino, vendita e reso interamente per variante, migrazione verificata su dati popolati.

### P3 - Cassa e vendita al banco

**P3.1 Sessione di cassa (M).** Apertura con fondo, chiusura con conteggio per metodo di pagamento, differenze motivate e audit.

**P3.2 Vendita rapida (L).** Schermata cassa ottimizzata per tastiera, touch e lettore barcode in emulazione tastiera; sconto riga o totale entro limiti di permesso; pagamento contanti, carta o misto; resto.
Accettazione: vendita di tre articoli scansionati in meno di 20 secondi; idempotenza su doppio invio.

**P3.3 Resi e cambi al banco (M).** Reso con o senza scontrino originale, cambio taglia in un'unica operazione, reintegro di magazzino.

**P3.4 Buoni e credito cliente (M).** Buono reso e carta regalo con codice, saldo, scadenza e ledger dedicato.

**P3.5 Ricevuta di cortesia (S).** Stampa termica da browser, chiaramente non fiscale fino alla fase P8.

Gate **G3 - Demo vendibile**: un operatore apre la cassa, vende, cambia una taglia, chiude la cassa senza aiuto. Validazione V2.

### P4 - Prezzi, stagioni e saldi

**P4.1 Collezioni e stagioni (S).** Collezione e stagione su modello; filtri e report.

**P4.2 Listini e promozioni (M).** Prezzo di listino, promozioni per collezione, categoria o selezione con periodo; prezzo barrato; precedenza delle regole documentata e testata.

**P4.3 Riprezzatura e saldi (M).** Applicazione di saldi in blocco con anteprima, audit e ripristino di fine periodo.

### P5 - Acquisti stagionali e magazzino

**P5.1 Ordine fornitore a griglia (M).** Ordine per modello con quantita per taglia e colore, consegne previste.

**P5.2 Ricezione con etichette (M).** Ricezione per variante con stampa immediata delle etichette.

**P5.3 Etichette e cartellini (M).** Modelli di etichetta con codice a barre, prezzo e prezzo barrato; stampa su etichettatrici comuni da browser.

**P5.4 Resi a fornitore (M).**

**P5.5 Scorta minima per articoli continuativi (M, ex 4.4).** Soglia per variante, proposta di riordino con formula visibile, conversione in ordine fornitore, nessun invio automatico.

### P6 - Documenti, clienti e comunicazioni

**P6.1 Modelli PDF dei documenti (M).** Logo, intestazione, testi configurabili per ricevuta, preventivo, DDT e fattura.

**P6.2 Preventivo e DDT (M).** Ciclo preventivo, ordine, DDT, fattura, con collegamenti e stati.

**P6.3 Invio email (M).** Configurazione SMTP, invio documenti al cliente, registro di consegna.

**P6.4 Clienti e consensi (M).** Anagrafica leggera al banco, consenso marketing separato e revocabile, storico acquisti; fidelity semplice a punti come modulo opzionale.

**P6.5 Moduli attivabili (M).** Configurazione per installazione dei moduli (cassa, acquisti, varianti, fidelity, portale clienti); menu e API rispettano lo stato del modulo.

Gate **G4 - Pilota affiancato**: un negozio usa magazzino, cassa ed etichette in parallelo al proprio registratore. Validazione V3.

### P7 - Dati

**P7.1 Import con area di staging (L, ex 4.5).** Modelli CSV per modelli, varianti, clienti, fornitori e giacenze iniziali; validazione riga per riga, prova a vuoto, conferma atomica.

**P7.2 Report di stagione (M).** Venduto per taglia e colore, rotture di taglia, percentuale venduta della collezione, rimanenze, margini per collezione.

**P7.3 Export per il commercialista (S).** Riepiloghi periodici di vendite, IVA e incassi.

**P7.4 Prestazioni su volumi target (M, ex 4.6).** Budget di risposta concordati e misurati su dataset sintetici.

### P8 - Fiscale tramite intermediario

Nessun tracciato fiscale costruito in casa. Prerequisito: scelta di intermediario e consulente (P8.1).

**P8.1 Scelta provider e consulenza (S + esterno).** Confronto di almeno due intermediari per corrispettivi e SDI (API, costi, sandbox, conservazione); commercialista di riferimento per requisiti e validazione.

**P8.2 Corrispettivi telematici (L).** Integrazione con registratore telematico o servizio equivalente tramite provider: documento commerciale, chiusure giornaliere, resi e annulli, errori e riprova idempotente.

**P8.3 Fattura elettronica SDI (L).** Dati anagrafici obbligatori (codice destinatario, PEC), invio tramite provider, ricevute e scarti, note di credito, conservazione (ADR 0006).

**P8.4 Validazione professionale (esterno).** Verifica dei flussi con il consulente; documenti simulati rimossi o chiaramente separati.

Gate **G5 - Fiscale validato**: sign-off scritto del professionista sui flussi coperti.

### P9 - Produzione

**P9.1 Backup fuori sede e ripristino parallelo (L, ex 4.7, F-17, F-18).**
**P9.2 Riavvio, alert e log centralizzati (M, ex 4.8, F-19, F-30).**
**P9.3 Supply chain e artefatti firmati con rollback (M, ex 5.1-5.2).**
**P9.4 Hardening e penetration test esterno (L + esterno, ex 5.3).**
**P9.5 Carico, guasti e disaster recovery (M, ex 5.4).**
**P9.6 Privacy e conservazione (L + professionista, ex 5.5, ADR 0006).** Registro dei trattamenti, informativa, DPA, diritti degli interessati, retention verificabile.
**P9.7 Perimetro pagamenti (S, ex 5.6).** POS fisico esterno con incasso registrato; nessuna esecuzione di pagamenti dal gestionale.

Gate **G6 - Produzione** (ex Gate E): nessun P0/P1, pentest chiuso, ripristino e DR provati, alert consegnati, privacy approvata.

### P10 - Commercializzazione

**P10.1 Installazione e primo avvio guidato (M, ex 6.1).** Istanza nuova da procedura documentata; configurazione guidata di azienda, IVA, numerazioni, moduli e import.
**P10.2 Aggiornamenti e supporto (M, ex 6.2).** Versioni, note di rilascio, aggiornamento e rollback provati, pacchetto diagnostico senza dati personali.
**P10.3 Permessi granulari e approvazioni (M, ex 6.3).** Profili cassa, magazzino, responsabile; approvazione per sconti e rettifiche oltre soglia.
**P10.4 Documentazione utente e assistenza (M).** Manuale per ruolo, guide in app, canale di supporto con tempi dichiarati.
**P10.5 Offerta e contratti (esterno).** Listino, condizioni d'uso, SLA, DPA, assicurazione professionale, posizione fiscale per vendere.
**P10.6 Ambiente demo e presentazione (S).** Istanza demo con dati realistici di un negozio di abbigliamento, ripristinabile.

Gate **G7 - Vendibile** (ex Gate F): primo cliente pagante installato e supportato senza interventi manuali sul database.

### P11 - Scala (guidata dalla domanda)

SaaS multi-tenant (ADR 0005, 0007), piu negozi con trasferimenti, sincronizzazione e-commerce, fidelity avanzata, app per inventario con fotocamera, TypeScript 7 e passaggio a Java 21 (con OpenPDF 3).

## 6. Costi e dipendenze esterne

Da preventivare prima di P8-P10: intermediario fiscale (canone e costo per documento), consulenza del commercialista, penetration test, hosting e backup, consulenza legale per contratti e privacy, assicurazione professionale. Nessuno di questi e sostituibile da codice.

## 7. Tracciabilita con il master

| Master | Nuova posizione |
|---|---|
| 4.4 Scorta minima e riordino | P5.5 |
| 4.5 Import con staging | P7.1 |
| 4.6 Prestazioni | P7.4 |
| 4.7 Backup off-site (F-17, F-18) | P9.1 |
| 4.8 Alert e log (F-19, F-30) | P9.2 |
| 5.1-5.2 Supply chain e artefatti | P9.3 |
| 5.3 Penetration test | P9.4 |
| 5.4 Carico e DR | P9.5 |
| 5.5 Privacy e retention | P9.6 |
| 5.6 Perimetro pagamenti | P9.7 |
| 6.1 Packaging e onboarding | P10.1 |
| 6.2 Supportabilita | P10.2 |
| 6.3 Permessi granulari | P10.3 |
| 6.4 Documenti fiscali tramite provider (F-29) | P8 |
| 6.5 Integrazioni | P11 |
| ORG-6 Passaggio di consegne | prima di una pausa lunga o di un collaboratore |
| Prove con operatore reale (3.1, 4.1, Gate C) | V2 e V3 |

## 8. Rischi principali

| Rischio | Mitigazione |
|---|---|
| Varianti piu costose del previsto | P2 diviso in sei step con migrazione verificata; nessuna nuova funzione costruita sul modello a giacenza singola |
| Fiscale piu lungo o costoso | P8.1 anticipabile in parallelo da P3; pilota affiancato (G4) possibile senza fiscale |
| Prodotto progettato senza utenti | Validazioni V1, V2, V3 con criteri espliciti |
| Processo piu pesante del prodotto | P0.3 e regola di manutenzione: un giro Dependabot a inizio sessione, poi funzioni |
| Mercato generalista affollato | Profondita sul segmento pilota e adattabilita come elementi distintivi |
