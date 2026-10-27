package it.giovannidefilippo.gestionale.product;

import it.giovannidefilippo.gestionale.user.AuthSessionService;
import it.giovannidefilippo.gestionale.user.SessionCookies;
import it.giovannidefilippo.gestionale.user.UserPermission;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vat-rates")
class VatRateController {
    private final VatRateService service;
    private final AuthSessionService authSessionService;

    VatRateController(VatRateService service, AuthSessionService authSessionService) {
        this.service = service;
        this.authSessionService = authSessionService;
    }

    @GetMapping
    List<VatRateResponse> findAll(@CookieValue(value = SessionCookies.SESSION_COOKIE, required = false) String token) {
        authSessionService.requirePermission(token, UserPermission.MANAGE_INVENTORY);
        return service.list();
    }
}
