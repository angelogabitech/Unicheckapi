package com.unicheck.Unicheckapi.service;

import com.unicheck.Unicheckapi.model.Aluno;
import com.unicheck.Unicheckapi.model.Disciplina;
import com.unicheck.Unicheckapi.model.Turma;
import com.unicheck.Unicheckapi.repository.AlunoRepository;
import com.unicheck.Unicheckapi.repository.DisciplinaRepository;
import com.unicheck.Unicheckapi.repository.TurmaRepository;
import com.unicheck.Unicheckapi.ws.RealtimeEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TurmaService {

    private final TurmaRepository turmaRepository;
    private final AlunoRepository alunoRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final RealtimeEventPublisher realtimeEventPublisher;

    public Turma criar(Turma turma) {
        Turma salva = turmaRepository.save(turma);
        realtimeEventPublisher.gestor("TURMA_CRIADA", "TURMA", salva.getId());
        realtimeEventPublisher.turma(salva.getId(), "TURMA_CRIADA");
        return salva;
    }

    public List<Turma> listar() {
        return turmaRepository.findAll();
    }

    public Turma buscarPorId(UUID id) {
        return turmaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Turma não encontrada"));
    }

    public Turma atualizar(UUID id, Turma dados) {
        Turma turma = buscarPorId(id);
        turma.setPeriodo(dados.getPeriodo());
        turma.setCurso(dados.getCurso());
        turma.setIdentificacao(dados.getIdentificacao());
        Turma salva = turmaRepository.save(turma);
        realtimeEventPublisher.gestor("TURMA_ATUALIZADA", "TURMA", salva.getId());
        realtimeEventPublisher.turma(salva.getId(), "TURMA_ATUALIZADA");
        return salva;
    }

    public void deletar(UUID id) {
        Turma turma = buscarPorId(id);

        List<Aluno> alunos = alunoRepository.findByTurmaId(id);
        alunos.forEach(aluno -> aluno.setTurma(null));
        alunoRepository.saveAll(alunos);

        List<Disciplina> disciplinas = disciplinaRepository.findByTurmaId(id);
        disciplinas.forEach(disciplina -> disciplina.setTurma(null));
        disciplinaRepository.saveAll(disciplinas);

        turmaRepository.delete(turma);
        realtimeEventPublisher.gestor("TURMA_DELETADA", "TURMA", id);
        realtimeEventPublisher.turma(id, "TURMA_DELETADA");
    }
}
