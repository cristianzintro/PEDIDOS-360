package cl.duoc.pedidos360.bff.controller;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bff")
public class StatusController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("service", "bff-service", "status", "UP");
    }

    @GetMapping("/me")
    public Map<String, Object> me(JwtAuthenticationToken authentication) {
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("name", authentication.getToken().getClaimAsString("name"));
        profile.put("username", authentication.getName());
        profile.put("roles", authentication.getToken().getClaimAsStringList("roles") == null
                ? List.of() : authentication.getToken().getClaimAsStringList("roles"));
        profile.put("scopes", authentication.getToken().getClaimAsString("scp"));
        return profile;
    }
}
