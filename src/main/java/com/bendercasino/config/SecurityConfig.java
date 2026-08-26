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

    // L-B4: a API de jogo e a de jogador ficam fechadas; /api/auth/** e os estáticos
    // da SPA ficam abertos. Sem formLogin — a autenticação é feita pelo AuthController
    // (POST /api/auth/login), que guarda o contexto na sessão HTTP.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                // estáticos + rotas do SPA (o SpaForwardController faz forward para index.html)
                .requestMatchers("/", "/index.html", "/favicon.ico", "/assets/**", "/styles/**",
                                 "/js/**", "/views/**", "/*.js", "/*.css", "/*.png", "/*.ico",
                                 "/error", "/{path:[^.]*}", "/{path:[^.]*}/{subpath:[^.]*}").permitAll()
                .requestMatchers("/api/games/**", "/api/players/**", "/api/blackjack/**").authenticated()
                .anyRequest().denyAll())
            .exceptionHandling(ex -> ex
                // 401 em JSON em vez de redirect para um form de login que não existe
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
        return http.build();
    }
}
