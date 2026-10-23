# Implementation progress

## Stato generale

- Current step: ORG-5 COMPLETED il 2026-10-21; rischio residuo Firefox/axe ridotto e diagnosticabile con la PR #17, integrata il 2026-10-23 come `6416717` con CI e Security verdi su main
- Next eligible step: ORG-6, handoff e prova di ripresa, soltanto con nuova autorizzazione dell'utente. Non iniziare Step 4.4. Collegamento backend/frontend alla copia database recuperata e login web restano da verificare; nessun cutover implicito
- Last completed step nel repository reale: 4.3, Inventario fisico e rettifiche approvate
- Last update: 2026-10-21
- Roadmap completion: 32/48 step completati, sincronizzati e verificati nel repository reale (66.7%)
- Il 66.7% misura gli step completati, non la prontezza produttiva. La precedente stima di maturita 95-97% non e supportata dai gate ancora aperti e viene ritirata.
- Gate C rivalidato localmente; CI e sicurezza remote verdi su `a3407d3`, inclusa scansione dipendenze backend
- Open P0: nessuno
- Open P1: F-17, F-18, F-19; F-04 rivalidato localmente e confermato dal gate SCA remoto
- Open P2: F-29, F-30
- Open P3: nessuno

## Rischio residuo Firefox/axe - 2026-10-21

- Su richiesta dell'utente, step mirato al rischio residuo ORG-5, prima di ORG-6. Branch `codex/firefox-axe-diagnostics` da main ef2d765.
- Aggiornati soltanto @playwright/test 1.61.1 -> 1.64.1 (Firefox piu recente) e @axe-core/playwright 4.13.0 -> 4.14.0, con axe-core 4.14.0. Le note di rilascio non citano una correzione per questo blocco: non e presentato come rimedio. Breaking change 1.64 verificati: viewport esplicite, nessuno snapshot o iframe nei test.
- Limite dedicato di 20 secondi sull'analisi axe con allegato testuale `axe-deadline-diagnostics`. Prova con analisi deliberatamente bloccata in un file temporaneo, poi rimosso: fallimento in 2,5 secondi nel passo `analisi axe`, allegato visibile nel log line. Una prova falsa del meccanismo e stata scartata perche non bloccava davvero.
- Verifiche locali con Node 26.8.1 e browser Playwright 1.64.1: test axe della pagina di accesso passato su Chromium (desktop, mobile, tablet), Firefox e WebKit contro build statica servita da vite preview; test da tastiera saltato senza credenziali, coperto dalla CI prod-like. `npm ci`, `npm test` 209/209 in 42 file, typecheck, typecheck E2E e build passati; contratto diagnostica Firefox 9/9; `npm audit --audit-level=high` exit 0.
- Nuovo avviso moderato su vitest 4.1.11 e @vitest/mocker (GHSA-qxg6-89mg-rqv8, GHSA-xp26-3454-mjgp): sotto la soglia high del gate, non introdotto da questo step, da trattare separatamente.
- PR #17, head ae65e98: CI 39764838539 e Security 39764838680 verdi, 13/13 controlli; backend 320/320, frontend 209/209, PostgreSQL 105/105, E2E 19/19 senza flaky.
- Diagnostica manuale CI 39771639739 su ae65e98 con `firefox_repetitions=100`, runner GitHub x64, Firefox 157.0 (Playwright 1.64.1): 100/100 login axe senza retry, poi suite completa 19/19. Limite axe mai scattato.
- Esecuzioni del test axe di accesso su Firefox nel runner GitHub dopo il fallimento del 19 ottobre: 34 con Playwright 1.61.1 e 102 con 1.64.1, nessun blocco. Con zero eventi su 136 la frequenza resta compatibile con circa il 2% al 95% di confidenza: rischio fortemente ridotto, non escluso. Causa ancora non dimostrata; in caso di ricorrenza la diagnostica allegata indica il punto del blocco.
- CI 39776709207 sul commit documentale 237f9d7: prod-like rosso per un test Chromium passato solo al retry, `acconto, annullo e storno restano atomici`; `failOnFlakyTests` ha bloccato correttamente il gate. Trace scaricata: dopo il click su `Crea bozza ordine` nessuna `POST /api/orders` parte e nessun avviso compare; il click cade mentre il carrello si e appena aggiornato e parte la ricerca prodotti debounced (300 ms). Schermata finale coerente: cliente, riga e pulsante abilitato.
- Correzione del test, stesso schema del 12 ottobre: `createAssistedOrder` attende le risposte delle ricerche cliente e prodotto con il valore digitato, verifica la riga nel carrello e il pulsante abilitato prima del click. Nessun retry, timeout o assertion allentati. Non e dimostrato se il click perso dipenda da uno spostamento del layout o dall'applicazione; se il test fallisse di nuovo con queste attese, va trattato come difetto applicativo. Non si esclude un effetto di Playwright 1.64 sui tempi: un caso su tre suite complete con 1.64.
- Verifica locale: typecheck E2E passato; il test richiede lo stack completo e viene eseguito dalla CI prod-like.
- Commit 860e30c: CI della PR 39803177389 e due esecuzioni manuali sequenziali 39803207068 e 39822049394, tutte verdi; ciascuna con backend 320/320, frontend 209/209, PostgreSQL 105/105 ed E2E 19/19 senza flaky. Security 39803177694 verde. Le esecuzioni manuali erano richieste tutte verdi, non un retry fino al verde.
- PR #17 integrata dall'utente nell'interfaccia GitHub con merge commit `6416717`. Su main: CI 39921304618 verde (320, 209, 105 ed E2E 19/19 senza flaky) e Security 39921304495 verde; Dependency review skipped sul push.
- Rischio residuo aggiornato: blocco Firefox/axe raro e diagnosticabile, causa non dimostrata; click perso nella vendita assistita non ripresentato con le nuove attese. Nessun dato utente o applicativo coinvolto. Restano 12 PR Dependabot aperte e l'avviso moderato su vitest, da trattare separatamente.
- Nessuna modifica applicativa, workflow, soglia, migrazione o database. Organizzazione 5/6, applicazione 32/48. ORG-6 e 4.4 non avviati.

## Chiusura ORG-5 - 2026-10-21

- PR #15 pubblicata con 4e05700 (commit e push autorizzati dall'utente): CI 39676174169 e Security 39676174338 verdi, 13 controlli su 13. Backend Java 17 320/320, frontend 209/209, PostgreSQL 105/105, E2E 19/19 senza flaky, Frontend npm audit passato.
- Merge autorizzato dall'utente ed eseguito da lui nell'interfaccia GitHub: merge commit `b68c5b8`, senza bypass. Main locale aggiornata solo fast-forward.
- Post-merge su main: CI 39678378207 verde (320, 209, 105 e 19/19 E2E senza flaky; Quality gate passato) e Security 39678378202 verde; Dependency review skipped sul push, non contata come prova.
- Protezione main riletta via API: zero approvazioni obbligatorie per decisione del 18 ottobre, dodici check, branch aggiornata, invalidazione delle review obsolete, conversazioni risolte, amministratori inclusi, force push e cancellazione vietati. Alert code scanning aperti: zero.
- Rischio residuo: il timeout Firefox/axe del 19 ottobre non si e ripresentato in 38 esecuzioni Firefox sul runner GitHub (30 ripetizioni diagnostiche del login axe, piu i 2 test Firefox in ciascuna delle 4 suite complete), di cui 34 del test axe, ma la causa non e dimostrata. In caso di ricorrenza usare la diagnostica manuale, senza retry o soglie piu permissive.
- Restano aperte e separate le PR Dependabot #11, #12 e #14, con aggiornamenti major non necessari alla chiusura. Nessuna modifica applicativa, migrazione o database operativo. Organizzazione 5/6 (83.3%), applicazione 32/48 (66.7%). ORG-6 e 4.4 non avviati.
- Caso reale: la versione pubblica principale passa tutti i controlli obbligatori, quindi una nuova sessione o un collaboratore riparte da una base verificata.

## Ripresa ORG-5 e advisory Vite - 2026-10-21

- Ripresa con un nuovo agente (Claude Code). Master, AGENTS e progress riletti; checkout `codex/org5-closeout` pulito su 99dd82d, remote `gestisco` verificato.
- Diagnostica manuale CI 39667419307 su runner GitHub x64, head 99dd82d: 30/30 ripetizioni del login Firefox senza retry, poi E2E completo 19/19. CI 39667424034 della PR #15: tutti i job verdi, E2E 19/19 senza flaky. Nessuna ricorrenza del timeout axe in 34 esecuzioni Firefox, 32 delle quali del test axe (la suite E2E da 19 test ne contiene 2 su Firefox; il conteggio di 49 pubblicato con b68c5b8 era errato e viene corretto qui): non prova la rimozione della causa, che resta non dimostrata.
- Security 39667423941 della PR #15 fallita solo su Frontend npm audit: vite 8.1.3 nell'intervallo vulnerabile 8.0.0-8.1.5, gravita high, advisory successivi al verde del 19 ottobre. Gli altri controlli Security sono passati.
- Correzione locale: vite 8.1.6 a versione esatta, rolldown 1.1.5 nel lockfile; nessun'altra dipendenza cambiata. Scartati `npm audit fix --force` (8.3.5, pubblicata il giorno stesso) e la PR Dependabot #14, che include major di TypeScript, Vitest, jsdom e jest-dom non necessarie allo step.
- Verifiche locali con Node 26.8.1: `npm ci` riuscito; `npm test` 209/209 in 42 file; `npm run typecheck` e `npm run typecheck:e2e` passati; `npm run build` riuscito; `npm audit --audit-level=high` 0 vulnerabilita. Backend, PostgreSQL ed E2E non rieseguiti localmente: modifica limitata al tooling frontend, la verifica completa spetta alla CI.
- Nessun commit, push, merge, migrazione, modifica applicativa, workflow o database. Organizzazione 4/6 (66.7%), applicazione 32/48 (66.7%). ORG-6 e 4.4 non avviati.
- Caso reale: il gate di sicurezza torna a poter essere verde senza abbassare le soglie, e chi sviluppa non lavora con un dev server vulnerabile.

## Diagnostica ORG-5 - 2026-10-21

- Recuperato il log della prova completa del 19 ottobre sullo snapshot 66c3dec: 19/19 E2E e 30/30 ripetizioni del login Firefox senza retry passati. Migrazioni V1-V35, header browser, hardening, osservabilita, rate limiting, backup/restore, privilegi PostgreSQL e cleanup verificati. Browser nel container ufficiale Playwright 1.61.1 Linux ARM64, non runner GitHub x64.
- Primo tentativo diagnostico non valido per confronto CI: host.docker.internal su HTTP causava il rifiuto browser di COOP e due test falliti. Risolto solo nel laboratorio usando il namespace di rete del frontend temporaneo e localhost; nessun header indebolito. Il runner containerizzato resta nelle evidenze private, non sostituisce quello CI.
- Aggiunta diagnostica manuale opzionale al workflow CI: 0, 30 o 100 ripetizioni del solo login Firefox, senza retry, stop al primo errore, trace separata e log pw:browser. Disattivata su push/PR/schedule; i controlli ordinari restano obbligatori. Nove test di contratto coprono disattivazione, argomenti, valori invalidi e propagazione del fallimento.
- La diagnostica non e una correzione del timeout e non autorizza la chiusura ORG-5. Il prossimo controllo deve acquisire un esito sul runner GitHub del nuovo candidato; un verde isolato non dimostra l'eliminazione del problema intermittente.
- Nessuna modifica applicativa, dipendenza, migrazione, database operativo, master o protezione main. Organizzazione 4/6 (66.7%), applicazione 32/48 (66.7%). ORG-6 e 4.4 non avviati.
- Caso reale: un blocco occasionale del controllo qualita diventa ripetibile e ispezionabile, senza pubblicare aggiornamenti dichiarati affidabili sulla sola base di un retry riuscito.

## Ripresa ORG-5 - 2026-10-19

- PR #13 integrata in main con `36ca5c75dc9de6726e2eb4b77ad4ebc6c6c50c31`; prima del merge tutti i dodici check obbligatori erano verdi su c43e680. Nessun bypass, force push, cancellazione branch o modifica del database operativo.
- Il 18 ottobre l'utente ha autorizzato il flusso sviluppatore singolo: zero approvazioni esterne obbligatorie, ma PR, dodici check, branch aggiornata, conversazioni risolte e protezioni amministratori invariati. Questa decisione sostituisce il precedente blocco per revisore distinto nei resoconti storici.
- Main locale allineata esclusivamente fast-forward a gestisco/main; nuove modifiche documentali su codex/org5-closeout. Vecchie branch, ref origin storiche, repository Swing e PR #2 preservati.
- Controlli post-merge: Security 39304147694 passata (Dependency review non applicabile al push); CI 39304147758 fallita nel job prod-like. Backend 320/320, frontend 209/209 e PostgreSQL 105/105 passati; E2E 18 passati e uno Firefox flaky, Quality gate skipped. La configurazione failOnFlakyTests ha correttamente impedito il falso verde.
- Trace: login visibile; evaluateExpression per il caricamento iniziale di axe resta pendente fino al timeout globale di 60 secondi. Nessuna analisi WCAG viene completata in quel tentativo. Non e un errore di focus dimostrato, ne una violazione WCAG rilevata; causa browser/tool non ancora provata. Il retry passato non chiude il problema.
- Riproduzione temporanea sul Mac, pagina login statica con stessa CSP e nessun backend/credenziale: 20 esecuzioni senza tracing e 20 con tracing/video completate, zero violazioni. Queste prove isolate non sostituiscono la CI Linux, il flusso autenticato o un test integrale prod-like.
- Ulteriori 20 esecuzioni con trace/video nel container ufficiale Playwright 1.61.1 noble, senza rete esterna o database: passate, zero violazioni. La riproduzione ridotta non espone il difetto; resta da confrontare l'esecuzione completa sul runner GitHub. Nessuna correzione speculativa applicata.
- Master, codice, workflow, dipendenze e migrazioni invariati in questa ripresa. Nessun nuovo finding chiuso o funzione applicativa aggiunta. Il checkpoint documentale registra il fallimento, non dichiara ORG-5 completato.
- Organizzazione 4/6 (66.7%) fino alla verifica post-merge; applicazione 32/48 (66.7%), non prontezza commerciale. ORG-6 e 4.4 non avviati.
- Beneficio reale: la versione pubblica principale include le correzioni verificate; lo sviluppatore singolo puo integrare tramite PR senza aggirare i controlli automatici o confondere il prodotto con Swing.

## Ripresa ORG-5 - 2026-10-18

- Commit applicativo pubblicato nella PR #13: `5e57d125ebb4617b43d4a7de4f4582b3faacdaf4`. CI 39013781946 e Security 39013781949 concluse con successo: backend Java 17 320/320, frontend 209/209, PostgreSQL, scansioni e build passati. Nessun merge; main resta cabc12f.
- Il risultato E2E non e 19/19 stabili: 18 passati al primo tentativo e uno Firefox passato al retry. La traccia mostra tutte le assertion di navigazione/focus superate, poi un timeout nell'iniezione di axe. La causa del timeout non e dimostrata e non si dichiara risolta con il semplice retry.
- Per evitare falsi verdi, Playwright ora usa `failOnFlakyTests` in CI, mantenendo il retry per diagnostica. Contratto verificato con fixture sintetica: stesso test intermittente, exit 0 prima ed exit 1 dopo; test stabile exit 0. Nessun test reale disabilitato, soglia allentata o timeout aumentato. Nuova pipeline richiesta sul commit della configurazione.
- Tentativi aggiuntivi npm test/build nel checkout Desktop interrotti con SIGINT dopo attesa senza output; non contati come superati. Candidato locale e checkout CI verificati separatamente. Backup Git completo del candidato creato e verificato; master invariato.
- ORG-5 resta IN_PROGRESS; review distinta necessaria prima del merge. Organizzazione 4/6 (66.7%), applicazione 32/48 (66.7%), non stima di vendibilita. ORG-6 e 4.4 non avviati.
- Beneficio reale: una regressione intermittente nell'uso quotidiano non viene nascosta da un secondo tentativo riuscito durante i controlli prima dell'integrazione.

