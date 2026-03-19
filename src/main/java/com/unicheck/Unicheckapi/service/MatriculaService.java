package com.unicheck.Unicheckapi.service;

import com.unicheck.Unicheckapi.model.Matricula;
import com.unicheck.Unicheckapi.repository.MatriculaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MatriculaService {

    private final MatriculaRepository matriculaRepository;

    public Matricula salvar(Matricula matricula){
        return matriculaRepository.save(matricula);
    }

    public List<Matricula> listar(){
        return matriculaRepository.findAll();
    }
}