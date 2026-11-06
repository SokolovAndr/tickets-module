package com.example.ticketsmodule.impl.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    @NotBlank(message = "JWT_SECRET environment variable must be set and not empty")
    @Size(min = 44, message = "JWT_SECRET must be at least 44 characters long (HS256 requires 256 bits)")
    private String secret;

    private String issuer;

    private Duration accessTokenTtl;

    private Duration refreshTokenTtl;
}