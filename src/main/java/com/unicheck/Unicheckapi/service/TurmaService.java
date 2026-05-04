package com.unicheck.Unicheckapi.service;

import com.unicheck.Unicheckapi.model.Aluno;
import com.unicheck.Unicheckapi.model.Disciplina;
import com.unicheck.Unicheckapi.model.Turma;
import com.unicheck.Unicheckapi.repository.AlunoRepository;
import com.unicheck.Unicheckapi.repository.DisciplinaRepository;
import com.unicheck.Unicheckapi.repository.TurmaRepository;
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

    public Turma criar(Turma turma) {
        return turmaRepository.save(turma);
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
        return turmaRepository.save(turma);
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
    }
}
