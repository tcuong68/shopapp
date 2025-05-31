package com.devteria.identity_service.configuration;

import com.devteria.identity_service.enums.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import javax.crypto.spec.SecretKeySpec;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final String[] PUBLIC_ENDPOINT = {
            "/users/register",
            "/auth/login",
            "/auth/introspect",
            "/products/search",
            "/reviews/**"
    };

    @Value("${jwt.signerKey}")
    private String signerKey;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(request -> request
                        // Public endpoints
                        .requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINT).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/products/**", "/reviews/product/{productId}", "/api/v1/payments/vnpay/callback", "/reviews/**").permitAll()

                        // USER role endpoints
                        .requestMatchers("/users/myInfo").hasRole(Role.USER.name())
                        .requestMatchers("/order/my-order").hasRole(Role.USER.name())
                        .requestMatchers("/cart/**").hasRole(Role.USER.name())
                        .requestMatchers(HttpMethod.POST, "/order/checkout", "/reviews").hasRole(Role.USER.name())
                        .requestMatchers(HttpMethod.DELETE, "/reviews/{id}").hasRole(Role.USER.name())

                        // ADMIN role endpoints
                        .requestMatchers("/categories/**").hasRole(Role.ADMIN.name())
                        .requestMatchers("/users/search").hasRole(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.POST, "order/admin/search", "products").hasRole(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.GET, "/order/admin").hasRole(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.GET, "/users/{userId}", "/users").hasRole(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.GET, "/order/{orderId}").hasRole(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.PUT, "/products/{productId}").hasRole(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, "/products/{productId}").hasRole(Role.ADMIN.name())

                        // Shared USER & ADMIN endpoints
                        .requestMatchers(HttpMethod.PUT, "/users/{userId}").hasAnyRole(Role.USER.name(), Role.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, "/users/{userId}").hasAnyRole(Role.USER.name(), Role.ADMIN.name())

                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwtConfigurer -> jwtConfigurer
                                .decoder(jwtDecoder())
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                        .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                );

        return httpSecurity.build();
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(), "HmacSHA512");
        return NimbusJwtDecoder.withSecretKey(secretKeySpec)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix(""); // Không cần thêm ROLE_
        grantedAuthoritiesConverter.setAuthoritiesClaimName("scope");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return converter;
    }
}
