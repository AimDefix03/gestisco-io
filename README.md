# Gestisco.IO

Gestionale web modulare per catalogo, acquisti, magazzino e vendite, sviluppato da Giovanni De Filippo con Spring Boot, React e PostgreSQL.

Il progetto nasce da un gestionale desktop per un negozio di computer e prosegue come prodotto web indipendente. La direzione e un gestionale per attivita commerciali, con una possibile evoluzione SaaS: non un servizio SaaS gia disponibile.

## Stato e perimetro

**In sviluppo attivo. MVP tecnico verificato, non dichiarato pronto per la produzione o la vendita.**

Il perimetro corrente resta una sola azienda e un solo magazzino, con pagamenti registrati manualmente e documenti simulati, non fiscali. Non sono disponibili multi-tenancy, abbonamenti SaaS, provisioning clienti o integrazione con un prestatore di servizi di pagamento.

La roadmap applicativa e arrivata allo Step 4.3: 32/48 step (66,7%). E un conteggio di attivita, non una percentuale di affidabilita o prontezza commerciale. Restano, tra gli altri, validazione con operatori, backup off-site, recovery e alert operativi, verifiche di sicurezza indipendenti e gate produttivi. Il sistema non e dichiarato conforme alla fatturazione elettronica, alla normativa fiscale o al GDPR.

[Stato e verifiche](IMPLEMENTATION_PROGRESS.md) | [Scope](docs/PRODUCT_SCOPE.md) | [Roadmap](docs/ROADMAP.md) | [Identita e vincoli](docs/GESTISCO_IDENTITY_ADDENDUM.md)

## Flussi implementati

| Area | Funzioni presenti | Confine operativo |
|---|---|---|
| Catalogo e anagrafiche | Prodotti attivi/disattivati, brand e tipo, clienti e fornitori | Identificatori canonici e protezione dei riferimenti storici |
| Vendite | Vendita assistita, ordini cliente, conferma, riserva, evasione e annullo | Autorizzazioni e ownership lato server |
| Acquisti | Ordini fornitore, ricezioni parziali e annullo del residuo | Ricezione collegata a movimento fisico e costo documentato |
| Magazzino | Ledger, disponibilita, inventario fisico e rettifiche approvate | Conteggio e approvazione separati; stock riservato protetto |
| Incassi e resi | Registrazione incassi, storni, resi multi-riga, rimborsi e riconciliazione | Nessuna esecuzione di pagamenti bancari; storico ambiguo da riconciliare |
| Documenti e report | Documenti simulati, configurazione azienda, KPI, export CSV/Excel/PDF | Nessun documento presentato come fiscalmente valido |
| Accessi e tracciabilita | Ruoli, permessi, sessioni revocabili, lifecycle account e audit | Registrazione pubblica solo cliente; staff creato da utenti autorizzati |

I flussi critici usano transazioni, locking, idempotenza e test comportamentali. Il frontend distingue il risultato di un comando dal successivo aggiornamento dei dati e preserva le bozze. Queste misure riducono rischi specifici, non costituiscono una garanzia di assenza di errori.

## Architettura

Monolite modulare, senza microservizi:

- **Backend:** Java 17, Spring Boot, Spring Security, JPA/Hibernate, Spring Modulith, Flyway e PostgreSQL.
- **Frontend:** React, TypeScript e Vite; test Vitest/React Testing Library e Playwright.
- **Ambiente di verifica:** Docker Compose, Nginx, Prometheus e workflow GitHub Actions.

Le versioni effettive sono definite nei POM e nel lockfile frontend. H2 e usato solo nei test veloci; concorrenza e upgrade richiedono le suite PostgreSQL dedicate.

```text
Gestisco.IO/
  AGENTS.md
  MASTER_SOURCE_AGENTE_AI.md
  IMPLEMENTATION_PROGRESS.md
  pom.xml
  web/backend/
  web/frontend/
  web/postgres/
  scripts/
  deploy/
  docs/
```

[Architettura](docs/ARCHITECTURE.md) | [Confini dei moduli](docs/MODULE_BOUNDARIES.md) | [Contratto API](docs/API_CONTRACT.md)

## Verificare il progetto

Prerequisiti: JDK 17, Maven e Node.js >=22 con npm. Docker e necessario per le suite PostgreSQL e prod-like, non per il solo frontend. Nessun Maven Wrapper e attualmente incluso.

Dalla root web, build e suite backend standard:

```bash
mvn -B verify
```

Frontend, dalla root web:

```bash
cd web/frontend
npm ci
npm test
npm run typecheck
npm run typecheck:e2e
npm run build
```

