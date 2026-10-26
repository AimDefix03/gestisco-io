package it.giovannidefilippo.gestionale.product;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductBarcodeControllerTest {
    private static final String SUPER_ADMIN_PASSWORD = "Test-Bootstrap-9842!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void storesBarcodeAndUnitAndFindsProductByExactBarcode() throws Exception {
        String token = login();
        String code = unique("SHOE");
        String barcode = internalBarcode();

        saveProduct(token, null, code, barcode, "PAIR")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.barcode").value(barcode))
                .andExpect(jsonPath("$.unitOfMeasure").value("PAIR"))
                .andExpect(jsonPath("$.unitLabel").value("Paio"));

        mockMvc.perform(get("/api/products").header("X-Session-Token", token).param("q", barcode.toLowerCase()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].code").value(code));
    }

    @Test
    void defaultsToPieceAndAllowsRemovingTheBarcode() throws Exception {
        String token = login();
        String code = unique("PIECE");

        saveProduct(token, null, code, "  ", null)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.barcode").doesNotExist())
                .andExpect(jsonPath("$.unitOfMeasure").value("PIECE"));
        saveProduct(token, code, code, internalBarcode(), "PIECE").andExpect(status().isOk());
        saveProduct(token, code, code, null, "PIECE")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.barcode").doesNotExist());
    }

    @Test
    void rejectsDuplicateAndMistypedBarcodes() throws Exception {
        String token = login();
        String barcode = internalBarcode();
        saveProduct(token, null, unique("FIRST"), barcode, null).andExpect(status().isCreated());

        saveProduct(token, null, unique("SECOND"), barcode.toLowerCase(), null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Esiste gia un prodotto con questo codice a barre."));
        saveProduct(token, null, unique("TYPO"), "4006381333932", null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Codice a barre 4006381333932 non valido: cifra di controllo errata."));
        saveProduct(token, null, unique("UNIT"), null, "BOX")
                .andExpect(status().isBadRequest());
    }

    private ResultActions saveProduct(String token, String existingCode, String code, String barcode, String unit) throws Exception {
        java.util.Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("code", code);
        body.put("name", "Prodotto " + code);
        body.put("description", "Prodotto per il contratto dei codici a barre.");
        body.put("category", "HARDWARE");
        body.put("brand", "BarcodeBrand");
        body.put("productType", "Calzatura");
        body.put("usageContext", "");
        body.put("price", 80.00);
        body.put("discount", 0);
        body.put("barcode", barcode);
        body.put("unitOfMeasure", unit);
        String json = objectMapper.writeValueAsString(body);
        return existingCode == null
                ? mockMvc.perform(post("/api/products").header("X-Session-Token", token).contentType(MediaType.APPLICATION_JSON).content(json))
                : mockMvc.perform(put("/api/products/" + existingCode).header("X-Session-Token", token).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private static String unique(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private static String internalBarcode() {
        return "INT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private String login() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/accounts/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "test_super_admin",
                                  "password": "%s",
                                  "role": "SUPER_ADMIN"
                                }
                                """.formatted(SUPER_ADMIN_PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("token").asText();
    }
}
