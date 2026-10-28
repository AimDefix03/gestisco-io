package it.giovannidefilippo.gestionale.product;

import java.util.Comparator;
import java.util.List;

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
                product.getVatRate().getCode(),
                product.getVatRate().getRate(),
                product.getVatRate().getDescription(),
                attributes(product),
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

    static List<ProductAttributeValueResponse> attributes(Product product) {
        return product.getAttributeValues().entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().getLabel(), String.CASE_INSENSITIVE_ORDER))
                .map(entry -> new ProductAttributeValueResponse(
                        entry.getKey().getCode(),
                        entry.getKey().getLabel(),
                        entry.getKey().getType(),
                        entry.getValue(),
                        entry.getKey().isActive()
                ))
                .toList();
    }
}
