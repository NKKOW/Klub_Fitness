package org.example.klubfitness.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. Wyłączamy CSRF (REST‐owe API zwykle z tego nie korzysta)
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Reguły dostępu:
                .authorizeHttpRequests(auth -> auth
                        // 2a) Swagger + OpenAPI – dostęp publiczny
                        .requestMatchers(
                                "/v3/api-docs/**",    // JSON specyfikacji
                                "/swagger-ui/**",     // wszystkie zasoby Swagger UI (JS/CSS/itp.)
                                "/swagger-ui.html",   // “stary” link do Swagger UI
                                "/webjars/**",        // ewentualne pliki WebJars (JS/CSS)
                                "/favicon.ico"        // ikona, jeśli ktoś jej szuka
                        ).permitAll()

                        // 2b) Rejestracja / logowanie – dostęp publiczny
                        .requestMatchers("/api/auth/**").permitAll()

                        // 2c) GET na /api/** – każdy zalogowany (USER, TRAINER lub ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/**")
                        .hasAnyRole("USER", "TRAINER", "ADMIN")

                        // 2d) CRUD trenerów (tylko ADMIN)
                        .requestMatchers("/api/trainers/**")
                        .hasRole("ADMIN")

                        // 2e) CRUD sesji i rezerwacji (USER, TRAINER lub ADMIN)
                        .requestMatchers("/api/training-sessions/**", "/api/reservations/**")
                        .hasAnyRole("USER", "TRAINER", "ADMIN")

                        // 2f) CRUD użytkowników (tylko ADMIN)
                        .requestMatchers("/api/users/**")
                        .hasRole("ADMIN")

                        // 2g) Reszta żądań wymaga uwierzytelnienia
                        .anyRequest().authenticated()
                )

                // 3. Włączamy Basic Auth (z użyciem Customizer.withDefaults())
                //     zamiast “http.httpBasic()” – tak, by uniknąć deprecjacji.
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