## Ripresa ORG-5 - 2026-10-17

- PR #13 su `codex/org5-publication-verification`, precedente head e85fd26. CI passata; Security inizialmente bloccata da Dependency graph non abilitato. Dopo l'abilitazione confermata dall'utente, rerun 38926093388 passato inclusa Dependency review. Nessuna protezione rimossa.
- Investigato anche il precedente fallimento Chromium sulla baseline main: ripristino del focus differito nel dialog e fallback che poteva selezionare Aggiorna invece della scheda attiva. Quattro nuove regressioni prima rosse, poi verdi; frontend completo 209/209, build e typecheck E2E passati nel candidato locale.
- Test browser rafforzato senza focus artificiale dopo la conferma. CI/Security sul nuovo commit della correzione ancora da verificare; non considerare automaticamente valide le pipeline dell'head precedente.
- Nessuna modifica backend, migrazione, dato operativo, master o dipendenza. Nessun merge. ORG-5 resta IN_PROGRESS; organizzazione 4/6 (66.7%), applicazione 32/48 (66.7%).
- Beneficio reale: dopo la conferma di una navigazione con bozza aperta, la tastiera resta sulla destinazione corretta; i controlli GitHub devono verificarlo prima dell'integrazione.

## Ripresa ORG-5 pubblica - 2026-10-16

- L'utente ha sostituito la precedente scelta privata con l'autorizzazione esplicita al repository pubblico. API GitHub: `AimDefix03/gestisco-io` PUBLIC e vuoto al preflight; endpoint rulesets accessibile, lista vuota. Nessun acquisto di abbonamento.
- Scelta esplicita: mantenere i diritti riservati. README e NOTICE non introducono una licenza open source; dipendenze e contenuti di terzi mantengono le proprie condizioni.
- Consolidamento della separazione e dei documenti pubblicato su main: `cabc12f52e0a6adeb5712babeb78a5b462562e0e`. Remote `gestisco`; nuova branch di verifica `codex/org5-publication-verification`. Nessun force push, riscrittura della storia o pubblicazione delle branch locali di archivio.
- L'account indicato per la review e `AimDefix03`, coincidente con l'autore. Non puo approvare le proprie PR: mantenere una approvazione obbligatoria e attendere un revisore distinto per i merge successivi. Nessuna approvazione fittizia o bypass.
- Baseline locale verificata: backend 320/320, frontend 205/205, build e typecheck E2E passati; 649 file del commit identici al candidato e master invariato. Gitleaks su candidato e 23 commit pubblicati senza rilevazioni. Java locale 23 con release 17, non prova runtime Java 17.
- Main protetta con una review obbligatoria, dodici check vincolati a GitHub Actions, branch aggiornata, amministratori inclusi e divieto di force push/cancellazione. CI 38925595841 e Security 38925596067 ancora da concludere; Dependency review richiede la PR. Dettagli e limiti nel [rapporto ORG-5](docs/ORG5_PUBLICATION.md).
- Vecchia PR #2 lasciata aperta e draft per conservare provenienza e review storiche; non usarla per promuovere il prodotto web nel repository legacy. Chiusura/archiviazione richiedono un'azione separata autorizzata.
- Nessuna funzionalita, migrazione, dato operativo o credenziale modificati. Organizzazione 4/6 (66.7%); applicazione 32/48 (66.7%). Beneficio reale: preparare una fonte pubblica riconoscibile del prodotto con controlli di integrazione verificabili, senza confondere il legacy o concedere una licenza open source.

## Preflight storico ORG-5 privato - 2026-10-16

Questo resoconto precede la successiva autorizzazione esplicita al pubblico; non rappresenta la visibilita corrente.

- Autorizzazione alla prosecuzione e scelta esplicita dell'utente: repository privato. Creato `https://github.com/AimDefix03/gestisco-io`, privato e vuoto. Nessun codice pubblicato, commit, remote aggiunto, merge, modifica della vecchia PR #2 o acquisto di abbonamento.
- GitHub restituisce HTTP 403 con richiesta di upgrade per ruleset e protezioni classiche. Code scanning non abilitato; compatibilita dei gate privati da risolvere senza rimuoverli. Non rendere pubblico il repository e non ridurre i gate implicitamente.
- Rapporto e punto di ripresa: [ORG-5 pubblicazione](docs/ORG5_PUBLICATION.md). Gitleaks sul bundle storico (25 commit) e sullo snapshot corrente (647 file): nessuna rilevazione; la prima scansione Docker del checkout ha letto zero commit per errore Git e NON vale come verifica superata. Backup ed evidenze in `/Users/aim_defix/GestiscoBackups/ORG5-20261016`.
- Modifiche limitate alla documentazione. Suite applicative e pipeline remote non rieseguite; nessuna modifica a codice, database, credenziali, master o workflow. Separazione e documenti ancora non committati.
- Organizzazione 4/6 (66.7%); roadmap applicativa 32/48 (66.7%). ORG-5 non completato. Beneficio reale: evitare una pubblicazione che sembri protetta ma permetta modifiche senza i controlli concordati.

## Risultato ORG-4 - 2026-10-15

- Identita documentale Gestisco.IO applicata a README, istruzioni agenti, scope, architettura e roadmap. Rimossi riferimenti correnti alla presenza di Swing nella root web e stime di maturita non supportate; mantenuti i resoconti storici datati.
- [Addendum](docs/GESTISCO_IDENTITY_ADDENDUM.md): recepisce nome e separazione autorizzati, senza ampliare il target single-company/single-warehouse o rendere disponibili funzioni SaaS, pagamenti bancari e documenti fiscali.
- [Verifiche e limiti](docs/ORG4_VERIFICATION.md): controlli documentali e di invarianza. Suite applicative non rieseguite; le ultime prove nelle destinazioni restano quelle ORG-3 del 2026-10-14.
- Nessun sorgente, UI, dipendenza, configurazione, master, migrazione, database o credenziale modificato. Nessuna operazione Git di scrittura o remota; branch `miglioramenti-gestionale`, HEAD `b077f7a`. Separazione e documentazione ancora non committate.
- Fonte autorevole: `/Users/aim_defix/Desktop/Gestisco.IO`; le altre copie non sono aggiornate da questo step. Evidenze private: `/Users/aim_defix/GestiscoBackups/ORG4-20261015`.
- Organizzazione 4/6 (66.7%, conteggio non pesato). Roadmap applicativa 32/48 (66.7%), invariata; nessun finding chiuso e nessuna percentuale di vendibilita dedotta.
- Beneficio reale: chi valuta o continua il progetto vede il prodotto web attuale e i suoi limiti, trova la root corretta e non scambia obiettivi SaaS futuri per funzionalita gia disponibili.

## Fonte di lavoro e risultato ORG-3 - 2026-10-14

- Sorgente web per le prossime attivita: `/Users/aim_defix/Desktop/Gestisco.IO`. Legacy congelato: `/Users/aim_defix/Desktop/GestionaleNegozioComputer`.
- L'originale `/Users/aim_defix/Desktop/Gestionale.Negozio.Computer `, incluso spazio finale, resta conservato per rollback; sorgenti e sette file dati locali invariati. Non continuare lo sviluppo in quella cartella o in codex_portfolio_sources.
- Entrambe le nuove copie derivano da `b077f7a` e preservano 25 commit e sei ref, inclusi tre commit locali non pubblicati; fsck strict passato. Nessun remote configurato, commit, tag, push o merge effettuato. Le ref origin conservate sono snapshot storici.
- Separazione non committata: 83 file legacy esclusi dal checkout web e 634 file esclusi dal checkout Swing, archiviati privatamente e ancora disponibili nella storia. Non annullare queste rimozioni intenzionali con reset/checkout.
- Verifiche nelle cartelle definitive: backend `mvn -B -o verify` 320/320; frontend `npm ci --offline`, 205/205 test in 42 file, build e typecheck E2E; Swing `mvn -B -o -f pom-legacy.xml verify` 41/41 e JAR. Nessun test saltato nelle suite Maven. Java locale 23.0.1 con release 17, non nuova prova runtime Java 17.
- Nessun codice applicativo, dipendenza, migrazione o configurazione operativa cambiato. Nessun dato/secret importato automaticamente. Master immutato. Nessun avvio o cutover dell'ambiente operativo, nessuna nuova prova browser/PostgreSQL.
- Istruzioni: [layout locale](docs/LOCAL_LAYOUT.md), [verifica finale](docs/ORG3_LOCAL_VERIFICATION.md), [storico preflight](docs/ORG3_SEPARATION.md).
- Evidenze private durevoli: `/Users/aim_defix/GestiscoBackups/SEPARATION-20261014`. Il recupero database precedente resta separato in RECOVERY-20261014-cMQTc8, senza nuova rotazione o reset account.
- Beneficio reale: lo sviluppo del prodotto web non si confonde piu con il progetto Swing; cronologia e dati restano recuperabili prima di qualsiasi futura pubblicazione.
- Organizzazione 3/6 (50%, conteggio non pesato); roadmap applicativa 32/48 (66.7%), invariata. Non e una percentuale di prontezza commerciale.

## Organizzazione Gestisco.IO - avviata il 2026-10-13

- [x] ORG-1: inventario cartelle, Git e GitHub. Fonte autorevole: `/Users/aim_defix/Desktop/Gestionale.Negozio.Computer ` (spazio finale nel nome), branch `miglioramenti-gestionale`, HEAD `b077f7afe42b08c7976713d14552cbd58e9107a2`, allineata a origin e pulita prima di questo aggiornamento documentale.
- [x] ORG-2: web senza Swing verificato (backend 320 test, frontend 205 e build); legacy isolato 41 test e JAR. Snapshot `9f9e23a` identico a HEAD per src/pom-legacy. Fsck passato; bundle di tutte le ref recuperato e verificato, inclusi tre commit locali.
- [x] ORG-3: COMPLETED; recupero isolato precedente e separazione locale dei sorgenti conclusi. Due copie autonome verificate, originale preservato, nessun cutover operativo. Evidenze finali in docs/ORG3_LOCAL_VERIFICATION.md.
- [x] ORG-4: identita e documentazione allineate a Gestisco.IO; addendum sulle decisioni organizzative approvate, master e perimetro funzionale invariati.
- [x] ORG-5: COMPLETED il 2026-10-21; PR #13 e #15 integrate, CI e Security verdi su main b68c5b8, protezioni rilette. Timeout Firefox/axe registrato come rischio residuo. Vedere docs/ORG5_PUBLICATION.md.
- [ ] ORG-6: consegna del contesto, istruzioni avvio/ripristino e verifica di ripresa da una nuova chat.
- Avanzamento organizzativo: 5/6 milestone (83.3%, conteggio non pesato). Roadmap applicativa invariata: 32/48 (66.7%); nessuna percentuale di prontezza commerciale dedotta.
- Report, evidenze, criteri di accettazione e stop condition: [Organizzazione repository](docs/REPOSITORY_ORGANIZATION.md).

### Note storiche dell'inventario ORG-1

Queste note descrivono il controllo iniziale del 2026-10-13. Per integrita, cartelle e test attuali fare riferimento al risultato ORG-3 sopra.

- Al controllo, 1.404 file `.git` risultavano `dataless` su macOS: disponibilita locale completa e integrita non ancora certificate. Non equivale a perdita o corruzione. Nessun archivio Git completo creato in questo step.
- Portfolio gia separato con modifiche preesistenti; copie di staging e review non promosse a sorgente autorevole. Nessuna cancellazione o sovrascrittura di tali copie.
- CI e Security verdi su `b077f7a` verificate tramite GitHub. Test applicativi non rieseguiti in questo step documentale. Nessun commit, push, merge, deploy, migrazione o accesso ai dati operativi.
- Beneficio reale: evita di continuare nella copia sbagliata o perdere lavoro locale, cronologia e dati durante la separazione dei progetti.

Le sezioni successive conservano la cronologia delle verifiche: gli stati PARTIAL o non iniziato indicati nei resoconti precedenti non sostituiscono lo stato corrente sopra.

### Verifiche ORG-2

- Dettaglio, hash, comandi e limiti: [ORG-2](docs/ORG2_VERIFICATION.md).
- Prove in copie temporanee estratte dal commit remoto esatto `b077f7a`, senza inizializzare Git, senza file .dat o configurazioni operative.
- `mvn -B -o verify`: primo tentativo fallito per 3 test Actuator impossibilitati ad aprire socket dal sandbox; ripetizione fuori sandbox PASS 320/320, nessuno skip, packaging riuscito. Java 23.0.1 con release 17, non nuova prova runtime Java 17.
- `npm ci --offline`, `npm test` 205/205 in 42 file, `npm run build` e `npm run typecheck:e2e` passati. Nessuna nuova scansione online o prova browser dichiarata.
- `mvn -B -o -f pom-legacy.xml verify`: PASS 41/41 e JAR, senza web nella copia di prova. Nessuna prova GUI o parita operatore.
- Legacy consigliato: `src/` e `pom-legacy.xml` a `9f9e23ab1089f640efe7d88a91debba24ac0884a`, identici a HEAD; main contiene solo 33 file src contro gli 82 della versione completa. Nessun tag creato.
- `git rev-list --branches --not --remotes` rileva 3 commit locali nelle branch pre-publication; refs censite nel report, non eliminate o pubblicate.
- Dopo attese nelle letture locali, `git fsck --full` concluso con exit 0: due oggetti dangling, nessun oggetto corrotto o mancante. Nessun prune o riparazione. Il conteggio intermedio di 406 file dataless non sostituisce questa verifica.
- Bundle completo `--all` verificato, clone mirror recuperato con `git fsck --full --strict` verde: tutte le sei ref preservate, 25 commit raggiungibili inclusi i tre locali. Copia conservata in `/Users/aim_defix/GestiscoBackups/ORG2-20261013/history.bundle`, checksum SHA-256 nel report. Non include dati ignorati, documenti non committati, reflog o oggetti dangling; documenti salvati separatamente.
- Verifica delle copie testate contro il clone recuperato: 722 blob corrispondenti agli hash Git, zero differenze, tre assertion di isolamento passate.
- Nessun sorgente, migrazione, database operativo o repository remoto modificato. Avanzamento concluso 2/6 organizzativo e 32/48 applicativo. ORG-3 non iniziato.

### Preflight ORG-3

