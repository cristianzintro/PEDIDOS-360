package cl.duoc.pedidos360.bff;

import cl.duoc.pedidos360.bff.security.EntraJwtAuthenticationConverter;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD;
import static org.springframework.http.HttpHeaders.ORIGIN;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BffSecurityIntegrationTest {

    private static final EntraJwtAuthenticationConverter JWT_CONVERTER =
            new EntraJwtAuthenticationConverter();
    private static final HttpServer DOWNSTREAM = startDownstream();

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void downstreamUrls(DynamicPropertyRegistry registry) {
        String baseUrl = "http://127.0.0.1:" + DOWNSTREAM.getAddress().getPort();
        registry.add("services.ot.url", () -> baseUrl);
        registry.add("services.audit.url", () -> baseUrl);
    }

    @AfterAll
    static void stopDownstream() {
        DOWNSTREAM.stop(0);
    }

    @Test
    void shouldExposePublicHealth() throws Exception {
        mockMvc.perform(get("/api/bff/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void shouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/bff/ots"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void shouldAllowAdminToReadOts() throws Exception {
        mockMvc.perform(get("/api/bff/ots").with(jwtWith("access_as_user", "ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowUserToReadOts() throws Exception {
        mockMvc.perform(get("/api/bff/ots").with(jwtWith("access_as_user", "USER")))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn403WithoutRequiredRole() throws Exception {
        mockMvc.perform(get("/api/bff/ots").with(jwtWith("access_as_user")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void shouldAllowAdminToReadAuditData() throws Exception {
        mockMvc.perform(get("/api/bff/events").with(jwtWith("access_as_user", "ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn403WhenUserReadsAuditData() throws Exception {
        mockMvc.perform(get("/api/bff/events")
                        .with(jwtWith("access_as_user", "USER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void shouldReturn403WithoutRequiredScope() throws Exception {
        mockMvc.perform(get("/api/bff/me")
                        .with(jwtWith(null, "USER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void shouldRejectUnsupportedHttpMethod() throws Exception {
        mockMvc.perform(patch("/api/bff/ots/OT-2026-000001")
                        .with(jwtWith("access_as_user", "ADMIN")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void shouldAllowCorsPreflightFromConfiguredOrigin() throws Exception {
        mockMvc.perform(options("/api/bff/ots")
                        .header(ORIGIN, "http://localhost:4200")
                        .header(ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200"))
                .andExpect(header().string(ACCESS_CONTROL_ALLOW_METHODS, containsString("GET")));
    }

    @Test
    void shouldReadAuthenticatedUserClaims() throws Exception {
        mockMvc.perform(get("/api/bff/me")
                        .with(jwtWith("access_as_user", token -> token
                                        .claim("preferred_username", "estudiante@ejemplo.cl")
                                        .claim("name", "Usuario Prueba"), "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Usuario Prueba"))
                .andExpect(jsonPath("$.roles[0]").value("USER"));
    }

    private static JwtRequestPostProcessor jwtWith(String scope, String... roles) {
        return jwtWith(scope, token -> { }, roles);
    }

    private static JwtRequestPostProcessor jwtWith(String scope, Consumer<Jwt.Builder> claims,
                                                   String... roles) {
        return jwt()
                .jwt(token -> {
                    if (scope != null) {
                        token.claim("scp", scope);
                    }
                    token.claim("roles", List.of(roles));
                    claims.accept(token);
                })
                .authorities(token -> Objects.requireNonNull(JWT_CONVERTER.convert(token)).getAuthorities());
    }

    private static HttpServer startDownstream() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/", exchange -> {
                byte[] body = "[]".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
                exchange.close();
            });
            server.start();
            return server;
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo iniciar el downstream de prueba", exception);
        }
    }
}
