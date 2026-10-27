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

import static it.giovannidefilippo.gestionale.support.SessionRequests.session;
import static it.giovannidefilippo.gestionale.support.SessionRequests.sessionToken;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductCategoryControllerTest {
    private static final String SUPER_ADMIN_PASSWORD = "Test-Bootstrap-9842!";
    private static final String CUSTOMER_PASSWORD = "CustomerStrong123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void staffBuildsHierarchyWithPathsAndAudit() throws Exception {
        String employee = createAndLoginStaff("EMPLOYEE");
        String root = unique("ABB");
        String child = unique("PANT");

        saveCategory(employee, root, "Abbigliamento " + root, null, null).andExpect(status().isCreated())
                .andExpect(jsonPath("$.depth").value(1))
                .andExpect(jsonPath("$.parentCode").doesNotExist());
        saveCategory(employee, child, "Pantaloni", root, null).andExpect(status().isCreated())
                .andExpect(jsonPath("$.path").value("Abbigliamento " + root + " / Pantaloni"))
                .andExpect(jsonPath("$.parentCode").value(root))
                .andExpect(jsonPath("$.depth").value(2));

        mockMvc.perform(get("/api/product-categories").with(session(employee)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].code", hasItem(child)))
                .andExpect(jsonPath("$[*].code", hasItem("HARDWARE")));

        mockMvc.perform(get("/api/audit").with(session(login("test_super_admin", SUPER_ADMIN_PASSWORD, "SUPER_ADMIN"))).param("q", child))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].action").value("CREATE_PRODUCT_CATEGORY"));
    }

    @Test
    void codesAndSiblingNamesAreUniqueIgnoringCase() throws Exception {
        String employee = createAndLoginStaff("EMPLOYEE");
        String first = unique("SCARPE");
        String parent = unique("UOMO");
        saveCategory(employee, first, "Scarpe " + first, null, null).andExpect(status().isCreated());
        saveCategory(employee, parent, "Uomo " + parent, null, null).andExpect(status().isCreated());

        saveCategory(employee, first.toLowerCase(), "Altro nome " + first, null, null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESOURCE_CONFLICT"));
        saveCategory(employee, unique("DUP"), "  SCARPE " + first.toLowerCase() + " ", null, null)
                .andExpect(status().isConflict());
        saveCategory(employee, unique("SAME"), "Scarpe " + first, parent, null)
                .andExpect(status().isCreated());
    }

    @Test
    void hierarchyRejectsCyclesExcessiveDepthAndCodeChanges() throws Exception {
        String employee = createAndLoginStaff("EMPLOYEE");
        String level1 = unique("L1");
        String level2 = unique("L2");
        String level3 = unique("L3");
        saveCategory(employee, level1, "Livello " + level1, null, null).andExpect(status().isCreated());
        saveCategory(employee, level2, "Livello " + level2, level1, null).andExpect(status().isCreated());
        saveCategory(employee, level3, "Livello " + level3, level2, null).andExpect(status().isCreated());

        saveCategory(employee, unique("L4"), "Troppo profondo", level3, null)
                .andExpect(status().isBadRequest());
        updateCategory(employee, level1, level1, "Livello " + level1, level3, null)
                .andExpect(status().isBadRequest());
        updateCategory(employee, level1, level1, "Livello " + level1, level1, null)
                .andExpect(status().isBadRequest());
        updateCategory(employee, level2, unique("ALTRO"), "Livello " + level2, level1, null)
                .andExpect(status().isBadRequest());

        String leaf = unique("FOGLIA");
        saveCategory(employee, leaf, "Foglia " + leaf, null, null).andExpect(status().isCreated());
        updateCategory(employee, leaf, leaf, "Foglia " + leaf, leaf, null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Una categoria non puo essere spostata sotto se stessa o una sua sottocategoria."));

        String other = unique("ALTRA");
        saveCategory(employee, other, "Altra radice " + other, null, null).andExpect(status().isCreated());
        updateCategory(employee, level2, level2, "Livello " + level2, other, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.path").value("Altra radice " + other + " / Livello " + level2));
        mockMvc.perform(get("/api/product-categories").with(session(employee)))
                .andExpect(jsonPath("$[?(@.code == '" + level3 + "')].path").value(hasItem("Altra radice " + other + " / Livello " + level2 + " / Livello " + level3)));
    }

    @Test
    void deactivationProtectsChildrenNewProductsAndCustomerView() throws Exception {
        String superAdmin = login("test_super_admin", SUPER_ADMIN_PASSWORD, "SUPER_ADMIN");
        String employee = createAndLoginStaff("EMPLOYEE");
        String customer = registerAndLoginCustomer();
        String parent = unique("ESTATE");
        String child = unique("COSTUMI");
        saveCategory(employee, parent, "Estate " + parent, null, null).andExpect(status().isCreated());
        saveCategory(employee, child, "Costumi", parent, null).andExpect(status().isCreated());
        String existingProduct = unique("PRD-COSTUME");
        createProduct(superAdmin, existingProduct, child).andExpect(status().isCreated());

        updateCategory(employee, parent, parent, "Estate " + parent, null, false)
                .andExpect(status().isConflict());
        updateCategory(employee, child, child, "Costumi", parent, false)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
        updateCategory(employee, parent, parent, "Estate " + parent, null, false)
                .andExpect(status().isOk());
        updateCategory(employee, child, child, "Costumi", parent, true)
                .andExpect(status().isBadRequest());

        createProduct(superAdmin, unique("PRD-NEW"), child)
                .andExpect(status().isBadRequest());
        createProduct(superAdmin, unique("PRD-MISSING"), unique("NESSUNA"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/products/" + existingProduct)
                        .with(session(superAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson(existingProduct, child, "Costume rinominato")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value(child));

        mockMvc.perform(get("/api/product-categories").with(session(employee)).param("includeInactive", "true"))
                .andExpect(jsonPath("$[*].code", hasItem(child)));
        mockMvc.perform(get("/api/product-categories").with(session(employee)))
                .andExpect(jsonPath("$[*].code", not(hasItem(child))));
        mockMvc.perform(get("/api/product-categories").with(session(customer)).param("includeInactive", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].code", not(hasItem(child))));
    }

    @Test
    void filtersIncludeSubcategoriesAndExposeCategoryPath() throws Exception {
        String superAdmin = login("test_super_admin", SUPER_ADMIN_PASSWORD, "SUPER_ADMIN");
        String parent = unique("DONNA");
        String child = unique("GONNE");
        String sibling = unique("BORSE");
        saveCategory(superAdmin, parent, "Donna " + parent, null, null).andExpect(status().isCreated());
        saveCategory(superAdmin, child, "Gonne", parent, null).andExpect(status().isCreated());
        saveCategory(superAdmin, sibling, "Borse " + sibling, null, null).andExpect(status().isCreated());
        String inChild = unique("PRD-GONNA");
        String inSibling = unique("PRD-BORSA");
        createProduct(superAdmin, inChild, child).andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoryName").value("Donna " + parent + " / Gonne"));
        createProduct(superAdmin, inSibling, sibling).andExpect(status().isCreated());

        mockMvc.perform(get("/api/products").with(session(superAdmin)).param("category", parent).param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].code", hasItem(inChild)))
                .andExpect(jsonPath("$.content[*].code", not(hasItem(inSibling))));
        mockMvc.perform(get("/api/products").with(session(superAdmin)).param("category", unique("INESISTENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
        mockMvc.perform(get("/api/reports/inventory").with(session(superAdmin)).param("category", parent))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products[0].code").value(inChild))
                .andExpect(jsonPath("$.products[0].categoryLabel").value("Donna " + parent + " / Gonne"));
    }

    @Test
    void writesRequireProductManagementPermission() throws Exception {
        String customer = registerAndLoginCustomer();

        mockMvc.perform(get("/api/product-categories")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/product-categories").with(session(customer))).andExpect(status().isOk());
        saveCategory(customer, unique("CLIENTE"), "Non consentita", null, null).andExpect(status().isForbidden());
        updateCategory(customer, "HARDWARE", "HARDWARE", "Hardware", null, false).andExpect(status().isForbidden());
    }

    private ResultActions saveCategory(String token, String code, String name, String parentCode, Boolean active) throws Exception {
        return mockMvc.perform(post("/api/product-categories")
                .with(session(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(categoryJson(code, name, parentCode, active)));
    }

    private ResultActions updateCategory(String token, String path, String code, String name, String parentCode, Boolean active) throws Exception {
        return mockMvc.perform(put("/api/product-categories/" + path)
                .with(session(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(categoryJson(code, name, parentCode, active)));
    }

    private String categoryJson(String code, String name, String parentCode, Boolean active) throws Exception {
        java.util.Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("code", code);
        body.put("name", name);
        body.put("parentCode", parentCode);
        body.put("active", active);
        return objectMapper.writeValueAsString(body);
    }

    private ResultActions createProduct(String token, String code, String category) throws Exception {
        return mockMvc.perform(post("/api/products")
                .with(session(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson(code, category, "Prodotto categoria")));
    }

    private String productJson(String code, String category, String name) {
        return """
                {
                  "code": "%s",
                  "name": "%s",
                  "description": "Prodotto per il contratto delle categorie.",
                  "category": "%s",
                  "brand": "CategoryBrand",
                  "productType": "Capo",
                  "usageContext": "",
                  "price": 50.00,
                  "discount": 0
                }
                """.formatted(code, name, category);
    }

    private static String unique(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private String registerAndLoginCustomer() throws Exception {
        String username = "category_customer_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        mockMvc.perform(post("/api/accounts/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s"
                                }
                                """.formatted(username, CUSTOMER_PASSWORD)))
                .andExpect(status().isCreated());
        return login(username, CUSTOMER_PASSWORD, "CUSTOMER");
    }

    private String createAndLoginStaff(String role) throws Exception {
        String superAdminToken = login("test_super_admin", SUPER_ADMIN_PASSWORD, "SUPER_ADMIN");
        String username = role.toLowerCase() + "_category_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String password = "StaffStrong123!";
        mockMvc.perform(post("/api/accounts")
                        .with(session(superAdminToken))
                        .header("X-Reauth-Password", SUPER_ADMIN_PASSWORD)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s",
                                  "role": "%s"
                                }
                                """.formatted(username, password, role)))
                .andExpect(status().isCreated());
        return login(username, password, role);
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
}
