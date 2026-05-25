package com.unicheck.Unicheckapi.service;


import com.unicheck.Unicheckapi.dto.AlunoRequestDTO;
import com.unicheck.Unicheckapi.dto.AtualizarPerfilDTO;
import com.unicheck.Unicheckapi.model.Aluno;
import com.unicheck.Unicheckapi.model.Role;
import com.unicheck.Unicheckapi.model.Turma;
import com.unicheck.Unicheckapi.model.Usuario;
import com.unicheck.Unicheckapi.repository.AlunoRepository;
import com.unicheck.Unicheckapi.repository.DisciplinaRepository;
import com.unicheck.Unicheckapi.repository.MatriculaRepository;
import com.unicheck.Unicheckapi.repository.PresencaRepository;
import com.unicheck.Unicheckapi.ws.RealtimeEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlunoService {

    private final AlunoRepository alunoRepository;
    private final PresencaRepository presencaRepository;
    private final MatriculaRepository matriculaRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final DisciplinaService disciplinaService;
    private final TurmaService turmaService;
    private final PasswordEncoder passwordEncoder;
    private final RealtimeEventPublisher realtimeEventPublisher;


    public Aluno salvar(Aluno aluno) {
        return alunoRepository.save(aluno);
    }

    public List<Aluno> listar() {
        return alunoRepository.findAll();
    }

    public Aluno buscarPorId(UUID id) {
        return alunoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aluno nÃ£o encontrado"));
    }

    public List<Aluno> listarPorTurmaPermitida(UUID turmaId) {
        Usuario usuario = disciplinaService.usuarioAutenticado();

        if (usuario.getRole() == Role.GESTOR) {
            return alunoRepository.findByTurmaId(turmaId);
        }

        if (usuario.getRole() == Role.PROFESSOR
                && disciplinaRepository.existsByProfessorIdAndTurmaIdAndAtivaTrue(usuario.getId(), turmaId)) {
            return alunoRepository.findByTurmaId(turmaId);
        }

        if (usuario.getRole() == Role.ALUNO) {
            Aluno aluno = buscarPorId(usuario.getId());
            if (aluno.getTurma() != null && aluno.getTurma().getId().equals(turmaId)) {
                return alunoRepository.findByTurmaId(turmaId);
            }
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Usuario sem permissao para listar alunos desta turma.");
    }

    public Aluno criar(AlunoRequestDTO dto) {
        Turma turma = turmaService.buscarPorId(dto.getTurmaId());

        Aluno aluno = new Aluno();
        aluno.setNome(dto.getNome());
        aluno.setEmail(dto.getEmail());
        aluno.setSenha(passwordEncoder.encode(dto.getSenha()));
        aluno.setMatricula(dto.getMatricula());
        aluno.setTurma(turma);
        aluno.setRole(Role.ALUNO);
        Aluno salvo = alunoRepository.save(aluno);
        publicarEventoAluno(salvo, "ALUNO_CRIADO");
        return salvo;
    }

    public Aluno atualizar(UUID id, AlunoRequestDTO dto) {
        Aluno aluno = buscarPorId(id);
        aluno.setNome(dto.getNome());
        aluno.setEmail(dto.getEmail());
        aluno.setMatricula(dto.getMatricula());
        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            aluno.setSenha(passwordEncoder.encode(dto.getSenha()));
        }
        if (dto.getTurmaId() != null) {
            aluno.setTurma(turmaService.buscarPorId(dto.getTurmaId()));
        }
        Aluno salvo = alunoRepository.save(aluno);
        publicarEventoAluno(salvo, "ALUNO_ATUALIZADO");
        return salvo;
    }

    public void deletar(UUID id) {
        Aluno aluno = buscarPorId(id);
        UUID turmaId = aluno.getTurma() != null ? aluno.getTurma().getId() : null;

        presencaRepository.deleteAll(presencaRepository.findByAlunoId(id));
        matriculaRepository.deleteAll(matriculaRepository.findByAlunoId(id));

        alunoRepository.delete(aluno);
        realtimeEventPublisher.gestor("ALUNO_DELETADO", "ALUNO", id);
        realtimeEventPublisher.turma(turmaId, "ALUNO_DELETADO");
    }

    public Aluno atualizarPerfil(UUID id, AtualizarPerfilDTO dto) {
        Aluno aluno = buscarPorId(id);
        if (dto.getNome() != null) aluno.setNome(dto.getNome());
        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            aluno.setSenha(passwordEncoder.encode(dto.getSenha()));
        }
        if (dto.getFotoUrl() != null) aluno.setFotoUrl(dto.getFotoUrl());
        return alunoRepository.save(aluno);
    }

    public void salvarFoto(UUID id, String fotoBase64) {
        Aluno aluno = buscarPorId(id);
        aluno.setFotoUrl(fotoBase64);
        alunoRepository.save(aluno);
    }

    private void publicarEventoAluno(Aluno aluno, String tipo) {
        realtimeEventPublisher.gestor(tipo, "ALUNO", aluno.getId());
        if (aluno.getTurma() != null) {
            realtimeEventPublisher.turma(aluno.getTurma().getId(), tipo);
        }
    }
}

