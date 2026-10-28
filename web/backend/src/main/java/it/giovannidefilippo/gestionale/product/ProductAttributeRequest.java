package it.giovannidefilippo.gestionale.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ProductAttributeRequest(
        @NotBlank @Size(max = 64) @Pattern(regexp = "[A-Za-z0-9][A-Za-z0-9_-]*", message = "usare lettere, numeri, trattino o trattino basso") String code,
        @NotBlank @Size(max = 120) String label,
        @NotNull ProductAttributeType type,
        String categoryCode,
        @Size(max = ProductAttributeService.MAX_OPTIONS) List<@NotBlank @Size(max = 120) String> options,
        Boolean active
) {
}
