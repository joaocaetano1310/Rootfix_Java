package br.com.fiap.rootfix.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rootfix.jwt")
public record JwtProperties(String secret, String issuer, long expiracaoMinutos) {
}
