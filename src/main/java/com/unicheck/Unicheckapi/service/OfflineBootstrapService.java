package com.unicheck.Unicheckapi.service;

import com.unicheck.Unicheckapi.dto.OfflineBootstrapDTO;
import com.unicheck.Unicheckapi.model.Aluno;
import com.unicheck.Unicheckapi.model.Disciplina;
import com.unicheck.Unicheckapi.model.Professor;
import com.unicheck.Unicheckapi.model.Role;
import com.unicheck.Unicheckapi.model.Turma;
import com.unicheck.Unicheckapi.model.Usuario;
import com.unicheck.Unicheckapi.repository.AlunoRepository;
import com.unicheck.Unicheckapi.repository.AulaRepository;
import com.unicheck.Unicheckapi.repository.DisciplinaRepository;
import com.unicheck.Unicheckapi.repository.HorarioAulaRepository;
import com.unicheck.Unicheckapi.repository.PresencaRepository;
import com.unicheck.Unicheckapi.repository.ProfessorRepository;
import com.unicheck.Unicheckapi.repository.TurmaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OfflineBootstrapService {

    private final DisciplinaService disciplinaService;
    private final TurmaRepository turmaRepository;
    private final ProfessorRepository professorRepository;
    private final AlunoRepository alunoRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final AulaRepository aulaRepository;
    private final HorarioAulaRepository horarioAulaRepository;
    private final PresencaRepository presencaRepository;
    private final QrCodeService qrCodeService;

    public OfflineBootstrapDTO carregar() {
        Usuario usuario = disciplinaService.usuarioAutenticado();

        if (usuario.getRole() == Role.GESTOR) {
            return montarGestor();
        }

        if (usuario.getRole() == Role.PROFESSOR) {
            return montarProfessor(usuario.getId());
        }

        if (usuario.getRole() == Role.ALUNO) {
            return montarAluno(usuario.getId());
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Perfil sem permissao para bootstrap offline.");
    }

    private OfflineBootstrapDTO montarGestor() {
        List<Disciplina> disciplinas = disciplinaRepository.findByAtivaTrue();
        List<UUID> disciplinaIds = idsDisciplinas(disciplinas);

        return OfflineBootstrapDTO.builder()
                .turmas(turmaRepository.findAll())
                .professores(professorRepository.findAll())
                .alunos(alunoRepository.findAll())
                .disciplinas(disciplinas)
                .aulas(aulasPorDisciplinas(disciplinaIds))
                .horarios(horariosPorDisciplinas(disciplinaIds))
                .presencas(presencasPorDisciplinas(disciplinaIds))
                .build();
    }

    private OfflineBootstrapDTO montarProfessor(UUID professorId) {
        Professor professor = professorRepository.findById(professorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Professor nao encontrado."));
        List<Disciplina> disciplinas = disciplinaRepository.findByProfessorIdAndAtivaTrue(professorId);
        List<UUID> disciplinaIds = idsDisciplinas(disciplinas);
        List<Turma> turmas = disciplinas.stream()
                .map(Disciplina::getTurma)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        List<UUID> turmaIds = turmas.stream().map(Turma::getId).toList();
        List<Aluno> alunos = turmaIds.stream()
                .flatMap(turmaId -> alunoRepository.findByTurmaId(turmaId).stream())
                .distinct()
                .toList();

        return OfflineBootstrapDTO.builder()
                .turmas(turmas)
                .professores(List.of(professor))
                .alunos(alunos)
                .disciplinas(disciplinas)
                .aulas(aulasPorDisciplinas(disciplinaIds))
                .horarios(horariosPorDisciplinas(disciplinaIds))
                .presencas(presencasPorDisciplinas(disciplinaIds))
                .build();
    }

    private OfflineBootstrapDTO montarAluno(UUID alunoId) {
        Aluno aluno = alunoRepository.findById(alunoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Aluno nao encontrado."));
        List<Disciplina> disciplinas = aluno.getTurma() == null
                ? List.of()
                : disciplinaRepository.findByTurmaIdAndAtivaTrue(aluno.getTurma().getId());
        List<UUID> disciplinaIds = idsDisciplinas(disciplinas);
        List<Professor> professores = disciplinas.stream()
                .map(Disciplina::getProfessor)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        return OfflineBootstrapDTO.builder()
                .turmas(aluno.getTurma() == null ? List.of() : List.of(aluno.getTurma()))
                .professores(professores)
                .alunos(List.of(aluno))
                .disciplinas(disciplinas)
                .aulas(aulasPorDisciplinas(disciplinaIds))
                .horarios(horariosPorDisciplinas(disciplinaIds))
                .presencas(presencasPorDisciplinas(disciplinaIds).stream()
                        .filter(presenca -> presenca.getAluno() != null
                                && presenca.getAluno().getId().equals(alunoId))
                        .toList())
                .qrCodeBase64(Base64.getEncoder().encodeToString(qrCodeService.gerarQrCode(alunoId.toString())))
                .build();
    }

    private List<UUID> idsDisciplinas(List<Disciplina> disciplinas) {
        return disciplinas.stream().map(Disciplina::getId).toList();
    }

    private List<com.unicheck.Unicheckapi.model.Aula> aulasPorDisciplinas(List<UUID> disciplinaIds) {
        return disciplinaIds.isEmpty() ? List.of() : aulaRepository.findByDisciplinaIdIn(disciplinaIds);
    }

    private List<com.unicheck.Unicheckapi.model.HorarioAula> horariosPorDisciplinas(List<UUID> disciplinaIds) {
        return disciplinaIds.isEmpty() ? List.of() : horarioAulaRepository.findByDisciplinaIdIn(disciplinaIds);
    }

    private List<com.unicheck.Unicheckapi.model.Presenca> presencasPorDisciplinas(List<UUID> disciplinaIds) {
        return disciplinaIds.isEmpty() ? List.of() : presencaRepository.findByAulaDisciplinaIdIn(disciplinaIds);
    }
}

