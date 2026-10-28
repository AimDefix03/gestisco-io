package it.giovannidefilippo.gestionale.dashboard;

import it.giovannidefilippo.gestionale.user.AuthSessionService;
import it.giovannidefilippo.gestionale.user.AuthenticatedUser;
import it.giovannidefilippo.gestionale.user.SessionCookies;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer/dashboard")
class CustomerDashboardController {
    private final DashboardService dashboardService;
    private final AuthSessionService authSessionService;

    CustomerDashboardController(DashboardService dashboardService, AuthSessionService authSessionService) {
        this.dashboardService = dashboardService;
        this.authSessionService = authSessionService;
    }

    @GetMapping
    CustomerDashboardResponse summary(@CookieValue(value = SessionCookies.SESSION_COOKIE, required = false) String token) {
        AuthenticatedUser actor = authSessionService.requireCustomer(token);
        return dashboardService.customerSummary(actor);
    }
}
