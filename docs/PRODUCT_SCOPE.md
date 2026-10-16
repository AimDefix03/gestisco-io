# Gestisco.IO - Product Scope

## Stato dello scope

Lo scope e stato congelato dal 2026-07-28 per concentrare il lavoro sulla stabilizzazione. Al 2026-10-15 la roadmap applicativa conta 32/48 step completati (66.7%), fino al 4.3; questo conteggio non misura la maturita produttiva. Le precedenti stime percentuali di maturita non sono mantenute. Il Gate C ha evidenze tecniche, ma la prova con operatore e i gate produttivi restano aperti: il prodotto non e dichiarato pronto per produzione o vendita. Lo stato puntuale dei finding e in `IMPLEMENTATION_PROGRESS.md` nella root.

ORG-4 allinea nome e documentazione secondo [l'addendum organizzativo](GESTISCO_IDENTITY_ADDENDUM.md), senza ampliare lo scope congelato o modificare il master.

Fino a una rivalutazione esplicita dello scope restano ammesse soltanto:

- correzioni P0 e P1;
- test, fixture e strumenti necessari a dimostrare le correzioni;
- interventi indispensabili su sicurezza, integrita dati, migration e compatibilita;
- documentazione necessaria a rendere verificabili le decisioni.

La chiusura del Gate B consente esclusivamente gli step MVP gia ordinati in `ROADMAP.md`, uno alla volta e dopo il relativo benchmark. Nuove funzionalita fuori roadmap, ampliamenti del dominio e rifacimenti grafici non necessari restano sospesi; ogni eccezione deve essere approvata dal responsabile decisionale e registrata prima dell'implementazione.

La progettazione dei nuovi workflow e governata da `WORKFLOW_BENCHMARKS.md`: il confronto con prodotti maturi precede il codice, preserva gli invarianti locali e vieta la copia di codice, testi, asset o grafica proprietaria.

## Responsabilita decisionale

Giovanni De Filippo, in qualita di proprietario del progetto, e il responsabile finale delle decisioni riguardanti:

- significato e variazione dello stock;
- registrazione, storno e riconciliazione dei pagamenti;
- conservazione, migrazione e interpretazione dello storico.

Le decisioni tecniche che modificano questi invarianti devono includere impatto sui dati, test richiesti, strategia di recovery e aggiornamento di `IMPLEMENTATION_PROGRESS.md`.

## Visione

Gestisco.IO e la linea web indipendente del progetto nato come gestionale desktop per un negozio di computer. La direzione scelta e un gestionale modulare per attivita commerciali, con possibile evoluzione SaaS. Il dominio implementato resta quello documentato qui: catalogo, acquisti, vendite e magazzino. Il cambio di nome non dimostra compatibilita con qualsiasi settore e non introduce un servizio SaaS disponibile.

L'obiettivo non e costruire subito un ERP completo, ma un prodotto credibile, modulare e dimostrabile in modo professionale.

## Target MVP congelato

Il primo MVP ha un perimetro intenzionalmente limitato:

- una sola azienda;
- un solo magazzino;
- attivita commerciale di dimensione piccola o media;
- pagamenti registrati manualmente, senza PSP o integrazioni bancarie;
- documenti simulati e non fiscali;
- monolite modulare Spring Boot, SPA React e PostgreSQL.

Qualunque requisito multi-azienda, multi-magazzino, SaaS, fiscale o di pagamento reale appartiene al dopo MVP e richiede una decisione architetturale separata.

## Utenti principali

- Super admin: governa configurazione iniziale e puo creare altri admin.
- Admin: gestisce catalogo, magazzino, ordini, documenti simulati, account e audit secondo permessi assegnati.
- Dipendente: opera su catalogo, magazzino, ordini e documenti operativi.
- Cliente: consulta catalogo e crea ordini/acquisti simulati.

## Perimetro funzionale del target

- Autenticazione e gestione sessione.
- Ruoli e permessi lato backend.
- Catalogo prodotti con brand, categoria tecnica, tipo prodotto e utilizzo opzionale.
- Movimenti di magazzino con storico.
- Ordini con righe prodotto e scarico stock.
- Documenti simulati collegati agli ordini, esplicitamente non fiscali.
- Configurazione aziendale e numerazioni documentali simulate per tipo ed esercizio.
- Pagamenti gestionali, rimborsi e resi senza integrazione con PSP o banche.
- Audit log delle operazioni sensibili.
- Dashboard operativa.
- Report interni vendite e magazzino con filtri ed export CSV, Excel e PDF.

## Fuori perimetro nella fase attuale

- Fatturazione elettronica reale.
- Contabilita certificata.
- Pagamenti reali.
- Spedizioni reali.
- Marketplace o integrazioni esterne.
- Multi-azienda e piani SaaS.
- Onboarding clienti, branding white-label, entitlement e provisioning automatico.
- Aggiornamento remoto o orchestrazione di una flotta di installazioni.
- Workflow GDPR, retention generale e legal hold.
- Conservazione elettronica a norma.
- Conformita legale o fiscale dichiarata.
- Business intelligence avanzata, data warehouse o report fiscali certificati.

La possibile evoluzione multi-azienda e multi-tenant e descritta come proposta futura in `MULTI_TENANCY_ARCHITECTURE.md`. Non fa parte delle funzionalita disponibili.

La possibile evoluzione privacy, conservazione e fatturazione elettronica e descritta in `PRIVACY_RETENTION_EINVOICING_ARCHITECTURE.md`. Nessuna delle funzionalita target e disponibile o dichiarata conforme nella versione corrente.

La possibile evoluzione commerciale del lifecycle cliente e descritta in `CUSTOMER_LIFECYCLE_AND_RELEASE_ARCHITECTURE.md`. Onboarding, branding, provisioning e aggiornamenti per coorti non sono disponibili nella versione corrente.

## Controllo delle eccezioni

Una proposta fuori dal backlog di stabilizzazione puo essere valutata soltanto se:

1. spiega perche non puo attendere;
2. identifica finding, dati e workflow coinvolti;
3. definisce test, rollback e rischio di regressione;
4. riceve approvazione esplicita dal responsabile decisionale;
5. viene inserita nella classificazione di `ROADMAP.md` prima di modificare il codice.

In assenza di tutti i requisiti la proposta resta nel dopo MVP.

## Differenza rispetto a una demo

Il prodotto deve mostrare regole operative, sicurezza, tracciabilita, dati persistenti, test e documentazione. Non basta avere CRUD funzionanti.

## Differenza rispetto a un ERP

Non deve coprire tutte le aree aziendali. Il perimetro corrente resta focalizzato su catalogo, acquisti, magazzino, ordini e operazioni commerciali; nuovi settori o modelli di servizio richiedono requisiti, verifica e approvazione separati.
