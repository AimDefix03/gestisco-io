package it.giovannidefilippo.gestionale.product;

public record ProductCategoryResponse(
        String code,
        String name,
        String path,
        String parentCode,
        int depth,
        boolean active
) {
    static ProductCategoryResponse from(ProductCategory category) {
        return new ProductCategoryResponse(
                category.getCode(),
                category.getName(),
                category.getPath(),
                category.getParent() == null ? null : category.getParent().getCode(),
                category.getDepth(),
                category.isActive()
        );
    }
}
