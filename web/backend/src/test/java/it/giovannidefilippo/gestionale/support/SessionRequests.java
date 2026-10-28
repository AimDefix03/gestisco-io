package it.giovannidefilippo.gestionale.support;

import it.giovannidefilippo.gestionale.user.SessionCookies;
import jakarta.servlet.http.Cookie;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.UUID;

/**
 * Richieste MockMvc autenticate come dal browser: cookie di sessione e double-submit CSRF (cookie e header uguali).
 * Non usa il post-processor csrf() di spring-security-test, che sostituisce il repository del CsrfFilter nel contesto condiviso.
 */
public final class SessionRequests {
    private SessionRequests() {
    }

    public static RequestPostProcessor session(String token) {
        return request -> {
            String csrfToken = UUID.randomUUID().toString();
            Cookie csrfCookie = new Cookie(SessionCookies.CSRF_COOKIE, csrfToken);
            if (token == null) {
                request.setCookies(csrfCookie);
            } else {
                request.setCookies(new Cookie(SessionCookies.SESSION_COOKIE, token), csrfCookie);
            }
            request.addHeader(SessionCookies.CSRF_HEADER, csrfToken);
            return request;
        };
    }

    public static String sessionToken(MvcResult result) {
        Cookie cookie = result.getResponse().getCookie(SessionCookies.SESSION_COOKIE);
        if (cookie == null || cookie.getValue().isBlank()) {
            throw new IllegalStateException("Cookie di sessione assente nella risposta.");
        }
        return cookie.getValue();
    }
}
