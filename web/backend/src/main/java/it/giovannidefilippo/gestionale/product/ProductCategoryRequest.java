package it.giovannidefilippo.gestionale.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProductCategoryRequest(
        @NotBlank @Size(max = 64) @Pattern(regexp = "[A-Za-z0-9][A-Za-z0-9_-]*", message = "usare lettere, numeri, trattino o trattino basso") String code,
        @NotBlank @Size(max = 120) String name,
        String parentCode,
        Boolean active
) {
}
