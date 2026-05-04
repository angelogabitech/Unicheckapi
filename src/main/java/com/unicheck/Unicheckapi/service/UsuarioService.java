package com.unicheck.Unicheckapi.service;

import com.unicheck.Unicheckapi.dto.UsuarioRequestDTO;
import com.unicheck.Unicheckapi.model.Usuario;
import com.unicheck.Unicheckapi.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario salvar(Usuario usuario) {

        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            throw new RuntimeException("Email Ã© obrigatÃ³rio");
        }

        if (usuario.getNome() == null || usuario.getNome().isBlank()) {
            throw new RuntimeException("Nome Ã© obrigatÃ³rio");
        }

        if (usuario.getSenha() == null || usuario.getSenha().isBlank()) {
            throw new RuntimeException("Senha Ã© obrigatÃ³ria");
        }

        return usuarioRepository.save(usuario);
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }
    public Usuario buscarPorId(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("UsuÃ¡rio nÃ£o encontrado"));
    }

    public void deletar(UUID id) {
        usuarioRepository.deleteById(id);
    }

    public Usuario atualizar(UUID id, UsuarioRequestDTO dto) {

        Usuario usuario = buscarPorId(id);

        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(dto.getSenha());
        usuario.setRole(dto.getRole());


        return usuarioRepository.save(usuario);
    }
}
