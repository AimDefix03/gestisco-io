package it.giovannidefilippo.gestionale.reporting;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReportExportServiceTest {
    private ReportExportService service;

    @BeforeEach
    void setUp() {
        service = new ReportExportService(new SpreadsheetReportWriter(), new PdfReportWriter());
    }

    @Test
    void csvUsesUtf8BomEscapesDelimitersAndNeutralizesSpreadsheetFormulas() {
        ReportFile file = service.sales(salesReport("=HYPERLINK(\"https://invalid\")", "Cliente; Test"), ReportFormat.CSV);

        assertThat(file.content()).startsWith((byte) 0xEF, (byte) 0xBB, (byte) 0xBF);
        String csv = new String(file.content(), StandardCharsets.UTF_8);
        assertThat(csv).contains("'=HYPERLINK");
        assertThat(csv).contains("\"Cliente; Test\"");
        assertThat(file.contentType()).isEqualTo("text/csv;charset=UTF-8");
        assertThat(file.filename()).endsWith(".csv");
    }

    @Test
    void xlsxIsARealWorkbookWithTypedCurrencyAndNeutralizedText() throws Exception {
        ReportFile file = service.sales(salesReport("ORD-1", "=SUM(1,1)"), ReportFormat.XLSX);

        assertThat(file.content()).startsWith((byte) 'P', (byte) 'K');
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(file.content()))) {
            assertThat(workbook.getSheet("Vendite")).isNotNull();
            assertThat(workbook.getSheet("Vendite").getRow(8).getCell(2).getStringCellValue()).isEqualTo("'=SUM(1,1)");
            assertThat(workbook.getSheet("Vendite").getRow(8).getCell(4).getNumericCellValue()).isEqualTo(120.0);
        }
    }

    @Test
    void pdfExportsHaveAValidPdfSignatureForBothReports() {
        ReportFile sales = service.sales(salesReport("ORD-1", "Cliente"), ReportFormat.PDF);
        ReportFile inventory = service.inventory(inventoryReport(), ReportFormat.PDF);

        assertThat(new String(sales.content(), 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
        assertThat(new String(inventory.content(), 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
        assertThat(sales.contentType()).isEqualTo("application/pdf");
        assertThat(inventory.filename()).endsWith(".pdf");
    }

    @Test
    void inventoryExportsAddOneColumnPerActiveAttributeInEveryFormat() throws Exception {
        InventoryReportResponse report = inventoryReportWithAttributes();

        String csv = new String(service.inventory(report, ReportFormat.CSV).content(), StandardCharsets.UTF_8);
        assertThat(csv).contains("Stato prodotto;Materiale;Vestibilita");
        assertThat(csv).contains("Attivo;Cotone;'=1+1");

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(service.inventory(report, ReportFormat.XLSX).content()))) {
            var sheet = workbook.getSheet("Magazzino");
            int headerRow = -1;
            for (int index = 0; index <= sheet.getLastRowNum(); index++) {
                if (sheet.getRow(index) != null && sheet.getRow(index).getCell(0) != null && "Codice".equals(sheet.getRow(index).getCell(0).toString())) headerRow = index;
            }
            assertThat(headerRow).isNotNegative();
            assertThat(sheet.getRow(headerRow).getCell(21).getStringCellValue()).isEqualTo("Materiale");
            assertThat(sheet.getRow(headerRow).getCell(22).getStringCellValue()).isEqualTo("Vestibilita");
            assertThat(sheet.getRow(headerRow + 1).getCell(21).getStringCellValue()).isEqualTo("Cotone");
            assertThat(sheet.getRow(headerRow + 1).getCell(22).getStringCellValue()).isEqualTo("'=1+1");
        }

        ReportFile pdf = service.inventory(report, ReportFormat.PDF);
        assertThat(new String(pdf.content(), 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }

    private static InventoryReportResponse inventoryReportWithAttributes() {
        InventoryReportResponse base = inventoryReport();
        InventoryReportResponse.InventoryProductRow row = base.products().get(0);
        InventoryReportResponse.InventoryProductRow withValues = new InventoryReportResponse.InventoryProductRow(
                row.code(), row.name(), row.category(), row.categoryLabel(), row.brand(), row.productType(),
                row.quantity(), row.reservedQuantity(), row.availableQuantity(), row.price(), row.discount(),
                row.discountedPrice(), row.potentialRetailValue(), row.lastPurchaseCost(), row.averagePurchaseCost(),
                row.costedQuantity(), row.uncostedQuantity(), row.costCoveragePercentage(), row.knownInventoryCost(),
                row.potentialGrossMarginOnCostedStock(), row.discontinued(), row.stockStatus(), row.stockStatusLabel(),
                List.of("Cotone", "=1+1")
        );
        return new InventoryReportResponse(
                base.generatedAt(), base.productCount(), base.physicalUnits(), base.reservedUnits(), base.availableUnits(),
                base.potentialRetailStockValue(), base.knownInventoryCostValue(), base.potentialGrossMarginOnCostedStock(),
                base.costedUnits(), base.uncostedUnits(), base.costCoveragePercentage(), base.lowStockCount(),
                base.outOfStockCount(), base.discontinuedCount(),
                List.of(new InventoryReportResponse.AttributeColumn("MATERIALE", "Materiale"), new InventoryReportResponse.AttributeColumn("VESTIBILITA", "Vestibilita")),
                List.of(withValues)
        );
    }

    private static SalesReportResponse salesReport(String code, String customer) {
        OffsetDateTime generatedAt = OffsetDateTime.of(2026, 7, 14, 10, 30, 0, 0, ZoneOffset.UTC);
        SalesReportResponse.SalesOrderRow order = new SalesReportResponse.SalesOrderRow(
                code,
                generatedAt.minusDays(1),
                customer,
                "FULFILLED",
                "Evaso",
                money("120.00"),
                money("100.00"),
                money("10.00"),
                money("90.00"),
                money("20.00")
        );
        return new SalesReportResponse(
                generatedAt,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 7, 14),
                "FULFILLED",
                "Evaso",
                1,
                money("120.00"),
                money("100.00"),
                money("10.00"),
                money("90.00"),
                money("20.00"),
                money("120.00"),
                List.of(order),
                List.of(new SalesReportResponse.TopProductRow("GPU-1", "Scheda grafica", 1, money("120.00")))
        );
    }

    private static InventoryReportResponse inventoryReport() {
        InventoryReportResponse.InventoryProductRow product = new InventoryReportResponse.InventoryProductRow(
                "GPU-1",
                "Scheda grafica",
                "HARDWARE",
                "Hardware",
                "Brand",
                "Scheda grafica",
                4,
                1,
                3,
                money("120.00"),
                money("10.00"),
                money("108.00"),
                money("432.00"),
                money("70.00"),
                money("70.00"),
                4,
                0,
                money("100.00"),
                money("280.00"),
                money("152.00"),
                false,
                "LOW",
                "Scorta bassa"
        );
        return new InventoryReportResponse(
                OffsetDateTime.of(2026, 7, 14, 10, 30, 0, 0, ZoneOffset.UTC),
                1,
                4,
                1,
                3,
                money("432.00"),
                money("280.00"),
                money("152.00"),
                4,
                0,
                money("100.00"),
                1,
                0,
                0,
                List.of(product)
        );
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value);
    }
}
