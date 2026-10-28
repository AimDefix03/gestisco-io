# Gestisco.IO - Addendum di identita e organizzazione

Versione 1, 2026-10-15. Ambito: ORG-4, esclusivamente documentale.

## Decisione recepita

Giovanni De Filippo ha scelto Gestisco.IO come nome della linea web, mantenendo separata la versione Swing con il nome GestionaleNegozioComputer, e ha autorizzato lo step organizzativo successivo a ORG-3. Questo documento recepisce quelle decisioni: non attribuisce approvazione a nuove funzioni, pubblicazione, riscrittura della storia o cambiamenti operativi.

Il [MASTER_SOURCE_AGENTE_AI.md](../MASTER_SOURCE_AGENTE_AI.md) resta immutato e vincolante. La sua gerarchia A.7, i gate, le dipendenze e le stop condition non cambiano. Il termine storico GestionaleNegozioComputer nel master identifica la provenienza del progetto, non impone di riunire Swing e web. Nessun finding viene chiuso dal cambio di nome.

## Identita e promessa verificabile

- Nome: **Gestisco.IO**, con questa grafia nei documenti correnti.
- Descrizione: gestionale web modulare per catalogo, acquisti, magazzino e vendite.
- Stato: sviluppo attivo; MVP tecnico con verifiche documentate, non dichiarato pronto per produzione o vendita.
- Scope corrente: una sola azienda, un solo magazzino, pagamenti registrati manualmente e documenti simulati non fiscali.
- Direzione futura: un gestionale per attivita commerciali con possibile evoluzione SaaS, non un servizio multi-tenant gia disponibile o un ERP universale.

Il [Product Scope](PRODUCT_SCOPE.md) resta congelato. Multi-azienda, multi-magazzino, provisioning, abbonamenti, integrazioni bancarie e fatturazione elettronica non vengono introdotti o autorizzati da questo addendum. La disponibilita del dominio, del marchio e del nome repository non e stata verificata.

## Radici e provenienza

| Elemento | Ruolo corrente |
|---|---|
| `/Users/aim_defix/Developer/Gestisco.IO` | Fonte autorevole del prodotto web (dal 2026-10-26; prima `/Users/aim_defix/Desktop/Gestisco.IO`) |
| `web/backend/src/` | Sorgenti e test del backend attivo, da conservare |
| `/Users/aim_defix/Desktop/GestionaleNegozioComputer` | Swing congelato; qui restano `src/` e `pom-legacy.xml` |
| `/Users/aim_defix/Desktop/Gestionale.Negozio.Computer ` | Originale con spazio finale, conservato per rollback |
| `codex_portfolio_sources` e copie di review | Non sono la fonte autorevole del gestionale |

La cronologia comune e preservata. Non cancellare vecchie ref o resoconti per nascondere la provenienza universitaria: il prodotto corrente e distinguibile senza riscrivere la sua storia. Il portfolio personale e un progetto indipendente e resta escluso.

## Compatibilita intenzionale

ORG-4 non cambia codice, UI, logo, titolo browser, package Java, artifact Maven/npm, API, schema, migrazioni, immagini, metriche, variabili `GESTIONALE_*`, nomi database, container o volumi. I nomi tecnici storici restano validi; non eseguire sostituzioni globali per farli coincidere con il marchio.

Il 2026-10-28, alla revisione del gate G1, l'utente ha deciso di portare il nome nei testi visibili: titolo del browser, intestazioni della pagina di accesso e del workspace, autore dei PDF esportati. Package, artifact, API, schema, variabili `GESTIONALE_*`, cookie, database, container e volumi mantengono i nomi tecnici storici.

Non e un revamp grafico. Eventuali futuri cambiamenti tecnici o di interfaccia richiedono uno step separato, verifiche e gestione della compatibilita. Rinominare la cartella non ricollega automaticamente i dati: seguire [Layout locale](LOCAL_LAYOUT.md) e [Deployment](DEPLOYMENT.md).

## Regole di presentazione

