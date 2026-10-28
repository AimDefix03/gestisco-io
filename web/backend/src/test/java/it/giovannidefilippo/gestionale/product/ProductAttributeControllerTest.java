package it.giovannidefilippo.gestionale.product;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static it.giovannidefilippo.gestionale.support.SessionRequests.session;
import static it.giovannidefilippo.gestionale.support.SessionRequests.sessionToken;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductAttributeControllerTest {
    private static final String SUPER_ADMIN_PASSWORD = "Test-Bootstrap-9842!";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void attributeDefinedFromTheApiIsStoredFilteredAndExportedWithoutCodeChanges() throws Exception {
        String admin = loginSuperAdmin();
        String category = createCategory(admin, unique("ABB"), null);
        String material = unique("MAT");
        String fit = unique("FIT");
        saveAttribute(admin, post("/api/product-attributes"), material, "Materiale " + material, "LIST", category, "[\"Cotone\", \"Lino\"]", null)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.options[0]").value("Cotone"))
                .andExpect(jsonPath("$.categoryCode").value(category));
        saveAttribute(admin, post("/api/product-attributes"), fit, "Vestibilita " + fit, "TEXT", category, "[]", null)
                .andExpect(status().isCreated());

        String cotton = unique("CAM");
        String linen = unique("CAM");
        saveProduct(admin, post("/api/products"), cotton, category, "{\"%s\": \"  cotone \", \"%s\": \"Slim fit\"}".formatted(material, fit))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.attributes[?(@.code == '%s')].value".formatted(material)).value(hasItem("Cotone")))
                .andExpect(jsonPath("$.attributes[?(@.code == '%s')].value".formatted(fit)).value(hasItem("Slim fit")));
        saveProduct(admin, post("/api/products"), linen, category, "{\"%s\": \"Lino\"}".formatted(material))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/products").with(session(admin)).param("attribute", material).param("attributeValue", "COTONE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].code", hasItem(cotton)))
                .andExpect(jsonPath("$.content[*].code", not(hasItem(linen))));
        mockMvc.perform(get("/api/products").with(session(admin)).param("attribute", fit).param("attributeValue", "slim"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].code").value(cotton));

        mockMvc.perform(get("/api/reports/inventory").with(session(admin)).param("attribute", material).param("attributeValue", "lino"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productCount").value(1))
                .andExpect(jsonPath("$.attributeColumns[*].label", hasItem("Materiale " + material)))
                .andExpect(jsonPath("$.products[0].code").value(linen))
                .andExpect(jsonPath("$.products[0].attributeValues", hasItem("Lino")));
        MvcResult export = mockMvc.perform(get("/api/reports/inventory/export").with(session(admin))
                        .param("attribute", material).param("attributeValue", "cotone").param("format", "CSV"))
                .andExpect(status().isOk())
                .andReturn();
        String csv = export.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(csv).contains("Materiale " + material).contains("Vestibilita " + fit).contains(cotton).contains("Cotone;Slim fit");

        mockMvc.perform(get("/api/audit").with(session(admin)).param("q", material))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].action", hasItem("CREATE_PRODUCT_ATTRIBUTE")));
    }

    @Test
    void serverRejectsInvalidDefinitionsAndValues() throws Exception {
        String admin = loginSuperAdmin();
        String category = createCategory(admin, unique("SCA"), null);
        String other = createCategory(admin, unique("BOR"), null);
        String list = unique("COL");
        saveAttribute(admin, post("/api/product-attributes"), list, "Colore " + list, "LIST", other, "[\"Nero\"]", null)
                .andExpect(status().isCreated());

        saveAttribute(admin, post("/api/product-attributes"), unique("EMPTY"), "Elenco vuoto", "LIST", null, "[]", null)
                .andExpect(status().isBadRequest());
        saveAttribute(admin, post("/api/product-attributes"), unique("DUP"), "Doppio", "LIST", null, "[\"Nero\", \" nero \"]", null)
                .andExpect(status().isBadRequest());
        saveAttribute(admin, post("/api/product-attributes"), unique("TXT"), "Testo con elenco", "TEXT", null, "[\"A\"]", null)
                .andExpect(status().isBadRequest());
        saveAttribute(admin, post("/api/product-attributes"), list.toLowerCase(), "Codice ripetuto", "LIST", null, "[\"A\"]", null)
                .andExpect(status().isConflict());
        saveAttribute(admin, put("/api/product-attributes/" + list), list, "Colore", "TEXT", other, "[]", null)
                .andExpect(status().isBadRequest());

        saveProduct(admin, post("/api/products"), unique("P"), other, "{\"%s\": \"Rosso\"}".formatted(list))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("non ammesso")));
        saveProduct(admin, post("/api/products"), unique("P"), category, "{\"%s\": \"Nero\"}".formatted(list))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("non vale per la categoria")));
        saveProduct(admin, post("/api/products"), unique("P"), other, "{\"INESISTENTE-%s\": \"x\"}".formatted(list))
                .andExpect(status().isBadRequest());
        saveProduct(admin, post("/api/products"), unique("P"), other, "{\"%s\": \"%s\"}".formatted(unique("TXTX"), "x"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/products").with(session(admin)).param("attribute", "NON-ESISTE").param("attributeValue", "x"))
                .andExpect(status().isBadRequest());

        saveAttribute(admin, put("/api/product-attributes/" + list), list, "Colore " + list, "LIST", other, "[\"Nero\"]", false)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
        saveProduct(admin, post("/api/products"), unique("P"), other, "{\"%s\": \"Nero\"}".formatted(list))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("disattivato")));
    }

    @Test
    void valuesInUseProtectOptionsScopeAndCategoryChanges() throws Exception {
        String admin = loginSuperAdmin();
        String root = createCategory(admin, unique("ROOT"), null);
        String child = createCategory(admin, unique("CHILD"), root);
        String sibling = createCategory(admin, unique("SIB"), null);
        String attribute = unique("MAT");
        saveAttribute(admin, post("/api/product-attributes"), attribute, "Materiale", "LIST", root, "[\"Pelle\", \"Tela\"]", null)
                .andExpect(status().isCreated());
        String product = unique("SCARPA");
        saveProduct(admin, post("/api/products"), product, child, "{\"%s\": \"pelle\"}".formatted(attribute))
                .andExpect(status().isCreated());

        saveAttribute(admin, put("/api/product-attributes/" + attribute), attribute, "Materiale", "LIST", root, "[\"Tela\"]", null)
                .andExpect(status().isConflict());
        saveAttribute(admin, put("/api/product-attributes/" + attribute), attribute, "Materiale", "LIST", sibling, "[\"Pelle\", \"Tela\"]", null)
                .andExpect(status().isConflict());
        saveAttribute(admin, put("/api/product-attributes/" + attribute), attribute, "Materiale principale", "LIST", child, "[\"Pelle\", \"Tela\", \"Gomma\"]", null)
                .andExpect(status().isOk());

        saveProduct(admin, put("/api/products/" + product), product, sibling, null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("svuotarlo")));
        saveProduct(admin, put("/api/products/" + product), product, child, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attributes[0].value").value("Pelle"));
        saveProduct(admin, put("/api/products/" + product), product, child, "{}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attributes").isEmpty());
        saveProduct(admin, put("/api/products/" + product), product, sibling, "{}")
                .andExpect(status().isOk());
    }

    @Test
    void customersReadActiveDefinitionsButCannotManageThem() throws Exception {
        String admin = loginSuperAdmin();
        String hidden = unique("HID");
        saveAttribute(admin, post("/api/product-attributes"), hidden, "Nascosto", "TEXT", null, "[]", false)
                .andExpect(status().isCreated());
        String customer = registerAndLoginCustomer();

        mockMvc.perform(get("/api/product-attributes").with(session(customer)).param("includeInactive", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].code", not(hasItem(hidden))));
        mockMvc.perform(get("/api/product-attributes").with(session(admin)).param("includeInactive", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].code", hasItem(hidden)));
        saveAttribute(customer, post("/api/product-attributes"), unique("NO"), "Vietato", "TEXT", null, "[]", null)
                .andExpect(status().isForbidden());
        saveAttribute(customer, put("/api/product-attributes/" + hidden), hidden, "Vietato", "TEXT", null, "[]", null)
                .andExpect(status().isForbidden());
    }

    private ResultActions saveAttribute(String token, org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request,
                                        String code, String label, String type, String categoryCode, String options, Boolean active) throws Exception {
        return mockMvc.perform(request
                .with(session(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "code": "%s",
                          "label": "%s",
                          "type": "%s",
                          "categoryCode": %s,
                          "options": %s,
                          "active": %s
                        }
                        """.formatted(code, label, type, categoryCode == null ? "null" : "\"" + categoryCode + "\"", options, active)));
    }

    private ResultActions saveProduct(String token, org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request,
                                      String code, String category, String attributes) throws Exception {
        return mockMvc.perform(request
                .with(session(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "code": "%s",
                          "name": "Prodotto %s",
                          "description": "Prodotto con attributi",
                          "category": "%s",
                          "brand": "Brand",
                          "productType": "Capo",
                          "price": 49.90,
                          "discount": 0,
                          "attributes": %s
                        }
                        """.formatted(code, code, category, attributes == null ? "null" : attributes)));
    }

    private String createCategory(String token, String code, String parentCode) throws Exception {
        mockMvc.perform(post("/api/product-categories")
                        .with(session(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "code": "%s", "name": "Categoria %s", "parentCode": %s }
                                """.formatted(code, code, parentCode == null ? "null" : "\"" + parentCode + "\"")))
                .andExpect(status().isCreated());
        return code;
    }

    private String loginSuperAdmin() throws Exception {
        return login("test_super_admin", SUPER_ADMIN_PASSWORD);
    }

    private String registerAndLoginCustomer() throws Exception {
        String username = "cliente_attr_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
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

    private static String unique(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
