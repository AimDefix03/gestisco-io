package it.giovannidefilippo.gestionale.product;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Map;

public record ProductRequest(
        @NotBlank String code,
        @NotBlank String name,
        @NotBlank String description,
        @NotBlank String category,
        @NotBlank String brand,
        @NotBlank String productType,
        String usageContext,
        @NotNull @DecimalMin("0.00") BigDecimal price,
        @NotNull @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal discount,
        @Size(max = Barcode.MAX_LENGTH) String barcode,
        UnitOfMeasure unitOfMeasure,
        @Size(max = 16) String vatRateCode,
        @Size(max = 100) Map<String, String> attributes
) {
    public ProductRequest(String code, String name, String description, String category, String brand,
                          String productType, String usageContext, BigDecimal price, BigDecimal discount) {
        this(code, name, description, category, brand, productType, usageContext, price, discount, null, null, null, null);
    }

    public ProductRequest(String code, String name, String description, String category, String brand,
                          String productType, String usageContext, BigDecimal price, BigDecimal discount,
                          String barcode, UnitOfMeasure unitOfMeasure, String vatRateCode) {
        this(code, name, description, category, brand, productType, usageContext, price, discount, barcode, unitOfMeasure, vatRateCode, null);
    }
}
