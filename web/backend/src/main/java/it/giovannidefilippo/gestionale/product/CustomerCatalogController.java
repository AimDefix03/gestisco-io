package it.giovannidefilippo.gestionale.product;

import it.giovannidefilippo.gestionale.common.PageResponse;
import it.giovannidefilippo.gestionale.user.AuthSessionService;
import it.giovannidefilippo.gestionale.user.SessionCookies;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer/catalog")
class CustomerCatalogController {
    private final ProductService productService;
    private final AuthSessionService authSessionService;

    CustomerCatalogController(ProductService productService, AuthSessionService authSessionService) {
        this.productService = productService;
        this.authSessionService = authSessionService;
    }

    @GetMapping
    PageResponse<CustomerProductResponse> findAll(
            @CookieValue(value = SessionCookies.SESSION_COOKIE, required = false) String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String productType,
            @RequestParam(required = false) String stock,
            @RequestParam(required = false) String sort
    ) {
        authSessionService.requireCustomer(token);
        return productService.searchCustomerCatalog(q, category, brand, productType, stock, sort, page, size);
    }
}
