package it.giovannidefilippo.gestionale.product;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static it.giovannidefilippo.gestionale.support.SessionRequests.session;
import static it.giovannidefilippo.gestionale.support.SessionRequests.sessionToken;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductLookupControllerTest {
    private static final String SUPER_ADMIN_PASSWORD = "Test-Bootstrap-9842!";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void productLookupRequiresCatalogPermission() throws Exception {
        mockMvc.perform(get("/api/products/lookup"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_UNAUTHORIZED"));
    }

    @Test
    void productLookupReturnsLightweightCatalogData() throws Exception {
        String token = login("test_super_admin", SUPER_ADMIN_PASSWORD, "SUPER_ADMIN");
        String code = "LOOK-" + UUID.randomUUID().toString().substring(0, 8);
        createProduct(token, code);

        mockMvc.perform(get("/api/products/lookup").with(session(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].code", hasItem(code)))
                .andExpect(jsonPath("$[*].brand", hasItem("LookupBrand")))
                .andExpect(jsonPath("$[*].productType", hasItem("Scheda lookup")))
                .andExpect(jsonPath("$[0].description").doesNotExist())
                .andExpect(jsonPath("$[0].price").doesNotExist())
                .andExpect(jsonPath("$[0].reservedQuantity").doesNotExist())
                .andExpect(jsonPath("$[*].code", not(hasItem(""))));
    }

    private String login(String username, String password, String role) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/accounts/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s",
                                  "role": "%s"
                                }
                                """.formatted(username, password, role)))
                .andExpect(status().isOk())
                .andReturn();
        return sessionToken(result);
    }

    private void createProduct(String token, String code) throws Exception {
        mockMvc.perform(post("/api/products")
                        .with(session(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "%s",
                                  "name": "Prodotto lookup",
                                  "description": "Prodotto usato per verificare il lookup leggero.",
                                  "category": "HARDWARE",
                                  "brand": "LookupBrand",
                                  "productType": "Scheda lookup",
                                  "usageContext": "",
                                  "price": 90.00,
                                  "discount": 0.00
                                }
                                """.formatted(code)))
                .andExpect(status().isCreated());
    }
}
