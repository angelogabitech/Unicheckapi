package com.unicheck.Unicheckapi.service;

import com.unicheck.Unicheckapi.dto.LoginResponseDTO;
import com.unicheck.Unicheckapi.model.RefreshToken;
import com.unicheck.Unicheckapi.model.Usuario;
import com.unicheck.Unicheckapi.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final int REFRESH_BYTES = 32;
    private static final int TTL_DIAS = 2;

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public LoginResponseDTO emitir(Usuario usuario) {
        String accessToken = jwtService.gerarToken(usuario.getEmail(), usuario.getRole().name());
        String refreshToken = gerarTokenCru();

        refreshTokenRepository.save(RefreshToken.builder()
                .tokenHash(hash(refreshToken))
                .usuarioId(usuario.getId())
                .email(usuario.getEmail())
                .role(usuario.getRole().name())
                .expiraEm(LocalDateTime.now().plusDays(TTL_DIAS))
                .build());

        return new LoginResponseDTO(accessToken, refreshToken);
    }

    @Transactional
    public LoginResponseDTO rotacionar(String refreshTokenCru) {
        validarRefreshInformado(refreshTokenCru);

        RefreshToken atual = refreshTokenRepository.findByTokenHash(hash(refreshTokenCru))
                .orElseThrow(() -> new RuntimeException("Refresh token invalido"));

        if (atual.isRevogado() || atual.getExpiraEm().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token expirado ou revogado");
        }

        refreshTokenRepository.delete(atual);

        String novoAccessToken = jwtService.gerarToken(atual.getEmail(), atual.getRole());
        String novoRefreshToken = gerarTokenCru();
        refreshTokenRepository.save(RefreshToken.builder()
                .tokenHash(hash(novoRefreshToken))
                .usuarioId(atual.getUsuarioId())
                .email(atual.getEmail())
                .role(atual.getRole())
                .expiraEm(LocalDateTime.now().plusDays(TTL_DIAS))
                .build());

        return new LoginResponseDTO(novoAccessToken, novoRefreshToken);
    }

    @Transactional
    public void revogar(String refreshTokenCru) {
        if (refreshTokenCru == null || refreshTokenCru.isBlank()) {
            return;
        }

        refreshTokenRepository.findByTokenHash(hash(refreshTokenCru)).ifPresent(refreshToken -> {
            refreshToken.setRevogado(true);
            refreshTokenRepository.save(refreshToken);
        });
    }

    @Transactional
    public void revogarTodosDoUsuario(Usuario usuario) {
        refreshTokenRepository.deleteByUsuarioId(usuario.getId());
    }

    private void validarRefreshInformado(String refreshTokenCru) {
        if (refreshTokenCru == null || refreshTokenCru.isBlank()) {
            throw new RuntimeException("Refresh token obrigatorio");
        }
    }

    private String gerarTokenCru() {
        byte[] bytes = new byte[REFRESH_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String valor) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(valor.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao gerar hash do refresh token", e);
        }
    }
}
