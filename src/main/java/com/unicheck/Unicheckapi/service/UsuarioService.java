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
<<<<<<< HEAD

        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            throw new RuntimeException("Email é obrigatório");
        }

        if (usuario.getNome() == null || usuario.getNome().isBlank()) {
            throw new RuntimeException("Nome é obrigatório");
        }

        if (usuario.getSenha() == null || usuario.getSenha().isBlank()) {
            throw new RuntimeException("Senha é obrigatória");
        }

=======
>>>>>>> 0fe0c1eff8687d7baa9153ab44cce2e9923c8612
        return usuarioRepository.save(usuario);
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }
    public Usuario buscarPorId(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
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

<<<<<<< HEAD

=======
>>>>>>> 0fe0c1eff8687d7baa9153ab44cce2e9923c8612
        return usuarioRepository.save(usuario);
    }
}
