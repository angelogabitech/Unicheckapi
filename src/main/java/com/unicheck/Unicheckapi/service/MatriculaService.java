package com.unicheck.Unicheckapi.service;

import com.unicheck.Unicheckapi.dto.MatriculaRequestDTO;
import com.unicheck.Unicheckapi.model.*;
import com.unicheck.Unicheckapi.repository.MatriculaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MatriculaService {

    private final MatriculaRepository matriculaRepository;

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

        return matriculaRepository.save(matricula);
    }

    public List<Matricula> listar(){
        return matriculaRepository.findAll();
    }
}