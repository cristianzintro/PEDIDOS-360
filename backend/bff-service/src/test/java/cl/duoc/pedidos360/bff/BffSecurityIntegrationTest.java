package cl.duoc.pedidos360.bff;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

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

    @Autowired
    private MockMvc mockMvc;

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
    void shouldReturn403WhenUserReadsAuditData() throws Exception {
        mockMvc.perform(get("/api/bff/events")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("SCOPE_access_as_user"),
                                new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void shouldReturn403WithoutRequiredScope() throws Exception {
        mockMvc.perform(get("/api/bff/me")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void shouldRejectUnsupportedHttpMethod() throws Exception {
        mockMvc.perform(patch("/api/bff/ots/OT-2026-000001")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("SCOPE_access_as_user"),
                                new SimpleGrantedAuthority("ROLE_ADMIN"))))
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
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("SCOPE_access_as_user"))
                                .jwt(token -> token
                                        .claim("preferred_username", "estudiante@ejemplo.cl")
                                        .claim("name", "Usuario Prueba")
                                        .claim("roles", java.util.List.of("USER")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Usuario Prueba"))
                .andExpect(jsonPath("$.roles[0]").value("USER"));
    }
}
