package cl.duoc.pedidos360.bff.config;

import cl.duoc.pedidos360.bff.security.AudienceValidator;
import cl.duoc.pedidos360.bff.security.EntraJwtAuthenticationConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.security.authorization.AuthorizationManagers.allOf;
import static org.springframework.security.authorization.AuthorityAuthorizationManager.hasAnyRole;
import static org.springframework.security.authorization.AuthorityAuthorizationManager.hasAuthority;

@Configuration
public class SecurityConfig {

    private final ObjectMapper objectMapper;

    public SecurityConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        AuthorizationManager<RequestAuthorizationContext> userAccess = allOf(
                hasAuthority("SCOPE_access_as_user"), hasAnyRole("USER", "ADMIN"));
        AuthorizationManager<RequestAuthorizationContext> adminAccess = allOf(
                hasAuthority("SCOPE_access_as_user"), hasAnyRole("ADMIN"));

        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/actuator/health", "/api/bff/health").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/bff/ots", "/api/bff/ots/**")
                        .access(userAccess)
                        .requestMatchers(HttpMethod.HEAD, "/api/bff/ots", "/api/bff/ots/**")
                        .access(userAccess)
                        .requestMatchers(HttpMethod.POST, "/api/bff/ots", "/api/bff/ots/*/items")
                        .access(userAccess)
                        .requestMatchers(HttpMethod.GET,
                                "/api/bff/events", "/api/bff/events/**",
                                "/api/bff/notifications", "/api/bff/notifications/**")
                        .access(adminAccess)
                        .requestMatchers(HttpMethod.HEAD,
                                "/api/bff/events", "/api/bff/events/**",
                                "/api/bff/notifications", "/api/bff/notifications/**")
                        .access(adminAccess)
                        .requestMatchers("/api/bff/me").hasAuthority("SCOPE_access_as_user")
                        .anyRequest().denyAll())
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, exception) ->
                                writeSecurityError(response, 401, "Unauthorized",
                                        "Token JWT ausente, expirado o invalido", request.getRequestURI()))
                        .accessDeniedHandler((request, response, exception) ->
                                writeSecurityError(response, 403, "Forbidden",
                                        "El usuario no tiene el rol requerido", request.getRequestURI())))
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(new EntraJwtAuthenticationConverter()))
                        .authenticationEntryPoint((request, response, exception) ->
                                writeSecurityError(response, 401, "Unauthorized",
                                        "Token JWT ausente, expirado o invalido", request.getRequestURI())))
                .build();
    }

    @Bean
    JwtDecoder jwtDecoder(@Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") String jwkSetUri,
                          @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuer,
                          @Value("${security.oauth2.audience}") String audience) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        OAuth2TokenValidator<Jwt> issuerAndTime = JwtValidators.createDefaultWithIssuer(issuer);
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                issuerAndTime, new AudienceValidator(audience)));
        return decoder;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${security.cors.allowed-origins}") String allowedOrigins) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toList());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setExposedHeaders(List.of("Location"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private void writeSecurityError(HttpServletResponse response, int status, String error,
                                    String message, String path) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", status);
        body.put("error", error);
        body.put("message", message);
        body.put("path", path);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
