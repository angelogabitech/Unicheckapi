package com.unicheck.Unicheckapi.Controller;

import com.unicheck.Unicheckapi.model.Aluno;
import com.unicheck.Unicheckapi.service.AlunoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alunos")
@RequiredArgsConstructor
public class AlunoController {

    private final AlunoService alunoService;

    @PostMapping
    public Aluno criar(@RequestBody Aluno aluno){
        return alunoService.salvar(aluno);
    }

    @GetMapping
    public List<Aluno> listar(){
        return alunoService.listar();
    }
}