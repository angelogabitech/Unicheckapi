package com.unicheck.Unicheckapi.Controller;

import com.unicheck.Unicheckapi.model.Disciplina;
import com.unicheck.Unicheckapi.service.DisciplinaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/disciplinas")
@RequiredArgsConstructor
public class DisciplinaController {

    private final DisciplinaService disciplinaService;

    @PostMapping
    public Disciplina criar(@RequestBody Disciplina disciplina){
        return disciplinaService.criar(disciplina);
    }

    @GetMapping
    public List<Disciplina> listar(){
        return disciplinaService.listar();
    }
}