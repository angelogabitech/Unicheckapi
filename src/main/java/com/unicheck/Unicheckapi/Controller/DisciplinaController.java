package com.unicheck.Unicheckapi.Controller;

import com.unicheck.Unicheckapi.dto.DisciplinaRequestDTO;
import com.unicheck.Unicheckapi.model.Disciplina;
import com.unicheck.Unicheckapi.service.DisciplinaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/disciplinas")
@RequiredArgsConstructor
public class DisciplinaController {

    private final DisciplinaService disciplinaService;

    @PostMapping
    public Disciplina criar(@RequestBody DisciplinaRequestDTO dto) {
        return disciplinaService.criar(dto);
    }
    @GetMapping
    public List<Disciplina> listar(){
        return disciplinaService.listar();
    }
}