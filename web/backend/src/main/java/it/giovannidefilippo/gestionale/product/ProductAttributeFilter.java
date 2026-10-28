package it.giovannidefilippo.gestionale.product;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.MapJoin;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

/** Filtro per attributo condiviso da catalogo e report: valore esatto per gli elenchi, contenuto per il testo, senza distinguere maiuscole. */
final class ProductAttributeFilter {
    private ProductAttributeFilter() {
    }

    static Specification<Product> of(ProductAttributeService attributeService, String attributeCode, String attributeValue) {
        if (attributeCode == null || attributeCode.isBlank() || attributeValue == null || attributeValue.isBlank()) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }
        ProductAttribute attribute = attributeService.requireFilter(attributeCode);
        String value = attributeValue.trim().toLowerCase(Locale.ROOT);
        return (root, query, criteriaBuilder) -> {
            MapJoin<Product, ProductAttribute, String> values = root.joinMap("attributeValues");
            Expression<String> stored = criteriaBuilder.lower(values.value());
            return criteriaBuilder.and(
                    criteriaBuilder.equal(values.key().get("id"), attribute.getId()),
                    attribute.getType() == ProductAttributeType.LIST
                            ? criteriaBuilder.equal(stored, value)
                            : criteriaBuilder.like(stored, "%" + value + "%")
            );
        };
    }
}
