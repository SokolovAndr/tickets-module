package com.example.ticketsmodule.impl.users.conversion;

import com.example.ticketsmodule.api.model.RegisterRequest;
import com.example.ticketsmodule.domain.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RegisterMapper unit tests")
class RegisterMapperTest {

    private RegisterMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new RegisterMapperImpl();
    }

    @Nested
    @DisplayName("field mapping")
    class FieldMappingTests {

        @Test
        @DisplayName("should map login and fullName from request")
        void shouldMapFields() {
            RegisterRequest request = new RegisterRequest();
            request.setLogin("newuser");
            request.setPassword("secret");
            request.setFullName("New User");

            User domain = mapper.toDomain(request); // <-- toDomain!

            assertThat(domain).isNotNull();
            assertThat(domain.getLogin()).isEqualTo("newuser");
            assertThat(domain.getFullName()).isEqualTo("New User");
        }
    }

    @Nested
    @DisplayName("ignored fields")
    class IgnoredFieldsTests {

        @Test
        @DisplayName("should ignore id, password, role, timestamps")
        void shouldIgnoreFields() {
            RegisterRequest request = new RegisterRequest();
            request.setLogin("newuser");
            request.setPassword("raw-secret");
            request.setFullName("New User");

            User domain = mapper.toDomain(request);

            assertThat(domain.getId()).isNull();
            assertThat(domain.getPassword()).isNull();
            assertThat(domain.getRole()).isNull();
            assertThat(domain.getCreatedAt()).isNull();
            assertThat(domain.getUpdatedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("edge cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should return null when source is null")
        void shouldReturnNullWhenSourceNull() {
            User domain = mapper.toDomain(null);

            assertThat(domain).isNull();
        }
    }
}