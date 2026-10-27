package it.giovannidefilippo.gestionale.document;

import it.giovannidefilippo.gestionale.common.TestCompanySettings;
import it.giovannidefilippo.gestionale.company.CompanySettingsService;
import it.giovannidefilippo.gestionale.inventory.InventoryService;
import it.giovannidefilippo.gestionale.order.OrderRequests;
import it.giovannidefilippo.gestionale.order.OrderResponse;
import it.giovannidefilippo.gestionale.order.OrderService;
import it.giovannidefilippo.gestionale.order.PaymentMethod;
import it.giovannidefilippo.gestionale.product.ProductRequest;
import it.giovannidefilippo.gestionale.product.ProductResponse;
import it.giovannidefilippo.gestionale.product.ProductService;
import it.giovannidefilippo.gestionale.user.AuthenticatedUser;
import it.giovannidefilippo.gestionale.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static it.giovannidefilippo.gestionale.support.SessionRequests.session;
import static it.giovannidefilippo.gestionale.support.SessionRequests.sessionToken;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MixedVatInvoiceTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductService productService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private FiscalDocumentService documentService;

    @Autowired
    private CompanySettingsService companySettingsService;

    @BeforeEach
    void configureCompany() {
        TestCompanySettings.configure(companySettingsService, actor());
    }

    @Test
    void invoiceSummarizesEachVatRateFromTheLinesSnapshot() {
        String standard = product("122.00", "IVA22");
        String reduced = product("55.00", "IVA10");
        String exempt = product("20.00", "ESENTE");
        OrderResponse order = orderService.create(
                new OrderRequests.CreateOrderRequest(
                        "cliente iva",
                        PaymentMethod.CARD,
                        List.of(
                                new OrderRequests.CreateOrderItemRequest(standard, 2),
                                new OrderRequests.CreateOrderItemRequest(reduced, 1),
                                new OrderRequests.CreateOrderItemRequest(exempt, 1)
                        )
                ),
                "cliente iva",
                actor()
        );
        assertThat(order.items()).extracting(OrderResponse.OrderItemResponse::vatRate)
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactlyInAnyOrder(new BigDecimal("0.22"), new BigDecimal("0.10"), BigDecimal.ZERO);
        orderService.confirm(order.code(), actor());
        orderService.fulfill(order.code(), actor());

        FiscalDocumentResponse invoice = documentService.createInvoice(new FiscalDocumentRequests.CreateInvoiceRequest(order.code()), actor());

        assertThat(invoice.totalAmount()).isEqualByComparingTo("319.00");
        assertThat(invoice.taxableAmount()).isEqualByComparingTo("270.00");
        assertThat(invoice.vatAmount()).isEqualByComparingTo("49.00");
        assertThat(invoice.vatRate()).isNull();
        assertThat(invoice.vatSummaries()).hasSize(3);
        assertSummary(invoice.vatSummaries().get(0), "0.22", null, "200.00", "44.00");
        assertSummary(invoice.vatSummaries().get(1), "0.10", null, "50.00", "5.00");
        assertSummary(invoice.vatSummaries().get(2), "0.00", "N4", "20.00", "0.00");
        assertThat(invoice.lines()).extracting(FiscalDocumentResponse.LineResponse::vatNature).contains("N4");

        FiscalDocumentResponse creditNote = documentService.createCreditNote(new FiscalDocumentRequests.CreateCreditNoteRequest(order.code(), "Storno IVA mista"), actor());
        assertThat(creditNote.vatSummaries()).hasSize(3);
        assertThat(creditNote.vatAmount()).isEqualByComparingTo("49.00");
    }

    @Test
    void productsUseTheDefaultRateUnlessAnActiveRateIsChosen() {
        ProductResponse defaulted = productService.create(request(uniqueCode("VAT-DEF"), "10.00", null));
        assertThat(defaulted.vatRateCode()).isEqualTo("IVA22");
        assertThat(defaulted.vatRate()).isEqualByComparingTo("0.22");

        assertThatThrownBy(() -> productService.create(request(uniqueCode("VAT-BAD"), "10.00", "IVA99")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("IVA99");
    }

    @Test
    void vatRatesAreListedForStaffOnly() throws Exception {
        String admin = login("test_super_admin", "Test-Bootstrap-9842!", "SUPER_ADMIN");
        mockMvc.perform(get("/api/vat-rates").with(session(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("IVA22"))
                .andExpect(jsonPath("$[?(@.code == 'ESENTE')].nature").value(org.hamcrest.Matchers.hasItem("N4")))
                .andExpect(jsonPath("$[?(@.defaultRate == true)].code").value(org.hamcrest.Matchers.contains("IVA22")));
        mockMvc.perform(get("/api/vat-rates")).andExpect(status().isUnauthorized());
        String customerName = "vat_customer_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        mockMvc.perform(post("/api/accounts/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + customerName + "\",\"password\":\"CustomerStrong123!\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/api/vat-rates").with(session(login(customerName, "CustomerStrong123!", "CUSTOMER"))))
                .andExpect(status().isForbidden());
    }

    private String product(String grossPrice, String vatRateCode) {
        String code = uniqueCode("VAT");
        productService.create(request(code, grossPrice, vatRateCode));
        inventoryService.initialBalance(code, 5, "Saldo iniziale IVA", "test", "Test");
        return code;
    }

    private static ProductRequest request(String code, String grossPrice, String vatRateCode) {
        return new ProductRequest(code, "Prodotto " + code, "Prodotto per il riepilogo IVA.", "HARDWARE", "VatBrand", "Capo", "",
                new BigDecimal(grossPrice), BigDecimal.ZERO, null, null, vatRateCode);
    }

    private static void assertSummary(FiscalDocumentResponse.VatSummaryResponse summary, String rate, String nature, String taxable, String vat) {
        assertThat(summary.vatRate()).isEqualByComparingTo(rate);
        assertThat(summary.vatNature()).isEqualTo(nature);
        assertThat(summary.taxableAmount()).isEqualByComparingTo(taxable);
        assertThat(summary.vatAmount()).isEqualByComparingTo(vat);
    }

    private String login(String username, String password, String role) throws Exception {
        MvcResult response = mockMvc.perform(post("/api/accounts/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\",\"role\":\"" + role + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return sessionToken(response);
    }

    private static String uniqueCode(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private static AuthenticatedUser actor() {
        return new AuthenticatedUser("admin", UserRole.SUPER_ADMIN);
    }
}
