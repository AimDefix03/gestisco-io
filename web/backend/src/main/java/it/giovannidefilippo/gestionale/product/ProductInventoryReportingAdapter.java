package it.giovannidefilippo.gestionale.product;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
@Transactional(readOnly = true)
class ProductInventoryReportingAdapter implements InventoryReportingUsage {
    private static final int LOW_STOCK_THRESHOLD = 3;

    private final ProductRepository repository;
    private final ProductCategoryService categoryService;
    private final ProductAttributeService attributeService;

    ProductInventoryReportingAdapter(ProductRepository repository, ProductCategoryService categoryService, ProductAttributeService attributeService) {
        this.repository = repository;
        this.categoryService = categoryService;
        this.attributeService = attributeService;
    }

    @Override
    public List<InventoryReportAttribute> reportAttributes() {
        return attributeService.activeAttributes().stream()
                .map(attribute -> new InventoryReportAttribute(attribute.getCode(), attribute.getLabel()))
                .toList();
    }

    @Override
    public List<InventoryProductReportSource> findForReport(String query, String category, String stock, Boolean discontinued, String attribute, String attributeValue, int maxRows) {
        Specification<Product> specification = specification(query, categoryService.filterIds(category).orElse(null), stock, discontinued)
                .and(ProductAttributeFilter.of(attributeService, attribute, attributeValue));
        long totalRows = repository.count(specification);
        if (totalRows > maxRows) {
            throw new IllegalArgumentException("Il report contiene " + totalRows + " prodotti. Restringi i filtri a un massimo di " + maxRows + " righe.");
        }
        return repository.findAll(specification, Sort.by("name").ascending().and(Sort.by("code").ascending()))
                .stream()
                .map(ProductInventoryReportingAdapter::toSource)
                .toList();
    }

    private static Specification<Product> specification(String query, Set<Long> categoryIds, String stock, Boolean discontinued) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (hasText(query)) {
                String term = contains(query);
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), term),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), term),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("brand")), term),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("productType")), term)
                ));
            }
            if (categoryIds != null) {
                predicates.add(root.get("category").get("id").in(categoryIds));
            }
            if (discontinued != null) {
                predicates.add(criteriaBuilder.equal(root.get("discontinued"), discontinued));
            }
            addStockPredicate(predicates, stock, root.get("quantity"), root.get("reservedQuantity"), criteriaBuilder);
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static void addStockPredicate(
            List<Predicate> predicates,
            String stock,
            Expression<Integer> quantity,
            Expression<Integer> reservedQuantity,
            jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder
    ) {
        if (!hasText(stock) || "ALL".equalsIgnoreCase(stock)) {
            return;
        }
        Expression<Integer> available = criteriaBuilder.diff(quantity, reservedQuantity);
        switch (stock.trim().toUpperCase(Locale.ROOT)) {
            case "AVAILABLE" -> predicates.add(criteriaBuilder.greaterThan(available, LOW_STOCK_THRESHOLD));
            case "LOW" -> predicates.add(criteriaBuilder.and(
                    criteriaBuilder.greaterThan(available, 0),
                    criteriaBuilder.lessThanOrEqualTo(available, LOW_STOCK_THRESHOLD)
            ));
            case "OUT" -> predicates.add(criteriaBuilder.equal(available, 0));
            default -> throw new IllegalArgumentException("Filtro stock non valido.");
        }
    }

    private static InventoryProductReportSource toSource(Product product) {
        return new InventoryProductReportSource(
                product.getCode(),
                product.getName(),
                product.getCategory().getCode(),
                product.getCategory().getPath(),
                product.getBrand(),
                product.getProductType(),
                product.getQuantity(),
                product.getReservedQuantity(),
                product.getAvailableQuantity(),
                product.getPrice(),
                product.getDiscount(),
                product.getDiscountedPrice(),
                product.getLastPurchaseCost(),
                product.getAveragePurchaseCost(),
                product.getCostedQuantity(),
                product.getUncostedQuantity(),
                product.getCostCoveragePercentage(),
                product.getKnownInventoryCost(),
                product.getPotentialGrossMarginOnCostedStock(),
                product.isDiscontinued(),
                activeAttributeValues(product)
        );
    }

    private static Map<String, String> activeAttributeValues(Product product) {
        Map<String, String> values = new LinkedHashMap<>();
        product.getAttributeValues().forEach((attribute, value) -> {
            if (attribute.isActive()) values.put(attribute.getCode(), value);
        });
        return values;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String contains(String value) {
        return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }
}
