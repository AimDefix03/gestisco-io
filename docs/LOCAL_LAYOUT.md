# Cartelle locali dopo ORG-3

Separazione del 2026-10-14; identita documentale allineata il 2026-10-15. Separazione dei sorgenti, non migrazione dell'ambiente operativo.

| Percorso | Scopo |
|---|---|
| `/Users/aim_defix/Desktop/Gestisco.IO` | Sorgente web per le prossime attivita: Spring Boot, React, migrazioni, script, CI e documentazione |
| `/Users/aim_defix/Desktop/GestionaleNegozioComputer` | Swing congelato, build esplicita con pom-legacy.xml |
| `/Users/aim_defix/Desktop/Gestionale.Negozio.Computer ` | Originale con spazio finale, conservato per rollback; non continuare qui lo sviluppo |
| `/Users/aim_defix/Desktop/PortfolioGiovanniDeFilippo` | Portfolio indipendente, fuori perimetro |
| `/Users/aim_defix/Desktop/codex_portfolio_sources` | Workspace storico della chat; non fonte autorevole del gestionale |

## Git e ripresa

Le nuove copie derivano dal bundle verificato della baseline `b077f7afe42b08c7976713d14552cbd58e9107a2`. Entrambe conservano 25 commit raggiungibili e sei ref, inclusi i commit locali non pubblicati. Non sono export senza storia e non usano oggetti Git condivisi con l'originale.

La branch di consolidamento e `miglioramenti-gestionale`. Le rimozioni legacy sono la separazione intenzionale del checkout, non perdita di codice: i file esclusi sono conservati nell'archivio privato, nella storia e nell'originale. Non annullarle con checkout/reset. ORG-5 ne prepara il commit e la pubblicazione con remote dedicato `gestisco`; `refs/remotes/origin/*` restano snapshot di provenienza. Verificare il rapporto ORG-5 e lo stato Git prima di assumere che commit, push o gate siano conclusi.

Il nome Gestisco.IO e recepito in ORG-4 nell'[addendum](GESTISCO_IDENTITY_ADDENDUM.md). Il 16 ottobre 2026, dopo il preflight privato, l'utente ha autorizzato il repository pubblico `AimDefix03/gestisco-io`, mantenendo i diritti riservati. UI, package e nomi tecnici restano invariati. Vedere [pubblicazione e gate](ORG5_PUBLICATION.md).

Il master originale resta invariato. Il piano applicativo resta fermo dopo 4.3; ORG-5 e in corso. Completare pubblicazione, controlli remoti e protezioni senza passare automaticamente a ORG-6 o 4.4. Consultare i rapporti datati ORG-3, ORG-4 e ORG-5 per gli esiti di verifica effettivi.

## Dati e Docker

Nessun `.dat`, dump, file secret, `.env.docker`, cache o configurazione IDE e importato nelle nuove copie. Non avviare Swing aspettandosi i vecchi account/dati. Gli archivi privati non devono essere pubblicati o allegati a review AI.

Il recupero PostgreSQL isolato e descritto in `ORG3_DATABASE_RECOVERY.md`. Il volume originale resta invariato; la copia recuperata resta ferma, senza rete o porte pubblicate. Non sono state modificate password applicative.

Prima di un futuro avvio web:

1. Concordare se usare un ambiente nuovo effimero o la copia recuperata; non riutilizzare implicitamente il volume originale.
2. Definire esplicitamente project Compose, container, porte e volume; i default esistenti possono collidere con i vecchi stack.
3. Collegare i file secret privati corretti senza copiarli nel repository e senza rigenerarli alla cieca.
4. Verificare migrazioni, readiness e login sulla destinazione scelta prima di qualunque cutover.

## Conservazione e rollback

Il bundle completo e in `/Users/aim_defix/GestiscoBackups/ORG2-20261013/history.bundle`. Manifest, file esclusi dalle due copie, documenti pre-separazione e prove di build sono in `/Users/aim_defix/GestiscoBackups/SEPARATION-20261014`.

Per annullare la separazione non serve cancellare o ripristinare il database: sospendere il lavoro nelle nuove copie e riesaminare l'originale con il manifest. Prima di ricominciare nell'originale conservare eventuali nuove modifiche web; non eseguire reset o sovrascritture automatiche. Questi backup sono locali, non cifrati/off-site.
