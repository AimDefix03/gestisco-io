package it.giovannidefilippo.gestionale.security;

import it.giovannidefilippo.gestionale.user.SessionCookies;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BrowserSessionCookieSecurityTest {
    private static final String SUPER_ADMIN_USERNAME = "test_super_admin";
    private static final String SUPER_ADMIN_PASSWORD = "Test-Bootstrap-9842!";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginKeepsTheTokenOutOfJavaScriptAndOutOfTheBody() throws Exception {
        MvcResult login = login();

        assertThat(login.getResponse().getContentAsString()).doesNotContain("\"token\"");
        String session = setCookie(login, SessionCookies.SESSION_COOKIE);
        assertThat(session).contains("HttpOnly").contains("Secure").contains("SameSite=Strict").contains("Path=/")
                .doesNotContain("Max-Age").doesNotContain("Expires").doesNotContain("Domain");
        String csrf = setCookie(login, SessionCookies.CSRF_COOKIE);
        assertThat(csrf).contains("Secure").contains("Path=/").doesNotContain("HttpOnly").doesNotContain("Max-Age");
        assertThat(login.getResponse().getCookie(SessionCookies.CSRF_COOKIE).getAttribute("SameSite")).isEqualTo("Strict");
    }

    @Test
    void sessionCookieRestoresTheSessionAfterAReloadAndAnswersEmptyWithoutIt() throws Exception {
        BrowserSession browser = BrowserSession.from(login());

        mockMvc.perform(get("/api/accounts/session").cookie(browser.sessionCookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.username").value(SUPER_ADMIN_USERNAME))
                .andExpect(jsonPath("$.expiresAt").exists())
                .andExpect(jsonPath("$.token").doesNotExist());
        mockMvc.perform(get("/api/accounts/session"))
                .andExpect(status().isNoContent());
    }

    @Test
    void reloadReissuesAMissingCsrfToken() throws Exception {
        BrowserSession browser = BrowserSession.from(login());

        MvcResult restored = mockMvc.perform(get("/api/accounts/session").cookie(browser.sessionCookie()))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(restored.getResponse().getCookie(SessionCookies.CSRF_COOKIE)).isNotNull();
        mockMvc.perform(get("/api/accounts/session").cookie(browser.sessionCookie(), browser.csrfCookie()))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(result.getResponse().getHeaders(HttpHeaders.SET_COOKIE)).isEmpty());
    }

    @Test
    void legacySessionHeaderIsNoLongerAccepted() throws Exception {
        BrowserSession browser = BrowserSession.from(login());

        mockMvc.perform(get("/api/accounts/session").header("X-Session-Token", browser.sessionToken()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/audit").header("X-Session-Token", browser.sessionToken()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void stateChangingRequestsRequireTheMatchingCsrfHeader() throws Exception {
        BrowserSession browser = BrowserSession.from(login());
        String category = """
                { "code": "CSRF-%s", "name": "Categoria CSRF", "parentCode": null, "active": true }
                """.formatted(System.nanoTime());

        mockMvc.perform(post("/api/product-categories")
                        .cookie(browser.sessionCookie(), browser.csrfCookie())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(category))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTH_CSRF_INVALID"));
        mockMvc.perform(post("/api/product-categories")
                        .cookie(browser.sessionCookie(), browser.csrfCookie())
                        .header(SessionCookies.CSRF_HEADER, "token-di-un-altro-sito")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(category))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTH_CSRF_INVALID"));
        mockMvc.perform(post("/api/product-categories")
                        .cookie(browser.sessionCookie())
                        .header(SessionCookies.CSRF_HEADER, browser.csrfToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(category))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTH_CSRF_INVALID"));
        mockMvc.perform(post("/api/product-categories")
                        .cookie(browser.sessionCookie(), browser.csrfCookie())
                        .header(SessionCookies.CSRF_HEADER, browser.csrfToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(category))
                .andExpect(status().isCreated());
    }

    @Test
    void logoutRevokesTheSessionAndExpiresBothCookies() throws Exception {
        BrowserSession browser = BrowserSession.from(login());

        MvcResult logout = mockMvc.perform(post("/api/accounts/logout")
                        .cookie(browser.sessionCookie(), browser.csrfCookie())
                        .header(SessionCookies.CSRF_HEADER, browser.csrfToken()))
                .andExpect(status().isNoContent())
                .andReturn();

        assertThat(setCookie(logout, SessionCookies.SESSION_COOKIE)).contains("Max-Age=0");
        assertThat(setCookie(logout, SessionCookies.CSRF_COOKIE)).contains("Max-Age=0");
        mockMvc.perform(get("/api/accounts/session").cookie(browser.sessionCookie()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/audit").cookie(browser.sessionCookie()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidSessionCookieIsExpiredByTheServer() throws Exception {
        Cookie forged = new Cookie(SessionCookies.SESSION_COOKIE, "sessione-revocata-o-inventata");
        MvcResult probe = mockMvc.perform(get("/api/accounts/session").cookie(forged))
                .andExpect(status().isNoContent())
                .andReturn();
        MvcResult rejected = mockMvc.perform(get("/api/audit").cookie(forged))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_UNAUTHORIZED"))
                .andReturn();

        assertThat(setCookie(probe, SessionCookies.SESSION_COOKIE)).contains("Max-Age=0");
        assertThat(setCookie(rejected, SessionCookies.SESSION_COOKIE)).contains("Max-Age=0");
    }

    @Test
    void renewalRotatesTheCookieAndTheCsrfToken() throws Exception {
        BrowserSession browser = BrowserSession.from(login());

        MvcResult renewed = mockMvc.perform(post("/api/accounts/session/renew")
                        .cookie(browser.sessionCookie(), browser.csrfCookie())
                        .header(SessionCookies.CSRF_HEADER, browser.csrfToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "password": "%s" }
                                """.formatted(SUPER_ADMIN_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andReturn();
        BrowserSession rotated = BrowserSession.from(renewed);

        assertThat(rotated.sessionToken()).isNotEqualTo(browser.sessionToken());
        assertThat(rotated.csrfToken()).isNotEqualTo(browser.csrfToken());
        mockMvc.perform(get("/api/accounts/session").cookie(browser.sessionCookie()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/accounts/session").cookie(rotated.sessionCookie()))
                .andExpect(status().isOk());
    }

    private MvcResult login() throws Exception {
        return mockMvc.perform(post("/api/accounts/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s"
                                }
                                """.formatted(SUPER_ADMIN_USERNAME, SUPER_ADMIN_PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();
    }

    private static String setCookie(MvcResult result, String name) {
        List<String> headers = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
        return headers.stream()
                .filter(header -> header.startsWith(name + "="))
                .reduce((first, second) -> second)
                .orElseThrow(() -> new AssertionError("Set-Cookie assente per " + name + ": " + headers));
    }

    private record BrowserSession(String sessionToken, String csrfToken) {
        static BrowserSession from(MvcResult result) {
            return new BrowserSession(
                    result.getResponse().getCookie(SessionCookies.SESSION_COOKIE).getValue(),
                    result.getResponse().getCookie(SessionCookies.CSRF_COOKIE).getValue()
            );
        }

        Cookie sessionCookie() {
            return new Cookie(SessionCookies.SESSION_COOKIE, sessionToken);
        }

        Cookie csrfCookie() {
            return new Cookie(SessionCookies.CSRF_COOKIE, csrfToken);
        }
    }
}
