package com.example.ticketsmodule.impl.carriers.conversion;

import com.example.ticketsmodule.api.model.CreateCarrierResponse;
import com.example.ticketsmodule.impl.carriers.domain.CarrierEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CarrierFromEntityConverter unit tests")
class CarrierFromEntityConverterTest {

    private CarrierFromEntityConverter converter;

    private UUID carrierId;

    @BeforeEach
    void setUp() {
        converter = new CarrierFromEntityConverter();
        carrierId = UUID.randomUUID();
    }

    // ---------- полное заполнение ----------

    @Nested
    @DisplayName("full entity")
    class FullEntityTests {

        @Test
        @DisplayName("should map all fields from entity to response")
        void shouldMapAllFields() {
            LocalDateTime now = LocalDateTime.now();

            CarrierEntity entity = CarrierEntity.builder()
                    .id(carrierId)
                    .name("Acme Transport")
                    .phone("+7-999-000-00-00")
                    .createdAt(now.minusDays(1))
                    .updatedAt(now)
                    .build();

            CreateCarrierResponse response = converter.convert(entity);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(carrierId);
            assertThat(response.getName()).isEqualTo("Acme Transport");
            assertThat(response.getPhone()).isEqualTo("+7-999-000-00-00");
            assertThat(response.getCreatedAt()).isEqualTo(now.minusDays(1));
            assertThat(response.getUpdatedAt()).isEqualTo(now);
        }
    }

    // ---------- edge cases ----------

    @Nested
    @DisplayName("edge cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should preserve null timestamps")
        void shouldPreserveNullTimestamps() {
            CarrierEntity entity = CarrierEntity.builder()
                    .id(carrierId)
                    .name("Acme")
                    .phone("+7-000")
                    .createdAt(null)
                    .updatedAt(null)
                    .build();

            CreateCarrierResponse response = converter.convert(entity);

            assertThat(response.getCreatedAt()).isNull();
            assertThat(response.getUpdatedAt()).isNull();
        }

        @Test
        @DisplayName("should handle empty strings")
        void shouldHandleEmptyStrings() {
            CarrierEntity entity = CarrierEntity.builder()
                    .id(carrierId)
                    .name("")
                    .phone("")
                    .build();

            CreateCarrierResponse response = converter.convert(entity);

            assertThat(response.getName()).isEmpty();
            assertThat(response.getPhone()).isEmpty();
        }

        @Test
        @DisplayName("should preserve null name and phone")
        void shouldPreserveNullStrings() {
            CarrierEntity entity = CarrierEntity.builder()
                    .id(carrierId)
                    .name(null)
                    .phone(null)
                    .build();

            CreateCarrierResponse response = converter.convert(entity);

            assertThat(response.getName()).isNull();
            assertThat(response.getPhone()).isNull();
        }
    }
}