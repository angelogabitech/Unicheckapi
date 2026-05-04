package com.unicheck.Unicheckapi.Controller;

<<<<<<< HEAD
import com.unicheck.Unicheckapi.dto.AtualizarPerfilDTO;
import com.unicheck.Unicheckapi.dto.ProfessorRequestDTO;
import com.unicheck.Unicheckapi.model.Professor;
import com.unicheck.Unicheckapi.service.AlunoService;
import com.unicheck.Unicheckapi.service.ProfessorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
=======
import com.unicheck.Unicheckapi.model.Professor;
import com.unicheck.Unicheckapi.service.ProfessorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
>>>>>>> 0fe0c1eff8687d7baa9153ab44cce2e9923c8612

@RestController
@RequestMapping("/professores")
public class ProfessorController {

    private final ProfessorService service;

<<<<<<< HEAD

=======
>>>>>>> 0fe0c1eff8687d7baa9153ab44cce2e9923c8612
    public ProfessorController(ProfessorService service) {
        this.service = service;
    }

    @PostMapping
<<<<<<< HEAD
    public ResponseEntity<Professor> criar(@RequestBody ProfessorRequestDTO dto) {
        return ResponseEntity.ok(service.criar(dto));
=======
    public Professor criar(@RequestBody Professor professor) {
        return service.salvar(professor);
>>>>>>> 0fe0c1eff8687d7baa9153ab44cce2e9923c8612
    }

    @GetMapping
    public List<Professor> listar() {
        return service.listar();
    }
<<<<<<< HEAD

    @GetMapping("/{id}")
    public ResponseEntity<Professor> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Professor> atualizar(@PathVariable UUID id, @RequestBody ProfessorRequestDTO dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/perfil")
    public ResponseEntity<Professor> atualizarPerfil(
            @PathVariable UUID id,
            @RequestBody AtualizarPerfilDTO dto) {
        return ResponseEntity.ok(service.atualizarPerfil(id, dto));
    }

    @PutMapping("/{id}/foto")
    public ResponseEntity<Void> uploadFoto(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        service.salvarFoto(id, body.get("fotoBase64"));
        return ResponseEntity.ok().build();
    }

}
=======
}
>>>>>>> 0fe0c1eff8687d7baa9153ab44cce2e9923c8612
