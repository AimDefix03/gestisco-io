# Legacy Swing

## Stato

La precedente applicazione desktop Java Swing e congelata come riferimento funzionale e dimostrazione didattica. Non riceve nuove funzionalita commerciali e non fa parte della build produttiva predefinita.

Da ORG-3 il codice si trova in `src/` nella cartella separata `/Users/aim_defix/Desktop/GestionaleNegozioComputer`, non nella root web `/Users/aim_defix/Developer/Gestisco.IO`. Le build sotto si eseguono nella cartella legacy. La cronologia completa e l'originale sono conservati: questa separazione dei sorgenti non certifica migrazione dei dati o parita di tutti i workflow.

## Build dedicata

```bash
mvn -f pom-legacy.xml test
mvn -f pom-legacy.xml package
```

Il JAR generato e `target/gestionale-negozio-computer-legacy-1.0.0.jar`.

## Pattern legacy

- Factory per distinguere prodotti hardware e software.
- Strategy dimostrativa per i metodi di pagamento.
- Command per l'aggiunta al carrello.
- Decorator per servizi aggiuntivi.

Questi pattern non vengono trasferiti automaticamente nel backend web. Verranno sostituiti da modelli di dominio o adapter soltanto quando esiste una necessita reale.

## Condizioni per l'archiviazione

- Tutti i workflow rilevanti sono disponibili nella web app.
- I dati `.dat` necessari sono importabili e verificabili in PostgreSQL.
- La suite web copre i flussi sostitutivi.
- Non esistono procedure operative documentate che richiedono Swing.

La separazione locale di ORG-3 esclude gia `src/` e `pom-legacy.xml` dal checkout web, senza cancellare i sorgenti o i dati originali. Le condizioni sopra restano necessarie prima di dismettere definitivamente procedure e dati legacy. Nessun tag o nuovo repository remoto e stato creato durante la separazione.
