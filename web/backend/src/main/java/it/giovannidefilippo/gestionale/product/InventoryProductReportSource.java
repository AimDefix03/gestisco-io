package it.giovannidefilippo.gestionale.product;

import java.math.BigDecimal;
import java.util.Map;

public record InventoryProductReportSource(
        String code,
        String name,
        String category,
        String categoryName,
        String brand,
        String productType,
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
        Map<String, String> attributes
) {
    public InventoryProductReportSource(String code, String name, String category, String categoryName, String brand, String productType,
                                        int quantity, int reservedQuantity, int availableQuantity, BigDecimal price, BigDecimal discount,
                                        BigDecimal discountedPrice, BigDecimal lastPurchaseCost, BigDecimal averagePurchaseCost,
                                        int costedQuantity, int uncostedQuantity, BigDecimal costCoveragePercentage,
                                        BigDecimal knownInventoryCost, BigDecimal potentialGrossMarginOnCostedStock, boolean discontinued) {
        this(code, name, category, categoryName, brand, productType, quantity, reservedQuantity, availableQuantity, price, discount,
                discountedPrice, lastPurchaseCost, averagePurchaseCost, costedQuantity, uncostedQuantity, costCoveragePercentage,
                knownInventoryCost, potentialGrossMarginOnCostedStock, discontinued, Map.of());
    }
}
