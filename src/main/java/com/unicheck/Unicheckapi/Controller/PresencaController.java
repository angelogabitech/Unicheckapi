package com.unicheck.Unicheckapi.Controller;

import com.unicheck.Unicheckapi.dto.AlunoPresencaResumoDTO;
import com.unicheck.Unicheckapi.dto.DashboardDisciplinaDTO;
import com.unicheck.Unicheckapi.dto.PresencaRegistroResponseDTO;
import com.unicheck.Unicheckapi.dto.SincronizacaoPresencaDTO;
import com.unicheck.Unicheckapi.model.Presenca;
import com.unicheck.Unicheckapi.service.PresencaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/presencas")
@RequiredArgsConstructor
public class PresencaController {

    private final PresencaService presencaService;

    @PostMapping("/registrar")
    public ResponseEntity<PresencaRegistroResponseDTO> registrar(@RequestBody Map<String, String> body) {
        String qrCode = body.get("qrCode");
        UUID aulaId = UUID.fromString(body.get("aulaId"));
        return ResponseEntity.ok(presencaService.registrarPresenca(qrCode, aulaId));
    }
    @GetMapping("/aluno/{id}")
    public List<Presenca> buscarPorAluno(@PathVariable UUID id){
        return presencaService.buscarPorAluno(id);
    }
    @GetMapping("/disciplina/{id}")
    public List<Presenca> porDisciplina(@PathVariable UUID id){
        return presencaService.buscarPorDisciplina(id);
    }
    @GetMapping("/disciplina/{id}/alunos")
    public List<AlunoPresencaResumoDTO> resumoAlunosPorDisciplina(@PathVariable UUID id) {
        return presencaService.resumoAlunosPorDisciplina(id);
    }

    @GetMapping("/aula/{id}")
    public List<Presenca> porAula(@PathVariable UUID id) {
        return presencaService.buscarPorAula(id);
    }

    @GetMapping
    public List<Presenca> listar(){
        return presencaService.listar();

    }
    // Dashboard do Gestor â€” todas as disciplinas
    @GetMapping("/dashboard")
    public ResponseEntity<List<DashboardDisciplinaDTO>> dashboard() {
        return ResponseEntity.ok(presencaService.gerarDashboard());
    }

    // Dashboard do Professor â€” apenas disciplinas dele
    @GetMapping("/dashboard/professor/{professorId}")
    public ResponseEntity<List<DashboardDisciplinaDTO>> dashboardProfessor(
            @PathVariable UUID professorId) {
        return ResponseEntity.ok(presencaService.gerarDashboardProfessor(professorId));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        presencaService.deletarPresenca(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sincronizar")
    public ResponseEntity<String> sincronizar(
            @RequestBody List<SincronizacaoPresencaDTO> lista) {
        presencaService.sincronizar(lista);
        return ResponseEntity.ok("Sincronizado com sucesso");
    }
}

