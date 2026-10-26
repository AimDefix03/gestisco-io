package it.giovannidefilippo.gestionale.document;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class VatSummaryCalculatorTest {
    @Test
    void groupsGrossLinesByRateAndNatureFromHighestRate() {
        List<FiscalDocumentVatSummary> summaries = VatSummaryCalculator.summarize(List.of(
                line("122.00", "0.2200", null),
                line("55.00", "0.1000", null),
                line("61.00", "0.22", null),
                line("20.00", "0.0000", "N4")
        ));

        assertThat(summaries).hasSize(3);
        assertSummary(summaries.get(0), "0.2200", null, "150.00", "33.00");
        assertSummary(summaries.get(1), "0.1000", null, "50.00", "5.00");
        assertSummary(summaries.get(2), "0.0000", "N4", "20.00", "0.00");
    }

    @Test
    void scorporatesOncePerRateInsteadOfPerLine() {
        List<FiscalDocumentLine> lines = new ArrayList<>();
        for (int index = 0; index < 7; index++) {
            lines.add(line("0.05", "0.2200", null));
        }

        FiscalDocumentVatSummary summary = VatSummaryCalculator.summarize(lines).get(0);

        // Riga per riga: 7 x 0.04 = 0.28 di imponibile; sul totale 0.35 / 1.22 = 0.29.
        assertSummary(summary, "0.2200", null, "0.29", "0.06");
    }

    @Test
    void taxablePlusVatAlwaysEqualsGrossForEveryRate() {
        Random random = new Random(20261026);
        String[] rates = {"0.2200", "0.1000", "0.0500", "0.0400"};
        for (int attempt = 0; attempt < 500; attempt++) {
            List<FiscalDocumentLine> lines = new ArrayList<>();
            BigDecimal gross = BigDecimal.ZERO;
            for (int index = 0; index < 1 + random.nextInt(6); index++) {
                BigDecimal amount = BigDecimal.valueOf(random.nextInt(100_000), 2);
                lines.add(line(amount.toPlainString(), rates[random.nextInt(rates.length)], null));
                gross = gross.add(amount);
            }
            BigDecimal summed = VatSummaryCalculator.summarize(lines).stream()
                    .map(FiscalDocumentVatSummary::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            assertThat(summed).isEqualByComparingTo(gross);
        }
    }

    private static FiscalDocumentLine line(String lineTotal, String vatRate, String nature) {
        return new FiscalDocumentLine("P", "Prodotto", 1, new BigDecimal(lineTotal), new BigDecimal(lineTotal), new BigDecimal(vatRate), nature);
    }

    private static void assertSummary(FiscalDocumentVatSummary summary, String rate, String nature, String taxable, String vat) {
        assertThat(summary.getVatRate()).isEqualByComparingTo(rate);
        assertThat(summary.getVatNature()).isEqualTo(nature);
        assertThat(summary.getTaxableAmount()).isEqualByComparingTo(taxable);
        assertThat(summary.getVatAmount()).isEqualByComparingTo(vat);
        assertThat(summary.getTotalAmount()).isEqualByComparingTo(new BigDecimal(taxable).add(new BigDecimal(vat)));
    }
}
