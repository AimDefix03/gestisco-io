# ADR 0009 - Sessione browser in cookie HttpOnly con protezione CSRF

## Stato

Accettata dall'utente il 2026-10-27 per lo step P1.6 della roadmap di prodotto. Sostituisce l'[ADR 0004](0004-browser-session-token-and-csrf.md), che resta come storico.

## Contesto

Con l'ADR 0004 il token di sessione restava nella memoria JavaScript e viaggiava nell'header `X-Session-Token`: ricaricare la pagina o aprire un link richiedeva un nuovo login, e un XSS poteva leggere il token (rischio R-024). Lo step P1.5 ha dato un indirizzo a ogni pagina; per usarli davvero la sessione deve sopravvivere alla ricarica senza diventare leggibile da JavaScript. L'ADR 0004 poneva come condizioni del passaggio a cookie: cambiamento atomico, protezione CSRF, TLS, test browser e piano di rollback.

## Decisione

Decisioni dell'utente del 2026-10-27: protezione CSRF double-submit di Spring Security, cookie fino alla chiusura del browser, rimozione completa dell'header.

1. **Cookie di sessione** `__Host-gestionale_session`: `HttpOnly`, `Secure`, `SameSite=Strict`, `Path=/`, senza `Domain`, `Max-Age` o `Expires`. Il browser lo elimina alla chiusura; scadenza assoluta, timeout di inattivita, revoca e rotazione restano lato server come prima. Il database conserva solo l'hash SHA-256 del token.
2. **Token non accessibile a JavaScript.** Login, rinnovo e `GET /api/accounts/session` restituiscono `user` ed `expiresAt` senza token. Al caricamento il frontend chiama `GET /api/accounts/session` e, se il cookie e valido, ripristina la sessione senza login.
3. **CSRF double-submit.** Spring Security con `CookieCsrfTokenRepository`: cookie `__Host-gestionale_xsrf` (`Secure`, `SameSite=Strict`, `Path=/`, leggibile dal frontend), confrontato con l'header `X-XSRF-TOKEN` su ogni richiesta che modifica dati. Il token ruota su login e rinnovo; una richiesta autenticata non lo rigenera, per non rompere le richieste parallele. Errore dedicato `403 AUTH_CSRF_INVALID`.
4. **Esenzioni CSRF** limitate a login, registrazione e verifica della password, che precedono la sessione: accettano solo JSON, non hanno CORS e sono gia limitate in frequenza da nginx. Actuator resta fuori dalla catena applicativa.
5. **Header rimosso del tutto.** `X-Session-Token` non e piu letto dal backend ne inviato dal frontend: un solo meccanismo da mantenere e testare. Test E2E e script usano i cookie del contesto browser.
6. **Logout, cambio password e cookie non valido** scadono entrambi i cookie (`Max-Age=0`); un cookie di sessione rifiutato viene scaduto dal server nella stessa risposta `401`.
7. **TLS obbligatorio.** Con `Secure` e il prefisso `__Host-` il browser rifiuta i cookie su HTTP in chiaro, tranne che su `localhost` e `127.0.0.1` (stack locali e CI). Un'installazione senza HTTPS non resta autenticata: il difetto e chiuso per costruzione, non silenzioso.

## Alternative valutate

### Token CSRF legato alla riga di sessione

Nessun secondo cookie, ma codice proprio da scrivere e mantenere per generazione, verifica e rotazione. Scartata a favore del meccanismo standard di Spring Security.

### Cookie persistente fino alla scadenza assoluta

Sopravvive al riavvio del browser. Scartata: sui PC di cassa condivisi chiudere il browser deve bastare a chiudere la sessione.

### Header mantenuto per client non browser

Utile per script e integrazioni, ma raddoppia i percorsi di autenticazione e lascia un token gestibile da JavaScript. Scartata; un'eventuale API per integrazioni avra credenziali proprie (P11).

## Conseguenze

- Ricaricare la pagina, aprire un link diretto o una nuova scheda non richiede il login finche la sessione e valida; chiudere il browser la termina.
- Un XSS non puo piu leggere il token di sessione, ma puo ancora inviare richieste autenticate dalla pagina compromessa: CSP senza `unsafe-inline`/`unsafe-eval`, sanitizzazione e controllo delle dipendenze restano necessari. R-024 e chiuso per la lettura del token.
- Contratto API cambiato in modo incompatibile: corpo di login e rinnovo senza `token`, header non piu accettato, CSRF obbligatorio sulle richieste che modificano dati.
- Rollback: ripristinare insieme backend e frontend della versione precedente; le sessioni nel database restano valide, gli utenti rifanno il login perche il vecchio frontend non conosce il cookie. Nessuna migrazione di schema coinvolta.

## Condizioni per rivalutare

- frontend e API pubblicati su origini o domini diversi (servirebbero CORS con credenziali e un cookie non `__Host-`);
- integrazioni server-to-server che richiedono credenziali proprie;
- introduzione di un identity provider o flusso OAuth/OIDC.
