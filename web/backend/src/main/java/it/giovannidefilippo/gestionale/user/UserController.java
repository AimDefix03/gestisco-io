package it.giovannidefilippo.gestionale.user;

import it.giovannidefilippo.gestionale.common.PageResponse;
import it.giovannidefilippo.gestionale.common.OperationalMetrics;
import it.giovannidefilippo.gestionale.common.UnauthorizedException;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
class UserController {
    private final UserService userService;
    private final AuthSessionService authSessionService;
    private final PasswordStrengthService passwordStrengthService;
    private final OperationalMetrics operationalMetrics;
    private final AccountSecurityReviewService accountSecurityReviewService;
    private final SessionCookies sessionCookies;

    UserController(
            UserService userService,
            AuthSessionService authSessionService,
            PasswordStrengthService passwordStrengthService,
            OperationalMetrics operationalMetrics,
            AccountSecurityReviewService accountSecurityReviewService,
            SessionCookies sessionCookies
    ) {
        this.userService = userService;
        this.authSessionService = authSessionService;
        this.passwordStrengthService = passwordStrengthService;
        this.operationalMetrics = operationalMetrics;
        this.accountSecurityReviewService = accountSecurityReviewService;
        this.sessionCookies = sessionCookies;
    }

    @PostMapping("/login")
    SessionResponse login(@Valid @RequestBody UserRequests.LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse response) {
        try {
            authSessionService.assertLoginAllowed(request.username());
            UserResponse authentication = userService.login(request.username(), request.password());
            authSessionService.clearFailedLogin(request.username());
            preventCaching(response);
            AuthSessionResponse session = authSessionService.create(authentication);
            sessionCookies.open(session.token(), httpRequest, response);
            operationalMetrics.recordAuthentication(OperationalMetrics.AuthenticationOutcome.SUCCESS);
            return SessionResponse.from(session);
        } catch (IllegalArgumentException exception) {
            operationalMetrics.recordAuthentication(OperationalMetrics.AuthenticationOutcome.INVALID_CREDENTIALS);
            authSessionService.registerFailedLogin(request.username());
            throw exception;
        } catch (UnauthorizedException exception) {
            operationalMetrics.recordAuthentication(OperationalMetrics.AuthenticationOutcome.LOCKED);
            throw exception;
        }
    }

    /** Ripristino dopo una ricarica: 204 senza sessione valida, cosi il caricamento non autenticato non produce errori. */
    @GetMapping("/session")
    ResponseEntity<SessionResponse> currentSession(
            @CookieValue(value = SessionCookies.SESSION_COOKIE, required = false) String token,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        preventCaching(response);
        if (token == null || token.isBlank()) {
            return ResponseEntity.noContent().build();
        }
        try {
            SessionResponse session = authSessionService.current(token);
            sessionCookies.ensureCsrfToken(httpRequest, response);
            return ResponseEntity.ok(session);
        } catch (UnauthorizedException exception) {
            sessionCookies.close(httpRequest, response);
            return ResponseEntity.noContent().build();
        }
    }

