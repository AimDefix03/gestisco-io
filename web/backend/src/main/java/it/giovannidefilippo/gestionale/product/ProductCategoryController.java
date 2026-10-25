package it.giovannidefilippo.gestionale.product;

import it.giovannidefilippo.gestionale.user.AuthSessionService;
import it.giovannidefilippo.gestionale.user.AuthenticatedUser;
import it.giovannidefilippo.gestionale.user.UserPermission;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/product-categories")
class ProductCategoryController {
    private final ProductCategoryService service;
    private final AuthSessionService authSessionService;

    ProductCategoryController(ProductCategoryService service, AuthSessionService authSessionService) {
        this.service = service;
        this.authSessionService = authSessionService;
    }

    @GetMapping
    List<ProductCategoryResponse> findAll(
            @RequestHeader(value = "X-Session-Token", required = false) String token,
            @RequestParam(defaultValue = "false") boolean includeInactive
    ) {
        AuthenticatedUser actor = authSessionService.requirePermission(token, UserPermission.VIEW_CATALOG);
        return service.list(includeInactive && actor.hasPermission(UserPermission.MANAGE_PRODUCTS));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ProductCategoryResponse create(@Valid @RequestBody ProductCategoryRequest request, @RequestHeader(value = "X-Session-Token", required = false) String token) {
        AuthenticatedUser actor = authSessionService.requirePermission(token, UserPermission.MANAGE_PRODUCTS);
        return service.create(request, actor);
    }

    @PutMapping("/{code}")
    ProductCategoryResponse update(@PathVariable String code, @Valid @RequestBody ProductCategoryRequest request, @RequestHeader(value = "X-Session-Token", required = false) String token) {
        AuthenticatedUser actor = authSessionService.requirePermission(token, UserPermission.MANAGE_PRODUCTS);
        return service.update(code, request, actor);
    }
}
