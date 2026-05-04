package com.unicheck.Unicheckapi.security;

import com.unicheck.Unicheckapi.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    SecurityFilterChain security(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // Apenas GESTOR pode gerenciar usuários, turmas e disciplinas
                        .requestMatchers(HttpMethod.POST, "/professores/**", "/alunos/**", "/turmas/**", "/disciplinas/**").hasRole("GESTOR")
                        .requestMatchers(HttpMethod.PUT, "/turmas/**", "/disciplinas/**").hasRole("GESTOR")
                        .requestMatchers(HttpMethod.DELETE, "/turmas/**", "/disciplinas/**", "/alunos/**", "/professores/**").hasRole("GESTOR")
                        .requestMatchers("/alunos/*/turma").hasRole("GESTOR")

                        // Professor pode iniciar/encerrar aulas e registrar presenças
                        .requestMatchers("/aulas/**").hasAnyRole("PROFESSOR", "GESTOR")
                        .requestMatchers("/horarios/**").hasAnyRole("PROFESSOR", "GESTOR")
                        .requestMatchers("/presencas/registrar").hasRole("PROFESSOR")
                        .requestMatchers(HttpMethod.DELETE, "/presencas/**").hasAnyRole("PROFESSOR", "GESTOR")

                        // Aluno pode ver seu QR Code
                        .requestMatchers("/qrcode/aluno/**").hasRole("ALUNO")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
