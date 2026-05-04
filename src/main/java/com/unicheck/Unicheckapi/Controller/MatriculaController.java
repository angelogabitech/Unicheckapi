package com.unicheck.Unicheckapi.Controller;

<<<<<<< HEAD
import com.unicheck.Unicheckapi.dto.MatriculaRequestDTO;
=======
>>>>>>> 0fe0c1eff8687d7baa9153ab44cce2e9923c8612
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
<<<<<<< HEAD
    public Matricula criar(@RequestBody MatriculaRequestDTO dto){
        return matriculaService.criar(dto);
=======
    public Matricula criar(@RequestBody Matricula matricula){
        return matriculaService.salvar(matricula);
>>>>>>> 0fe0c1eff8687d7baa9153ab44cce2e9923c8612
    }

    @GetMapping
    public List<Matricula> listar(){
        return matriculaService.listar();
    }
}