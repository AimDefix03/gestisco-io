package it.giovannidefilippo.gestionale.product;

import java.math.BigDecimal;

public record VatRateResponse(
        String code,
        BigDecimal rate,
        String nature,
        String description,
        boolean defaultRate,
        boolean active
) {
    static VatRateResponse from(VatRate vatRate) {
        return new VatRateResponse(vatRate.getCode(), vatRate.getRate(), vatRate.getNature(), vatRate.getDescription(), vatRate.isDefaultRate(), vatRate.isActive());
    }
}
