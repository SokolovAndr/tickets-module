package com.example.ticketsmodule.impl.carriers.conversion;

import com.example.ticketsmodule.api.model.CreateCarrierRequest;
import com.example.ticketsmodule.api.model.CreateCarrierResponse;
import com.example.ticketsmodule.domain.carrier.model.Carrier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CarrierMapper unit tests")
class CarrierMapperTest {

    private CarrierMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CarrierMapperImpl();
    }

    // ---------- toDomain() ----------

    @Nested
    @DisplayName("toDomain()")
    class ToDomainTests {

        @Test
        @DisplayName("should map name and phone from request")
        void shouldMapNameAndPhone() {
            CreateCarrierRequest request = new CreateCarrierRequest();
            request.setName("Acme Transport");
            request.setPhone("+7-999-000-00-00");

            Carrier carrier = mapper.toDomain(request);

            assertThat(carrier).isNotNull();
            assertThat(carrier.getName()).isEqualTo("Acme Transport");
            assertThat(carrier.getPhone()).isEqualTo("+7-999-000-00-00");
        }

        @Test
        @DisplayName("should ignore id, createdAt, updatedAt")
        void shouldIgnoreSystemFields() {
            CreateCarrierRequest request = new CreateCarrierRequest();
            request.setName("Acme");
            request.setPhone("+7-000");

            Carrier carrier = mapper.toDomain(request);

            assertThat(carrier.getId()).isNull();
            assertThat(carrier.getCreatedAt()).isNull();
            assertThat(carrier.getUpdatedAt()).isNull();
        }

        @Test
        @DisplayName("should return null when source is null")
        void shouldReturnNullWhenSourceNull() {
            Carrier carrier = mapper.toDomain(null);
            assertThat(carrier).isNull();
        }
    }

    // ---------- toResponse() ----------

    @Nested
    @DisplayName("toResponse()")
    class ToResponseTests {

        @Test
        @DisplayName("should map all fields from domain")
        void shouldMapAllFields() {
            UUID id = UUID.randomUUID();
            LocalDateTime created = LocalDateTime.now().minusDays(1);
            LocalDateTime updated = LocalDateTime.now();

            Carrier carrier = Carrier.builder()
                    .id(id)
                    .name("Acme Transport")
                    .phone("+7-999-000-00-00")
                    .createdAt(created)
                    .updatedAt(updated)
                    .build();

            CreateCarrierResponse response = mapper.toResponse(carrier);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(id);
            assertThat(response.getName()).isEqualTo("Acme Transport");
            assertThat(response.getPhone()).isEqualTo("+7-999-000-00-00");
            assertThat(response.getCreatedAt()).isEqualTo(created);
            assertThat(response.getUpdatedAt()).isEqualTo(updated);
        }

        @Test
        @DisplayName("should return null when source is null")
        void shouldReturnNullWhenSourceNull() {
            CreateCarrierResponse response = mapper.toResponse(null);
            assertThat(response).isNull();
        }
    }
}