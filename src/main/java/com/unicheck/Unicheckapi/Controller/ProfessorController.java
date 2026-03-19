package com.unicheck.Unicheckapi.Controller;

import com.unicheck.Unicheckapi.model.Professor;
import com.unicheck.Unicheckapi.service.ProfessorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/professores")
public class ProfessorController {

    private final ProfessorService service;

    public ProfessorController(ProfessorService service) {
        this.service = service;
    }

    @PostMapping
    public Professor criar(@RequestBody Professor professor) {
        return service.salvar(professor);
    }

    @GetMapping
    public List<Professor> listar() {
        return service.listar();
    }
}