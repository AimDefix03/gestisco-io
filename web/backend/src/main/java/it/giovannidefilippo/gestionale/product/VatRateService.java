package it.giovannidefilippo.gestionale.product;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class VatRateService {
    private final VatRateRepository repository;

    VatRateService(VatRateRepository repository) {
        this.repository = repository;
    }

    public List<VatRateResponse> list() {
        return repository.findAllByOrderBySortOrderAscCodeAsc().stream().map(VatRateResponse::from).toList();
    }

    /** Aliquota assegnabile: il codice indicato, se attivo, oppure la predefinita quando il codice manca. */
    VatRate requireAssignable(String code) {
        if (code == null || code.isBlank()) {
            List<VatRate> defaults = repository.findByDefaultRateTrue();
            if (defaults.size() != 1) {
                throw new IllegalStateException("Configurazione IVA non valida: serve una sola aliquota predefinita.");
            }
            return defaults.get(0);
        }
        VatRate vatRate = repository.findByCodeIgnoreCase(code.trim())
                .orElseThrow(() -> new IllegalArgumentException("Aliquota IVA " + code.trim() + " inesistente."));
        if (!vatRate.isActive()) {
            throw new IllegalArgumentException("L'aliquota IVA " + vatRate.getCode() + " non e piu utilizzabile.");
        }
        return vatRate;
    }
}
