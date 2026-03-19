package com.unicheck.Unicheckapi.Controller;

import com.unicheck.Unicheckapi.dto.UsuarioRequestDTO;
import com.unicheck.Unicheckapi.model.Usuario;
import com.unicheck.Unicheckapi.service.UsuarioService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public Usuario criar(@RequestBody UsuarioRequestDTO dto) {

        Usuario usuario = Usuario.builder()
                .nome(dto.getNome())
                .email(dto.getEmail())
                .senha(dto.getSenha())
                .role(dto.getRole())
                .build();

        return usuarioService.salvar(usuario);
    }

    @GetMapping
    public List<Usuario> listar() {
        return usuarioService.listar();
    }

    @GetMapping("/{id}")
    public Usuario buscarPorId(@PathVariable UUID id) {
        return usuarioService.buscarPorId(id);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable UUID id) {
        usuarioService.deletar(id);
    }

    @PutMapping("/{id}")
    public Usuario atualizar(
            @PathVariable UUID id,
            @RequestBody UsuarioRequestDTO dto) {

        return usuarioService.atualizar(id, dto);
    }

}