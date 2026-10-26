package it.giovannidefilippo.gestionale.document;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Riepilogo IVA da righe con prezzi IVA inclusa: per ogni coppia aliquota/natura il totale lordo
 * viene scorporato una sola volta e arrotondato al centesimo (half-up). Regola da confermare con
 * il consulente fiscale prima dell'emissione di documenti reali (fase P8).
 */
final class VatSummaryCalculator {
    private VatSummaryCalculator() {
    }

    static List<FiscalDocumentVatSummary> summarize(List<FiscalDocumentLine> lines) {
        Map<Key, BigDecimal> grossByRate = new LinkedHashMap<>();
        for (FiscalDocumentLine line : lines) {
            grossByRate.merge(new Key(line.getVatRate().stripTrailingZeros(), line.getVatNature()), line.getLineTotal(), BigDecimal::add);
        }
        List<FiscalDocumentVatSummary> summaries = new ArrayList<>();
        grossByRate.entrySet().stream()
                .sorted(Comparator.comparing((Map.Entry<Key, BigDecimal> entry) -> entry.getKey().rate()).reversed()
                        .thenComparing(entry -> Objects.toString(entry.getKey().nature(), "")))
                .forEach(entry -> {
                    BigDecimal gross = entry.getValue().setScale(2, RoundingMode.HALF_UP);
                    BigDecimal taxable = gross.divide(BigDecimal.ONE.add(entry.getKey().rate()), 2, RoundingMode.HALF_UP);
                    summaries.add(new FiscalDocumentVatSummary(entry.getKey().rate().setScale(4, RoundingMode.UNNECESSARY), entry.getKey().nature(), taxable, gross.subtract(taxable)));
                });
        return summaries;
    }

    private record Key(BigDecimal rate, String nature) {
    }
}
