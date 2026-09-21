package cl.duoc.pedidos360.bff.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.ArrayList;
import java.util.Collection;

public class EntraJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtAuthenticationConverter delegate;
    private final JwtGrantedAuthoritiesConverter scopeConverter;
    private final JwtGrantedAuthoritiesConverter roleConverter;

    public EntraJwtAuthenticationConverter() {
        scopeConverter = new JwtGrantedAuthoritiesConverter();
        scopeConverter.setAuthoritiesClaimName("scp");
        scopeConverter.setAuthorityPrefix("SCOPE_");

        roleConverter = new JwtGrantedAuthoritiesConverter();
        roleConverter.setAuthoritiesClaimName("roles");
        roleConverter.setAuthorityPrefix("ROLE_");

        delegate = new JwtAuthenticationConverter();
        delegate.setPrincipalClaimName("preferred_username");
        delegate.setJwtGrantedAuthoritiesConverter(this::authorities);
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        return delegate.convert(jwt);
    }

    private Collection<GrantedAuthority> authorities(Jwt jwt) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        addAuthorities(authorities, scopeConverter.convert(jwt));
        addAuthorities(authorities, roleConverter.convert(jwt));
        return authorities;
    }

    private void addAuthorities(Collection<GrantedAuthority> target,
                                Collection<GrantedAuthority> authorities) {
        if (authorities != null) {
            target.addAll(authorities);
        }
    }
}
