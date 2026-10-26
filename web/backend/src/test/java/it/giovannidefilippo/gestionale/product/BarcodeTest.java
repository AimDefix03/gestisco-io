package it.giovannidefilippo.gestionale.product;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BarcodeTest {
    @ParameterizedTest
    @ValueSource(strings = {"4006381333931", "96385074", "036000291452", "8001234567897"})
    void acceptsGtinWithCorrectCheckDigit(String gtin) {
        assertThat(Barcode.normalize(gtin)).isEqualTo(gtin);
    }

    @ParameterizedTest
    @ValueSource(strings = {"4006381333932", "96385075", "036000291453", "8001234567890"})
    void rejectsGtinWithWrongCheckDigit(String gtin) {
        assertThatThrownBy(() -> Barcode.normalize(gtin))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cifra di controllo");
    }

    @Test
    void acceptsInternalCodesAndTrims() {
        assertThat(Barcode.normalize("  SKU-ABC_01.2 ")).isEqualTo("SKU-ABC_01.2");
        assertThat(Barcode.normalize("1234567")).isEqualTo("1234567");
        assertThat(Barcode.normalize("12345678901234")).isEqualTo("12345678901234");
    }

    @Test
    void treatsBlankAsAbsentAndRejectsUnsafeValues() {
        assertThat(Barcode.normalize(null)).isNull();
        assertThat(Barcode.normalize("   ")).isNull();
        assertThatThrownBy(() -> Barcode.normalize("ABC 123")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Barcode.normalize("-ABC")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Barcode.normalize("A".repeat(33))).isInstanceOf(IllegalArgumentException.class);
    }
}
