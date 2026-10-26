package it.giovannidefilippo.gestionale.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;

/** Aliquota IVA applicabile ai prodotti. {@code rate} e una frazione (0.22 = 22%). */
@Entity
@Table(name = "vat_rates")
public class VatRate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private long version;

    @Column(nullable = false, unique = true, length = 16)
    private String code;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal rate;

    @Column(length = 8)
    private String nature;

    @Column(nullable = false, length = 160)
    private String description;

    @Column(name = "default_rate", nullable = false)
    private boolean defaultRate;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private int sortOrder;

    protected VatRate() {
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public String getNature() {
        return nature;
    }

    public String getDescription() {
        return description;
    }

    public boolean isDefaultRate() {
        return defaultRate;
    }

    public boolean isActive() {
        return active;
    }

    public int getSortOrder() {
        return sortOrder;
    }
}
