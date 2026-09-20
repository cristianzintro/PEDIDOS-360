package cl.duoc.pedidos360.bff.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AudienceValidatorTest {

    private final AudienceValidator validator = new AudienceValidator("api://pedidos360");

    @Test
    void shouldAcceptExpectedAudience() {
        Jwt jwt = tokenWithAudience("api://pedidos360");

        assertThat(validator.validate(jwt).hasErrors()).isFalse();
    }

    @Test
    void shouldRejectUnexpectedAudience() {
        Jwt jwt = tokenWithAudience("api://another-api");

        assertThat(validator.validate(jwt).hasErrors()).isTrue();
    }

    private Jwt tokenWithAudience(String audience) {
        Instant now = Instant.now();
        return new Jwt("token", now, now.plusSeconds(300),
                java.util.Map.of("alg", "RS256"), java.util.Map.of("aud", List.of(audience)));
    }
}
