# ORG-3 - Recupero database isolato

Data: 2026-10-14. Recupero: COMPLETED nel perimetro database. ORG-3 complessivo: PARTIAL, sorgenti non ancora separati.

## Autorizzazione e perimetro

L'utente ha autorizzato backup del volume originale, ripristino su copia isolata, eventuali nuove credenziali soltanto sulla copia e verifica dati/accesso. Non sono stati autorizzati o effettuati cutover, cancellazioni, reset password utenti applicativi, deploy, commit, push o modifiche ai database originali.

Originale: container `gestionale-step43-demo-postgres`, volume `gestionale-step43-demo_gestionale-prodlike-postgres-data`, fermo prima e dopo l'intervento. Il secondo stack `gestionalenegoziocomputer` e i progetti terzi non sono stati modificati.

## Procedura ed evidenze

1. Nessun container attivo sul volume; PostgreSQL control data indica `shut down`. Nessun symlink o tablespace esterno rilevato.
2. Volume originale montato esclusivamente read-only da helper senza rete. Snapshot fisico completo di 51.649.024 byte, checksum SHA-256 e manifest dei 1.482 file. Hash sorgenti invariati prima/dopo il backup.
3. Vecchia immagine Docker assente: primo tentativo fallito prima di creare backup o modificare dati. Versione cluster letta: 16. Usata l'immagine locale `sha256:e013e867e712fec275706a6c51c966f0bb0c93cfa8f51000f85a15f9865a28cb`, PostgreSQL 16 Alpine arm64, senza pull/aggiornamenti.
4. Creato nuovo volume etichettato `org3-recovery-20261014-cmqt8-data`; restore fisico dal backup con 1.482/1.482 checksum corrispondenti prima dell'avvio.
5. Avviato `org3-recovery-20261014-cmqt8-postgres` con network none, zero porte pubblicate, filesystem root read-only, utente postgres e capability eliminate. Accesso temporaneo trust limitato al socket Unix interno della sola copia per il recupero.
6. Cinque password database nuove e casuali: bootstrap database, migrator, runtime, backup, restore. Conservate fuori repository in directory stabile privata; nessuna credenziale mostrata, nessuna password applicativa cambiata. Privilegi e ruoli conservati.
7. Arresto e riavvio della copia con HBA effettivo esclusivamente SCRAM. Password errata rifiutata; accesso positivo per tutti i cinque ruoli.
8. 25 tabelle pubbliche confrontate tramite conteggi e impronte deterministiche prima/dopo rotazione e dopo riavvio: nessuna variazione. Flyway: 35 migrazioni riuscite, zero fallite.
9. Runtime: CREATE TABLE rifiutato. Migrator: SET ROLE owner riuscito. Dump logico effettuato con ruolo backup (155.034 byte), checksum e catalogo pg_restore verificati.
10. Copia fermata pulitamente e salvata in secondo snapshot fisico abbinato alle nuove credenziali. Volume originale riletto in read-only: tutti i 1.482 hash ancora identici; container originale stesso ID/stato.
11. Provate le istruzioni consegnate: start della copia, verifica accesso runtime con 35 migrazioni, stop della copia. Lasciata ferma.

Il metodo di copia a cluster fermo e il controllo dell'autenticazione seguono i prerequisiti della documentazione PostgreSQL: [backup filesystem](https://www.postgresql.org/docs/16/backup-file.html), [pg_hba.conf](https://www.postgresql.org/docs/16/auth-pg-hba-conf.html).

## Esiti

12 controlli automatici positivi/negativi passati, oltre a confronto fisico, controllo finale originale e prova di riavvio. Nessuna modifica a sorgenti, migrazioni o API: suite applicative non rieseguite, restano le evidenze ORG-2.

- [x] Backup originale conservato prima delle modifiche alla copia.
- [x] Restore fisico isolato e checksum verificati.
- [x] Credenziali database funzionanti solo sulla copia; SCRAM effettivo verificato.
- [x] Dati applicativi e migrazioni preservati.
- [x] Originale invariato e copia lasciata ferma.
- [ ] Avvio backend/frontend e login UI sulla copia: non parte della verifica conclusa.
- [ ] Separazione cartelle e test nelle nuove destinazioni: ancora da eseguire.

## Backup e consegna

Directory privata: `/Users/aim_defix/GestiscoBackups/RECOVERY-20261014-cMQTc8`.

Contiene snapshot fisico originale e recuperato, dump logico, checksum, metadata, manifest e cinque file password. Non allegare la directory a una review AI/pubblica: contiene dati e credenziali. Permessi directory 0700, archivi 0600, password 0400. Non e una copia cifrata o off-site.

Il file `RECOVERY_README.md` contiene comandi verificati per avviare, controllare e fermare esclusivamente la copia. `check-recovered-database.mjs` verifica identita container/volume, isolamento e accesso runtime senza mostrare password. `/tmp` interno al container serve solo al socket; i secret host sono nella directory stabile.

Il ripristino fisico richiede anche la configurazione HBA rigorosa fornita: non avviare un archivio con entrypoint/default arbitrari. I metadata non includono password. Nessun file segreto o dump e stato copiato nel repository.

## Limiti e ripresa

Le vecchie password non sono state trovate: sono state sostituite soltanto sulla copia. Gli account applicativi, incluso il super admin, non sono stati resettati. La validazione non prova tutti i workflow del gestionale e non certifica fatturazione, privacy o produzione.

Non eseguiti: secondo restore del dump logico, UI/browser, avvio backend/frontend, recupero del secondo stack, backup remoto o migrazione dell'ambiente operativo. Nessun cambio implicito del database autorevole. Il futuro Step 4.7 resta aperto.

Prossima attivita: completare ORG-3, separando i sorgenti con build indipendenti e originale conservato. Prima di usare il gestionale web associare esplicitamente configurazione e database recuperato, bootstrap account disabilitato, rete privata e accesso SCRAM. Non avviare i Compose originali con nomi di progetto/volumi dedotti dalla nuova cartella.

Beneficio reale: la perdita dei file di credenziali non ha richiesto di azzerare dati o account; esiste una copia recuperata e verificata su cui preparare il nuovo ambiente mantenendo l'originale intatto.

Avanzamento: organizzazione 2/6 (33.3%); roadmap applicativa 32/48 (66.7%), invariate. Nessun avvio di ORG-4 o Step 4.4.
