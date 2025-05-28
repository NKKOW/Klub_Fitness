package org.example.klubfitness.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http

                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth
                        // Swagger & OpenAPI publicznie
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/webjars/**"
                        ).permitAll()

                        // rejestracja / logowanie
                        .requestMatchers("/api/auth/**").permitAll()

                        // każdy zalogowany na GET
                        .requestMatchers(HttpMethod.GET, "/api/**")
                        .hasAnyRole("USER","TRAINER","ADMIN")

                        // CRUD trenerów tylko ADMIN
                        .requestMatchers("/api/trainers/**").hasRole("ADMIN")

                        // CRUD sesji i rezerwacji: USER, TRAINER, ADMIN
                        .requestMatchers("/api/training-sessions/**", "/api/reservations/**")
                        .hasAnyRole("USER","TRAINER","ADMIN")

                        // CRUD użytkowników: ADMIN
                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .httpBasic(withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
