package ua.foxminded.carrestservice.security;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.oauth2.core.authorization.OAuth2AuthorizationManagers.hasScope;
import static ua.foxminded.carrestservice.security.ScopeConstants.CARS_WRITE;
import static ua.foxminded.carrestservice.security.ScopeConstants.CATALOG_WRITE;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET,
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/**")
                        .permitAll()
                        .requestMatchers("/api/v1/cars", "/api/v1/cars/**")
                        .access(hasScope(CARS_WRITE))
                        .requestMatchers("/api/v1/**")
                        .access(hasScope(CATALOG_WRITE))
                        .anyRequest()
                        .denyAll())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(withDefaults()));

        return http.build();
    }
}
