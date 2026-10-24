# AGENTS.md

Regole permanenti per lavorare su questo repository.

## Fonte primaria

Leggere integralmente `MASTER_SOURCE_AGENTE_AI.md`, poi `IMPLEMENTATION_PROGRESS.md` e i documenti tecnici pertinenti. La gerarchia A.7 del master resta vincolante: criteri e gate, invarianti di sicurezza e dominio, codice reale, test comportamentali, schema/migration, documentazione. Verificare sempre le affermazioni sul codice e sullo stato Git: un test che codifica un comportamento insicuro non giustifica il bug.

L'addendum `docs/GESTISCO_IDENTITY_ADDENDUM.md` recepisce soltanto il nome Gestisco.IO e la separazione approvati dall'utente. Non modifica il master, i gate o il perimetro funzionale. Non inizializzare Git se `.git` e assente; segnalare la root errata o il blocco.

## Stato del progetto

Gestisco.IO contiene esclusivamente la linea web in `web/`, con backend Spring Boot e frontend React. Root locale: `/Users/aim_defix/Desktop/Gestisco.IO`. Scope corrente: una sola azienda, un solo magazzino, pagamenti manuali e documenti simulati non fiscali. La direzione SaaS non autorizza multi-tenancy o ampliamenti automatici. Dal 2026-10-24 l'ordine dei lavori segue `docs/ROADMAP_PRODOTTO.md` (ADR 0008): nucleo generalista, segmento pilota abbigliamento e calzature, uno step per volta con autorizzazione.

La parte Swing congelata e separata in `/Users/aim_defix/Desktop/GestionaleNegozioComputer`. Non ricreare qui `src/` nella root o `pom-legacy.xml`; `web/backend/src/` resta necessario. La cronologia Git contiene ancora entrambe le linee per preservarne la provenienza.

ORG-5 ha pubblicato la baseline `cabc12f` su main di `https://github.com/AimDefix03/gestisco-io`, pubblico per decisione esplicita del 16 ottobre 2026. Remote dedicato `gestisco`, main protetta; verifiche remote e chiusura dello step in `docs/ORG5_PUBLICATION.md`. Verificare sempre `git remote -v` prima di qualsiasi push. Le ref origin conservate sono snapshot storici, non stato GitHub aggiornato. Non effettuare checkout di vecchie branch per annullare le rimozioni intenzionali. Leggere `docs/LOCAL_LAYOUT.md`, `docs/ORG3_SEPARATION.md` e `IMPLEMENTATION_PROGRESS.md` prima di intervenire.

La PR #13 e stata integrata il 19 ottobre con `36ca5c7` e la PR #15 il 21 ottobre con `b68c5b8`, che chiude ORG-5; main locale e aggiornata esclusivamente fast-forward e segue `gestisco/main`. Le altre branch e ref storiche restano conservate. Stato corrente ed evidenze in `docs/ORG5_PUBLICATION.md`.

Il codice originale resta a diritti riservati: non aggiungere una licenza open source senza una nuova decisione dell'autore. La visibilita pubblica non autorizza a pubblicare segreti, dati operativi o copie di recovery. La baseline iniziale di un repository vuoto non autorizza futuri push diretti su main o riduzioni dei gate.

Il 18 ottobre l'utente, sviluppatore singolo, ha autorizzato soltanto la riduzione delle approvazioni esterne obbligatorie da una a zero. Restano obbligatori pull request, dodici check GitHub Actions, aggiornamento alla base e conversazioni risolte; protezioni applicate anche agli amministratori, force push e cancellazione vietati. Documentare la revisione tecnica, senza presentarla come approvazione indipendente. Commit, push e merge richiedono comunque autorizzazione pertinente; non usare bypass o ridurre i controlli per sbloccare una PR. Il 2026-10-24 l'utente ha autorizzato in modo permanente commit, push di branch dedicate e apertura di PR verso main per gli step di `docs/ROADMAP_PRODOTTO.md`, uno step per volta; il merge resta sempre all'utente. L'autorizzazione non copre push diretti su main, force push, cancellazione di branch o ref, modifiche alle protezioni, operazioni su database o ambienti operativi, chiusura di PR altrui o lavoro fuori dalla roadmap.

Il repository originale `/Users/aim_defix/Desktop/Gestionale.Negozio.Computer ` (spazio finale) e conservato per rollback, non per nuove funzionalita. Le configurazioni Docker non sono state migrate: non avviare gli stack predefiniti senza associare esplicitamente ambiente, secret, project e volumi. Non copiare credenziali o dati nei repository.

## Protocollo di lavoro

- Procedere sempre a step piccoli, approvati e verificabili.
- Non implementare piu moduli grandi nello stesso step.
- Fermarsi alla conclusione dello step autorizzato: `AUTO_ADVANCE: false`. Leggere lo stato corrente prima di scegliere il prossimo, senza dedurlo dal numero dell'ultimo step applicativo.
- Non rinominare automaticamente package, artifact, variabili `GESTIONALE_*`, database, volumi o UI per adeguarli al nome documentale. Le compatibilita operative sono prioritarie.
- Non modificare file non pertinenti allo step approvato.
- Non effettuare commit, push, merge, reset o cancellazioni distruttive senza autorizzazione esplicita.
- Non dichiarare conformita fiscale, legale, GDPR o produttiva senza verifica professionale.
- Non lasciare TODO, placeholder o integrazioni simulate presentate come complete.

## Qualita tecnica

- Backend: usare DTO pubblici, servizi transazionali, validazione input, errori coerenti e autorizzazioni lato server.
- Frontend: ogni vista deve gestire caricamento, vuoto, errore, successo, permessi e sessione scaduta.
- Database: ogni evoluzione strutturale futura deve passare da migrazioni versionate.
- Sicurezza: il frontend puo nascondere azioni, ma il backend deve sempre far rispettare permessi e regole.

## Comandi di verifica

Build web dalla root:

```bash
mvn verify
```

Backend web:

```bash
cd web/backend
mvn test
```

Frontend web:

```bash
cd web/frontend
npm ci
npm test
npm run typecheck
npm run typecheck:e2e
npm run build
```

I comandi Swing vanno eseguiti soltanto nella cartella legacy separata.

## Documentazione

Ogni step che cambia architettura, modello dati, API, sicurezza o deployment deve aggiornare la documentazione in `docs/`.

Riportare comandi e risultati realmente osservati, limiti, prossimo step e un breve caso d'uso reale. Separare il conteggio delle milestone organizzative da quello degli step applicativi; nessuno dei due misura la prontezza alla vendita. Conservare i resoconti storici datati senza presentarli come nuove verifiche.
