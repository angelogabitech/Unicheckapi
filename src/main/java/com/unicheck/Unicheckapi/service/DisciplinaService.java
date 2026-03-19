package com.unicheck.Unicheckapi.service;

import com.unicheck.Unicheckapi.model.Disciplina;
import com.unicheck.Unicheckapi.model.Professor;
import com.unicheck.Unicheckapi.repository.DisciplinaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DisciplinaService {

    private final DisciplinaRepository disciplinaRepository;
    private final ProfessorService professorService;

    public Disciplina criar(Disciplina dto) {

        Professor professor =
                professorService.buscarPorId(dto.getProfessor().getId());

        Disciplina disciplina = Disciplina.builder()
                .nome(dto.getNome())
                .codigo(dto.getCodigo())
                .professor(professor)
                .build();

        return disciplinaRepository.save(disciplina);
    }

    public List<Disciplina> listar(){
        return disciplinaRepository.findAll();
    }
}