package com.unicheck.Unicheckapi.service;

import com.unicheck.Unicheckapi.dto.LoginRequestDTO;
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
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public String login(LoginRequestDTO dto){

        Usuario usuario = usuarioRepository
                .findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if(!passwordEncoder.matches(dto.getSenha(), usuario.getSenha())){
            throw new RuntimeException("Senha inválida");
        }


        return jwtService.gerarToken(usuario.getEmail());
    }
    public void register(RegisterRequestDTO dto){

        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(dto.getSenha());

        usuarioRepository.save(usuario);
    }

}