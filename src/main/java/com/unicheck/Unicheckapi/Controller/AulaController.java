package com.unicheck.Unicheckapi.Controller;

import com.unicheck.Unicheckapi.model.Aula;
import com.unicheck.Unicheckapi.service.AulaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/aulas")
@RequiredArgsConstructor
public class AulaController {

    private final AulaService aulaService;

    // Professor inicia aula: POST /aulas/iniciar
    // Body: { "disciplinaId": "uuid", "titulo": "Introdução a Redes" }
    @PostMapping("/iniciar")
    public ResponseEntity<Aula> iniciar(@RequestBody Map<String, String> body) {
        UUID disciplinaId = UUID.fromString(body.get("disciplinaId"));
        String titulo = body.get("titulo");
        return ResponseEntity.ok(aulaService.iniciarAula(disciplinaId, titulo));
    }

    // Professor encerra aula: PATCH /aulas/{id}/encerrar
    @PatchMapping("/{id}/encerrar")
    public ResponseEntity<Aula> encerrar(@PathVariable UUID id) {
        return ResponseEntity.ok(aulaService.encerrarAula(id));
    }

    // Listar aulas de uma disciplina
    @GetMapping("/disciplina/{disciplinaId}")
    public ResponseEntity<List<Aula>> listarPorDisciplina(@PathVariable UUID disciplinaId) {
        return ResponseEntity.ok(aulaService.listarPorDisciplina(disciplinaId));
    }
}