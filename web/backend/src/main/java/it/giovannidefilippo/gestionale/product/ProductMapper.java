package it.giovannidefilippo.gestionale.product;

final class ProductMapper {
    private ProductMapper() {
    }

    static ProductResponse toResponse(Product product) {
        return toResponse(product, ProductCapabilities.none());
    }

    static ProductResponse toResponse(Product product, ProductCapabilities capabilities) {
        return new ProductResponse(
                product.getId(),
                product.getCode(),
                product.getName(),
                product.getDescription(),
                product.getCategory().getCode(),
                product.getCategory().getPath(),
                product.getBrand(),
                product.getProductType(),
                product.getUsageContext(),
                product.getBarcode(),
                product.getUnitOfMeasure(),
                product.getUnitOfMeasure().getLabel(),
                product.getQuantity(),
                product.getReservedQuantity(),
                product.getAvailableQuantity(),
                product.getPrice(),
                product.getDiscount(),
                product.getDiscountedPrice(),
                product.getLastPurchaseCost(),
                product.getAveragePurchaseCost(),
                product.getCostedQuantity(),
                product.getUncostedQuantity(),
                product.getCostCoveragePercentage(),
                product.getKnownInventoryCost(),
                product.getPotentialGrossMarginOnCostedStock(),
                product.isDiscontinued(),
                capabilities
        );
    }
}
