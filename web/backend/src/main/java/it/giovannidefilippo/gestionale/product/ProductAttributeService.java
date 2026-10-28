package it.giovannidefilippo.gestionale.product;

import it.giovannidefilippo.gestionale.audit.AuditCategory;
import it.giovannidefilippo.gestionale.audit.AuditService;
import it.giovannidefilippo.gestionale.audit.AuditSeverity;
import it.giovannidefilippo.gestionale.common.BusinessIdentifierCanonicalizer;
import it.giovannidefilippo.gestionale.common.ResourceConflictException;
import it.giovannidefilippo.gestionale.common.TimeProvider;
import it.giovannidefilippo.gestionale.user.AuthenticatedUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class ProductAttributeService {
    static final int MAX_OPTIONS = 50;
    static final int MAX_VALUE_LENGTH = 200;

    private final ProductAttributeRepository repository;
    private final ProductCategoryRepository categoryRepository;
    private final AuditService auditService;
    private final TimeProvider timeProvider;

    ProductAttributeService(ProductAttributeRepository repository, ProductCategoryRepository categoryRepository, AuditService auditService, TimeProvider timeProvider) {
        this.repository = repository;
        this.categoryRepository = categoryRepository;
        this.auditService = auditService;
        this.timeProvider = timeProvider;
    }

    public List<ProductAttributeResponse> list(boolean includeInactive) {
        return repository.findAll().stream()
                .filter(attribute -> includeInactive || attribute.isActive())
                .sorted(Comparator.comparing(ProductAttribute::getLabel, String.CASE_INSENSITIVE_ORDER))
                .map(ProductAttributeResponse::from)
                .toList();
    }

    @Transactional
    public ProductAttributeResponse create(ProductAttributeRequest request, AuthenticatedUser actor) {
        String canonicalCode = BusinessIdentifierCanonicalizer.canonical(request.code());
        if (repository.existsByCodeCanonical(canonicalCode)) {
            throw new ResourceConflictException("Esiste gia un attributo con codice " + request.code().trim() + ".");
        }
        List<String> options = validOptions(request.type(), request.options());
        ProductCategory category = optionalCategory(request.categoryCode(), true);
        LocalDateTime now = timeProvider.localDateTime();
        ProductAttribute attribute = repository.saveAndFlush(new ProductAttribute(request.code(), request.label(), request.type(), category, options, now));
        if (Boolean.FALSE.equals(request.active())) {
            attribute.update(attribute.getLabel(), category, options, false, now);
        }
        auditService.record(actor.username(), actor.roleLabel(), "CREATE_PRODUCT_ATTRIBUTE", attribute.getCode(),
                "Creato attributo " + describe(attribute), AuditCategory.PRODUCT, AuditSeverity.INFO, "PRODUCT_ATTRIBUTE");
        return ProductAttributeResponse.from(attribute);
    }

    @Transactional
    public ProductAttributeResponse update(String code, ProductAttributeRequest request, AuthenticatedUser actor) {
        ProductAttribute attribute = requireByCode(code);
        if (!attribute.getCodeCanonical().equals(BusinessIdentifierCanonicalizer.canonical(request.code()))) {
            throw new IllegalArgumentException("Il codice di un attributo non puo essere modificato.");
        }
        if (attribute.getType() != request.type()) {
            throw new IllegalArgumentException("Il tipo di un attributo non puo essere modificato: crearne uno nuovo.");
        }
        List<String> options = validOptions(request.type(), request.options());
        ProductCategory category = optionalCategory(request.categoryCode(), !sameCategory(attribute.getCategory(), request.categoryCode()));
        validateValuesInUse(attribute, category, options);
        String before = describe(attribute);
        attribute.update(request.label(), category, options, request.active() == null || request.active(), timeProvider.localDateTime());
        repository.flush();
        auditService.record(actor.username(), actor.roleLabel(), "UPDATE_PRODUCT_ATTRIBUTE", attribute.getCode(),
                "Attributo " + before + " -> " + describe(attribute), AuditCategory.PRODUCT, AuditSeverity.INFO, "PRODUCT_ATTRIBUTE");
        return ProductAttributeResponse.from(attribute);
    }

    /**
     * Valori da salvare sul prodotto per gli attributi attivi indicati nella richiesta. Rifiuta codici inesistenti,
     * attributi disattivati, attributi che non valgono per la categoria del prodotto e voci non previste dall'elenco.
     */
    Map<ProductAttribute, String> resolveValues(Map<String, String> requested, ProductCategory productCategory) {
        Map<ProductAttribute, String> resolved = new LinkedHashMap<>();
        if (requested == null) {
            return resolved;
        }
        Set<String> seen = new HashSet<>();
        requested.forEach((code, value) -> {
            ProductAttribute attribute = requireByCode(code);
            if (!seen.add(attribute.getCodeCanonical())) {
                throw new IllegalArgumentException("Attributo " + attribute.getLabel() + " indicato piu volte.");
            }
            Optional<String> normalized = attribute.normalize(value);
            if (normalized.isEmpty()) {
                return;
            }
            if (!attribute.isActive()) {
                throw new IllegalArgumentException("L'attributo " + attribute.getLabel() + " e disattivato.");
            }
            if (!attribute.appliesTo(productCategory)) {
                throw new IllegalArgumentException("L'attributo " + attribute.getLabel() + " non vale per la categoria " + productCategory.getPath() + ".");
            }
            if (normalized.get().length() > MAX_VALUE_LENGTH) {
                throw new IllegalArgumentException("Il valore dell'attributo " + attribute.getLabel() + " supera " + MAX_VALUE_LENGTH + " caratteri.");
            }
            resolved.put(attribute, normalized.get());
        });
        return resolved;
    }

    /** Attributo usato come filtro: deve esistere; il valore va confrontato esatto per gli elenchi e per contenuto per il testo. */
    ProductAttribute requireFilter(String code) {
        return requireByCode(code);
    }

    List<ProductAttribute> activeAttributes() {
        return repository.findAll().stream()
                .filter(ProductAttribute::isActive)
                .sorted(Comparator.comparing(ProductAttribute::getLabel, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private ProductAttribute requireByCode(String code) {
        return repository.findByCodeCanonical(BusinessIdentifierCanonicalizer.canonical(code))
                .orElseThrow(() -> new IllegalArgumentException("Attributo " + BusinessIdentifierCanonicalizer.display(code) + " inesistente."));
    }

    private ProductCategory optionalCategory(String code, boolean requireActive) {
        if (code == null || code.isBlank()) {
            return null;
        }
        ProductCategory category = categoryRepository.findByCodeCanonical(BusinessIdentifierCanonicalizer.canonical(code))
                .orElseThrow(() -> new IllegalArgumentException("Categoria " + BusinessIdentifierCanonicalizer.display(code) + " inesistente."));
        if (requireActive && !category.isActive()) {
            throw new IllegalArgumentException("La categoria " + category.getPath() + " e disattivata.");
        }
        return category;
    }

    private static boolean sameCategory(ProductCategory current, String requestedCode) {
        if (current == null) {
            return requestedCode == null || requestedCode.isBlank();
        }
        return requestedCode != null && current.getCodeCanonical().equals(BusinessIdentifierCanonicalizer.canonical(requestedCode));
    }

    private static List<String> validOptions(ProductAttributeType type, List<String> requested) {
        List<String> options = requested == null ? List.of() : requested.stream().map(String::trim).toList();
        if (type == ProductAttributeType.TEXT) {
            if (!options.isEmpty()) {
                throw new IllegalArgumentException("Un attributo di testo non prevede un elenco di valori.");
            }
            return options;
        }
        if (options.isEmpty()) {
            throw new IllegalArgumentException("Un attributo a elenco richiede almeno un valore.");
        }
        Set<String> canonical = new HashSet<>();
        for (String option : options) {
            if (!canonical.add(ProductAttributeOption.canonical(option))) {
                throw new IllegalArgumentException("Il valore " + option + " compare piu volte nell'elenco.");
            }
        }
        return options;
    }

    private void validateValuesInUse(ProductAttribute attribute, ProductCategory category, List<String> options) {
        if (attribute.getType() == ProductAttributeType.LIST) {
            Set<String> allowed = new HashSet<>(options.stream().map(ProductAttributeOption::canonical).toList());
            List<String> removedInUse = repository.findCanonicalValuesInUse(attribute.getId()).stream()
                    .filter(value -> !allowed.contains(value))
                    .toList();
            if (!removedInUse.isEmpty()) {
                throw new ResourceConflictException("Valori ancora usati da prodotti: " + String.join(", ", removedInUse) + ". Aggiornare prima i prodotti.");
            }
        }
        if (category != null) {
            boolean outsideScope = repository.findCategoryIdsInUse(attribute.getId()).stream()
                    .map(id -> categoryRepository.findById(id).orElseThrow())
                    .anyMatch(productCategory -> !productCategory.getId().equals(category.getId()) && !productCategory.isDescendantOf(category));
            if (outsideScope) {
                throw new ResourceConflictException("Alcuni prodotti con questo attributo sono fuori dalla categoria " + category.getPath() + ".");
            }
        }
    }

    private static String describe(ProductAttribute attribute) {
        String scope = attribute.getCategory() == null ? "tutti i prodotti" : attribute.getCategory().getPath();
        String options = attribute.getType() == ProductAttributeType.LIST ? " [" + String.join(", ", attribute.getOptionValues()) + "]" : "";
        return attribute.getLabel() + " (" + attribute.getType() + ", " + scope + ")" + options + (attribute.isActive() ? "" : " disattivato");
    }
}
