package com.unicheck.Unicheckapi.security;

import com.unicheck.Unicheckapi.model.Role;
import com.unicheck.Unicheckapi.model.Usuario;
import com.unicheck.Unicheckapi.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioRepository.findByEmail("gestor@unicheck.com").isEmpty()) {
            Usuario gestor = new Usuario();
            gestor.setNome("Gestor");
            gestor.setEmail("gestor@unicheck.com");
            gestor.setSenha(passwordEncoder.encode("admin123")); // troque em produção
            gestor.setRole(Role.GESTOR);
            usuarioRepository.save(gestor);
            System.out.println("Gestor criado com sucesso.");
        }
    }
}
