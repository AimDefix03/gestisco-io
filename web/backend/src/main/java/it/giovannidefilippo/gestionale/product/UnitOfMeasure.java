package it.giovannidefilippo.gestionale.product;

public enum UnitOfMeasure {
    PIECE("Pezzo", "pz"),
    PAIR("Paio", "paia"),
    PACK("Confezione", "conf"),
    KILOGRAM("Chilogrammo", "kg"),
    METER("Metro", "m"),
    LITER("Litro", "l");

    private final String label;
    private final String symbol;

    UnitOfMeasure(String label, String symbol) {
        this.label = label;
        this.symbol = symbol;
    }

    public String getLabel() {
        return label;
    }

    public String getSymbol() {
        return symbol;
    }
}
