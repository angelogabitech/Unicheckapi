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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    SecurityFilterChain security(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // Apenas GESTOR pode gerenciar usuÃ¡rios, turmas e disciplinas
                        .requestMatchers(HttpMethod.GET, "/disciplinas").hasRole("GESTOR")
                        .requestMatchers(HttpMethod.GET, "/disciplinas/minhas", "/disciplinas/turma/**").hasAnyRole("ALUNO", "PROFESSOR", "GESTOR")
                        .requestMatchers(HttpMethod.GET, "/disciplinas/professor/**").hasAnyRole("PROFESSOR", "GESTOR")
                        .requestMatchers(HttpMethod.GET, "/alunos").hasRole("GESTOR")
                        .requestMatchers(HttpMethod.GET, "/alunos/turma/**").hasAnyRole("ALUNO", "PROFESSOR", "GESTOR")
                        .requestMatchers(HttpMethod.POST, "/professores/**", "/alunos/**", "/turmas/**", "/disciplinas/**").hasRole("GESTOR")
                        .requestMatchers(HttpMethod.PUT, "/turmas/**", "/disciplinas/**").hasRole("GESTOR")
                        .requestMatchers(HttpMethod.DELETE, "/turmas/**", "/disciplinas/**", "/alunos/**", "/professores/**").hasRole("GESTOR")
                        .requestMatchers("/alunos/*/turma").hasRole("GESTOR")

                        // Professor pode iniciar/encerrar aulas e registrar/invalidar presenÃ§as
                        .requestMatchers(HttpMethod.GET, "/aulas/disciplina/**").hasAnyRole("ALUNO", "PROFESSOR", "GESTOR")
                        .requestMatchers(HttpMethod.POST, "/aulas/**").hasAnyRole("PROFESSOR", "GESTOR")
                        .requestMatchers(HttpMethod.PATCH, "/aulas/**").hasAnyRole("PROFESSOR", "GESTOR")
                        .requestMatchers(HttpMethod.GET, "/horarios/disciplina/**", "/horarios/turma/**").hasAnyRole("ALUNO", "PROFESSOR", "GESTOR")
                        .requestMatchers(HttpMethod.GET, "/presencas/dashboard").hasRole("GESTOR")
                        .requestMatchers(HttpMethod.GET, "/presencas/aluno/**").hasAnyRole("ALUNO", "GESTOR")
                        .requestMatchers(HttpMethod.GET, "/presencas/dashboard/professor/**", "/presencas/disciplina/**", "/presencas/aula/**").hasAnyRole("PROFESSOR", "GESTOR")
                        .requestMatchers("/presencas/registrar").hasRole("PROFESSOR")
                        .requestMatchers("/presencas/sincronizar").hasAnyRole("PROFESSOR", "GESTOR")
                        .requestMatchers("/sync/offline").hasAnyRole("PROFESSOR", "GESTOR")
                        .requestMatchers(HttpMethod.GET, "/offline/bootstrap").hasAnyRole("ALUNO", "PROFESSOR", "GESTOR")
                        .requestMatchers(HttpMethod.GET, "/presencas").hasRole("GESTOR")
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