- Autorizzato dall'utente il 2026-10-13. HEAD originale ancora `b077f7a`; preservati progress e due report ORG-1/ORG-2 non committati. Bundle ORG-2 nuovamente verificato.
- Sei file `.dat` e un dump storico locale sono conservati fuori dal repository, con confronto checksum sorgente/copia e permessi privati. Non e un nuovo backup del database corrente e non certifica un restore.
- Due stack gestionali rilevati, entrambi fermi: `gestionale-step43-demo` e `gestionalenegoziocomputer`. Entrambi i volumi PostgreSQL esistono; contenuto e recuperabilita non certificati da un semplice inventario.
- Lo stack demo usa sei file secret sotto `/tmp/gestionale-step43-demo/secrets`; la directory e assente. Nessuna `.env.docker` locale rilevata nel repository. Ricerca mirata di copie nei backup organizzativi, nel workspace e in Documents/REVIEW senza risultati; non e una ricerca esaustiva dell'host.
- Stop condition rispettata: nessuna cartella destinazione creata, nessun sorgente spostato, nessun container/volume modificato, nessun deploy, commit o push. Il progetto originale resta autorevole. Portfolio escluso.
- Ripresa: recuperare configurazione e secret da copia privata/secret manager senza incollarli in chat. In assenza, concordare un recupero isolato con backup del volume prima di qualsiasi rotazione o ricreazione. Non generare password nuove fingendo che corrispondano al database esistente.
- Organizzazione ancora 2/6 (33.3%); applicazione 32/48 (66.7%). Beneficio reale: evita di separare il codice lasciando inosservata la perdita delle configurazioni necessarie al riavvio.

### Recupero database ORG-3 - 2026-10-14

- L'utente ha autorizzato backup del volume e recupero isolato, incluse eventuali nuove credenziali soltanto sulla copia. Non autorizzati implicitamente cutover, reset account applicativi o modifiche al database originale.
- Backup fisico a database fermo, originale montato esclusivamente read-only e senza rete. Restore in volume nuovo: 1.482/1.482 file identici. La vecchia immagine Docker era assente; usata un'immagine locale PostgreSQL 16 compatibile fissata per ID, senza download o aggiornamenti.
- Copia PostgreSQL avviata senza rete/porte pubblicate. Cinque password ruoli database generate casualmente e conservate in directory privata stabile. Nessuna password utente applicativa cambiata. Trust locale temporaneo confinato alla copia e poi sostituito da SCRAM su tutte le regole effettive.
- 12 controlli positivi/negativi passati: cinque credenziali, password errata rifiutata, HBA SCRAM, impronte delle 25 tabelle invariate, runtime senza DDL, SET ROLE migrator, dump logico/catalogo e isolamento. 35 migrazioni riuscite; prova aggiuntiva riavvio e accesso runtime riuscita.
- Originale nuovamente verificato a fine intervento: 1.482 file invariati e container ancora fermo con stesso ID. Copia arrestata pulitamente. Nessun mount in scrittura sul volume originale, nessuna operazione sul secondo stack storico o su progetti terzi.
- Backup privato e istruzioni: `/Users/aim_defix/GestiscoBackups/RECOVERY-20261014-cMQTc8`. Snapshot fisico originale, snapshot recuperato, dump logico, checksum e nuove credenziali. Non cifrato/off-site, non condivisibile per review pubblica.
- Non verificati: UI/login web, avvio backend/frontend sulla copia, secondo restore del dump logico e validazione operatore. Non sostituisce il futuro Step 4.7.
- ORG-3 rimane PARTIAL: cartelle non separate. Percentuali 2/6 (33.3%) organizzativo e 32/48 (66.7%) applicativo invariate.
- Beneficio reale: credenziali infrastrutturali smarrite non costringono ad azzerare il gestionale; esiste una copia accessibile e verificata, senza cambiare account o dati originali.

## Consolidamento 2026-10-11

