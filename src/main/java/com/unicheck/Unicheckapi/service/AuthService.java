package com.unicheck.Unicheckapi.service;

import com.unicheck.Unicheckapi.dto.LoginRequestDTO;
import com.unicheck.Unicheckapi.dto.LoginResponseDTO;
import com.unicheck.Unicheckapi.dto.MeResponseDTO;
import com.unicheck.Unicheckapi.dto.RegisterRequestDTO;
import com.unicheck.Unicheckapi.model.Usuario;
import com.unicheck.Unicheckapi.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;

    public LoginResponseDTO login(LoginRequestDTO dto) {
        Usuario usuario = usuarioRepository
                .findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));

        if (!passwordEncoder.matches(dto.getSenha(), usuario.getSenha())) {
            throw new RuntimeException("Senha invalida");
        }

        return refreshTokenService.emitir(usuario);
    }

    public LoginResponseDTO refresh(String refreshToken) {
        return refreshTokenService.rotacionar(refreshToken);
    }

    public void logout(String refreshToken) {
        refreshTokenService.revogar(refreshToken);
    }

    public void register(RegisterRequestDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuarioRepository.save(usuario);
    }

    public MeResponseDTO me(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));

        return MeResponseDTO.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .role(usuario.getRole().name())
                .fotoUrl(usuario.getFotoUrl())
                .build();
    }
}
