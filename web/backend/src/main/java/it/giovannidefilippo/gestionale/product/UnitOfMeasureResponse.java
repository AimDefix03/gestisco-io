package it.giovannidefilippo.gestionale.product;

public record UnitOfMeasureResponse(
        String code,
        String label,
        String symbol,
        boolean active,
        boolean base,
        long productCount
) {
    static UnitOfMeasureResponse from(UnitOfMeasure unit, long productCount) {
        return new UnitOfMeasureResponse(unit.getCode(), unit.getLabel(), unit.getSymbol(), unit.isActive(), unit.isBase(), productCount);
    }
}