Usare italiano diretto e descrizioni di comportamenti reali. Distinguere funzioni disponibili, evidenze datate, limiti e proposte future. Evitare claim come enterprise-ready, sicurezza assoluta, conformita certificata o percentuali di prontezza commerciale non dimostrate.

I conteggi organizzativi e applicativi misurano attivita concluse, non affidabilita o vendibilita. Conservare le date dei test: una modifica ai documenti non rinnova l'esito di una scansione di sicurezza o di una prova operatore. La scelta della licenza resta una decisione esplicita prima della pubblicazione.

## Lavoro e verifiche

Leggere [AGENTS.md](../AGENTS.md) e [IMPLEMENTATION_PROGRESS.md](../IMPLEMENTATION_PROGRESS.md) dalla root web. Build backend: `mvn -B verify`. Per il frontend entrare in `web/frontend`, eseguire `npm ci`, `npm test`, `npm run typecheck`, `npm run typecheck:e2e` e `npm run build`. Gate PostgreSQL e prod-like richiedono i runner isolati descritti in [Testing](TESTING.md).

ORG-4 verifica coerenza dei documenti, collegamenti locali, corrispondenza con configurazione corrente e invarianza di master, codice, configurazione e riferimenti Git. Le suite applicative non vengono presentate come rieseguite. Evidenze e limitazioni in [ORG4_VERIFICATION.md](ORG4_VERIFICATION.md).

## Accettazione e stop

- README, AGENTS, progress, scope e roadmap concordano su nome, root, comandi e limiti.
- Le istruzioni non reintroducono Swing nel web e non autorizzano l'avvio sui vecchi volumi.
- Master, file applicativi, configurazioni e migrazioni restano byte per byte invariati.
- Nessun commit, push, merge, remote, database o segreto modificato in questo step.
- Modifiche concorrenti o verifiche fallite bloccano la copia dei documenti; non sovrascrivere alla cieca.

Prossimo step organizzativo: ORG-5, previa autorizzazione, per destinazione GitHub, provenienza/storia, gestione della PR #2, licenza/visibilita, scansione completa dei contenuti da pubblicare, CI e protezioni. ORG-6 preparera handoff e prova di ripresa. Lo Step 4.4 applicativo resta sospeso; `AUTO_ADVANCE: false`.

Beneficio reale: un collaboratore o un valutatore riconosce il prodotto web attuale, trova i comandi corretti e distingue subito le funzioni utilizzabili dagli obiettivi futuri, senza lavorare sulla copia Swing o collegarsi per errore a un database storico.

## Decisione successiva ORG-5 - 2026-10-16

L'utente ha autorizzato la pubblicazione su `AimDefix03/gestisco-io` e, dopo il blocco delle protezioni private, il passaggio al pubblico. Ha scelto di mantenere i diritti riservati, senza licenza open source; vedere [NOTICE](../NOTICE.md). Questa decisione successiva non altera il resoconto ORG-4 sopra, il master o lo scope. Stato effettivo di pubblicazione e verifiche nel [rapporto ORG-5](ORG5_PUBLICATION.md).

## Flusso sviluppatore singolo - 2026-10-18

L'utente ha dichiarato di lavorare da solo e ha autorizzato di portare a zero soltanto le approvazioni esterne obbligatorie. Restano pull request, tutti i dodici check automatici, branch aggiornata, conversazioni risolte, protezioni applicate agli amministratori e divieti di force push/cancellazione. La revisione tecnica documentata non equivale a una revisione umana indipendente. Nessuna autorizzazione generale a merge, deploy o indebolimento dei gate deriva da questa scelta.

Il 19 ottobre, alla ripresa della chiusura ORG-5 richiesta dall'utente, e stata integrata la PR #13 con merge commit `36ca5c75dc9de6726e2eb4b77ad4ebc6c6c50c31`. Gli esiti successivi al merge sono documentati separatamente nel rapporto ORG-5. La nuova decisione sostituisce il precedente vincolo organizzativo del revisore distinto, senza alterare il master o le invarianti applicative.
