package it.giovannidefilippo.gestionale.product;

import java.util.List;

public record ProductAttributeResponse(
        String code,
        String label,
        ProductAttributeType type,
        String categoryCode,
        String categoryPath,
        List<String> options,
        boolean active
) {
    static ProductAttributeResponse from(ProductAttribute attribute) {
        return new ProductAttributeResponse(
                attribute.getCode(),
                attribute.getLabel(),
                attribute.getType(),
                attribute.getCategory() == null ? null : attribute.getCategory().getCode(),
                attribute.getCategory() == null ? null : attribute.getCategory().getPath(),
                attribute.getOptionValues(),
                attribute.isActive()
        );
    }
}
