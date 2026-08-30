package com.bendercasino.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/", "/index.html", "/favicon.ico", "/assets/**", "/styles/**",
                                 "/js/**", "/views/**", "/*.js", "/*.css", "/*.png", "/*.ico",
                                 "/error", "/{path:[^.]*}", "/{path:[^.]*}/{subpath:[^.]*}").permitAll()
                .requestMatchers("/api/games/**", "/api/players/**", "/api/blackjack/**", "/api/roulette/**").authenticated()
                .anyRequest().denyAll())
            .exceptionHandling(ex -> ex
                // no formLogin configured, so fall back to plain 401 instead of a redirect
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
        return http.build();
    }
}
