package it.giovannidefilippo.gestionale.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.stereotype.Component;

/**
 * Cookie della sessione browser (ADR 0009): token di sessione `HttpOnly` e token CSRF double-submit leggibile dal
 * frontend, entrambi `Secure`, `SameSite=Strict`, con prefisso `__Host-` e senza scadenza lato browser.
 */
@Component
public class SessionCookies {
    public static final String SESSION_COOKIE = "__Host-gestionale_session";
    public static final String CSRF_COOKIE = "__Host-gestionale_xsrf";
    public static final String CSRF_HEADER = "X-XSRF-TOKEN";

    private final CookieCsrfTokenRepository csrfTokenRepository;

    public SessionCookies() {
        csrfTokenRepository = new CookieCsrfTokenRepository();
        csrfTokenRepository.setCookieName(CSRF_COOKIE);
        csrfTokenRepository.setHeaderName(CSRF_HEADER);
        csrfTokenRepository.setCookieCustomizer(cookie -> cookie.httpOnly(false).secure(true).sameSite("Strict").path("/"));
    }

    public CsrfTokenRepository csrfTokenRepository() {
        return csrfTokenRepository;
    }

    /** Apre la sessione nel browser e ruota il token CSRF, cosi un token precedente all'accesso non resta valido. */
    public void open(String sessionToken, HttpServletRequest request, HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, sessionCookie(sessionToken, -1).toString());
        csrfTokenRepository.saveToken(csrfTokenRepository.generateToken(request), request, response);
    }

    public void ensureCsrfToken(HttpServletRequest request, HttpServletResponse response) {
        if (csrfTokenRepository.loadToken(request) == null) {
            csrfTokenRepository.saveToken(csrfTokenRepository.generateToken(request), request, response);
        }
    }

    public void close(HttpServletRequest request, HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, sessionCookie("", 0).toString());
        csrfTokenRepository.saveToken(null, request, response);
    }

    private static ResponseCookie sessionCookie(String value, long maxAgeSeconds) {
        return ResponseCookie.from(SESSION_COOKIE, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();
    }
}
