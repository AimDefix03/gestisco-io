package it.giovannidefilippo.gestionale.document;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/** Imponibile e imposta di un documento per una singola aliquota (e natura, per le operazioni senza IVA). */
@Entity
@Table(name = "fiscal_document_vat_summaries")
public class FiscalDocumentVatSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id")
    private FiscalDocument document;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal vatRate;

    @Column(length = 8)
    private String vatNature;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal taxableAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal vatAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    protected FiscalDocumentVatSummary() {
    }

    FiscalDocumentVatSummary(BigDecimal vatRate, String vatNature, BigDecimal taxableAmount, BigDecimal vatAmount) {
        this.vatRate = vatRate;
        this.vatNature = vatNature;
        this.taxableAmount = taxableAmount;
        this.vatAmount = vatAmount;
        this.totalAmount = taxableAmount.add(vatAmount);
    }

    FiscalDocumentVatSummary copy() {
        return new FiscalDocumentVatSummary(vatRate, vatNature, taxableAmount, vatAmount);
    }

    void assignDocument(FiscalDocument document) {
        this.document = document;
    }

    public BigDecimal getVatRate() { return vatRate; }
    public String getVatNature() { return vatNature; }
    public BigDecimal getTaxableAmount() { return taxableAmount; }
    public BigDecimal getVatAmount() { return vatAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
}
