package it.giovannidefilippo.gestionale.dashboard;

import it.giovannidefilippo.gestionale.user.AuthSessionService;
import it.giovannidefilippo.gestionale.user.AuthenticatedUser;
import it.giovannidefilippo.gestionale.user.SessionCookies;
import it.giovannidefilippo.gestionale.user.UserPermission;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
class DashboardController {
    private final DashboardService dashboardService;
    private final AuthSessionService authSessionService;

    DashboardController(DashboardService dashboardService, AuthSessionService authSessionService) {
        this.dashboardService = dashboardService;
        this.authSessionService = authSessionService;
    }

    @GetMapping
    DashboardResponse summary(@CookieValue(value = SessionCookies.SESSION_COOKIE, required = false) String token) {
        AuthenticatedUser actor = authSessionService.requirePermission(token, UserPermission.VIEW_REPORTS);
        return dashboardService.summary(actor);
    }
}
