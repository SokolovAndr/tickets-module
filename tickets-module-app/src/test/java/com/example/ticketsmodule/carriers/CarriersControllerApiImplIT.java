package com.example.ticketsmodule.carriers;

import com.example.ticketsmodule.api.model.CarrierPatchRequest;
import com.example.ticketsmodule.api.model.CreateCarrierRequest;
import com.example.ticketsmodule.impl.carriers.domain.CarrierEntity;
import com.example.ticketsmodule.impl.carriers.repository.CarriersRepository;
import com.example.ticketsmodule.support.JwtTestHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("CarriersControllerApiImpl integration tests")
class CarriersControllerApiImplIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private CarriersRepository carriersRepository;

    @BeforeEach
    void cleanDb() {
        carriersRepository.deleteAll();
    }

    // ---------- Security ----------

    @Nested
    @DisplayName("security")
    class SecurityTests {

        @Test
        @DisplayName("GET /api/carriers without auth → 401")
        void shouldReturn401WithoutAuth() throws Exception {
            mockMvc.perform(get("/api/carriers"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/carriers with USER role → 200")
        void shouldAllowUserRole() throws Exception {
            mockMvc.perform(get("/api/carriers")
                            .with(JwtTestHelper.withRole("USER")))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST /api/carriers with USER role → 403 (only ADMIN)")
        void shouldForbidUserToCreateCarrier() throws Exception {
            CreateCarrierRequest request = new CreateCarrierRequest();
            request.setName("Test");
            request.setPhone("+7-000");

            mockMvc.perform(post("/api/carriers")
                            .with(JwtTestHelper.withRole("USER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    // ---------- CREATE ----------

    @Nested
    @DisplayName("POST /api/carriers")
    class CreateTests {

        @Test
        @DisplayName("should create carrier and return 201")
        void shouldCreateCarrier() throws Exception {
            CreateCarrierRequest request = new CreateCarrierRequest();
            request.setName("Acme Transport");
            request.setPhone("+7-999-000-00-00");

            mockMvc.perform(post("/api/carriers")
                            .with(JwtTestHelper.withRole("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("Acme Transport"))
                    .andExpect(jsonPath("$.phone").value("+7-999-000-00-00"))
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.updatedAt").exists());

            assertThat(carriersRepository.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("should return 400 when name is blank")
        void shouldReturn400WhenNameBlank() throws Exception {
            CreateCarrierRequest request = new CreateCarrierRequest();
            request.setName("");
            request.setPhone("+7-999");

            mockMvc.perform(post("/api/carriers")
                            .with(JwtTestHelper.withRole("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            assertThat(carriersRepository.findAll()).isEmpty();
        }

        @Test
        @DisplayName("should return 400 when name is duplicate")
        void shouldReturn400WhenDuplicateName() throws Exception {
            CarrierEntity existing = CarrierEntity.builder()
                    .name("Acme Transport")
                    .phone("+7-111")
                    .build();
            carriersRepository.save(existing);

            CreateCarrierRequest request = new CreateCarrierRequest();
            request.setName("Acme Transport");
            request.setPhone("+7-222");

            mockMvc.perform(post("/api/carriers")
                            .with(JwtTestHelper.withRole("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ---------- READ ----------

    @Nested
    @DisplayName("GET /api/carriers/{id}")
    class ReadTests {

        @Test
        @DisplayName("should return 200 with carrier")
        void shouldReturnCarrier() throws Exception {
            CarrierEntity saved = carriersRepository.save(
                    CarrierEntity.builder()
                            .name("Acme")
                            .phone("+7-999")
                            .build());

            mockMvc.perform(get("/api/carriers/{id}", saved.getId())
                            .with(JwtTestHelper.withRole("USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(saved.getId().toString()))
                    .andExpect(jsonPath("$.name").value("Acme"))
                    .andExpect(jsonPath("$.phone").value("+7-999"));
        }

        @Test
        @DisplayName("should return 404 when carrier not found")
        void shouldReturn404WhenNotFound() throws Exception {
            mockMvc.perform(get("/api/carriers/{id}", UUID.randomUUID())
                            .with(JwtTestHelper.withRole("USER")))
                    .andExpect(status().isNotFound());
        }
    }

    // ---------- LIST ----------

    @Nested
    @DisplayName("GET /api/carriers")
    class ListTests {

        @Test
        @DisplayName("should return 200 with empty list")
        void shouldReturnEmptyList() throws Exception {
            mockMvc.perform(get("/api/carriers")
                            .with(JwtTestHelper.withRole("USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)));
        }

        @Test
        @DisplayName("should return 200 with list of carriers")
        void shouldReturnList() throws Exception {
            carriersRepository.save(CarrierEntity.builder()
                    .name("Acme")
                    .phone("+7-111").build());
            carriersRepository.save(CarrierEntity.builder()
                    .name("Beta")
                    .phone("+7-222").build());

            mockMvc.perform(get("/api/carriers")
                            .with(JwtTestHelper.withRole("USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)));
        }

        @Test
        @DisplayName("should filter by name")
        void shouldFilterByName() throws Exception {
            carriersRepository.save(CarrierEntity.builder()
                    .name("Acme").phone("+7-111").build());
            carriersRepository.save(CarrierEntity.builder()
                    .name("Beta").phone("+7-222").build());

            mockMvc.perform(get("/api/carriers")
                            .param("name", "acme")
                            .with(JwtTestHelper.withRole("USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].name").value("Acme"));
        }
    }

    // ---------- PATCH ----------

    @Nested
    @DisplayName("PATCH /api/carriers/{id}")
    class PatchTests {

        @Test
        @DisplayName("should patch name and return 200")
        void shouldPatchName() throws Exception {
            CarrierEntity saved = carriersRepository.save(
                    CarrierEntity.builder()
                            .name("Old Name")
                            .phone("+7-111")
                            .build());

            CarrierPatchRequest patch = new CarrierPatchRequest();
            patch.setName("New Name");

            mockMvc.perform(patch("/api/carriers/{id}", saved.getId())
                            .with(JwtTestHelper.withRole("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(patch)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("New Name"))
                    .andExpect(jsonPath("$.phone").value("+7-111"));

            CarrierEntity updated = carriersRepository.findById(saved.getId()).orElseThrow();
            assertThat(updated.getName()).isEqualTo("New Name");
        }

        @Test
        @DisplayName("should return 404 when carrier not found")
        void shouldReturn404WhenNotFound() throws Exception {
            CarrierPatchRequest patch = new CarrierPatchRequest();
            patch.setName("New");

            mockMvc.perform(patch("/api/carriers/{id}", UUID.randomUUID())
                            .with(JwtTestHelper.withRole("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(patch)))
                    .andExpect(status().isNotFound());
        }
    }
}