package it.giovannidefilippo.gestionale.product;

import it.giovannidefilippo.gestionale.user.AuthSessionService;
import it.giovannidefilippo.gestionale.user.AuthenticatedUser;
import it.giovannidefilippo.gestionale.user.SessionCookies;
import it.giovannidefilippo.gestionale.user.UserPermission;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/units-of-measure")
class UnitOfMeasureController {
    private final UnitOfMeasureService service;
    private final AuthSessionService authSessionService;

    UnitOfMeasureController(UnitOfMeasureService service, AuthSessionService authSessionService) {
        this.service = service;
        this.authSessionService = authSessionService;
    }

    @GetMapping
    List<UnitOfMeasureResponse> findAll(
            @CookieValue(value = SessionCookies.SESSION_COOKIE, required = false) String token,
            @RequestParam(defaultValue = "false") boolean includeInactive
    ) {
        AuthenticatedUser actor = authSessionService.requirePermission(token, UserPermission.VIEW_CATALOG);
        return service.list(includeInactive && actor.hasPermission(UserPermission.MANAGE_PRODUCTS));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    UnitOfMeasureResponse create(@Valid @RequestBody UnitOfMeasureRequest request, @CookieValue(value = SessionCookies.SESSION_COOKIE, required = false) String token) {
        AuthenticatedUser actor = authSessionService.requirePermission(token, UserPermission.MANAGE_PRODUCTS);
        return service.create(request, actor);
    }

    @PutMapping("/{code}")
    UnitOfMeasureResponse update(@PathVariable String code, @Valid @RequestBody UnitOfMeasureRequest request, @CookieValue(value = SessionCookies.SESSION_COOKIE, required = false) String token) {
        AuthenticatedUser actor = authSessionService.requirePermission(token, UserPermission.MANAGE_PRODUCTS);
        return service.update(code, request, actor);
    }
}
