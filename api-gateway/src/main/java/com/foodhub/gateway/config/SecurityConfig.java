package com.foodhub.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges
                // Public endpoints
                .pathMatchers("/api/v1/auth/**").permitAll()
                .pathMatchers("/actuator/**").permitAll()
                .pathMatchers("/swagger-ui/**", "/v3/api-docs/**",
                              "/webjars/**", "/swagger-ui.html").permitAll()
                // Customer endpoints
                .pathMatchers("/api/v1/restaurants/**").permitAll()
                .pathMatchers("/api/v1/menu/**").permitAll()
                // Protected endpoints
                .pathMatchers("/api/v1/orders/**").hasAnyRole("CUSTOMER", "ADMIN")
                .pathMatchers("/api/v1/users/**").hasAnyRole("CUSTOMER", "ADMIN")
                .pathMatchers("/api/v1/delivery/**").hasAnyRole("DELIVERY_DRIVER", "ADMIN")
                .pathMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .pathMatchers("/api/v1/restaurant-owner/**").hasAnyRole("RESTAURANT_OWNER", "ADMIN")
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(new ReactiveJwtAuthenticationConverterAdapter(new KeycloakJwtConverter())))
            );
        return http.build();
    }
}

