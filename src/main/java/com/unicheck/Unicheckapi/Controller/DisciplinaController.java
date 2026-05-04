package com.unicheck.Unicheckapi.Controller;

import com.unicheck.Unicheckapi.dto.DisciplinaBulkRequestDTO;
import com.unicheck.Unicheckapi.dto.DisciplinaRequestDTO;
import com.unicheck.Unicheckapi.model.Disciplina;
import com.unicheck.Unicheckapi.service.DisciplinaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/disciplinas")
@RequiredArgsConstructor
public class DisciplinaController {

    private final DisciplinaService disciplinaService;

    @PostMapping
    public Disciplina criar(@RequestBody DisciplinaRequestDTO dto) {
        return disciplinaService.criar(dto);
    }

    @PostMapping("/bulk")
    public List<Disciplina> criarEmLote(@RequestBody DisciplinaBulkRequestDTO dto) {
        return disciplinaService.criarEmLote(dto);
    }

    @GetMapping
    public List<Disciplina> listar(){
        return disciplinaService.listar();
    }

    @GetMapping("/minhas")
    public List<Disciplina> listarMinhas() {
        return disciplinaService.listarMinhas();
    }

    @GetMapping("/{id}")
    public Disciplina buscarPorId(@PathVariable UUID id) {
        return disciplinaService.buscarDisciplinaPermitidaParaUsuario(id);
    }

    @PutMapping("/{id}")
    public Disciplina atualizar(@PathVariable UUID id, @RequestBody DisciplinaRequestDTO dto) {
        return disciplinaService.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable UUID id) {
        disciplinaService.deletar(id);
    }

    @GetMapping("/turma/{turmaId}")
    public List<Disciplina> listarPorTurma(@PathVariable UUID turmaId) {
        return disciplinaService.listarPorTurma(turmaId);
    }

    @GetMapping("/professor/{professorId}")
    public List<Disciplina> listarPorProfessor(@PathVariable UUID professorId) {
        return disciplinaService.listarPorProfessorPermitido(professorId);
    }
}