- Stato: COMPLETED sul commit applicativo `a3407d3`. Correzioni pubblicate, CI e sicurezza remote verificate. Nessuna nuova funzionalita dello Step 4.4; nessun merge o deploy.
- Checkpoint `2d783d5`, documentazione `76b1843`, parser download `622efb7` e flusso inventario `f330b00` gia pubblicati su `miglioramenti-gestionale`, PR #2 in draft.
- La CI di `f330b00` ha evidenziato un contratto E2E obsoleto, un verificatore header non portabile su Linux e rilevazioni SCA backend. I 19 test browser remoti erano passati, ma questo non rendeva verde il gate prod-like completo.
- Contratto aggiornato al conteggio con invio e approvazione separata; regressioni frontend su conservazione della bozza al refresh e isolamento tra sessioni.
- Riprodotto il fallimento Linux del confronto `Retry-After` su CRLF e normalizzato il terminatore senza cambiare rate limiting o soglie. Aggiunti 16 casi positivi/negativi per login e registrazione.
- Patch Spring Boot 4.1.1 / Spring Framework 7.0.9 / Spring Security 7.1.1 e Tomcat 11.0.26; Vitest 4.1.11 e patch transitiva source-map-js. Nessuna soppressione CVE o esclusione dei test.
- Copia di verifica estratta dal commit remoto esatto `f330b0081c36d194e28f4dc8c3b300b8e27895dc`: l'estrazione Git locale si arrestava in lettura dei file iCloud. Non e stato inizializzato un repository alternativo.
- Verifiche locali della patch `a56df5f`: backend `mvn -B verify` 311/311; frontend `npm ci`, 205/205 test in 42 file, build TypeScript/Vite e typecheck E2E passati. Java locale 23 con release di compilazione 17; build Docker Java 17 riuscita.
- PostgreSQL `fast`: 105/105 test; fixture popolate V14, V16 e V18 migrate fino a V35; nessuna migrazione nuova.
- OWASP Dependency-Check 12.2.2: 66 dipendenze, 0 dipendenze vulnerabili e 0 CVE; aggiornamento NVD completato il 2026-10-11. Audit npm sul lockfile aggiornato: 0 vulnerabilita. Nessuna garanzia di assenza assoluta di vulnerabilita.
- Runner prod-like `local-1-1d982202dc0b01db`: 19/19 Playwright, V1-V35, rate limit login/registrazione, CSP, container non-root/read-only, metriche/log, backup/restore (9 secondi sul dataset di prova), ruoli PostgreSQL separati e rotazione credenziali passati. Risorse temporanee del run rimosse; fail-closed startup 2/2 passato separatamente.
- Ripresa del 2026-10-12: repository reale pulito su `miglioramenti-gestionale`, HEAD `f330b00`; controllo Gitleaks 8.30.1 senza rete sui 15 file candidati completato senza rilevazioni.
- I 15 file coincidono byte per byte con la copia verificata e sono pubblicati nel commit `a56df5f5bf89e879e26376f16ab691dc500c3bc1` su `miglioramenti-gestionale`. PR #2 aggiornata e mantenuta in draft.
- [CI 38228668606](https://github.com/AimDefix03/gestionale-negozio-computer/actions/runs/38228668606): SUCCESS, inclusi backend Java 17, frontend, PostgreSQL, Docker prod-like e quality gate.
- [Security 38228668503](https://github.com/AimDefix03/gestionale-negozio-computer/actions/runs/38228668503): Gitleaks, dependency review, npm audit e CodeQL Java/TypeScript passati; `Backend dependency inventory` ancora IN_PROGRESS al controllo del 2026-10-12. Le analisi CodeQL del merge ref `65090889c92c52b8287f7eaf31b0eb681a5c2fd7` riportano 0 risultati; nessun alert aperto sulla PR.
- Alla prima ripresa il log finale SCA non era disponibile; gli esiti erano stati salvati nel commit locale `6e13671`, senza interrompere il run. Lo scan locale verde non sostituisce questo controllo.
- Diagnosi successiva autorizzata: annullato il run `38228668503` dopo oltre due ore e acquisiti i log. Il runner, privo di API key NVD, era ancora nel download iniziale di 140.000/404.465 record (35%), prima dell'analisi. Il run annullato NON e superato.
- Correzione circoscritta alla CI: feed JSON 2.0 ufficiali NIST al posto dell'API paginata, database CVE temporaneo isolato, controllo aggiornamenti sempre attivo, timeout scan 20 minuti/job 30 e artifact del log anche su errore. Soglia CVSS 7.0 e `failOnError=true` invariati.
- Test del contratto sicurezza: 5/5 passati; prova runtime con feed localhost irraggiungibile: fallimento atteso (exit 1), senza falso verde. Scansione positiva da cache vuota completata in 9 minuti e 40 secondi: 66 dipendenze, 0 vulnerabilita rilevate, feed NVD aggiornato al 2026-10-12. OSS Index non eseguito per assenza delle credenziali richieste dal servizio; non e stato presentato come copertura verificata. Nuova CI da completare prima della chiusura.
- Il checkpoint documentale `6e13671` e stato successivamente pubblicato insieme alla correzione; gli esiti dei run successivi sono riportati sotto.
- Suite backend completa dopo il nuovo test: `mvn -B verify`, 312/312 test, zero errori o skip, packaging riuscito. YAML valido e `git diff --check` senza errori.
- Pubblicati `6e13671` e `cca78d3`. CI `38263059026` verde: backend 312/312, frontend 205/205, PostgreSQL 105/105, prod-like e quality gate. Il dettaglio Playwright contiene 18 test passati e 1 flaky (acconto/annullo/storno passato al retry), quindi non viene descritto come esecuzione stabile 19/19.
- La trace del primo tentativo mostra il refresh della ricerca ordini debounced durante il click su Registra incasso, senza invio del POST. Corretto l'helper E2E per attendere la risposta GET con il filtro esatto prima di aprire il dettaglio. Nessun timeout aumentato, retry aggiunto o controllo finanziario modificato. Typecheck E2E passato; runner locale `local-1-088b1283b3c8c1d3`: 19/19 browser senza retry, rate limit, backup/restore (7 secondi sul dataset sintetico), privilegi/rotazione e cleanup passati.
- Security `38263059365` FALLITO: timeout di 20 minuti durante il caricamento dei feed NIST diretti, prima dell'analisi. Il log e stato conservato correttamente. Nessun esito SCA remoto positivo viene dichiarato.
- Seconda correzione: mirror mantenuto da Dependency-Check e alimentato da NVD, con preflight obbligatorio che rifiuta dati oltre 48 ore, timestamp futuri o metadati invalidi/assenti. Nessun aumento dei timeout o riduzione della soglia CVSS. Test mirati 13/13; scansione a freddo in 1m24s: 66 dipendenze e 0 vulnerabilita rilevate, metadati e report con ultimo aggiornamento 2026-10-11T16:16:31Z. Preflight reale passato. Nuova CI da verificare.
- Suite backend completa sulla seconda correzione: `mvn -B verify`, 320/320 test, nessun errore, fallimento o skip; packaging completato. I controlli remoti sul nuovo commit restano necessari.
- Chiusura del 2026-10-12: gli otto file verificati coincidono con la copia di test e sono pubblicati in `a3407d35f5526f4a0e85c31578201f0fc951dea3`. Il repository reale e pulito e allineato a origin prima dell'aggiornamento documentale conclusivo.
- [CI 38303795754](https://github.com/AimDefix03/gestionale-negozio-computer/actions/runs/38303795754): SUCCESS su `a3407d3`; backend Java 17 320/320, frontend 205/205 in 42 file, PostgreSQL 105/105, 19/19 Playwright senza retry, build e quality gate verdi. Backup/restore completato in 6 secondi sul dataset sintetico; privilegi/rotazione PostgreSQL e cleanup del runner isolato passati.
- [Security 38303795737](https://github.com/AimDefix03/gestionale-negozio-computer/actions/runs/38303795737): SUCCESS su `a3407d3`, merge ref `3a3553934bed4dea30475ad766add141b54a9332`. Artifact remoto verificato: Dependency-Check 12.2.2, 66 dipendenze, 0 dipendenze vulnerabili e 0 CVE rilevate; NVD aggiornato al 2026-10-12T20:10:00Z; scansione Maven in 1m38s. Gitleaks, dependency review e npm audit verdi; CodeQL Java e TypeScript senza errori, 0 finding e nessun alert aperto nella PR.
- PR #2 ancora draft; protezioni main confermate (quattro check obbligatori, branch aggiornata, una approvazione, nessun bypass amministratore, force push e cancellazione disabilitati). `main` resta a `6da79514b5f354ea730be255d05c36855477b0e6`. I risultati sopra identificano il commit applicativo verificato: ogni push successivo deve mantenere i propri controlli verdi prima del merge.
- Beneficio operativo: gli aggiornamenti del gestionale sono controllati con dati CVE recenti e con una prova stabile del flusso incasso/annullo/storno; un feed assente o obsoleto blocca la scansione invece di dare falsa rassicurazione. Nessuna garanzia di assenza assoluta di vulnerabilita o di prontezza produttiva.
- Nessuna migrazione nuova o modifica dei database operativi; nessun merge o deploy.

## Step

| Step | Stato | Commit/patch | Test | Note |
|---|---|---|---|---|
| 0.1 | COMPLETED | `9f9e23a` baseline; `945fcd0` CI; `62a8d14` security; `fa52524` gate finali; `8d9be88` hardening CodeQL | backend 155/155; frontend 32/32; legacy 41/41; CI, security e code scanning remoti verdi | PR #2 aperta in draft; `main` protetta con backend, frontend, migration e quality gate obbligatori |
| 0.2 | COMPLETED | checkpoint `2d783d5` | verifica backlog e coerenza documentale | Scope congelato; target MVP, responsabile decisionale e tre classi di backlog formalizzati |
| 0.3 | COMPLETED | checkpoint `2d783d5` | contratto fixture 5/5; backend 160/160; PostgreSQL V14/V16/V18 profilo `small` | Fixture sintetiche versionate, checksum, profili di volume e verificatore PostgreSQL isolato |
| 1.1 | COMPLETED | checkpoint `2d783d5` | backend 166/166; frontend 32/32; typecheck e build verdi; contratto gateway 1/1 | Registrazione pubblica limitata a CUSTOMER, staff confinato al flusso admin, audit SELF_SERVICE e rate limit dedicato |
| 1.2 | COMPLETED | checkpoint `2d783d5` | backend 171/171; test mirati 5/5; fixture PostgreSQL V14/V16/V18/V19 profilo `small` | Provenienza account, quarantena conservativa degli operativi storici non verificati, revoca sessioni e revisione manuale super admin |
| 1.3 | COMPLETED | checkpoint `2d783d5` | backend 178/178; PostgreSQL concorrenza 6/6; fixture V14/V16/V18/V19/V20 | Subject account ID stabile, versione credenziali, revoca globale e race revoca/touch serializzate |
| 1.4 | COMPLETED | checkpoint `2d783d5` | backend 188/188; frontend 33/33; PostgreSQL ownership 8/8; fixture V14/V16/V18/V19/V20/V21 | Ownership account/partner basata su ID, storico ambiguo `UNRESOLVED` e link account-partner esplicito |
| 1.5 | COMPLETED | checkpoint `2d783d5` | contratto 9/9; backend 194/194; runtime Docker sentinel/collision/HUP/INT/TERM/KILL; prod-like completo con Playwright 3/3 | Runner Docker isolati per project/run ID, cleanup per ownership e CI prod-like riattivata |
| 1.6 | COMPLETED | checkpoint `2d783d5` | backend 201/201; frontend 33/33; SCA 66 dipendenze/0 vulnerabilita; fail-closed 2/2 | Spring Boot 4.1, Security 7.1, bootstrap sicuro, H2 test-only e gate SCA |
| 2.1 | COMPLETED | checkpoint `2d783d5` | backend 204/204; frontend 34/34; PostgreSQL upgrade V14/V16/V18, medium/large e concorrenza 12/12 | V22 conserva V15/V17, inizializza contatori, introduce `UNRECONCILED` e procedura manuale protetta |
| 2.2 | COMPLETED | checkpoint `2d783d5` | backend 211/211; frontend 34/34; PostgreSQL canonical identity 5/5; migrazione 2/2 | Colonne canoniche V23, vincoli DB concorrenti, preflight e conflitti API uniformi |
| 2.3 | COMPLETED | checkpoint `2d783d5` | backend 219/219; frontend 37/37; PostgreSQL ledger 9/9; build Vite verde | Ledger autorevole V24, comandi inventariali, protezione riserve e riconciliazione |
| 2.4 | COMPLETED | checkpoint `2d783d5` | backend 224/224; frontend 42/42; PostgreSQL idempotenza 10/10; build Vite verde | Claim atomico V25, replay durevole, lease e chiavi frontend legate all'intento |
| 2.5 | COMPLETED | checkpoint `2d783d5` | backend 224/224; frontend 50/50; typecheck e build Vite verdi | Risposta comando applicata subito, refresh mirati, esiti distinti, protezione doppio invio e bozze preservate |
| 2.6 | COMPLETED | checkpoint `2d783d5` | backend 232/232; frontend 51/51; PostgreSQL 18/18; typecheck e build Vite verdi | Reversal manuale atomico, rilascio riserve, audit, idempotenza e UI operativa |
| 2.7 | COMPLETED | checkpoint `2d783d5` | backend 238/238; frontend 52/52; PostgreSQL 22/22; typecheck e build Vite verdi | Ledger finanziario autorevole, link rimborso-reso, report read-only, metriche e vincoli temporali |
| 2.8 | COMPLETED | checkpoint `2d783d5` | backend 239/239; frontend 52/52; build Vite; Playwright 3/3; PostgreSQL V1-V27, backup/restore e least privilege verdi | Owner `NOLOGIN`, migrator owner-scoped, runtime senza DDL, backup read-only, restore e rotazione separati |
| 2.9 | COMPLETED | checkpoint `2d783d5` | backend 243/243; PostgreSQL 95/95; frontend 52/52; typecheck e build Vite; upgrade popolati V14/V16/V18 e least privilege verdi | Testcontainers PostgreSQL obbligatorio, profili CI fast/nightly, fixture fino a V29 e concorrenza trasversale |
| 3.0 | COMPLETED | checkpoint `2d783d5` | backend 246/246; contratto benchmark 3/3; frontend 52/52; typecheck e build Vite verdi | Template vincolante e WB-001 vendita assistita basato su fonti ufficiali; validazione operatore predisposta per lo Step 3.1 |
| 3.1 | COMPLETED | checkpoint `2d783d5` | backend 251/251; frontend 56/56; typecheck e build Vite verdi; PostgreSQL upgrade V14/V16/V18 fino a V30 | Vendita separata dal catalogo, cliente censito per ID stabile o occasionale esplicito, snapshot righe, audit, self-service protetto e bozza preservata sugli errori; prova operatore ancora pendente |
| 3.2 | COMPLETED | checkpoint `2d783d5` | backend 258/258; frontend 61/61; typecheck e build Vite verdi | Projection server-side per ruolo, disponibilita commerciale discreta e dashboard limitata agli ordini dell'account cliente |
| 3.3 | COMPLETED | checkpoint `2d783d5` | backend 262/262; PostgreSQL 95/95; frontend 64/64; fixture V14/V16/V18 fino a V31; typecheck e build Vite verdi | Lifecycle account non distruttivo, credenziali e sessioni revocabili, login senza ruolo dichiarato dal client, lockout temporaneo atomico |
| 3.4 | COMPLETED | checkpoint `2d783d5` | backend 262/262; frontend 78/78 in 23 file; typecheck e build Vite verdi | Session provider centralizzato e pilot Account con query state, debounce, cancellazione richieste e latest-request-wins |
| 3.5 | COMPLETED (6/6) | checkpoint `2d783d5` | backend 262/262; frontend 179/179 in 35 file; typecheck applicativo/E2E e build Vite verdi | Sessione, catalogo, ordini/pagamenti/resi, partner, documenti/report e amministrazione estratti |
| 3.6 | COMPLETED | checkpoint `2d783d5` | backend 265/265; frontend 183/183 in 35 file; build Vite verde | Dettagli prodotto/ordine, capability server-side, storici mirati e dashboard senza fallback parziali |
| 3.7 | COMPLETED | checkpoint `2d783d5` | backend 271/271; frontend 184/184; build Vite; PostgreSQL upgrade V14/V16/V18 fino a V32 | KPI separati per stato e natura economica, fuso IANA aziendale, eventi con offset e dati emittente obbligatori |
| 3.8 | COMPLETED | checkpoint `2d783d5` | backend 274/274; frontend 188/188; PostgreSQL 97/97; build Vite verde | Resi multi-riga, residui autorevoli, note per return ID, outcome tipizzati e ledger rimborso isolato |
| 3.9 | COMPLETED | checkpoint `2d783d5` | backend 274/274; frontend 198/198 in 39 file; typecheck applicativo/E2E; build Vite; Playwright/axe 13/13; prod-like completo | Semantica tabelle e form, dialog accessibili, navigazione da tastiera, draft store cross-view, dirty guard, touch target e download difensivi |
| 3.10 | COMPLETED | checkpoint `2d783d5` | backend 277/277; frontend 198/198; typecheck applicativo/E2E; build Vite; Playwright/axe 19/19; PostgreSQL fast e upgrade popolati V14/V16/V18 fino a V32 | Sette workflow verticali MVP, matrice browser accessibile, runner unico e Gate C verificato tecnicamente |
| 4.1 | COMPLETED | checkpoint `2d783d5` | backend 285/285; PostgreSQL 98/98; frontend 200/200 in 40 file; typecheck applicativo/E2E e build Vite verdi; upgrade popolati V14/V16/V18 fino a V33 | Ordini fornitore con ID e snapshot stabili, stati, ricezioni amministrative parziali, annullo residuo, idempotenza, concorrenza, audit e UI; stock e costo restano invariati fino allo Step 4.2 |
| 4.2 | COMPLETED | checkpoint `2d783d5` | backend 288/288; PostgreSQL 99/99; frontend 202/202; typecheck applicativo/E2E e build Vite verdi; upgrade popolati V14/V16/V18 fino a V34 | Ricezione fisica, costo effettivo, media mobile e KPI con copertura costo; prova operatore ancora pendente |
| 4.3 | COMPLETED | checkpoint `2d783d5` | backend 295/295; PostgreSQL 105/105; frontend 202/202; typecheck applicativo/E2E e build Vite verdi; upgrade popolati V14/V16/V18 fino a V35 | Conteggio con snapshot, approvazione separata, compensazione dei movimenti intermedi, rettifica auditata e UI operativa; prova operatore ancora pendente |
| 4.4 | PENDING | - | - | Scorta minima e proposte riordino |
| 4.5 | PENDING | - | - | Importazione con staging |
| 4.6 | PENDING | - | - | Performance su volumi target |
| 4.7 | PENDING | - | - | Backup off-site e restore parallelo |
| 4.8 | PENDING | - | - | Restart, alert delivery e log centralizzati |
| 5.1 | PENDING | - | - | Supply-chain security |
| 5.2 | PENDING | - | - | Artifact immutabile, promozione e rollback |
| 5.3 | PENDING | - | - | Security hardening e pentest |
| 5.4 | PENDING | - | - | Load, concorrenza, failure e DR |
| 5.5 | PENDING | - | - | Privacy e retention |
| 5.6 | PENDING | - | - | Decisione perimetro pagamenti |
| 6.1 | PENDING | - | - | Packaging, onboarding e configurazione guidata |
| 6.2 | PENDING | - | - | Supportabilita e policy upgrade |
| 6.3 | PENDING | - | - | Permessi granulari e approvazioni |
| 6.4 | PENDING | - | - | Documenti fiscali tramite provider e consulenza |
| 6.5 | PENDING | - | - | Integrazioni e funzioni vendibili |

## Decisioni approvate

- Il 2026-10-18 e stata autorizzata la rimozione del solo requisito di approvazione esterna per lo sviluppatore singolo; PR e dodici controlli automatici restano obbligatori. Il 2026-10-19 e stata richiesta la ripresa della chiusura ORG-5, inclusa l'integrazione verificata della PR #13; nessun deploy o avvio ORG-6 implicito.
- `MASTER_SOURCE_AGENTE_AI.md` e la fonte operativa vincolante.
- Target produttivo: monolite modulare Spring Boot + React con PostgreSQL.
- La cartella `src/` e legacy e non riceve nuove funzioni salvo step esplicito.
- Un solo step viene eseguito per turno; avanzamento automatico disabilitato.
- Nessun deploy, commit, push, PR o operazione distruttiva senza autorizzazione esplicita.
- Nessuna nuova funzione commerciale prima della chiusura dei P0 e dei gate dipendenti.
- Il 2026-07-27 e stato autorizzato il commit della baseline e la configurazione della branch protection su `main`.
- Il 2026-07-27 e stato autorizzato il push della branch `miglioramenti-gestionale`.
- Il 2026-07-27 e stata autorizzata l'apertura della pull request e il completamento remoto dello Step 0.1.
- Il 2026-09-28 e stato creato il checkpoint verificato `2d783d5` per gli Step 0.2-4.3; la pubblicazione remota viene verificata separatamente.
- Dal 2026-07-28 lo scope e congelato almeno fino al Gate A: sono ammessi soltanto P0/P1, test e correzioni necessarie.
- Il target MVP e limitato a singola azienda, singolo magazzino, pagamenti manuali e documenti simulati.
- Giovanni De Filippo e il responsabile finale delle decisioni su stock, pagamenti e conservazione dello storico.
- Il backlog e classificato in stabilizzazione, MVP e dopo MVP; “dopo MVP” non rende facoltativi i gate di produzione.

## Blocker

- Nessun blocker tecnico aperto nello Step 4.3.
- La validazione con operatore reale resta `PENDING_OPERATOR_EXECUTION` e non viene dichiarata eseguita.
- I 61 file autorizzati dello Step 4.3 sono sincronizzati nel repository reale; la verifica byte per byte e le suite sul repository reale chiudono lo step.
- I 79 file autorizzati dello Step 4.2 sono sincronizzati nel repository reale e verificati byte per byte; nessun file estraneo all'inventario e stato sovrascritto.
- I 12 file autorizzati dello Step 3.10 sono sincronizzati nel repository reale e verificati byte per byte; nessun file estraneo all'inventario e stato sovrascritto.
- I 63 file autorizzati dello Step 3.9 restano sincronizzati nel repository reale e verificati byte per byte; nessun file estraneo all'inventario e stato sovrascritto.
- Il checkpoint applicativo `2d783d5` e pubblicato; il consolidamento e chiuso dai controlli remoti CI e sicurezza su `a3407d3`, senza riusare esiti precedenti falliti o annullati.
- Nessun blocker tecnico residuo del consolidamento; la PR resta in draft, senza merge. L'aggiornamento documentale finale deve mantenere i propri check verdi prima di qualunque integrazione.
- F-20A e chiuso nel repository reale con ricezione merce, costo documentato e KPI che distinguono copertura nota e quota non valorizzata.

## Verifiche Step 4.3

- Ogni sessione salva lo snapshot teorico per prodotto, il conteggio dell'operatore, la differenza e la motivazione senza modificare lo stock durante il rilevamento.
- Invio e approvazione sono separati: il responsabile deve essere distinto dall'operatore che ha eseguito il conteggio e deve possedere `APPROVE_INVENTORY_COUNTS`.
- Carichi, scarichi e vendite avvenuti durante il conteggio non vengono persi: in approvazione la differenza osservata viene applicata alla giacenza corrente sotto lock.
- La rettifica non puo portare la giacenza sotto la quantita riservata; doppia approvazione, sessione attiva duplicata e codici prodotto ambigui sono rifiutati.
- Ogni approvazione genera un solo movimento ledger collegato alla sessione e conserva attori, timestamp, conteggio, differenza e causale per audit e report.
- La rettifica diretta e stata rimossa dalla UI e rifiutata dall'API con un errore operativo gestito.
- Backend completo: 295/295 test; frontend completo: 202/202; typecheck applicativo/E2E e build Vite completati.
- PostgreSQL `fast`: 105/105 test su PostgreSQL 16, migrazioni V1-V35 e upgrade popolati V14, V16 e V18 completati.
- WB-007 e `IMPLEMENTED`; la prova guidata con operatore di magazzino e responsabile resta `PENDING_OPERATOR_EXECUTION`.
- I 61 file autorizzati coincidono byte per byte tra staging e repository reale; nessun file estraneo all'inventario e stato sovrascritto.

## Verifiche Step 4.2

- La ricezione fornitore applica ordine, riga ricevuta, carico fisico, movimento autorevole e valorizzazione nella stessa transazione.
- Costo concordato e costo effettivo restano distinti; scostamento, totale, stato posting e movimento collegato sono snapshot verificabili.
- Ultimo costo e media ponderata mobile usano quattro decimali `HALF_UP`; KPI aggregati e valori monetari usano due decimali.
- La quantita valorizzata non supera la giacenza fisica; stock storico, carichi manuali e resi privi di costo documentato non ricevono valori inventati.
- Due ricezioni concorrenti da ordini diversi sullo stesso prodotto producono due movimenti, una sola baseline e una media ponderata deterministica.
- Dashboard, catalogo e report dello staff espongono valore noto a costo, copertura e margine potenziale sulla sola quota valorizzata; il cliente non riceve informazioni di costo.
- Backend completo: 288/288 test; frontend completo: 202/202 in 41 file; typecheck applicativo/E2E e build Vite completati.
- PostgreSQL `fast`: 99/99 test, migrazioni V1-V34 e upgrade popolati V14, V16 e V18 completati senza inferire costi storici.
- WB-006 e `IMPLEMENTED`; la prova guidata con un operatore acquisti o magazzino resta `PENDING_OPERATOR_EXECUTION`.
- I 79 file autorizzati coincidono byte per byte tra staging e repository reale; lo Step 4.3 non e stato iniziato.

## Verifiche Step 4.1

- Il modulo `purchase` gestisce ordini fornitore in stato `DRAFT`, `SENT`, `PARTIALLY_RECEIVED`, `RECEIVED` e `CANCELED` con codice business stabile, optimistic version e lock pessimista sulle transizioni concorrenti.
- Fornitore e prodotti sono collegati tramite ID stabili; codice, nome, prezzo concordato, quantita e date previste restano snapshot storici dell'ordine.
- Creazione, invio, ricezione amministrativa e annullo sono autorizzati dal backend, idempotenti per intento e auditati. Le ricezioni cumulative non possono superare l'ordinato e l'annullo preserva quanto gia ricevuto.
- Il catalogo impedisce rinomina ed eliminazione di prodotti referenziati da ordini fornitore, preservando lo storico di approvvigionamento.
- Lo Step 4.1 non modifica giacenza fisica, ledger inventariale o costo: il posting della ricezione merce appartiene esclusivamente allo Step 4.2.
- Frontend: pagina acquisti paginata, filtri, bozza multi-riga, dettaglio, residui, ricezioni e capability server-side; gli errori preservano la bozza e il confine amministrativo della ricezione e dichiarato nella UI.
- Backend completo: 285/285 test passati con Java 23 sulla baseline Java 17; 33 migrazioni Flyway validate e applicate su schema H2 pulito.
- PostgreSQL `fast`: 98/98 test passati su PostgreSQL 16, inclusa la concorrenza sull'ultima quantita residua; upgrade popolati V14, V16 e V18 fino a V33 completati.
- Frontend completo: 200/200 test in 40 file; typecheck applicativo, typecheck E2E e build Vite di produzione completati.
- WB-005 e `IMPLEMENTED`; la prova guidata con un operatore acquisti resta `PENDING_OPERATOR_EXECUTION`.

## Verifiche Step 3.10

- Sette scenari verticali coprono creazione dipendente, vendita assistita, ciclo stock-ordine-riserva-evasione, acconto-annullamento-storno, reso multi-riga-ricezione-rimborso, scadenza sessione durante una query e autorizzazione negativa cliente.
- `scripts/e2e/run-mvp-gate.sh` compone il gate prod-like e il gate PostgreSQL `fast` in un solo ingresso operativo con arresto immediato sul primo errore.
- Playwright/axe: 19/19 test passati sullo stack reale; i sette workflow MVP sono eseguiti in Chromium e le verifiche di autenticazione e accessibilita coprono anche Firefox e WebKit.
- Il rinnovo della sessione non reinizializza piu il workspace e non cancella il feedback operativo; il flusso di retry dopo una query scaduta e verificato end-to-end.
- Il token colore di errore e stato corretto per raggiungere il contrasto WCAG AA senza alterare la gerarchia visiva.
- Backend completo: 277/277 test; frontend completo: 198/198 in 39 file; typecheck applicativo/E2E, build Vite e audit npm con zero vulnerabilita passati.
- PostgreSQL `fast`: migrazioni V1-V32, concorrenza e upgrade popolati V14, V16 e V18 completati preservando ordini e prodotti.
- Gate prod-like: hardening, CSP, cache, metriche, rate limiting, backup/restore e least privilege PostgreSQL verificati senza risorse residue.
- F-31 e Gate C risultano chiusi sul piano tecnico nel repository reale.
- La prova guidata con un operatore reale resta `PENDING_OPERATOR_EXECUTION` e non viene confusa con il gate automatico.

## Verifiche Step 3.9

- Tutte le tabelle applicative espongono `caption`, `thead`, intestazioni di colonna e intestazioni di riga coerenti; form, controlli e azioni hanno nomi accessibili verificati sull'albero semantico.
- `AccessibleDialog` applica focus trap, gestione `Escape`, focus iniziale e ripristino del controllo di origine senza propagare comandi al workspace sottostante.
- Menu orizzontali e schede sono navigabili da tastiera; il titolo `Nuovo ordine` e coerente per il cliente in menu, scheda, header e regione accessibile.
- Le bozze vengono conservate in `sessionStorage` per vista ed entita, senza password, e protette da dirty guard su cambio vista, chiusura scheda, logout e uscita browser.
- Contrasto, indicatori di focus, target touch da almeno 44 px, layout a 390/768/1440 px e `prefers-reduced-motion` sono coperti dalla matrice browser.
- Il download parser gestisce `filename*`, valori quotati o semplici, separatori e nomi non sicuri; la revoca URL differita e verificata anche su WebKit.
- Frontend completo: 198/198 test in 39 file, typecheck applicativo/E2E e build Vite passati; `npm audit` riporta 0 vulnerabilita.
- Playwright/axe: 13/13 test passati su Chromium 1440, Chromium 390, Chromium 768, Firefox 1440 e WebKit 1440, senza violazioni axe critiche o serie.
- Backend invariato: 274/274 test passati fuori dalla sandbox confinata; 32 migrazioni Flyway validate.
- Gate prod-like completo: hardening, CSP, cache, metriche, rate limiting, backup/restore e least privilege PostgreSQL verificati senza risorse residue.
- F-26 e F-34 risultano chiusi nel repository reale; lo Step 3.10 non e stato iniziato.

## Verifiche Step 3.8

- Il builder resi consente di selezionare piu righe dello stesso ordine e invia un solo comando con quantita positive; righe a zero non entrano nel payload.
- Ogni riga ordine espone quantita ordinata, quantita gia restituita o impegnata da resi attivi e quantita residua restituibile calcolate dal backend.
- Due richieste concorrenti sulla stessa quantita residua vengono serializzate dal lock dell'ordine: una sola richiesta puo riuscire e il database conserva un solo reso.
- Note di revisione, selezione del reso da rimborsare e dettaglio ledger usano l'ID stabile del reso; il codice descrittivo resta solo snapshot e fallback per transazioni storiche prive di ID.
- Ogni reso espone esclusivamente le proprie transazioni `REFUND`, con codice, importo, causale, riferimento, operatore e timestamp.
- Gli esiti comando sono tipizzati come `success`, `warning` o `error`; `warning` distingue un comando persistito con refresh fallito da un esito non salvato.
- Form di richiesta, revisione e rimborso vengono azzerati soltanto quando l'esito conferma la persistenza; errore e replay ambiguo conservano la bozza.
- Test backend completi: 274/274; test frontend completi: 188/188 in 35 file; build TypeScript/Vite completata.
- Gate PostgreSQL `fast`: 97/97 test taggati, migrazioni V1-V32 e upgrade popolati V14/V16/V18 completati.
- Nessuna migrazione o modifica distruttiva allo schema; il contratto API cambia soltanto in modo additivo.
- La scheda WB-004 e `IMPLEMENTED`; la prova con operatore reale resta pendente.
- F-26 e risolto sul perimetro dello Step 3.8; accessibilita estesa e draft store cross-view restano oggetto dello Step 3.9.

## Verifiche Step 3.7

- Dashboard ordini separata tra bozze, confermati, evasi e annullati; valori distinti tra lordo ordini, incassato, rimborsato e netto incassato.
- Il prezzo di vendita scontato moltiplicato per la giacenza viene denominato soltanto `potentialRetailStockValue` e “valore potenziale di vendita”; non e presentato come valore inventario o valore contabile.
- Tutti i KPI esposti sono definiti in `docs/KPI_DEFINITIONS.md` con fonte, formula, inclusioni, esclusioni e limiti interpretativi.
- La configurazione aziendale espone un fuso IANA validato e l'elenco dei dati emittente mancanti; la generazione documentale e le relative capability restano bloccate finche la configurazione non e completa.
- Gli eventi API generali espongono `OffsetDateTime` in UTC; i documenti conservano il fuso aziendale nello snapshot ed espongono l'offset coerente con l'istante di emissione.
- Anno e progressivo documentale sono determinati dall'istante corrente nel fuso aziendale; il caso `2026-12-31T23:30Z` in `Europe/Rome` produce correttamente esercizio 2027.
- I filtri giorno dei report vengono interpretati nel fuso aziendale e trasformati in intervalli UTC, incluse le giornate di 23 o 25 ore causate dal cambio DST.
- Il disclaimer resta esattamente `DOCUMENTO SIMULATO - NON VALIDO AI FINI FISCALI`; nessuna conformita fiscale viene dichiarata.
- Migrazione Flyway `V32` verificata su schema pulito e su upgrade popolati V14, V16 e V18; i documenti storici privi di evidenza del fuso ricevono conservativamente lo snapshot `UTC`.
- Backend completo: 271/271 test passati fuori dalla sandbox confinata, inclusi tre test Actuator su porte locali effimere.
- Frontend completo: 184/184 test passati in 35 file; build TypeScript/Vite di produzione completata senza errori.
- Patch censita rispetto alla baseline dello Step 3.6: 88 file modificati o aggiunti, nessuna cancellazione e nessun artefatto generato incluso.
- F-27 e chiuso. F-20A resta aperto soltanto per la valutazione a costo prevista dallo Step 4.2; F-29 resta aperto per il provider fiscale e la validazione professionale previsti dallo Step 6.4.

## Verifiche Step 3.6

- I dettagli operativi `GET /api/products/{code}/detail` e `GET /api/orders/{code}/detail` compongono la risorsa selezionata con storico mirato e capability calcolate dal backend.
- Prodotti, ordini, resi e documenti simulati espongono capability esplicite derivate da permessi, ownership, stato e vincoli di dominio; il frontend non ricostruisce piu le azioni da liste paginate.
- Lo storico prodotto usa query dedicate per movimenti e ordini collegati anche quando tali record non appartengono alla pagina globale corrente.
- La capability per creare una nota credito considera tutti i documenti collegati all'ordine, indipendentemente da filtro tipo e pagina attivi.
- La selezione ordine resta disponibile tramite dettaglio dedicato anche quando filtri o paginazione lo escludono dall'elenco visibile.
- La dashboard operativa usa esclusivamente l'aggregazione server-side e non ripiega piu sui primi otto prodotti, ordini o movimenti caricati dal client.
- Backend completo: 265/265 test passati fuori dalla sandbox confinata; la suite include 31 migrazioni Flyway e i tre test Actuator che richiedono porte locali effimere.
- Frontend completo: 183/183 test passati in 35 file; build TypeScript/Vite di produzione completata senza errori.
- Regressioni dedicate verificano storico fuori pagina, capability documento indipendente dai filtri, dettaglio ordine escluso dalla pagina e capability distinte dei resi.
- La patch censita contiene 51 file: 20 sorgenti backend, 2 test backend, 23 file frontend/test e 6 documenti; nessun file generato e incluso.
- Nessuna migrazione Flyway o modifica allo schema dati richiesta.
- F-24 e chiuso per correttezza funzionale; budget e indici su volumi target restano oggetto dello Step 4.6.

## Verifiche Step 3.5.6

- La sesta e ultima slice prevista dallo Step 3.5 estrae account, audit, configurazione aziendale e monitoraggio di sistema da `App.tsx` in un unico dominio Amministrazione coerente.
- `useAdministrationFlow` applica debounce, cancellazione tramite `AbortSignal` e latest-request-wins alle pagine account e audit; monitoraggio e stato sistema vengono caricati in modo lazy e conservati tra le viste.
- Creazione account, abilitazione/disabilitazione, reset password, revoca sessioni e cambio ruolo continuano a usare il `CommandExecutor`, senza retry automatico delle mutazioni e con aggiornamenti locali seguiti da refresh mirato.
- Form account e conferma di ri-autenticazione vengono azzerati soltanto dopo una risposta confermata e restano disponibili in caso di errore.
- Le regole che impediscono auto-disabilitazione, auto-cambio ruolo e operazioni non autorizzate restano applicate prima dell'invio e sono coperte da test.
- `AdministrationExperience` compone account, audit, configurazione aziendale e monitoraggio con loading, empty state, errore, retry, dati precedenti durante il refresh e avvisi di aggiornamento non riuscito.
- `App.tsx` passa da 707 a 477 righe e non possiede piu stato o handler dei domini Account, Audit, Configurazione aziendale e Monitoraggio.
- Frontend completo: 179/179 test passati in 35 file; i 33 test netti aggiunti coprono segnali, permessi, caricamento lazy, cache, `401`, `403`, `500`, offline, risposte fuori ordine, comandi, protezioni account, conservazione dei form e composizione delle pagine.
- Suite mirata: 48/48 test passati nei tre file interessati; typecheck applicativo, typecheck E2E e build Vite di produzione completati senza errori.
- Backend completo: 262/262 test passati fuori dalla sandbox confinata; la suite include tutte le 31 migrazioni Flyway. I tre test Actuator richiedono il bind di una porta locale effimera, non consentito dalla sandbox.
- Nessuna modifica a backend, endpoint, payload, schema dati o migrazioni Flyway.
- F-25 e chiuso sul piano tecnico: tutte le sei vertical slice previste sono estratte e lo Step 3.5 e completato al 100% interno.

## Verifiche Step 3.5.5

- La quinta delle sei slice previste dallo Step 3.5 estrae query documenti, report vendite e report inventario, stato pagina, causale nota credito e comandi da `App.tsx`.
- `useDocumentReportFlow` applica debounce, cancellazione tramite `AbortSignal` e latest-request-wins ai documenti; i report vengono caricati solo alla prima apertura e restano disponibili tornando tra le viste.
- Emissione fattura e nota credito continuano a usare il `CommandExecutor`, applicano la risposta confermata localmente e attivano un refresh mirato senza retry automatico delle mutazioni.
- La causale della nota credito viene conservata quando il comando fallisce; gli export impediscono duplicati dello stesso formato, espongono lo stato occupato e distinguono successo ed errore.
- `DocumentReportExperience` compone loading, errore, retry, dati precedenti durante il refresh e pagine documenti/report senza duplicare stato nel workspace; i retry falliti sono intercettati senza rejection non gestite.
- `App.tsx` passa da 797 a 707 righe e non possiede piu stato o handler del dominio Documenti/Report.
- Frontend completo: 146/146 test passati in 33 file; i 19 test netti aggiunti coprono permessi, segnali, caricamento lazy, cache, `401`, `403`, `500`, offline, risposte fuori ordine, comandi, conservazione causale, export e composizione delle pagine.
- Suite mirata: 35/35 test passati nei quattro file interessati; typecheck applicativo, typecheck E2E e build Vite di produzione completati senza errori.
- Backend completo: 262/262 test passati; la verifica include tutte le 31 migrazioni Flyway.
- Nessuna modifica a backend, endpoint, payload, schema dati o migrazioni Flyway.
- F-25 resta aperto: lo Step 3.5 e completato all'83.3% interno e l'accettazione finale verra valutata soltanto dopo la slice account/audit.

## Verifiche Step 3.5.4

- La quarta delle sei slice previste dallo Step 3.5 estrae query, pagina, form, modifica e comandi partner da `App.tsx`.
- `usePartnerFlow` applica debounce, cancellazione con `AbortSignal` e latest-request-wins alla pagina anagrafiche e al lookup degli account cliente; `fetchAccounts` propaga il segnale su tutte le pagine richieste.
- Creazione, modifica, disattivazione e collegamento/scollegamento account mantengono il `CommandExecutor`, gli aggiornamenti locali e il refresh mirato senza retry automatico delle mutazioni.
- Il form viene azzerato soltanto dopo una risposta confermata e resta disponibile in caso di errore; una disattivazione confermata rimuove l'anagrafica dai risultati attivi.
- Il lookup account e caricato soltanto con `MANAGE_ACCOUNTS` e viene aggiornato tramite callback esplicita quando il dominio Account cambia ruolo o stato di un cliente.
- `PartnerExperience` compone loading, errore, retry, elenco e form senza duplicare stato nel workspace.
- `App.tsx` passa da 921 a 797 righe e non possiede piu stato o handler del dominio Partner.
- Frontend completo: 127/127 test passati in 31 file; aggiunti 14 test su permessi, segnali, `401`, `403`, `500`, offline, risposte fuori ordine, form, comandi, disattivazione e composizione della pagina.
- Typecheck e build Vite di produzione completati senza errori.
- Backend completo: 262/262 test passati fuori sandbox; la verifica include tutte le 31 migrazioni Flyway, ownership partner/account e architettura modulare.
- Nessuna modifica a backend, endpoint, payload, schema dati o migrazioni Flyway.
- F-25 resta aperto: lo Step 3.5 e completato al 66.7% interno e l'accettazione finale verra valutata soltanto dopo tutte le sei slice.

## Verifiche Step 3.5.3

- La terza delle sei slice previste dallo Step 3.5 estrae query ordini e clienti vendita, riconciliazione finanziaria, bozza/carrello e comandi di ordine, pagamento e reso da `App.tsx`.
- `useOrderFlow` applica debounce, cancellazione con `AbortSignal` e latest-request-wins alle query; i client ordini, anagrafiche vendita e riconciliazione propagano il segnale fino al trasporto HTTP.
- Creazione bozza, conferma, evasione, annullamento, ricevuta, richiesta/approvazione/rifiuto/ricezione reso e rimborso conservano il `CommandExecutor`, gli aggiornamenti locali e i refresh mirati senza retry automatico delle mutazioni.
- La bozza viene svuotata soltanto dopo una creazione confermata e resta disponibile in caso di errore; permessi e ownership cliente continuano a limitare le azioni esposte.
- `OrderExperience` compone vendita assistita, vendita cliente e gestione ordini; catalogo, inventario, dashboard e documenti restano dipendenze esplicite tramite dati e callback.
- `App.tsx` passa da 1.241 a 921 righe e non possiede piu stato o handler del dominio ordini/pagamenti/resi.
- Frontend completo: 113/113 test passati in 29 file; aggiunti 17 test su ruoli, `401`, `403`, `500`, offline, richieste fuori ordine, conservazione bozza, comandi, capability e composizione della pagina.
- Typecheck e build Vite di produzione completati senza errori.
- Backend completo: 262/262 test passati fuori sandbox; la prima esecuzione confinata aveva bloccato soltanto il bind delle porte effimere del test Actuator con `Operation not permitted`.
- Nessuna modifica a backend, endpoint, payload, schema dati o migrazioni Flyway.
- F-25 resta aperto: lo Step 3.5 e completato al 50% interno e l'accettazione finale verra valutata soltanto dopo tutte le sei slice.

## Verifiche Step 3.5.2

- La seconda delle sei slice previste dallo Step 3.5 estrae query, pagine staff/cliente, lookup, selezione, focus, modifica e comandi prodotto da `App.tsx`.
- `useCatalogFlow` usa query debounced e risorse annullabili con semantica latest-request-wins; il client prodotti propaga `AbortSignal` fino al trasporto HTTP.
- `CatalogExperience` compone loading, errore, retry, catalogo cliente, catalogo operativo, dettaglio e form, mantenendo magazzino e ordini come callback/dati espliciti.
- Creazione, modifica, eliminazione singola/multipla e disattivazione conservano la pipeline comandi esistente, gli aggiornamenti locali e il refresh mirato senza retry automatico delle mutazioni.
- `App.tsx` passa da 1.421 a 1.241 righe e non possiede piu stato o handler CRUD del catalogo.
- Frontend completo: 96/96 test passati in 27 file; aggiunti 11 test su staff/cliente, `401`, `403`, `500`, offline, risposta fuori ordine, creazione, fallimento, eliminazione e composizione della pagina.
- Typecheck e build Vite di produzione completati senza errori.
- Backend completo: 262/262 test passati; la verifica di non regressione include tutte le 31 migrazioni Flyway e il test di architettura modulare.
- Nessuna modifica a backend, endpoint, payload, schema dati o migrazioni Flyway.
- F-25 resta aperto: lo Step 3.5 e completato al 33.3% interno e l'accettazione finale verra valutata soltanto dopo tutte le sei slice.

## Verifiche Step 3.5.1

- La prima delle sei slice previste dallo Step 3.5 estrae autenticazione, registrazione, rinnovo, logout, cambio password e relativi form da `App.tsx`.
- `useSessionFlow` possiede stato form e command handler della sessione; `SessionEntry` e `SessionOverlays` compongono pagina di accesso e modali senza duplicare la logica nel workspace.
- I doppi submit concorrenti del login vengono bloccati prima della richiesta; logout remoto fallito pulisce comunque token e dati locali.
- Il rinnovo conserva il fallback controllato al login quando il token e gia scaduto, senza introdurre retry automatici sulle mutazioni applicative.
- Il cambio password chiude la sessione, azzera lo stato delle altre slice tramite callback esplicita e conserva il messaggio necessario al nuovo accesso.
- `App.tsx` passa da 1.531 a 1.421 righe; mantiene temporaneamente il reset aggregato delle altre slice, che saranno estratte una per volta nei cinque sotto-step successivi.
- Frontend completo: 85/85 test passati in 25 file; aggiunti 7 test su login, registrazione, errore rete, doppio invio, rinnovo, cambio password, logout e cablaggio pagina/modali.
- Typecheck e build Vite di produzione completati senza errori.
- Backend completo: 262/262 test passati; la verifica di non regressione include tutte le 31 migrazioni Flyway e il test di architettura modulare.
- Nessuna modifica a backend, API, schema dati o migrazioni Flyway.
- F-25 resta aperto: lo Step 3.5 e completato al 16.7% interno e l'accettazione finale verra valutata soltanto dopo tutte le sei slice.

## Verifiche Step 3.4

- `SessionProvider` e l'unico proprietario frontend di identita autenticata, token, scadenza, avviso e invalidazione centralizzata della sessione.
- Un `401` ricevuto con token attivo notifica il provider e apre il rinnovo; i `401` pubblici del login restano errori di autenticazione ordinari.
- Il pilot Account usa `usePaginatedResource` con stati `loading`, `refreshing`, `error` e `data`, refresh esplicito e aggiornamento locale della risposta alle mutazioni.
- Filtri account sottoposti a debounce di 300 ms; ogni nuova query annulla la precedente tramite `AbortController` e una sequenza monotona garantisce che soltanto l'ultima risposta possa aggiornare lo stato.
- Errori `401`, `403`, `500`, offline e risposte fuori ordine sono coperti da test; le promise delle query vengono gestite internamente senza rejection non intercettate.
- Le mutazioni non applicano retry automatici ambigui e continuano a usare l'esecutore dei comandi e le chiavi di idempotenza introdotti negli step precedenti.
- Suite backend completa eseguita fuori sandbox: 262/262 test passati, inclusi i tre test Actuator su porte locali effimere.
- Frontend: 78/78 test passati in 23 file con il comando standard `npm test`; typecheck e build Vite di produzione completati.
- Nessuna modifica a backend, API, schema dati o migrazioni Flyway.
- F-23 e chiuso sul piano tecnico per il dominio pilota Account. L'estensione alle altre vertical slice avverra durante la decomposizione progressiva, senza anticipare lo Step 3.5.

## Verifiche Step 3.3

- Il login accetta soltanto username e password: il ruolo viene derivato lato server dall'account autenticato e un eventuale campo legacy `role` non puo elevare i privilegi.
- Il lifecycle account e non distruttivo: disabilitazione e riabilitazione preservano storico, ownership e audit; username e identita canonica restano riservati e non possono essere ricreati dopo la disabilitazione.
- Un account disabilitato non puo autenticarsi e le sessioni gia emesse vengono invalidate tramite incremento della versione credenziali e revoca server-side.
- Il cambio password personale richiede la password corrente; reset amministrativo, cambio ruolo, disabilitazione, riabilitazione e revoca globale delle sessioni applicano autorizzazioni backend, ri-autenticazione e audit.
- Un amministratore gestisce dipendenti e clienti; soltanto il super admin gestisce account amministrativi. L'auto-disabilitazione e la gestione del super admin sono bloccate.
- La migrazione Flyway `V31__account_lifecycle.sql` introduce stato, autore, motivo e timestamp di disabilitazione, con vincolo di coerenza e indice operativo; gli account storici restano abilitati senza perdita di dati.
- Il lockout login resta temporaneo, atomico e protetto da lock pessimista con retry limitato; lo stato account permanente non viene confuso con il blocco anti-abuso.
- Suite backend standard eseguita nel repository reale: 262/262 test passati, inclusi i tre test Actuator su porte locali effimere.
- PostgreSQL 16 eseguito nel repository reale: 95/95 test passati con tutte le migrazioni V1-V31; upgrade popolati V14, V16 e V18 completati fino a V31 con checksum e assertion lifecycle verdi.
- Frontend eseguito nel repository reale: 64/64 test passati in 20 file; typecheck e build Vite di produzione completati.
- WB-003 e `IMPLEMENTED`; la prova guidata con operatore reale resta `PENDING_OPERATOR_EXECUTION`. Il recupero pubblico password resta intenzionalmente rinviato finche non esiste un canale verificato.
- F-31A e chiuso sul piano tecnico. Lo Step 3.4 resta intenzionalmente non iniziato.

## Verifiche Step 3.2

- `CUSTOMER` usa `/api/customer/catalog`: la risposta espone soltanto dati commerciali e i livelli `AVAILABLE`, `LIMITED` e `UNAVAILABLE`; ID, stock fisico, riserve, disponibilita numerica, stato interno e valore potenziale di vendita sono assenti dal JSON.
- I prodotti disattivati non compaiono nella projection cliente. L'ordinamento per quantita non e disponibile e il carrello non riceve un massimo numerico ricavato dallo stock.
- `/api/customer/dashboard` usa l'ID account stabile della sessione e restituisce conteggi per stato e ordini recenti propri, senza KPI globali, ricavi, catalogo o movimenti di magazzino.
- Gli endpoint operativi `/api/products` e `/api/dashboard` restituiscono `403` a `CUSTOMER`; personale e cliente non possono scambiarsi le projection dedicate.
- Suite backend completa fuori sandbox: 258/258 test passati, inclusi i tre test Actuator su porte locali effimere e i contract test per forma risposta, ownership e matrice ruoli.
- Frontend: 61/61 test passati in 18 file; typecheck e build Vite di produzione completati.
- Nessuna migrazione Flyway necessaria: lo step separa autorizzazioni, query, DTO, client e presentazione senza cambiare lo schema dati.
- WB-002 e marcato `IMPLEMENTED`; la prova guidata con un operatore reale resta `PENDING_OPERATOR_EXECUTION` e non viene dichiarata completata.
- F-14 e chiuso sul piano tecnico. Lo Step 3.3 resta intenzionalmente non iniziato.

## Verifiche Step 3.1

- `Gestione catalogo` e `Vendita` sono flussi separati: il catalogo mantiene le operazioni anagrafiche, mentre la vendita espone ricerca prodotti, carrello, destinatario e creazione della sola bozza.
- Il personale seleziona un cliente censito tramite ID stabile del `BusinessPartner` oppure dichiara esplicitamente un cliente occasionale; non vengono create anagrafiche fittizie.
- Un account `CUSTOMER` puo creare esclusivamente ordini self-service associati alla propria identita stabile e non puo indicare o impersonare un altro cliente.
- Prezzo scontato e descrizione prodotto vengono congelati nella riga ordine; le modifiche successive del catalogo non alterano lo storico della bozza.
- La creazione assistita produce audit `CREATE_ASSISTED_ORDER` con operatore autenticato; conferma, riserva stock, evasione e pagamento restano operazioni indipendenti.
- Errori di validazione, stock insufficiente, rete o server non svuotano il carrello; soltanto la risposta confermata azzera la bozza locale. L'idempotency key protegge retry e doppio invio concorrente.
- Migrazione Flyway `V30__assisted_sales_customer_and_line_snapshots.sql`: tipo cliente esplicito, snapshot descrizione riga, vincoli e indice. I record storici restano `LEGACY_UNRESOLVED` senza inferenze non verificabili.
- Suite backend completa fuori sandbox: 251/251 test passati, inclusi 4 test del workflow assistito, 11 test di idempotenza e 3 test Actuator su porte locali effimere.
- PostgreSQL reale: checksum fixture verdi; snapshot V14/V16/V18 e upgrade popolati V14, V16 e V18 completati fino a V30.
- Frontend: 56/56 test passati in 16 file; typecheck e build Vite di produzione completati.
- F-15 e chiuso sul piano tecnico. La prova guidata con un operatore reale resta un'evidenza operativa separata e deve produrre l'esito `OPERATOR_VALIDATED` oppure `REJECTED`; non viene sostituita dal Gate C automatico.

## Verifiche Step 3.0

- `WORKFLOW_BENCHMARKS.md` introduce il template obbligatorio in sei parti, gli esiti decisionali e il divieto esplicito di copiare prodotti terzi.
- WB-001 confronta fonti ufficiali Odoo, ERPNext e Business Central e traduce le osservazioni negli invarianti gia presenti nel monolite.
- La vendita assistita futura mantiene cliente identificato stabilmente, cliente occasionale esplicito, bozza separata da conferma/evasione/pagamento, stock autorevole, audit e idempotenza.
- Il protocollo operatore definisce dati sintetici, sette scenari, criteri di esito ed evidenze; nello Step 3.0 era predisposto per la successiva implementazione e ora resta correttamente marcato `PENDING_OPERATOR_EXECUTION`.
- Tre test contrattuali statici proteggono template, fonti, invarianti, anti-copia e registro delle schede future.
- Suite backend completa fuori sandbox: 246/246 test passati, inclusi i tre test Actuator su porte locali effimere. Il primo tentativo confinato aveva prodotto soltanto tre `SocketException: Operation not permitted` ambientali.
- Frontend invariato verificato con installazione pulita: 52/52 test, typecheck applicativo, typecheck E2E e build Vite di produzione completati.
- Nessun codice applicativo, contratto API o migrazione Flyway e stato modificato.

## Verifiche Step 2.9

- Il profilo Maven `postgresql-it` usa Testcontainers con PostgreSQL 16, applica V1-V29 e mantiene Hibernate in `validate`; 95/95 test critici sono passati sul database reale.
- La matrice PostgreSQL copre sessioni, lockout concorrente, identita canoniche, idempotenza, stock, ledger, numerazioni, annullamenti, pagamenti, riconciliazione e autorizzazioni negative.
- I tentativi login concorrenti usano lock pessimista, retry limitato e timestamp monotoni; richieste completate fuori ordine non possono violare la cronologia del blocco.
- Gli upgrade popolati V14, V16 e V18 arrivano a V29 dopo una riconciliazione canonica esplicita e verificano storico ordini, ledger, idempotenza, annullamenti e mismatch finanziari intenzionali.
- La migrazione V29 ripara in modo conservativo i claim legacy `IN_PROGRESS` privi di token o lease e introduce il vincolo che ne impedisce la ricomparsa.
- Il job CI `Database migrations` esegue il profilo `fast` su push e pull request e il profilo `nightly` settimanale con fixture volumetrica `large`.
- Suite backend completa fuori sandbox: 243/243 test passati, inclusi i test Actuator su porte locali effimere.
- Frontend: 52/52 test passati in 15 file; typecheck e build Vite di produzione completati.
- Least privilege PostgreSQL verificato dopo V29: runtime DML senza DDL, backup read-only, migrator owner-scoped, restore dedicato e rotazione credenziali.
- F-21 e F-28 erano chiusi al termine dello Step 2.9; in quel momento F-31 restava aperto fino al gate E2E MVP dello Step 3.10.

## Verifiche Step 2.8

- PostgreSQL usa cinque identita distinte: owner `NOLOGIN`, migrator con `SET ROLE` controllato, runtime DML-only, backup read-only e restore dedicato con `CREATEDB` limitato al recovery operativo.
- L'utente runtime non puo eseguire `CREATE`, `ALTER` o `DROP`; il test applicativo di riconciliazione finanziaria usa la stessa identita runtime dopo l'applicazione di V1-V27 tramite migrator e owner.
- Backup e restore non riusano le credenziali applicative. La prova di restore ricrea un database isolato, verifica i dati e completa il cleanup.
- La rotazione aggiorna separatamente le credenziali dei ruoli e dimostra che la password precedente viene rifiutata.
- La porta PostgreSQL pubblicata dai profili locali e prod-like e vincolata al loopback; la documentazione prescrive di rimuoverla nel deployment reale.
- Suite backend sequenziale fuori sandbox: 239/239 test passati, inclusi i test Actuator su porte locali effimere.
- Frontend: 52/52 test passati in 15 file; typecheck e build Vite di produzione completati.
- Stack prod-like completo: 27 migrazioni Flyway validate, 3/3 smoke Playwright, rate limiting login e registrazione, hardening container, CSP, metriche, backup/restore e least privilege verificati.
- Il runner prod-like ha completato il cleanup delle risorse isolate e ha terminato con exit code 0.
- F-12 e chiuso. Lo Step 2.9 resta intenzionalmente non iniziato.

## Verifiche Step 2.7

- `payment_transactions` e la fonte finanziaria autorevole per incassi, rimborsi, reversal e riconciliazioni; le proiezioni di pagamento e reso vengono ricalcolate dai movimenti.
- Ogni rimborso e collegato al reso tramite foreign key; codice reso e metadati restano snapshot descrittivi e non sostituiscono l'identita relazionale.
- La migrazione `V27` crea un solo movimento `RECONCILIATION` per i pagamenti storici gia verificati con importo positivo, senza cambiare o inventare importi.
- Vincoli database impediscono collegamenti incompatibili, rimborsi senza reso, riconciliazioni duplicate e cronologie di pagamento o reso impossibili.
- `GET /api/financial-reconciliation` e il job programmato rilevano drift di importi, stati, collegamenti e timestamp senza correggere silenziosamente il ledger.
- Metriche Prometheus a cardinalita finita espongono anomalie correnti, totale rilevato, ultimo successo ed esito delle esecuzioni.
- La UI mostra il report agli utenti con `VIEW_REPORTS` e distingue chiaramente stato coerente e anomalie operative.
- Test backend sequenziale fuori sandbox: 238/238 passati, incluse tre prove Actuator su porte locali effimere.
- PostgreSQL 16.14: 22/22 test passati su V1-V27, migrazione, vincoli negativi, rollback e rimborsi concorrenti; container effimero rimosso.
- Frontend: 52/52 test in 15 file, typecheck applicativo ed E2E e build Vite di produzione completati.
- F-22 e chiuso. Le evidenze storiche mai registrate restano intenzionalmente soggette a riconciliazione umana; il sistema non formula conclusioni finanziarie false.

## Verifiche Step 2.6

- `POST /api/orders/{code}/cancel` accetta causale obbligatoria e riferimento contabile obbligatorio quando esiste un incasso netto.
- L'annullamento di un ordine incassato registra una sola transazione immutabile `REVERSAL` per l'intero netto, porta il pagamento a `REFUNDED`, azzera il netto e collega lo storno all'ordine.
- Bozze e ordini confermati senza incassi restano annullabili; il pagamento pendente passa a `CANCELED` e le riserve dell'ordine confermato vengono liberate.
- Stato ordine, pagamento, reversal, scorte e audit sono modificati nella stessa transazione; un errore durante il rilascio di una riga ripristina anche gli aggiornamenti gia eseguiti.
- Lock pessimista sull'ordine, vincolo univoco sul reversal e `Idempotency-Key` impediscono doppio annullo in concorrenza o dopo risposta persa.
- La migrazione `V26` conserva gli annullamenti storici con metadati espliciti e impedisce reversal senza ordine/riferimento, piu reversal per ordine e metadati di annullamento su ordini attivi.
- La UI operativa mostra importo da stornare, riferimento, causale, attore e storico; il comando usa la response autorevole senza richiedere conferme generiche.
- Suite backend completa: 232 test passati fuori dalla sandbox, inclusi i test Actuator su porte locali effimere.
- PostgreSQL 16.14: 18 test mirati passati sulle 26 migrazioni, inclusi acconto, saldo, rollback, due operatori concorrenti e replay idempotente; container effimero rimosso.
- Frontend: 51 test in 14 file, typecheck applicativo e build Vite di produzione completati.
- F-08 e chiuso. F-22 resta aperto fino alla riconciliazione finanziaria dello Step 2.7.

## Verifiche Step 2.5

- Il nuovo hook `useCommandExecution` separa gli esiti `saved`, `saved-refresh-failed`, `failed` e `duplicate`, evitando di confondere il risultato del comando con quello del refresh successivo.
- La risposta autorevole della mutazione viene applicata immediatamente allo stato locale; il refresh successivo interessa soltanto pagine, lookup e aggregazioni dipendenti dall'entita modificata.
- Se il comando e confermato ma la presentazione locale o il refresh falliscono, l'interfaccia comunica esplicitamente che l'operazione e stata salvata e invita ad aggiornare i dati senza ripeterla.
- Form prodotto, dati account, movimenti e carrello vengono azzerati soltanto dopo una risposta confermata; timeout ed errori di rete conservano i dati inseriti per un retry sicuro.
- Chiavi attive per operazione e disabilitazione dei controlli di mutazione impediscono doppi invii concorrenti senza bloccare link o funzioni di sola lettura.
- Test frontend dedicati: comando `200` seguito da refresh `500`, timeout del comando, retry dopo timeout e doppio click con una sola richiesta; aggiunte anche regressioni sullo stato paginato e sulla preservazione/reset dei form.
- Suite frontend completa: 50 test passati in 14 file; typecheck applicativo e build Vite di produzione completati.
- Suite backend completa: 224 test passati fuori dalla sandbox, inclusi i tre test Actuator che richiedono porte HTTP locali effimere. Il primo tentativo confinato aveva prodotto soltanto tre `SocketException: Operation not permitted` ambientali.
- Nessuna API, migrazione Flyway o regola di dominio e stata modificata in questo step.
- Il finding F-13 e chiuso; F-30 resta aperto fino allo step dedicato alla gestione trasversale degli errori.

## Verifiche Step 2.4

- La migrazione `V25` trasforma i record idempotenti in claim durevoli con account ID stabile, fingerprint versionato, token di ownership, lease, scadenza e stati `IN_PROGRESS`, `COMPLETED` e `FAILED_RETRYABLE`.
- Il vincolo univoco su account, operazione e chiave assegna atomicamente un solo esecutore; i concorrenti osservano lo stato senza attendere il lock della mutazione.
- Mutazione business e risposta serializzata vengono confermate nella stessa transazione, evitando una modifica senza replay registrato dopo un crash tra i due commit.
- La stessa chiave e lo stesso payload restituiscono la risposta completata; una chiave riusata con payload diverso produce `409 IDEMPOTENCY_CONFLICT`.
- Un claim ancora attivo oltre l'attesa breve produce `409 IDEMPOTENCY_IN_PROGRESS` e `Retry-After`; un claim scaduto o fallito in modo ripetibile puo essere riacquisito in sicurezza.
- Il frontend genera chiavi per intento e conserva la stessa chiave dopo timeout, errore di rete, errore server, risposta persa o claim ancora attivo; successo, errore definitivo o payload cambiato chiudono l'intento.
- Cleanup programmato e TTL frontend eliminano record e intenti scaduti dopo la retention predefinita di 24 ore.
- Test H2 mirati: 10 casi controller e 1 upgrade V24-V25, inclusi doppio click, due thread, timeout, retry dopo errore, replay e payload diverso.
- Test PostgreSQL 16.14: 10 casi passati sulle 25 migrazioni, inclusa la concorrenza reale con una sola mutazione; container effimero rimosso senza volumi persistenti.
- Suite backend completa: 224 test passati con Java 23 e bytecode Java 17, inclusi i test Actuator su porte locali effimere.
- Frontend: 42 test in 12 file, typecheck E2E e build Vite di produzione completati.
- Il finding F-31B e chiuso; F-13 e stato successivamente chiuso dallo Step 2.5 e F-30 resta aperto fino al relativo step di gestione errori.

## Verifiche Step 2.3

- La migrazione `V24` rende i movimenti di magazzino un ledger autorevole collegato al prodotto tramite ID stabile, con delta firmato, origine, snapshot prima/dopo e vincoli di coerenza.
- I movimenti precedenti a `V24` restano osservazioni legacy non autorevoli; ogni prodotto storico riceve una baseline di migrazione esplicitamente non verificata, senza inventare evidenze operative.
- L'API anagrafica prodotto non accetta piu la quantita: ogni nuovo prodotto nasce a giacenza zero e lo stock varia solo tramite saldo iniziale, rettifica, carico/scarico, evasione o reso.
- Il saldo iniziale e unico per prodotto; rettifiche e movimenti richiedono causale, rispettano lo stock riservato e scrivono ledger e proiezione nella stessa transazione.
- Evasioni e resi registrano movimenti con origine esplicita; cancellare un prodotto con giacenza, riserve o riferimenti ordine viene rifiutato.
- Il report di riconciliazione classifica `BALANCED`, `MISSING_INITIAL_BALANCE`, `UNVERIFIED_INITIAL_BALANCE`, `CHAIN_BROKEN` e `LEDGER_DRIFT` senza modificare automaticamente lo storico.
- Test dedicati H2: saldo iniziale, catena e somma del ledger, drift, baseline migrata, rettifiche sotto riserva, concorrenza, endpoint protetti e payload prodotto incapace di variare la quantita.
- Test dedicati PostgreSQL 16.14: 9 passati su 24 migrazioni, baseline storica, vincoli, saldo iniziale, rettifiche concorrenti e protezione delle riserve; container effimero rimosso.
- Suite backend completa: 219 test passati con Java 23 e bytecode Java 17, inclusi i test Actuator su porte locali effimere.
- Frontend: 37 test in 12 file e build Vite di produzione completati; form prodotto e pagina inventario seguono il nuovo contratto.
- Il finding F-05 e chiuso; F-21 resta aperto fino alla suite PostgreSQL trasversale obbligatoria dello Step 2.9.
- Rischio residuo: le baseline migrate devono essere riconciliate manualmente contro evidenze reali prima di poter essere considerate verificate.

## Verifiche Step 2.2

- Username, codice prodotto e codice anagrafica conservano un valore visuale separato e usano `lower(trim(valore))` come identita canonica.
- La migrazione `V23` valorizza le colonne canoniche storiche e applica vincoli `NOT NULL`, `CHECK` e `UNIQUE`; eventuali collisioni interrompono l'upgrade senza rinominare o fondere dati automaticamente.
- Il preflight `scripts/db/preflight-canonical-identifiers.sql` individua le collisioni prima dell'upgrade; la procedura manuale e documentata in `docs/CANONICAL_IDENTIFIERS.md`.
- Creazione e modifica non dipendono da pre-check per l'unicita: il database decide anche sotto concorrenza e i vincoli canonici noti vengono tradotti in `409 RESOURCE_CONFLICT` con messaggi di dominio stabili.
- Test dedicati H2: 7 passati su valori visuali, lookup case/space-insensitive, collisioni sequenziali e concorrenti, risposta HTTP 409, upgrade pulito e arresto su storico ambiguo.
- Test dedicati PostgreSQL 16: 5 passati su account, prodotti e anagrafiche, inclusi tre inserimenti concorrenti; tutte le 23 migrazioni applicate e container effimero rimosso.
- Suite backend completa: 211 test passati con Java 23 e bytecode Java 17, inclusi i test Actuator su porte locali effimere.
- Frontend: 34 test in 11 file e build Vite di produzione completati.
- Il finding F-11 e chiuso; F-21 resta aperto fino alla suite PostgreSQL trasversale obbligatoria dello Step 2.9.

## Verifiche Step 2.1

- La nuova migrazione `V22` corregge gli esiti di V15 e V17 senza alterare le migration distribuite: i contatori documentali mancanti o arretrati partono da `max(sequence_number) + 1` per tipo e anno fiscale.
- I pagamenti storici senza transazioni o altra evidenza verificabile passano da un falso `PENDING` a `UNRECONCILED`; importi pagati, rimborsati e residui restano intenzionalmente non dichiarati finche non vengono riconciliati.
- Incassi e report economici non trattano gli ordini `UNRECONCILED` come insoluti; il frontend mostra “Da verificare” e blocca le azioni finanziarie incompatibili.
- La riconciliazione manuale richiede `SUPER_ADMIN`, ri-autenticazione, `Idempotency-Key`, riferimento e causale; produce metadata persistenti e audit critico.
- Preflight PostgreSQL rileva collisioni di codice/tupla prima dell'upgrade; l'allocatore serializzato salta eventuali codici storici gia presenti e il primo documento successivo viene verificato automaticamente.
- Upgrade PostgreSQL 16 verificati: V14-V22 163 ms, V16-V22 147 ms, V18-V22 131 ms; V18-V22 medium con 5.005 ordini 165 ms e large con 50.005 ordini 588 ms.
- Test PostgreSQL applicativi su pagamenti e numerazione concorrente: 12 passati; fixture, preflight, primo documento e cleanup completati senza volumi persistenti.
- Suite backend completa: 204 test passati con Java 23 e bytecode Java 17; JAR Spring Boot generato.
- Frontend: 34 test in 11 file, typecheck applicativo/E2E e build Vite completati.
- I finding F-09 e F-10 sono chiusi; F-21 resta aperto fino alla suite PostgreSQL trasversale obbligatoria dello Step 2.9.

## Verifiche Step 1.6

- Backend aggiornato a Spring Boot `4.1.0`, Spring Security `7.1.0` e Spring Modulith `2.1.0`; il controllo runtime conferma una linea non affetta da CVE-2026-22732.
- Il primo gate OWASP ha bloccato correttamente Tomcat `11.0.22` e pgJDBC `42.7.11`; la baseline finale usa Tomcat `11.0.24`, pgJDBC `42.7.12` e Log4j `2.25.5`.
- OWASP Dependency-Check `12.2.2` ha analizzato 66 dipendenze: 0 dipendenze vulnerabili e 0 vulnerabilita nel report HTML/JSON finale, con soglia fail-closed CVSS `7.0`.
- Nessun profilo applicativo viene attivato implicitamente; il JAR senza profilo e il profilo `prod` senza segreti terminano entrambi in modo fail-closed.
- Il bootstrap del super admin e disabilitato per default e rifiuta le credenziali locali note in ogni profilo senza conservarne la password in chiaro nel codice produttivo.
- H2 e confinato allo scope test; la security chain non contiene bypass `/h2-console` e applica `X-Frame-Options: DENY`.
- Header di sicurezza verificati su risposte 2xx, 4xx e 5xx; i contratti runtime/configurazione aggiungono 7 test dedicati.
- Suite backend completa dopo gli aggiornamenti: 201 test passati con Java 23 e bytecode Java 17; JAR Spring Boot generato.
- Frontend confermato: 33 test in 11 file, typecheck applicativo/E2E e build Vite completati; `npm ci` non ha rilevato vulnerabilita.
- I finding F-04, F-16 e F-33 sono chiusi; tutti i criteri del Gate A risultano soddisfatti.

## Verifiche Step 1.5

- I runner prod-like ed E2E generano project name casuali con prefissi rispettivamente `gestionale-prodlike-` e `gestionale-e2e-` e rifiutano collisioni prima di creare risorse.
- Container, volumi e rete ricevono sia la label Compose del progetto sia il run ID univoco; l'inventario viene salvato nella diagnostica.
- Il cleanup non usa `docker compose down` e rimuove soltanto ID inventariati dopo avere verificato l'ownership di tutte le risorse associate al progetto.
- Se una risorsa del progetto e priva del run ID atteso, il cleanup si arresta senza rimuovere alcuna risorsa.
- I trap gestiscono `HUP`, `INT` e `TERM`; un `SIGKILL` puo lasciare risorse etichettate, recuperabili con `scripts/ci/cleanup-docker-run.sh <project> <run-id>`.
- Test runtime Docker superato con volume sentinella persistente, collisione project name, interruzioni nelle fasi preflight/rete/volume/container e recovery dopo `SIGKILL`.
- Contratti backend Docker e workflow: 9 test passati.
- Suite backend completa: 194 test passati.
- Gate prod-like completo: 21 migrazioni Flyway, hardening, secret, CSP, osservabilita, rate limit login/registrazione, backup/restore e 3 smoke test Playwright completati.
- Cleanup finale verificato: nessun container, volume o rete residuo per il project/run ID della prova.
- Il job prod-like e nuovamente attivo in CI ed e una dipendenza obbligatoria del quality gate.
- Il finding F-03 e chiuso; non restano finding P0 aperti.

## Verifiche Step 1.4

- La migrazione Flyway `V21` aggiunge agli ordini `customer_account_id`, `partner_id` e lo stato esplicito `ACCOUNT`, `PARTNER` o `UNRESOLVED`.
- Nome cliente e codice anagrafica restano snapshot descrittivi e non partecipano piu alle decisioni di autorizzazione.
- Gli ordini storici vengono collegati soltanto quando `customer_code` identifica deterministicamente un'anagrafica; nessun nome o username viene usato per inferire l'ownership.
- Gli ordini storici ambigui restano `UNRESOLVED` e non sono accessibili ai clienti.
- Le anagrafiche cliente possono essere associate esplicitamente a un solo account `CUSTOMER`; il frontend amministrativo usa l'ID account e mostra lo stato del collegamento.
- Creazione, lista, dettaglio, annullamento e richiesta di reso del cliente usano l'ID stabile della sessione; rinominare username o display name non trasferisce ordini.
- Eliminare un account scollega le foreign key tramite `ON DELETE SET NULL`; ricreare lo stesso username non eredita ordini precedenti.
- Test H2 dedicati a migrazione, omonimie, rename, account non collegato, display name duplicati e transizioni: 9 passati.
- Test PostgreSQL 16 dedicati all'ownership e al contratto API: 8 passati tramite runner isolato e cleanup completato.
- Fixture PostgreSQL 16 profilo `small`: snapshot V14, V16, V18, V19, V20 e V21 caricati e validati con checksum corretti.
- Suite backend completa: 188 test passati, inclusi i test Actuator su porte locali effimere.
- Suite frontend completa: 33 test passati; typecheck applicativo/E2E e build Vite completati.
- La prova PostgreSQL ha rilevato una transazione sessione erroneamente read-only durante un lock pessimista; il confine transazionale e stato corretto e coperto da regressione.
- Nessun runner prod-like o Playwright E2E eseguito prima dello Step 1.5.
- Il finding F-02 e chiuso; F-03 resta l'unico P0 aperto.

## Verifiche Step 1.3

- La migrazione Flyway `V20` aggiunge `credential_version` agli account e collega ogni sessione tramite `account_id` con foreign key `ON DELETE CASCADE`.
- Lo username resta uno snapshot diagnostico; autenticazione, conteggi e revoca usano l'ID stabile dell'account.
- Le sessioni legacy vengono revocate intenzionalmente durante la migrazione per evitare che token emessi con il subject precedente sopravvivano al cambio di identita.
- Cambio password e cambio ruolo incrementano la versione credenziali e revocano tutte le sessioni dell'account.
- Eliminare e ricreare lo stesso username non rende nuovamente valido alcun vecchio token.
- Logout, rotazione e aggiornamento dell'ultima attivita acquisiscono un lock pessimista sulla sessione; revoca e rotazione prevalgono su touch concorrenti.
- Test H2 dedicati al subject stabile: 6 passati.
- Test PostgreSQL 16 dedicati alla concorrenza delle sessioni: 6 passati tramite runner isolato, senza porte host o volumi persistenti e con cleanup completato.
- Fixture PostgreSQL 16 profilo `small`: snapshot V14, V16, V18, V19 e V20 caricati e validati con checksum corretti.
- Suite backend completa: 178 test passati con Java 23 e compilazione `release 17`.
- Suite frontend completa: 32 test passati; typecheck applicativo/E2E e build Vite completati.
- Nessun runner prod-like o Playwright E2E eseguito prima dello Step 1.5.
- I finding F-06 e F-07 sono chiusi per il perimetro applicativo dello step; la suite PostgreSQL autorevole estesa resta pianificata nello Step 2.9.

## Verifiche Step 1.2

- La migrazione Flyway `V19` aggiunge provenienza e stato di verifica operativa agli account senza eliminare o declassificare dati storici.
- Le registrazioni self-service `CUSTOMER` sono censite separatamente dagli account `EMPLOYEE` sospetti; account admin, super admin, bootstrap e staff creato dal pannello amministrativo sono preservati.
- Gli account operativi self-service o di provenienza non dimostrabile restano in quarantena fino a revisione manuale; login e sessioni esistenti vengono rifiutati.
- Prima della revoca la migrazione registra un audit critico `QUARANTINE_OPERATIONAL_ACCOUNT`, preservando l'evidenza della decisione.
- Il report super admin espone provenienza, classificazione, sessioni emesse/potenzialmente attive e conteggi audit per magazzino, ordini, pagamenti, resi e documenti.
- La verifica manuale richiede sessione `SUPER_ADMIN` e ri-autenticazione; abilita soltanto i login futuri e non riattiva i token precedentemente revocati.
- Un test inizialmente fallito ha rilevato che l'eccezione `401` annullava la revoca transazionale; il confine transazionale e stato corretto e la revoca ora resta persistente.
- Test mirati finali: 5 test passati su servizio, autorizzazione controller e migrazione incrementale V18-V19.
- Suite backend completa: 171 test passati con Java 23 e compilazione `release 17`.
- Verificatore PostgreSQL 16 con profilo `small`: snapshot V14, V16, V18 e V19 caricati e validati; checksum verificati e container effimero rimosso.
- Nessuna modifica frontend richiesta dal contratto dello step; nessun runner prod-like o E2E eseguito prima dello Step 1.5.
- Il finding F-01 e chiuso. F-07 e stato successivamente chiuso dallo Step 1.3.

## Verifiche Step 1.1

- Contratto pubblico ridotto a `username` e `password`; qualsiasi campo `role` valido viene rifiutato con `422 VALIDATION_FAILED`.
- Il servizio self-service assegna sempre `CUSTOMER` e registra l'audit con attore `SELF_SERVICE`.
- Gli account `EMPLOYEE` e `ADMIN` restano creabili soltanto dall'endpoint amministrativo autenticato e ri-autenticato.
- Il frontend non mostra il selettore ruolo durante la registrazione e accede automaticamente come cliente dopo la creazione.
- Nginx dispone di una zona rate limit dedicata alla registrazione, configurabile separatamente dal login.
- Test di regressione mirati inizialmente falliti in modo deterministico sul comportamento vulnerabile: 5 errori backend su 6 casi e 2 errori frontend su 5 casi.
- Test backend mirati finali: 6 test passati; fixture legacy coinvolte: 37 test passati.
- Suite backend completa: 166 test passati con Java 23 e compilazione `release 17`.
- Suite frontend completa: 32 test passati; typecheck applicativo ed E2E e build Vite completati.
- Sintassi di entrypoint e verificatori shell valida; `git diff --check` completato senza errori.
- Nessuna migrazione Flyway necessaria: lo step non modifica lo schema dati.
- Il test runtime del rate limit e lo smoke Playwright aggiornato sono predisposti ma non eseguiti, per rispettare la stop condition precedente allo Step 1.5.
- Il finding F-01 resta formalmente aperto fino al censimento degli account storici e alla revoca delle sessioni nello Step 1.2.

## Verifiche Step 0.3

- Fixture incrementali e completamente sintetiche create per gli schemi V14, V16 e V18.
- Stati ordine, pagamenti, resi, documenti, collisioni case-insensitive, ownership storica, drift magazzino e riconciliazione finanziaria coperti dal manifest.
- Profili deterministici disponibili: `small` con 20 prodotti, 10 anagrafiche e 40 ordini; `medium` con 500, 250 e 5.000; `large` con 5.000, 2.500 e 50.000.
- Checksum SHA-256 verificati con successo su tutti gli 11 file dichiarati.
- Contratto Java `HistoricalFixtureContractTest`: 5 test passati.
- Verificatore PostgreSQL 16 eseguito con profilo `small`: snapshot V14, V16 e V18 caricati e validati su database puliti.
- Conteggi verificati per ogni snapshot: 25 prodotti e 45 ordini, inclusi i record mirati aggiunti ai volumi del profilo.
- Container PostgreSQL effimero eseguito senza porte host o volumi persistenti e rimosso dopo il controllo della label univoca; nessuna risorsa residua.
- Suite backend completa: 160 test passati con Java 23 e compilazione `release 17`.
- Nessun runner prod-like o E2E eseguito e nessuno stack Docker esistente modificato.
- I criteri tecnici del Gate 0 sono soddisfatti localmente; la pubblicazione Git della patch resta separata e non autorizzata.

## Verifiche Step 0.2

- Freeze e politica delle eccezioni formalizzati in `docs/PRODUCT_SCOPE.md`.
- Target MVP verificato contro le assunzioni vincolanti di `MASTER_SOURCE_AGENTE_AI.md`.
- Responsabilita su stock, pagamenti e storico assegnata esplicitamente.
- Tutti i 46 step residui riclassificati in stabilizzazione, MVP e dopo MVP in `docs/ROADMAP.md`.
- Stato pubblico del progetto corretto da “MVP avanzato” a “prototipo avanzato con P0” nel `README.md`.
- Nessuna modifica a codice, database, API, dipendenze o runtime.
- Nessun runner prod-like/E2E eseguito prima dello Step 1.5.

## Verifiche Step 0.1

- Branch locale verificata: `miglioramenti-gestionale`.
- Scansione Gitleaks cronologia: 3 commit, nessuna rilevazione.
- Scansione Gitleaks baseline candidata: circa 1,69 MB, nessuna rilevazione.
- File sensibili o artefatti locali candidati: nessuno; sono presenti soltanto template `.env.example` e migrazioni Flyway intenzionali.
- Baseline pubblicata su `origin/miglioramenti-gestionale`: commit `9f9e23a`.
- Workflow CI pubblicato tramite sessione GitHub autenticata: commit `945fcd0`.
- Workflow security pubblicato tramite sessione GitHub autenticata: commit `62a8d14`.
- Workflow CI corretto per separare il gate migrazioni e sospendere l'esecuzione prod-like fino allo Step 1.5: commit `fa52524`.
- Alert CodeQL corretti senza dismiss manuali: identificatori casuali generati con CSPRNG e CSRF abilitato per default con esclusioni esplicite per API stateless: commit `8d9be88`.
- I commit locali originali sono preservati nelle branch `baseline-pre-publication-20260727` e `miglioramenti-gestionale-pre-publication`.
- Clone pulito creato dal repository reale e verificato senza file esterni non documentati.
- Backend nel clone pulito: `mvn -B verify`, 154 test passati, JAR generato; dopo l'hardening CodeQL: 155 test passati.
- Frontend nel clone pulito: `npm ci`; 32 test passati; typecheck applicativo ed E2E passati; build Vite passata.
- Legacy nel clone pulito: `mvn -B -f pom-legacy.xml test`, 41 test passati.
- Flyway: 18 migrazioni validate e applicate su schema H2 pulito durante i test.
- Compose locale e prod-like nel clone pulito: configurazioni valide con placeholder non sensibili.
- Stack prod-like ed E2E non avviati per rispettare la stop condition precedente allo Step 1.5.
- Runtime Maven verificato: Java 23.0.1 con compilazione `release 17`; il test della baseline passa anche su questo runtime.
- Artifact CI backend, frontend e diagnostica prod-like includono `github.sha`.
- Branch protection remota attiva su `main`: pull request obbligatoria, una approvazione, dismiss stale approvals, approvazione dell'ultimo push, conversazioni risolte, nessun bypass amministratore, force push e cancellazione disabilitati.
- Pull request draft aperta: `#2`, branch `miglioramenti-gestionale` verso `main`.
- Branch protection verificata con branch aggiornata obbligatoria e check richiesti: `Backend Spring Boot`, `Frontend React`, `Database migrations`, `Quality gate`.
- Run CI remoto finale `30341354456`: repository hygiene, backend, frontend, migrazioni e quality gate completati con successo; prod-like correttamente saltato.
- Run security remoto finale `30341354476`: Gitleaks, dependency review, npm audit, inventario dipendenze backend e CodeQL Java/TypeScript completati con successo.
- Check separato `Code scanning results / CodeQL` completato con successo: nessun nuovo alert aperto nella pull request.
- Dependency Graph del repository abilitato per rendere operativo il dependency review.

## Rischi residui

- F-20 e chiuso: baseline reale, clone pulito, CI remota e protezione branch sono verificati.
- Il gate migrazioni obbligatorio usa PostgreSQL 16 reale tramite Testcontainers; H2 resta soltanto feedback rapido della suite standard.
- I controlli security erano verdi su `a3407d3` nel repository storico. Nel nuovo Gestisco.IO sono obbligatori insieme ai gate CI, per un totale di dodici check associati a GitHub Actions. Gli esiti aggiornati sono nel rapporto ORG-5; non effettuare merge con scansioni fallite o pendenti.
- La CI segnala Actions con runtime deprecato, migrazione imminente di `ubuntu-latest` e punteggi OpenSSF bassi su alcune dipendenze transitive. Aggiornamenti e pinning sono da trattare nello Step 5.1; non sono stati soppressi gli avvisi. La PR di consolidamento contiene 702 file e richiede revisione per dominio: l'integrazione CodeQL segnala il limite di 300 file nel recupero del diff, pur producendo analisi senza errori e 0 finding.
- La fonte NVD del manutentore e best-effort: il preflight limita l'eta dichiarata a 48 ore, ma non certifica la completezza dei dati upstream. OSS Index non e verificato per mancanza delle credenziali richieste dal servizio; 0 CVE rilevate non equivale ad assenza assoluta di vulnerabilita.
- Gli account operativi storici non verificati sono in quarantena; la revisione manuale deve essere completata prima di consentire nuovi login agli eventuali dipendenti legittimi.
- Le sessioni usano l'ID account stabile e una versione credenziali; il lifecycle non distruttivo di disabilitazione, riabilitazione e revoca e coperto dallo Step 3.3.
- Le race critiche di sessioni e lockout sono incluse nella suite PostgreSQL trasversale obbligatoria.
- Gli ordini usano ID account/partner stabili; gli snapshot testuali non autorizzano accessi e lo storico ambiguo resta `UNRESOLVED` fino a riconciliazione esplicita.
- Il rate limit della registrazione e stato verificato sullo stack prod-like aggiornato.
- Un arresto non intercettabile con `SIGKILL` puo lasciare risorse temporanee; il recupero richiede project name e run ID registrati nella diagnostica e il cleanup guardato rifiuta ownership miste.
- Gate A, Gate B e Gate C sono chiusi tecnicamente nel repository reale.
- I pagamenti storici `UNRECONCILED` richiedono una decisione umana basata su evidenze: la migrazione evita conclusioni false, ma non puo ricostruire informazioni mai registrate.
- Le misure di V22 sono state raccolte su fixture sintetiche in PostgreSQL 16 e descrivono questo ambiente di prova; vanno ripetute sui volumi e sull'infrastruttura del rilascio reale.
- F-21 e chiuso dalla suite PostgreSQL trasversale obbligatoria; F-31 e chiuso tecnicamente dallo Step 3.10.
- Il recupero pubblico della password resta rinviato finche non viene introdotto un canale verificato; non vengono usate domande di sicurezza o reset privi di verifica dell'identita.
- La validazione operatore di WB-001, WB-002 e WB-003 resta pendente e non viene sostituita dai test automatici.
