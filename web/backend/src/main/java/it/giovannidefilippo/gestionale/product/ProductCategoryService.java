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
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class ProductCategoryService {
    static final int MAX_DEPTH = 3;

    private final ProductCategoryRepository repository;
    private final AuditService auditService;
    private final TimeProvider timeProvider;

    ProductCategoryService(ProductCategoryRepository repository, AuditService auditService, TimeProvider timeProvider) {
        this.repository = repository;
        this.auditService = auditService;
        this.timeProvider = timeProvider;
    }

    public List<ProductCategoryResponse> list(boolean includeInactive) {
        return repository.findAll().stream()
                .filter(category -> includeInactive || category.isActive())
                .sorted(Comparator.comparing(ProductCategory::getPath, String.CASE_INSENSITIVE_ORDER))
                .map(ProductCategoryResponse::from)
                .toList();
    }

    @Transactional
    public ProductCategoryResponse create(ProductCategoryRequest request, AuthenticatedUser actor) {
        String canonicalCode = BusinessIdentifierCanonicalizer.canonical(request.code());
        if (repository.existsByCodeCanonical(canonicalCode)) {
            throw new ResourceConflictException("Esiste gia una categoria con codice " + request.code().trim() + ".");
        }
        ProductCategory parent = optionalParent(request.parentCode());
        validateDepth(parent, 0);
        validateSiblingName(parent, request.name(), null);
        LocalDateTime now = timeProvider.localDateTime();
        ProductCategory category = repository.saveAndFlush(new ProductCategory(request.code(), request.name(), parent, now));
        if (Boolean.FALSE.equals(request.active())) {
            category.changeActive(false, now);
        }
        auditService.record(actor.username(), actor.roleLabel(), "CREATE_PRODUCT_CATEGORY", category.getCode(),
                "Creata categoria " + category.getPath(), AuditCategory.PRODUCT, AuditSeverity.INFO, "PRODUCT_CATEGORY");
        return ProductCategoryResponse.from(category);
    }

    @Transactional
    public ProductCategoryResponse update(String code, ProductCategoryRequest request, AuthenticatedUser actor) {
        ProductCategory category = requireByCode(code);
        if (!category.getCodeCanonical().equals(BusinessIdentifierCanonicalizer.canonical(request.code()))) {
            throw new IllegalArgumentException("Il codice di una categoria non puo essere modificato.");
        }
        String before = category.getPath() + (category.isActive() ? "" : " (disattivata)");
        LocalDateTime now = timeProvider.localDateTime();
        ProductCategory parent = optionalParent(request.parentCode());
        if (parent != null && (parent.getId().equals(category.getId()) || parent.isDescendantOf(category))) {
            throw new IllegalArgumentException("Una categoria non puo essere spostata sotto se stessa o una sua sottocategoria.");
        }
        validateDepth(parent, subtreeHeight(category) - 1);
        validateSiblingName(parent, request.name(), category.getId());
        category.rename(request.name(), now);
        category.moveTo(parent, now);
        boolean active = request.active() == null || request.active();
        if (!active && category.isActive()) {
            boolean hasActiveChildren = repository.findByParentId(category.getId()).stream().anyMatch(ProductCategory::isActive);
            if (hasActiveChildren) {
                throw new ResourceConflictException("Disattivare prima le sottocategorie attive.");
            }
        }
        if (active && parent != null && !parent.isActive()) {
            throw new IllegalArgumentException("Una categoria attiva non puo stare sotto una categoria disattivata.");
        }
        category.changeActive(active, now);
        repository.flush();
        String after = category.getPath() + (category.isActive() ? "" : " (disattivata)");
        auditService.record(actor.username(), actor.roleLabel(), "UPDATE_PRODUCT_CATEGORY", category.getCode(),
                "Categoria " + before + " -> " + after, AuditCategory.PRODUCT, AuditSeverity.INFO, "PRODUCT_CATEGORY");
        return ProductCategoryResponse.from(category);
    }

    /** Categoria assegnabile a un prodotto: deve esistere ed essere attiva. */
    ProductCategory requireAssignable(String code) {
        ProductCategory category = repository.findByCodeCanonical(BusinessIdentifierCanonicalizer.canonical(code))
                .orElseThrow(() -> new IllegalArgumentException("Categoria " + BusinessIdentifierCanonicalizer.display(code) + " inesistente."));
        if (!category.isActive()) {
            throw new IllegalArgumentException("La categoria " + category.getPath() + " e disattivata.");
        }
        return category;
    }

    /**
     * Identificativi della categoria indicata e di tutte le sue sottocategorie, per i filtri.
     * Vuoto se il filtro non e richiesto; insieme impossibile se il codice non esiste.
     */
    Optional<Set<Long>> filterIds(String code) {
        if (code == null || code.isBlank() || "ALL".equalsIgnoreCase(code.trim())) {
            return Optional.empty();
        }
        Optional<ProductCategory> root = repository.findByCodeCanonical(BusinessIdentifierCanonicalizer.canonical(code));
        if (root.isEmpty()) {
            return Optional.of(Set.of(-1L));
        }
        Set<Long> ids = new HashSet<>();
        for (ProductCategory candidate : repository.findAll()) {
            if (candidate.getId().equals(root.get().getId()) || candidate.isDescendantOf(root.get())) {
                ids.add(candidate.getId());
            }
        }
        return Optional.of(ids);
    }

    private ProductCategory requireByCode(String code) {
        return repository.findByCodeCanonical(BusinessIdentifierCanonicalizer.canonical(code))
                .orElseThrow(() -> new IllegalArgumentException("Categoria " + BusinessIdentifierCanonicalizer.display(code) + " inesistente."));
    }

    private ProductCategory optionalParent(String parentCode) {
        if (parentCode == null || parentCode.isBlank()) {
            return null;
        }
        return requireByCode(parentCode);
    }

    private void validateDepth(ProductCategory parent, int additionalLevelsBelow) {
        int parentDepth = parent == null ? 0 : parent.getDepth();
        if (parentDepth + 1 + additionalLevelsBelow > MAX_DEPTH) {
            throw new IllegalArgumentException("Le categorie possono avere al massimo " + MAX_DEPTH + " livelli.");
        }
    }

    private void validateSiblingName(ProductCategory parent, String name, Long excludedId) {
        long parentKey = parent == null ? 0 : parent.getId();
        String canonicalName = ProductCategory.canonicalName(name);
        boolean duplicate = excludedId == null
                ? repository.existsByParentKeyAndNameCanonical(parentKey, canonicalName)
                : repository.existsByParentKeyAndNameCanonicalAndIdNot(parentKey, canonicalName, excludedId);
        if (duplicate) {
            throw new ResourceConflictException("Esiste gia una categoria \"" + name.trim() + "\" allo stesso livello.");
        }
    }

    private int subtreeHeight(ProductCategory category) {
        int height = 1;
        for (ProductCategory child : repository.findByParentId(category.getId())) {
            height = Math.max(height, 1 + subtreeHeight(child));
        }
        return height;
    }
}
