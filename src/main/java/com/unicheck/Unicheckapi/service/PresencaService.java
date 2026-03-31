
package com.unicheck.Unicheckapi.service;

import com.unicheck.Unicheckapi.dto.AlunoResponseDTO;
import com.unicheck.Unicheckapi.model.*;
import com.unicheck.Unicheckapi.repository.PresencaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PresencaService {

    private final PresencaRepository presencaRepository;
    private final QrCodeService qrService;

    public AlunoResponseDTO registrarPresenca(String qrCode) {


        // QRService deve retornar aluno + disciplina
        Matricula matricula = qrService.validarQrCode(qrCode);

        Aluno aluno = (Aluno) matricula.getAluno();
        Disciplina disciplina = matricula.getDisciplina();

        Presenca presenca = Presenca.builder()
                .aluno(aluno)
                .disciplina(disciplina)
                .build();

        presencaRepository.save(presenca);

        return AlunoResponseDTO.builder()
                .nome(aluno.getNome())
                .matricula(aluno.getMatricula())
                .fotoUrl(aluno.getFotoUrl())
                .build();
    }
    public List<Presenca> listar(){
        return presencaRepository.findAll();
    }
    public List<Presenca> buscarPorAluno(UUID id){
        return presencaRepository.findByAlunoId(id);
    }
    public List<Presenca> buscarPorDisciplina(UUID disciplinaId){
        return presencaRepository.findByDisciplinaId(disciplinaId);
    }
}