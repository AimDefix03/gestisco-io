package it.giovannidefilippo.gestionale.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UnitOfMeasureRequest(
        @NotBlank @Size(max = 16) @Pattern(regexp = "[A-Za-z0-9][A-Za-z0-9_]*", message = "usare lettere, numeri o trattino basso") String code,
        @NotBlank @Size(max = 60) String label,
        @NotBlank @Size(max = 12) String symbol,
        Boolean active
) {
}
