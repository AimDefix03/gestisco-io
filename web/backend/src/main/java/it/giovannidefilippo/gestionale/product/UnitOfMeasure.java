package it.giovannidefilippo.gestionale.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Unita di misura configurabile. I prodotti la referenziano per codice, che resta immutabile;
 * {@link #BASE_CODE} e l'unita predefinita e non si puo disattivare.
 */
@Entity
@Table(name = "units_of_measure")
public class UnitOfMeasure implements Serializable {
    public static final String BASE_CODE = "PIECE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private long version;

    @Column(nullable = false, unique = true, length = 16)
    private String code;

    @Column(nullable = false, length = 60)
    private String label;

    @Column(nullable = false, length = 12)
    private String symbol;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected UnitOfMeasure() {
    }

    UnitOfMeasure(String code, String label, String symbol, LocalDateTime now) {
        this.code = canonicalCode(code);
        this.active = true;
        this.createdAt = now;
        update(label, symbol, true, now);
    }

    void update(String label, String symbol, boolean active, LocalDateTime now) {
        this.label = label.trim();
        this.symbol = symbol.trim();
        this.active = active;
        this.updatedAt = now;
    }

    static String canonicalCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    public boolean isBase() {
        return BASE_CODE.equals(code);
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public String getSymbol() {
        return symbol;
    }

    public boolean isActive() {
        return active;
    }
}
