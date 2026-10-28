package it.giovannidefilippo.gestionale.product;

import java.math.BigDecimal;
import java.util.List;

public record ProductResponse(
        Long id,
        String code,
        String name,
        String description,
        String category,
        String categoryName,
        String brand,
        String productType,
        String usageContext,
        String barcode,
        String unitOfMeasure,
        String unitLabel,
        String vatRateCode,
        BigDecimal vatRate,
        String vatLabel,
        List<ProductAttributeValueResponse> attributes,
        int quantity,
        int reservedQuantity,
        int availableQuantity,
        BigDecimal price,
        BigDecimal discount,
        BigDecimal discountedPrice,
        BigDecimal lastPurchaseCost,
        BigDecimal averagePurchaseCost,
        int costedQuantity,
        int uncostedQuantity,
        BigDecimal costCoveragePercentage,
        BigDecimal knownInventoryCost,
        BigDecimal potentialGrossMarginOnCostedStock,
        boolean discontinued,
        ProductCapabilities capabilities
) {
}
