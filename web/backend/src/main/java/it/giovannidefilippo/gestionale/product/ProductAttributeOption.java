package it.giovannidefilippo.gestionale.product;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Locale;

@Embeddable
public class ProductAttributeOption {
    @Column(name = "option_value", nullable = false, length = 120)
    private String value;

    @Column(name = "option_value_canonical", nullable = false, length = 120)
    private String valueCanonical;

    protected ProductAttributeOption() {
    }

    ProductAttributeOption(String value) {
        this.value = value.trim();
        this.valueCanonical = canonical(value);
    }

    static String canonical(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    public String getValue() {
        return value;
    }

    public String getValueCanonical() {
        return valueCanonical;
    }
}
