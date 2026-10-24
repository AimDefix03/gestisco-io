# ADR 0008 - Direzione di prodotto: nucleo generalista e segmento pilota abbigliamento

## Stato

Accettata dall'utente il 2026-10-24. Sostituisce l'ordine degli step 4.4-6.5 del master e il congelamento dello scope del 2026-07-28; non modifica il contratto operativo del master (parte A), le invarianti di sicurezza e dominio, i gate di qualita o i finding aperti.

## Contesto

- Il prodotto ha chiuso P0, Gate A-C e gli step 4.1-4.3: ordini, acquisti, magazzino a ledger, inventario fisico, incassi e resi sono solidi, ma l'applicazione non e utilizzabile come software principale di un negozio.
- Il catalogo ha categorie fisse nel codice (`HARDWARE`, `SOFTWARE`) e una sola giacenza per prodotto; l'IVA e un'unica aliquota aziendale; non esistono cassa, codici a barre, varianti, documenti stampabili o adempimenti fiscali.
- La sequenza residua del master (riordino, import, prestazioni, poi hardening e commercializzazione) non conduce a un prodotto vendibile per nessun segmento preciso.
- L'utente vuole un gestionale generalista, adattabile per configurazione, vendibile come prodotto completo e non come versione beta, nel minor tempo possibile.

## Decisione

1. **Nucleo generalista.** Le funzioni sono progettate in forma generica e configurabile (categorie, attributi, varianti, moduli attivabili), senza fork del codice per cliente.
2. **Segmento pilota: abbigliamento e calzature.** Il segmento decide l'ordine dei lavori e i criteri di accettazione, non restringe il modello dati: si parla di varianti, non di taglie.
3. **Roadmap operativa:** `docs/ROADMAP_PRODOTTO.md`, fasi P0-P11 con gate G1-G7. Tutti gli step aperti del master vi sono tracciati (sezione 7); nessun finding viene chiuso per effetto del riordino.
4. **Erogazione iniziale:** istanza dedicata e gestita per ciascun cliente, modalita gia prevista da ADR 0005. Il SaaS condiviso con `tenant_id` resta la direzione di scala (P11).
5. **Mercato italiano.** "Vendibile" include corrispettivi telematici, fattura elettronica e privacy tramite intermediari e professionisti (ADR 0006), mai costruiti in casa ne dichiarati conformi senza validazione.
6. **Obiettivo temporale dell'utente:** 2-3 mesi. Le stime della roadmap restano piu ampie e vengono riviste a fine di ogni fase; i tempi esterni (intermediario, consulenti, penetration test) sono il percorso critico e vanno avviati subito, in parallelo allo sviluppo.

## Alternative valutate

### Verticale su negozi di informatica

Valorizza l'origine del progetto (seriali, garanzie, riparazioni), ma restringe il mercato. Scartata dall'utente a favore del nucleo generalista; seriali e lotti restano possibili come tracciabilita generica.

### Gestionale affiancato al registratore, senza fiscale (versione beta)

Piu rapido, ma impone la doppia registrazione al banco. Scartata dall'utente: l'obiettivo e un prodotto completo.

### Proseguire la sequenza del master (4.4 e seguenti)

Mantiene la coerenza formale, ma costruirebbe funzioni sul modello a giacenza singola, da rifare con le varianti. Scartata.

## Conseguenze

- Le varianti (P2) precedono cassa, acquisti stagionali ed etichette: nessuna funzione nuova si appoggia alla giacenza unica per prodotto.
- La sessione browser in cookie `HttpOnly` (P1.6) richiede una nuova ADR che aggiorni ADR 0004.
- La scorta minima (ex 4.4) passa a P5.5; backup e alert (F-17, F-18, F-19) restano obbligatori prima della produzione (P9).
- `PRODUCT_SCOPE.md` e `ROADMAP.md` rinviano a questa decisione; `IMPLEMENTATION_PROGRESS.md` segue la numerazione P0-P11. Il conteggio 32/48 resta come storico del master.

## Condizioni

- Ogni step resta piccolo, testato e autorizzato singolarmente; nessun controllo viene allentato per rispettare l'obiettivo temporale.
- Le validazioni con persone reali (V1-V3) sono parte dei gate, non opzionali.
- A fine di ogni fase si confrontano stime e avanzamento reale e si aggiorna la roadmap.
