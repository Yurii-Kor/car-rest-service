package ua.foxminded.carrestservice.security;

import static org.springframework.security.config.Customizer.withDefaults;

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
                        .requestMatchers(HttpMethod.GET, "/api/v1/**")
                        .permitAll()
                        .requestMatchers("/api/v1/cars", "/api/v1/cars/**")
                        .hasAuthority("SCOPE_cars:write")
                        .requestMatchers("/api/v1/**")
                        .hasAuthority("SCOPE_catalog:write")
                        .anyRequest()
                        .denyAll())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(withDefaults()));

        return http.build();
    }
}
