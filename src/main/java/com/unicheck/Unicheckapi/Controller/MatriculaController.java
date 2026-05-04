package com.unicheck.Unicheckapi.Controller;

import com.unicheck.Unicheckapi.dto.MatriculaRequestDTO;
import com.unicheck.Unicheckapi.model.Matricula;
import com.unicheck.Unicheckapi.service.MatriculaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/matriculas")
@RequiredArgsConstructor
public class MatriculaController {

    private final MatriculaService matriculaService;

    @PostMapping
    public Matricula criar(@RequestBody MatriculaRequestDTO dto){
        return matriculaService.criar(dto);
    }

    @GetMapping
    public List<Matricula> listar(){
        return matriculaService.listar();
    }
}
