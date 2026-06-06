package com.foodhub.gateway.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.*;

/**
 * Synchronous converter: Jwt -> AbstractAuthenticationToken
 * We'll adapt this using ReactiveJwtAuthenticationConverterAdapter in the security config.
 */
public class KeycloakJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        return new JwtAuthenticationToken(jwt, authorities);
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // Extract realm roles
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null) {
            Object rolesObj = realmAccess.get("roles");
            if (rolesObj instanceof Collection) {
                for (Object r : (Collection<?>) rolesObj) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + String.valueOf(r).toUpperCase()));
                }
            }
        }

        // Extract resource roles
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess != null) {
            for (Object access : resourceAccess.values()) {
                if (access instanceof Map) {
                    Object rolesObj = ((Map<?, ?>) access).get("roles");
                    if (rolesObj instanceof Collection) {
                        for (Object r : (Collection<?>) rolesObj) {
                            authorities.add(new SimpleGrantedAuthority("ROLE_" + String.valueOf(r).toUpperCase()));
                        }
                    }
                }
            }
        }

        return authorities;
    }
}

