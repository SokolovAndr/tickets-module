package com.example.ticketsmoduleimpl.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String secret;
    private String issuer;
    private Duration accessTokenTtl;
    private Duration refreshTokenTtl;
}