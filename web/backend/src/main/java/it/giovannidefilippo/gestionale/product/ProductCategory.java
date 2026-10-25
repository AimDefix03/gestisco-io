package it.giovannidefilippo.gestionale.product;

import it.giovannidefilippo.gestionale.common.BusinessIdentifierCanonicalizer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "product_categories")
public class ProductCategory {
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
    private String name;

    @Column(nullable = false, length = 120)
    private String nameCanonical;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private ProductCategory parent;

    @Column(nullable = false)
    private long parentKey;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected ProductCategory() {
    }

    ProductCategory(String code, String name, ProductCategory parent, LocalDateTime now) {
        this.code = BusinessIdentifierCanonicalizer.display(code);
        this.codeCanonical = BusinessIdentifierCanonicalizer.canonical(code);
        this.active = true;
        this.createdAt = now;
        rename(name, now);
        moveTo(parent, now);
    }

    void rename(String name, LocalDateTime now) {
        this.name = name.trim();
        this.nameCanonical = canonicalName(name);
        this.updatedAt = now;
    }

    void moveTo(ProductCategory parent, LocalDateTime now) {
        this.parent = parent;
        this.parentKey = parent == null ? 0 : parent.getId();
        this.updatedAt = now;
    }

    void changeActive(boolean active, LocalDateTime now) {
        this.active = active;
        this.updatedAt = now;
    }

    static String canonicalName(String name) {
        return BusinessIdentifierCanonicalizer.canonical(name);
    }

    /** Categorie dalla radice a questa, inclusa. */
    public List<ProductCategory> lineage() {
        List<ProductCategory> lineage = new ArrayList<>();
        for (ProductCategory current = this; current != null; current = current.parent) {
            lineage.add(current);
        }
        Collections.reverse(lineage);
        return lineage;
    }

    public String getPath() {
        return String.join(" / ", lineage().stream().map(ProductCategory::getName).toList());
    }

    public int getDepth() {
        return lineage().size();
    }

    public boolean isDescendantOf(ProductCategory candidateAncestor) {
        for (ProductCategory current = parent; current != null; current = current.parent) {
            if (current.getId().equals(candidateAncestor.getId())) {
                return true;
            }
        }
        return false;
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

    public String getName() {
        return name;
    }

    public ProductCategory getParent() {
        return parent;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
