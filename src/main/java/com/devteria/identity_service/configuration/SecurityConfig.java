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

    private final String[] PUBLIC_ENDPOINT = {"/users/register", "/auth/token", "/auth/introspect", "/products/search"};

    @Value("${jwt.signerKey}")
    private String signerKey;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeHttpRequests(request ->
                request.requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINT).permitAll()
                        .requestMatchers(HttpMethod.GET, "/products/**", "/reviews/product/{productId}").permitAll()
                        .requestMatchers("/categories/**", "/users/search").hasRole(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.GET,"/users/{userId}", "/users", "/order/admin", "/order/{orderId}").hasRole(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.POST,"order/admin/search", "products", "/users/search").hasRole(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.PUT,"/products/{productId}").hasRole(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE,"/users/{userId}", "/products/{productId}").hasRole(Role.ADMIN.name())

                        .requestMatchers("/cart/**").hasRole(Role.USER.name())
                        .requestMatchers(HttpMethod.GET,"/users/myInfo", "/order/my-order").hasRole(Role.USER.name())
                        .requestMatchers(HttpMethod.POST,"/order/checkout", "/reviews").hasRole(Role.USER.name())
                        .requestMatchers(HttpMethod.PUT,"/users/{userId}").hasRole(Role.USER.name())
                        .requestMatchers(HttpMethod.DELETE,"/users/{userId}", "/reviews/{id}").hasRole(Role.USER.name())

                        .anyRequest().authenticated());

        httpSecurity.oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwtConfigurer ->
                                jwtConfigurer.decoder(jwtDecoder())
                                        .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                        .authenticationEntryPoint(new JwtAuthenticationEntryPoint())

//                        .accessDeniedHandler((request, response, accessDeniedException) -> {
//                            ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
//                            ApiResponse<Object> apiResponse = ApiResponse.builder()
//                                    .code(errorCode.getCode())        // 1007
//                                    .message(errorCode.getMessage())   // "You dont have permission"
//                                    .build();
//
//                            response.setStatus(errorCode.getStatusCode().value()); // 403
//                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//                            response.setCharacterEncoding("UTF-8");
//                            new ObjectMapper().writeValue(response.getWriter(), apiResponse);
//                        })
        );

        httpSecurity.csrf(AbstractHttpConfigurer::disable);
        httpSecurity.cors(Customizer.withDefaults()); // <--- Dòng này để bật CORS

        return httpSecurity.build();
    }

    //config Angular
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
    JwtDecoder jwtDecoder() {
        SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(), "HS512");
        return NimbusJwtDecoder.withSecretKey(secretKeySpec)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }

    ;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    //custom SCOPE_ADMIN -> ROLE_ADMIN  
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
}
