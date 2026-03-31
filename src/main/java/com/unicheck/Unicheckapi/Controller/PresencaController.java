package com.unicheck.Unicheckapi.Controller;

import com.unicheck.Unicheckapi.dto.AlunoResponseDTO;
import com.unicheck.Unicheckapi.model.Presenca;
import com.unicheck.Unicheckapi.service.PresencaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/presencas")
@RequiredArgsConstructor
public class PresencaController {

    private final PresencaService presencaService;

    @PostMapping("/registrar")
    public AlunoResponseDTO registrar(@RequestParam String qrCode){
        return presencaService.registrarPresenca(qrCode);
    }
    @GetMapping("/aluno/{id}")
    public List<Presenca> buscarPorAluno(@PathVariable UUID id){
        return presencaService.buscarPorAluno(id);
    }
    @GetMapping("/disciplina/{id}")
    public List<Presenca> porDisciplina(@PathVariable UUID id){
        return presencaService.buscarPorDisciplina(id);
    }

    @GetMapping
    public List<Presenca> listar(){
        return presencaService.listar();
    }
}