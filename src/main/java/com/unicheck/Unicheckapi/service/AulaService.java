package com.unicheck.Unicheckapi.service;

import com.unicheck.Unicheckapi.model.Aula;
import com.unicheck.Unicheckapi.model.Disciplina;
import com.unicheck.Unicheckapi.repository.AulaRepository;
import com.unicheck.Unicheckapi.ws.RealtimeEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AulaService {

    private final AulaRepository aulaRepository;
    private final DisciplinaService disciplinaService;
    private final RealtimeEventPublisher realtimeEventPublisher;

    // Professor inicia uma sessão de aula informando o título
    public Aula iniciarAula(UUID disciplinaId, String titulo) {
        Disciplina disciplina = disciplinaService.buscarDisciplinaPermitidaParaUsuario(disciplinaId);

        Aula aula = Aula.builder()
                .disciplina(disciplina)
                .titulo(titulo)
                .dataHora(LocalDateTime.now())
                .ativa(true)
                .build();

        Aula salva = aulaRepository.save(aula);
        realtimeEventPublisher.disciplina(disciplina.getId(), "AULA_INICIADA");
        if (disciplina.getTurma() != null) {
            realtimeEventPublisher.turma(disciplina.getTurma().getId(), "AULA_INICIADA");
        }
        return salva;
    }

    // Professor encerra a sessão
    public Aula encerrarAula(UUID aulaId) {
        Aula aula = buscarPorId(aulaId);
        disciplinaService.validarPermissaoDisciplina(aula.getDisciplina());
        aula.setAtiva(false);
        Aula salva = aulaRepository.save(aula);
        realtimeEventPublisher.disciplina(aula.getDisciplina().getId(), "AULA_ENCERRADA");
        if (aula.getDisciplina().getTurma() != null) {
            realtimeEventPublisher.turma(aula.getDisciplina().getTurma().getId(), "AULA_ENCERRADA");
        }
        return salva;
    }

    public Aula buscarPorId(UUID id) {
        return aulaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aula não encontrada"));
    }

    public List<Aula> listarPorDisciplina(UUID disciplinaId) {
        disciplinaService.buscarDisciplinaPermitidaParaUsuario(disciplinaId);
        return aulaRepository.findByDisciplinaId(disciplinaId);
    }
}
