package it.giovannidefilippo.gestionale.security;

import it.giovannidefilippo.gestionale.common.ApiErrorCode;
import it.giovannidefilippo.gestionale.common.UnauthorizedException;
import it.giovannidefilippo.gestionale.user.AuthSessionService;
import it.giovannidefilippo.gestionale.user.AuthenticatedUser;
import it.giovannidefilippo.gestionale.user.SessionCookies;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
class SessionAuthenticationFilter extends OncePerRequestFilter {
    private final AuthSessionService authSessionService;
    private final SessionCookies sessionCookies;
    private final SecurityErrorWriter errorWriter;

    SessionAuthenticationFilter(AuthSessionService authSessionService, SessionCookies sessionCookies, SecurityErrorWriter errorWriter) {
        this.authSessionService = authSessionService;
        this.sessionCookies = sessionCookies;
        this.errorWriter = errorWriter;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        String method = request.getMethod();
        return path.equals("/api/accounts/login")
                || path.equals("/api/accounts/register")
                || path.equals("/api/accounts/password-strength")
                || (path.equals("/api/accounts/session") && HttpMethod.GET.matches(method))
                || path.equals("/actuator/health/liveness")
                || path.equals("/actuator/health/readiness")
                || path.equals("/actuator/prometheus")
                || (HttpMethod.OPTIONS.matches(method));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = sessionToken(request);
        if (token == null || token.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            AuthenticatedUser user = authSessionService.require(token);
            var authorities = user.role().getPermissions().stream()
                    .map(permission -> new SimpleGrantedAuthority(permission.name()))
                    .toList();
            var authentication = new UsernamePasswordAuthenticationToken(user.username(), null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (UnauthorizedException exception) {
            SecurityContextHolder.clearContext();
            sessionCookies.close(request, response);
            errorWriter.write(request, response, HttpStatus.UNAUTHORIZED, ApiErrorCode.AUTH_UNAUTHORIZED, exception.getMessage());
        }
    }

    private static String sessionToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        return Arrays.stream(cookies)
                .filter(cookie -> SessionCookies.SESSION_COOKIE.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
