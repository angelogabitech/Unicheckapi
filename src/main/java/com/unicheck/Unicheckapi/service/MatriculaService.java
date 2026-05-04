package com.unicheck.Unicheckapi.service;

<<<<<<< HEAD
import com.unicheck.Unicheckapi.dto.MatriculaRequestDTO;
import com.unicheck.Unicheckapi.model.*;
=======
import com.unicheck.Unicheckapi.model.Matricula;
>>>>>>> 0fe0c1eff8687d7baa9153ab44cce2e9923c8612
import com.unicheck.Unicheckapi.repository.MatriculaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MatriculaService {

    private final MatriculaRepository matriculaRepository;

<<<<<<< HEAD
    private final AlunoService alunoService;
    private final DisciplinaService disciplinaService;

    public Matricula criar(MatriculaRequestDTO dto){

        Aluno aluno = alunoService.buscarPorId(dto.getAlunoId());

        Disciplina disciplina =
                disciplinaService.buscarPorId(dto.getDisciplinaId());

        Matricula matricula = Matricula.builder()
                .aluno(aluno)
                .disciplina(disciplina)
                .build();

=======
    public Matricula salvar(Matricula matricula){
>>>>>>> 0fe0c1eff8687d7baa9153ab44cce2e9923c8612
        return matriculaRepository.save(matricula);
    }

    public List<Matricula> listar(){
        return matriculaRepository.findAll();
    }
}