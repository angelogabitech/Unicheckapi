package com.unicheck.Unicheckapi.Controller;

import com.unicheck.Unicheckapi.dto.HorarioAulaRequestDTO;
import com.unicheck.Unicheckapi.model.HorarioAula;
import com.unicheck.Unicheckapi.service.HorarioAulaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/horarios")
@RequiredArgsConstructor
public class HorarioAulaController {

    private final HorarioAulaService horarioAulaService;


    @PostMapping
    public ResponseEntity<HorarioAula> criar(@Valid @RequestBody HorarioAulaRequestDTO dto) {
        HorarioAula criado = horarioAulaService.criar(dto);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.getId())
                .toUri();
        return ResponseEntity.created(location).body(criado);
    }


    @GetMapping("/disciplina/{disciplinaId}")
    public ResponseEntity<List<HorarioAula>> listarPorDisciplina(@PathVariable UUID disciplinaId) {
        return ResponseEntity.ok(horarioAulaService.listarPorDisciplina(disciplinaId));
    }

    @GetMapping("/turma/{turmaId}")
    public ResponseEntity<List<HorarioAula>> listarPorTurma(@PathVariable UUID turmaId) {
        return ResponseEntity.ok(horarioAulaService.listarPorTurma(turmaId));
    }


    @PutMapping("/{id}")
    public ResponseEntity<HorarioAula> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody HorarioAulaRequestDTO dto) {
        return ResponseEntity.ok(horarioAulaService.atualizar(id, dto));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        horarioAulaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}


