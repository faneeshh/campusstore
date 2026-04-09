package com.campusstore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // CSRF disabled — all access control is handled manually via
        // WebConfig interceptor and session attributes, not Spring Security.
        http
            .csrf(csrf -> csrf.disable())
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable())
            .logout(logout -> logout.disable())
            // Spring Security 7 no longer enables anonymous auth by default.
            // Without this, unauthenticated requests have a null Authentication,
            // causing ExceptionTranslationFilter to redirect to /login when
            // any downstream error occurs (e.g. template rendering failure).
            .anonymous(Customizer.withDefaults())
            .sessionManagement(session ->
                // IF_REQUIRED is the safe default — creates a session only when
                // needed. ALWAYS triggers eager session-fixation protection in SS7,
                // which expects a valid Authentication and can force a redirect.
                session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}