Con Docker disponibile, i gate isolati sono documentati in [Testing](docs/TESTING.md) e [Verifica prod-like](docs/PROD_LIKE_VERIFICATION.md). In particolare:

```bash
scripts/db/verify-postgresql-suite.sh fast
scripts/ci/run-prod-like-verification.sh
```

I runner vanno letti e configurati secondo la documentazione: non sostituiscono l'avvio di un ambiente operativo e non vanno adattati per usare volumi esistenti.

Ultima verifica delle cartelle separate, ORG-3 del 14 ottobre 2026: 320 test backend, 205 frontend e 41 legacy passati; build web e Swing riuscite. Quella prova locale usa Java 23 con compilazione release 17 e non equivale a una nuova prova runtime Java 17. Dettagli e limiti nel [rapporto ORG-3](docs/ORG3_LOCAL_VERIFICATION.md). ORG-4 e documentale: non presenta queste suite come rieseguite.

## Avvio e dati

Seguire [Deployment](docs/DEPLOYMENT.md), [Configurazione](docs/CONFIGURATION.md) e [Gestione dei segreti](docs/SECRET_ROTATION.md). Non sono fornite credenziali predefinite; il bootstrap iniziale va abilitato esplicitamente su un ambiente nuovo.

**La separazione del codice non ha spostato o ricollegato i database.** Prima di avviare Compose nel checkout locale, leggere [Layout locale](docs/LOCAL_LAYOUT.md): scegliere esplicitamente ambiente, project, porte, volumi e file secret. Non rigenerare password presumendo che corrispondano a un database esistente. Nessun avvio automatico o migrazione dei dati e implicito nel cambio di nome.

Non includere in commit o pacchetti di review credenziali, `.env.docker`, file `.dat`, dump, backup o dati personali. Le copie di recovery private non sono materiale pubblico.

## Provenienza e pubblicazione

La versione Java Swing e conservata separatamente con il nome GestionaleNegozioComputer. Non fa parte della build web e non riceve nuove funzioni. [Politica legacy](docs/LEGACY_SWING.md).

La cronologia comune e conservata senza riscritture. Il repository del prodotto web e [AimDefix03/gestisco-io](https://github.com/AimDefix03/gestisco-io), reso pubblico su autorizzazione dell'autore il 16 ottobre 2026. Questa baseline consolida la separazione Swing/web e l'identita Gestisco.IO. Pubblicazione, protezioni e nuove verifiche CI/Security sono registrate nello [stato ORG-5](docs/ORG5_PUBLICATION.md): la sola presenza dei sorgenti non certifica il superamento dei gate.

Il nome Gestisco.IO e un'identita di progetto: disponibilita del dominio e del marchio non verificate.

## Diritti sul codice

Copyright (c) 2026 Giovanni De Filippo. Tutti i diritti riservati sul codice originale del progetto. Il repository e pubblico per consultazione, ma non viene concessa una licenza open source. La pubblicazione non e un'autorizzazione generale al riutilizzo o alla distribuzione commerciale. Restano applicabili i diritti previsti dalla legge e dai termini di GitHub, inclusi quelli relativi ai fork sulla piattaforma. Le dipendenze e gli eventuali contenuti di terzi mantengono le rispettive licenze e attribuzioni. Vedere [NOTICE.md](NOTICE.md).

## Lavorare al progetto

Leggere [AGENTS.md](AGENTS.md), il [master vincolante](MASTER_SOURCE_AGENTE_AI.md), l'[addendum organizzativo](docs/GESTISCO_IDENTITY_ADDENDUM.md) e lo [stato corrente](IMPLEMENTATION_PROGRESS.md). Il master resta immutabile. Si procede con un solo step autorizzato per volta, senza refactor o nuove feature estranei allo scope.

- [Manuale utente](docs/USER_MANUAL.md)
- [Sicurezza](docs/SECURITY.md)
- [Backup e restore](docs/BACKUP_RESTORE.md)
- [Osservabilita](docs/OBSERVABILITY.md)
- [Checklist di rilascio](docs/RELEASE_CHECKLIST.md)
- [Organizzazione repository](docs/REPOSITORY_ORGANIZATION.md)

Le proposte [multi-tenant](docs/MULTI_TENANCY_ARCHITECTURE.md), [privacy e documenti fiscali](docs/PRIVACY_RETENTION_EINVOICING_ARCHITECTURE.md) e [lifecycle commerciale](docs/CUSTOMER_LIFECYCLE_AND_RELEASE_ARCHITECTURE.md) descrivono evoluzioni future, non funzioni disponibili.
