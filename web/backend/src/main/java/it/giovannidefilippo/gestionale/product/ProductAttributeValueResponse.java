package it.giovannidefilippo.gestionale.product;

public record ProductAttributeValueResponse(
        String code,
        String label,
        ProductAttributeType type,
        String value,
        boolean active
) {
}
