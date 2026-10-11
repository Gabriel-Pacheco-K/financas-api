package com.gabrielpacheco.financas.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testa a API de ponta a ponta: HTTP, segurança, validação, JPA e migrações Flyway.
 */
@SpringBootTest
@AutoConfigureMockMvc
class FinancasApiIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    private String token;

    @BeforeEach
    void registerUser() throws Exception {
        token = register(UUID.randomUUID() + "@email.com");
    }

    @Test
    void protectedRoutesRequireToken() throws Exception {
        mvc.perform(get("/api/transactions")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/transactions").header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void duplicateEmailIsRejected() throws Exception {
        String email = UUID.randomUUID() + "@email.com";
        register(email);

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("name", "Outro", "email", email.toUpperCase(), "password", "senha-segura"))))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void wrongPasswordReturns401() throws Exception {
        String email = UUID.randomUUID() + "@email.com";
        register(email);

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("email", email, "password", "senha-errada"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidPayloadReturnsFieldErrors() throws Exception {
        long categoryId = createCategory("Mercado", "EXPENSE");

        mvc.perform(auth(post("/api/transactions"))
                        .content(body(Map.of("description", "", "amount", -10, "date", "2026-10-05", "categoryId", categoryId))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.description").exists())
                .andExpect(jsonPath("$.errors.amount").exists());
    }

    @Test
    void fullFlowProducesMonthlyReport() throws Exception {
        long salary = createCategory("Salário", "INCOME");
        long rent = createCategory("Aluguel", "EXPENSE");
        long market = createCategory("Mercado", "EXPENSE");

        createTransaction("Salário outubro", "5000.00", "2026-10-05", salary);
        createTransaction("Aluguel", "1500.00", "2026-10-10", rent);
        createTransaction("Compra do mês", "800.00", "2026-10-12", market);
        createTransaction("Mercado setembro", "999.00", "2026-09-28", market);

        mvc.perform(auth(get("/api/reports/monthly").param("month", "2026-10")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(5000.00))
                .andExpect(jsonPath("$.totalExpense").value(2300.00))
                .andExpect(jsonPath("$.balance").value(2700.00))
                .andExpect(jsonPath("$.savingsRate").value(54.00))
                .andExpect(jsonPath("$.byCategory", hasSize(3)));

        mvc.perform(auth(get("/api/transactions"))
                        .param("type", "EXPENSE").param("start", "2026-10-01").param("end", "2026-10-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].description").value("Compra do mês"))
                .andExpect(jsonPath("$.page.totalElements").value(2));
    }

    @Test
    void usersCannotSeeEachOthersData() throws Exception {
        long category = createCategory("Mercado", "EXPENSE");
        long transaction = createTransaction("Compra", "50.00", "2026-10-01", category);

        token = register(UUID.randomUUID() + "@email.com");

        mvc.perform(auth(get("/api/transactions/" + transaction))).andExpect(status().isNotFound());
        mvc.perform(auth(delete("/api/categories/" + category))).andExpect(status().isNotFound());
        mvc.perform(auth(get("/api/transactions"))).andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void categoryInUseCannotBeDeleted() throws Exception {
        long category = createCategory("Mercado", "EXPENSE");
        createTransaction("Compra", "50.00", "2026-10-01", category);

        mvc.perform(auth(delete("/api/categories/" + category))).andExpect(status().isUnprocessableEntity());
    }

    private String register(String email) throws Exception {
        String response = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("name", "Gabriel", "email", email, "password", "senha-segura"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(response).get("token").asText();
    }

    private long createCategory(String name, String type) throws Exception {
        return idOf(mvc.perform(auth(post("/api/categories")).content(body(Map.of("name", name, "type", type))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
    }

    private long createTransaction(String description, String amount, String date, long categoryId) throws Exception {
        return idOf(mvc.perform(auth(post("/api/transactions"))
                        .content(body(Map.of("description", description, "amount", amount, "date", date, "categoryId", categoryId))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
    }

    private MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder request) {
        return request.header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON);
    }

    private String body(Object value) throws Exception {
        return json.writeValueAsString(value);
    }

    private long idOf(String response) throws Exception {
        JsonNode node = json.readTree(response);
        return node.get("id").asLong();
    }
}
