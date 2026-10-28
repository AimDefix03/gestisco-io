package it.giovannidefilippo.gestionale.product;

import it.giovannidefilippo.gestionale.common.BusinessIdentifierCanonicalizer;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** Attributo personalizzato di prodotto (P1.2): descrive il prodotto senza generare varianti. */
@Entity
@Table(name = "product_attributes")
public class ProductAttribute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private long version;

    @Column(nullable = false, length = 64)
    private String code;

    @Column(nullable = false, length = 64, unique = true)
    private String codeCanonical;

    @Column(nullable = false, length = 120)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ProductAttributeType type;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private ProductCategory category;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_attribute_options", joinColumns = @JoinColumn(name = "attribute_id"))
    @OrderColumn(name = "position")
    private List<ProductAttributeOption> options = new ArrayList<>();

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected ProductAttribute() {
    }

    ProductAttribute(String code, String label, ProductAttributeType type, ProductCategory category, List<String> options, LocalDateTime now) {
        this.code = BusinessIdentifierCanonicalizer.display(code);
        this.codeCanonical = BusinessIdentifierCanonicalizer.canonical(code);
        this.type = type;
        this.active = true;
        this.createdAt = now;
        update(label, category, options, true, now);
    }

    void update(String label, ProductCategory category, List<String> options, boolean active, LocalDateTime now) {
        this.label = label.trim();
        this.category = category;
        this.options.clear();
        options.forEach(option -> this.options.add(new ProductAttributeOption(option)));
        this.active = active;
        this.updatedAt = now;
    }

    /** Vale per ogni prodotto se non ha categoria, altrimenti per la categoria indicata e le sue sottocategorie. */
    public boolean appliesTo(ProductCategory productCategory) {
        return category == null
                || category.getId().equals(productCategory.getId())
                || productCategory.isDescendantOf(category);
    }

    /** Valore canonico da salvare: per gli elenchi la voce ammessa con la sua grafia, per il testo il valore ripulito. */
    Optional<String> normalize(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        if (type == ProductAttributeType.TEXT) {
            return Optional.of(value.trim());
        }
        String canonical = ProductAttributeOption.canonical(value);
        return Optional.of(options.stream()
                .filter(option -> option.getValueCanonical().equals(canonical))
                .map(ProductAttributeOption::getValue)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Valore " + value.trim() + " non ammesso per l'attributo " + label + ".")));
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getCodeCanonical() {
        return codeCanonical;
    }

    public String getLabel() {
        return label;
    }

    public ProductAttributeType getType() {
        return type;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public List<String> getOptionValues() {
        return Collections.unmodifiableList(options.stream().map(ProductAttributeOption::getValue).toList());
    }

    public boolean isActive() {
        return active;
    }
}
