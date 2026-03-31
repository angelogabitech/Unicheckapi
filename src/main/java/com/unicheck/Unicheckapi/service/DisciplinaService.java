package com.unicheck.Unicheckapi.service;

import com.unicheck.Unicheckapi.dto.DisciplinaRequestDTO;
import com.unicheck.Unicheckapi.model.Aluno;
import com.unicheck.Unicheckapi.model.Disciplina;
import com.unicheck.Unicheckapi.model.Professor;
import com.unicheck.Unicheckapi.repository.AlunoRepository;
import com.unicheck.Unicheckapi.repository.DisciplinaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DisciplinaService {

    private final DisciplinaRepository disciplinaRepository;
    private final ProfessorService professorService;

    public Disciplina buscarPorId(UUID id){
        return disciplinaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Disciplina não encontrada"));
    }

    public Disciplina criar(DisciplinaRequestDTO dto) {

        Professor professor =
                professorService.buscarPorId(dto.getProfessorId());

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
