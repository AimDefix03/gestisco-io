package it.giovannidefilippo.gestionale.product;

import it.giovannidefilippo.gestionale.audit.AuditCategory;
import it.giovannidefilippo.gestionale.audit.AuditService;
import it.giovannidefilippo.gestionale.audit.AuditSeverity;
import it.giovannidefilippo.gestionale.common.ResourceConflictException;
import it.giovannidefilippo.gestionale.common.TimeProvider;
import it.giovannidefilippo.gestionale.user.AuthenticatedUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class UnitOfMeasureService {
    private final UnitOfMeasureRepository repository;
    private final AuditService auditService;
    private final TimeProvider timeProvider;

    UnitOfMeasureService(UnitOfMeasureRepository repository, AuditService auditService, TimeProvider timeProvider) {
        this.repository = repository;
        this.auditService = auditService;
        this.timeProvider = timeProvider;
    }

    /** Unita base prima, poi in ordine di etichetta; il conteggio dei prodotti aiuta a decidere le disattivazioni. */
    public List<UnitOfMeasureResponse> list(boolean includeInactive) {
        Map<String, Long> productCounts = new HashMap<>();
        for (Object[] row : repository.countProductsByCode()) {
            productCounts.put((String) row[0], (Long) row[1]);
        }
        return repository.findAll().stream()
                .filter(unit -> includeInactive || unit.isActive())
                .sorted(Comparator.comparing((UnitOfMeasure unit) -> !unit.isBase())
                        .thenComparing(UnitOfMeasure::getLabel, String.CASE_INSENSITIVE_ORDER))
                .map(unit -> UnitOfMeasureResponse.from(unit, productCounts.getOrDefault(unit.getCode(), 0L)))
                .toList();
    }

    @Transactional
    public UnitOfMeasureResponse create(UnitOfMeasureRequest request, AuthenticatedUser actor) {
        String code = UnitOfMeasure.canonicalCode(request.code());
        if (repository.existsByCode(code)) {
            throw new ResourceConflictException("Esiste gia un'unita di misura con codice " + code + ".");
        }
        if (repository.existsByLabelIgnoreCase(request.label().trim())) {
            throw new ResourceConflictException("Esiste gia un'unita di misura \"" + request.label().trim() + "\".");
        }
        LocalDateTime now = timeProvider.localDateTime();
        UnitOfMeasure unit = new UnitOfMeasure(code, request.label(), request.symbol(), now);
        if (Boolean.FALSE.equals(request.active())) {
            unit.update(request.label(), request.symbol(), false, now);
        }
        unit = repository.saveAndFlush(unit);
        auditService.record(actor.username(), actor.roleLabel(), "CREATE_UNIT_OF_MEASURE", unit.getCode(),
                "Creata unita di misura " + describe(unit), AuditCategory.PRODUCT, AuditSeverity.INFO, "UNIT_OF_MEASURE");
        return UnitOfMeasureResponse.from(unit, 0);
    }

    @Transactional
    public UnitOfMeasureResponse update(String code, UnitOfMeasureRequest request, AuthenticatedUser actor) {
        UnitOfMeasure unit = requireByCode(code);
        if (!unit.getCode().equals(UnitOfMeasure.canonicalCode(request.code()))) {
            throw new IllegalArgumentException("Il codice di un'unita di misura non puo essere modificato.");
        }
        if (repository.existsByLabelIgnoreCaseAndIdNot(request.label().trim(), unit.getId())) {
            throw new ResourceConflictException("Esiste gia un'unita di misura \"" + request.label().trim() + "\".");
        }
        boolean active = request.active() == null || request.active();
        if (!active && unit.isBase()) {
            throw new IllegalArgumentException("L'unita base " + unit.getLabel() + " non puo essere disattivata.");
        }
        String before = describe(unit);
        unit.update(request.label(), request.symbol(), active, timeProvider.localDateTime());
        repository.flush();
        auditService.record(actor.username(), actor.roleLabel(), "UPDATE_UNIT_OF_MEASURE", unit.getCode(),
                "Unita di misura " + before + " -> " + describe(unit), AuditCategory.PRODUCT, AuditSeverity.INFO, "UNIT_OF_MEASURE");
        return UnitOfMeasureResponse.from(unit, productCount(unit));
    }

    /** Unita assegnabile a un prodotto: il codice indicato se attivo, oppure l'unita base quando il codice manca. */
    UnitOfMeasure requireAssignable(String code) {
        UnitOfMeasure unit = requireByCode(code == null || code.isBlank() ? UnitOfMeasure.BASE_CODE : code);
        if (!unit.isActive()) {
            throw new IllegalArgumentException("L'unita di misura " + unit.getLabel() + " e disattivata.");
        }
        return unit;
    }

    private UnitOfMeasure requireByCode(String code) {
        return repository.findByCode(UnitOfMeasure.canonicalCode(code))
                .orElseThrow(() -> new IllegalArgumentException("Unita di misura " + code.trim() + " inesistente."));
    }

    private long productCount(UnitOfMeasure unit) {
        return repository.countProductsByCode().stream()
                .filter(row -> unit.getCode().equals(row[0]))
                .mapToLong(row -> (Long) row[1])
                .findFirst()
                .orElse(0L);
    }

    private static String describe(UnitOfMeasure unit) {
        return unit.getLabel() + " (" + unit.getSymbol() + ")" + (unit.isActive() ? "" : " disattivata");
    }
}