    @PostMapping("/session/renew")
    SessionResponse renewSession(
            @Valid @RequestBody UserRequests.PasswordRequest request,
            @CookieValue(value = SessionCookies.SESSION_COOKIE, required = false) String token,
            Authentication authentication,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        try {
            userService.verifyPassword(authentication.getName(), request.password());
            preventCaching(response);
            AuthSessionResponse session = authSessionService.rotate(token);
            sessionCookies.open(session.token(), httpRequest, response);
            operationalMetrics.recordSessionEvent(OperationalMetrics.SessionEvent.RENEWED);
            return SessionResponse.from(session);
        } catch (RuntimeException exception) {
            operationalMetrics.recordSessionEvent(OperationalMetrics.SessionEvent.RENEWAL_FAILED);
            throw exception;
        }
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void logout(
            @CookieValue(value = SessionCookies.SESSION_COOKIE, required = false) String token,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        authSessionService.logout(token);
        sessionCookies.close(httpRequest, response);
        operationalMetrics.recordSessionEvent(OperationalMetrics.SessionEvent.LOGOUT);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    UserResponse register(@Valid @RequestBody UserRequests.RegisterRequest request) {
        return userService.registerPublic(request.username(), request.password());
    }

    @GetMapping
    PageResponse<UserResponse> findAll(
            @CookieValue(value = SessionCookies.SESSION_COOKIE, required = false) String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) Boolean enabled
    ) {
        authSessionService.requireManageAccounts(token);
        return userService.search(q, role, enabled, page, size);
    }

    @GetMapping("/security-review")
    AccountSecurityReviewReport securityReview(
            @CookieValue(value = SessionCookies.SESSION_COOKIE, required = false) String token
    ) {
        authSessionService.requireSuperAdmin(token);
        return accountSecurityReviewService.report();
    }

    @PostMapping("/security-review/{username}/verify")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void verifyOperationalAccount(
            @PathVariable String username,
            @CookieValue(value = SessionCookies.SESSION_COOKIE, required = false) String token,
            @RequestHeader(value = "X-Reauth-Password", required = false) String reauthPassword
    ) {
        AuthenticatedUser actor = authSessionService.requireSuperAdmin(token);
        requirePasswordConfirmation(actor, reauthPassword);
        accountSecurityReviewService.verifyOperationalAccount(username, actor.username());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    UserResponse create(
            @Valid @RequestBody UserRequests.CreateAccountRequest request,
            @CookieValue(value = SessionCookies.SESSION_COOKIE, required = false) String token,
            @RequestHeader(value = "X-Reauth-Password", required = false) String reauthPassword
    ) {
        AuthenticatedUser actor = authSessionService.requireManageAccounts(token);
        requirePasswordConfirmation(actor, reauthPassword);
        return userService.createAccount(request.username(), request.password(), request.role(), actor.username(), "Creazione da pannello admin");
    }

    @PostMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void changeOwnPassword(
            @Valid @RequestBody UserRequests.ChangePasswordRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        userService.changeOwnPassword(authentication.getName(), request.currentPassword(), request.newPassword());
        sessionCookies.close(httpRequest, response);
    }

    @PostMapping("/{username}/disable")
    UserResponse disable(
            @PathVariable String username,
            @Valid @RequestBody UserRequests.AccountStateRequest request,
            @CookieValue(value = SessionCookies.SESSION_COOKIE, required = false) String token,
            @RequestHeader(value = "X-Reauth-Password", required = false) String reauthPassword
    ) {
        AuthenticatedUser actor = authSessionService.requireManageAccounts(token);
        requirePasswordConfirmation(actor, reauthPassword);
        return userService.disable(username, request.reason(), actor.username());
    }

    @PostMapping("/{username}/enable")
    UserResponse enable(
            @PathVariable String username,
            @Valid @RequestBody UserRequests.AccountStateRequest request,
            @CookieValue(value = SessionCookies.SESSION_COOKIE, required = false) String token,
            @RequestHeader(value = "X-Reauth-Password", required = false) String reauthPassword
    ) {
        AuthenticatedUser actor = authSessionService.requireManageAccounts(token);
        requirePasswordConfirmation(actor, reauthPassword);
        return userService.enable(username, request.reason(), actor.username());
    }

    @PostMapping("/{username}/password-reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void resetPassword(
            @PathVariable String username,
            @Valid @RequestBody UserRequests.ResetPasswordRequest request,
            @CookieValue(value = SessionCookies.SESSION_COOKIE, required = false) String token,
            @RequestHeader(value = "X-Reauth-Password", required = false) String reauthPassword
    ) {
        AuthenticatedUser actor = authSessionService.requireManageAccounts(token);
        requirePasswordConfirmation(actor, reauthPassword);
        userService.resetPassword(username, request.newPassword(), request.reason(), actor.username());
    }

    @PostMapping("/{username}/sessions/revoke")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void revokeSessions(
            @PathVariable String username,
            @Valid @RequestBody UserRequests.RevokeSessionsRequest request,
            @CookieValue(value = SessionCookies.SESSION_COOKIE, required = false) String token,
            @RequestHeader(value = "X-Reauth-Password", required = false) String reauthPassword
    ) {
        AuthenticatedUser actor = authSessionService.requireManageAccounts(token);
        requirePasswordConfirmation(actor, reauthPassword);
        userService.revokeSessions(username, request.reason(), actor.username());
    }

    @PostMapping("/{username}/role")
    UserResponse changeRole(
            @PathVariable String username,
            @Valid @RequestBody UserRequests.ChangeRoleRequest request,
            @CookieValue(value = SessionCookies.SESSION_COOKIE, required = false) String token,
            @RequestHeader(value = "X-Reauth-Password", required = false) String reauthPassword
    ) {
        AuthenticatedUser actor = authSessionService.requireSuperAdmin(token);
        requirePasswordConfirmation(actor, reauthPassword);
        return userService.changeRole(username, request.role(), request.reason(), actor.username());
    }

    @DeleteMapping("/{username}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(
            @PathVariable String username,
            @CookieValue(value = SessionCookies.SESSION_COOKIE, required = false) String token,
            @RequestHeader(value = "X-Reauth-Password", required = false) String reauthPassword
    ) {
        AuthenticatedUser actor = authSessionService.requireManageAccounts(token);
        requirePasswordConfirmation(actor, reauthPassword);
        userService.delete(username, actor.username());
    }

    @PostMapping("/bulk-delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteMany(
            @RequestBody UserRequests.DeleteAccountsRequest request,
            @CookieValue(value = SessionCookies.SESSION_COOKIE, required = false) String token,
            @RequestHeader(value = "X-Reauth-Password", required = false) String reauthPassword
    ) {
        AuthenticatedUser actor = authSessionService.requireManageAccounts(token);
        requirePasswordConfirmation(actor, reauthPassword);
        userService.deleteMany(request.usernames(), actor.username());
    }

    @PostMapping("/password-strength")
    PasswordStrengthResponse passwordStrength(@Valid @RequestBody UserRequests.PasswordRequest request) {
        return passwordStrengthService.evaluate(request.password());
    }

    private void requirePasswordConfirmation(AuthenticatedUser actor, String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Conferma la password della sessione per continuare.");
        }
        userService.verifyPassword(actor.username(), password);
    }

    private void preventCaching(HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        response.setHeader(HttpHeaders.PRAGMA, "no-cache");
    }
}
