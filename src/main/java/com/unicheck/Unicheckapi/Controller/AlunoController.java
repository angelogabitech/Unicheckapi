package com.unicheck.Unicheckapi.Controller;

import com.unicheck.Unicheckapi.dto.AlunoRequestDTO;
import com.unicheck.Unicheckapi.dto.AtualizarPerfilDTO;
import com.unicheck.Unicheckapi.model.Aluno;
import com.unicheck.Unicheckapi.service.AlunoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/alunos")
@RequiredArgsConstructor
public class AlunoController {

    private final AlunoService alunoService;

    @PostMapping
    public ResponseEntity<Aluno> criar(@RequestBody AlunoRequestDTO dto) {
        return ResponseEntity.ok(alunoService.criar(dto));
    }

    @GetMapping
    public List<Aluno> listar(){
        return alunoService.listar();
    }

    @GetMapping("/turma/{turmaId}")
    public List<Aluno> listarPorTurma(@PathVariable UUID turmaId) {
        return alunoService.listarPorTurmaPermitida(turmaId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Aluno> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(alunoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Aluno> atualizar(@PathVariable UUID id, @RequestBody AlunoRequestDTO dto) {
        return ResponseEntity.ok(alunoService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        alunoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{id}/perfil")
    public ResponseEntity<Aluno> atualizarPerfil(
            @PathVariable UUID id,
            @RequestBody AtualizarPerfilDTO dto) {
        return ResponseEntity.ok(alunoService.atualizarPerfil(id, dto));
    }

    @PutMapping("/{id}/foto")
    public ResponseEntity<Void> uploadFoto(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        alunoService.salvarFoto(id, body.get("fotoBase64"));
        return ResponseEntity.ok().build();
    }

}

