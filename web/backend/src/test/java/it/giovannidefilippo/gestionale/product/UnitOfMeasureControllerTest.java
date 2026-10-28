package it.giovannidefilippo.gestionale.product;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static it.giovannidefilippo.gestionale.support.SessionRequests.session;
import static it.giovannidefilippo.gestionale.support.SessionRequests.sessionToken;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UnitOfMeasureControllerTest {
    private static final String SUPER_ADMIN_PASSWORD = "Test-Bootstrap-9842!";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void unitDefinedFromTheApiIsAssignedToProductsWithoutCodeChanges() throws Exception {
        String admin = loginSuperAdmin();
        String unit = unique("ROLL");
        saveUnit(admin, post("/api/units-of-measure"), unit.toLowerCase(), "Rotolo " + unit, " rot ", null)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(unit))
                .andExpect(jsonPath("$.symbol").value("rot"))
                .andExpect(jsonPath("$.base").value(false));

        String product = unique("TESSUTO");
        saveProduct(admin, post("/api/products"), product, unit.toLowerCase())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.unitOfMeasure").value(unit))
                .andExpect(jsonPath("$.unitLabel").value("Rotolo " + unit));

        saveUnit(admin, put("/api/units-of-measure/" + unit), unit, "Bobina " + unit, "bob", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productCount").value(1));
        mockMvc.perform(get("/api/products/" + product).with(session(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unitLabel").value("Bobina " + unit));
        mockMvc.perform(get("/api/units-of-measure").with(session(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("PIECE"))
                .andExpect(jsonPath("$[*].code", hasItem(unit)));
        mockMvc.perform(get("/api/audit").with(session(admin)).param("q", unit))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].action", hasItem("CREATE_UNIT_OF_MEASURE")))
                .andExpect(jsonPath("$.content[*].action", hasItem("UPDATE_UNIT_OF_MEASURE")));
    }

    @Test
    void serverRejectsDuplicatesCodeChangesAndDeactivatedOrBaseUnits() throws Exception {
        String admin = loginSuperAdmin();
        String unit = unique("BOX");
        saveUnit(admin, post("/api/units-of-measure"), unit, "Scatola " + unit, "sc", null)
                .andExpect(status().isCreated());

        saveUnit(admin, post("/api/units-of-measure"), unit.toLowerCase(), "Altra " + unit, "x", null)
                .andExpect(status().isConflict());
        saveUnit(admin, post("/api/units-of-measure"), unique("DUP"), " scatola " + unit.toLowerCase(), "x", null)
                .andExpect(status().isConflict());
        saveUnit(admin, post("/api/units-of-measure"), "NO-HYPHEN", "Trattino", "x", null)
                .andExpect(status().isUnprocessableEntity());
        saveUnit(admin, put("/api/units-of-measure/" + unit), unique("OTHER"), "Scatola " + unit, "sc", null)
                .andExpect(status().isBadRequest());
        saveUnit(admin, put("/api/units-of-measure/PIECE"), "PIECE", "Pezzo", "pz", false)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("unita base")));

        String product = unique("CASSA");
        saveProduct(admin, post("/api/products"), product, unit).andExpect(status().isCreated());
        saveUnit(admin, put("/api/units-of-measure/" + unit), unit, "Scatola " + unit, "sc", false)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        saveProduct(admin, post("/api/products"), unique("NUOVO"), unit)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("disattivata")));
        saveProduct(admin, put("/api/products/" + product), product, unit)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unitOfMeasure").value(unit));
        saveProduct(admin, put("/api/products/" + product), product, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unitOfMeasure").value(unit));
        saveProduct(admin, put("/api/products/" + product), product, "PAIR")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unitOfMeasure").value("PAIR"));
        saveProduct(admin, put("/api/products/" + product), product, unit)
                .andExpect(status().isBadRequest());
    }

    @Test
    void customersReadActiveUnitsButCannotManageThem() throws Exception {
        String admin = loginSuperAdmin();
        String hidden = unique("HID");
        saveUnit(admin, post("/api/units-of-measure"), hidden, "Nascosta " + hidden, "n", false)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.active").value(false));
        String customer = registerAndLoginCustomer();

        mockMvc.perform(get("/api/units-of-measure").with(session(customer)).param("includeInactive", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].code", not(hasItem(hidden))));
        mockMvc.perform(get("/api/units-of-measure").with(session(admin)).param("includeInactive", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].code", hasItem(hidden)));
        saveUnit(customer, post("/api/units-of-measure"), unique("NO"), "Vietata", "v", null)
                .andExpect(status().isForbidden());
        saveUnit(customer, put("/api/units-of-measure/" + hidden), hidden, "Vietata", "v", true)
                .andExpect(status().isForbidden());
    }

    private ResultActions saveUnit(String token, MockHttpServletRequestBuilder request, String code, String label, String symbol, Boolean active) throws Exception {
        return mockMvc.perform(request
                .with(session(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "code": "%s", "label": "%s", "symbol": "%s", "active": %s }
                        """.formatted(code, label, symbol, active)));
    }

    private ResultActions saveProduct(String token, MockHttpServletRequestBuilder request, String code, String unit) throws Exception {
        return mockMvc.perform(request
                .with(session(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "code": "%s",
                          "name": "Prodotto %s",
                          "description": "Prodotto con unita configurabile",
                          "category": "HARDWARE",
                          "brand": "Brand",
                          "productType": "Tessuto",
                          "price": 12.50,
                          "discount": 0,
                          "unitOfMeasure": %s
                        }
                        """.formatted(code, code, unit == null ? "null" : "\"" + unit + "\"")));
    }

    private String loginSuperAdmin() throws Exception {
        return login("test_super_admin", SUPER_ADMIN_PASSWORD);
    }

    private String registerAndLoginCustomer() throws Exception {
        String username = "cliente_unit_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String password = "CustomerStrong123!";
        mockMvc.perform(post("/api/accounts/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "username": "%s", "password": "%s" }
                                """.formatted(username, password)))
                .andExpect(status().isCreated());
        return login(username, password);
    }

    private String login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/accounts/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "username": "%s", "password": "%s" }
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andReturn();
        return sessionToken(result);
    }

    /** Codice valido per le unita: lettere maiuscole, cifre e trattino basso, al massimo 16 caratteri. */
    private static String unique(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
