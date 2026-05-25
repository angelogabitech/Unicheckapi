package com.unicheck.Unicheckapi.service;

import com.unicheck.Unicheckapi.dto.DisciplinaBulkRequestDTO;
import com.unicheck.Unicheckapi.dto.DisciplinaRequestDTO;
import com.unicheck.Unicheckapi.model.Aluno;
import com.unicheck.Unicheckapi.model.Disciplina;
import com.unicheck.Unicheckapi.model.Professor;
import com.unicheck.Unicheckapi.model.Role;
import com.unicheck.Unicheckapi.model.Turma;
import com.unicheck.Unicheckapi.model.Usuario;
import com.unicheck.Unicheckapi.repository.AlunoRepository;
import com.unicheck.Unicheckapi.repository.DisciplinaRepository;
import com.unicheck.Unicheckapi.repository.UsuarioRepository;
import com.unicheck.Unicheckapi.ws.RealtimeEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DisciplinaService {

    private final DisciplinaRepository disciplinaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AlunoRepository alunoRepository;
    private final ProfessorService professorService;
    private final TurmaService turmaService;
    private final RealtimeEventPublisher realtimeEventPublisher;

    public Disciplina criar(DisciplinaRequestDTO dto) {
        Professor professor = professorService.buscarPorId(dto.getProfessorId());
        Turma turma = turmaService.buscarPorId(dto.getTurmaId());

        boolean conflito = disciplinaRepository.existsByProfessorIdAndTurmaIdAndAtivaTrue(dto.getProfessorId(), dto.getTurmaId());
        if (conflito) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Este professor ja possui uma disciplina nesta turma.");
        }

        Disciplina disciplina = Disciplina.builder()
                .nome(dto.getNome())
                .codigo(dto.getCodigo())
                .turma(turma)
                .professor(professor)
                .build();

        Disciplina salva = disciplinaRepository.save(disciplina);
        publicarEventoDisciplina(salva, "DISCIPLINA_CRIADA");
        return salva;
    }

    public List<Disciplina> criarEmLote(DisciplinaBulkRequestDTO dto) {
        return dto.getTurmaIds().stream()
                .map(turmaId -> {
                    DisciplinaRequestDTO request = new DisciplinaRequestDTO();
                    request.setNome(dto.getNome());
                    request.setCodigo(dto.getCodigo());
                    request.setProfessorId(dto.getProfessorId());
                    request.setTurmaId(turmaId);
                    return criar(request);
                })
                .toList();
    }

    public Disciplina buscarPorId(UUID id) {
        return disciplinaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Disciplina nao encontrada"));
    }

    public Usuario usuarioAutenticado() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario nao autenticado");
        }

        return usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario nao encontrado"));
    }

    public Disciplina buscarPermitidaParaUsuario(UUID id) {
        Disciplina disciplina = buscarPorId(id);
        Usuario usuario = usuarioAutenticado();

        if (usuario.getRole() == Role.GESTOR) {
            return disciplina;
        }

        if (usuario.getRole() == Role.PROFESSOR
                && disciplina.getProfessor() != null
                && disciplina.getProfessor().getId().equals(usuario.getId())) {
            return disciplina;
        }

        if (usuario.getRole() == Role.ALUNO
                && disciplina.getTurma() != null) {
            Aluno aluno = alunoRepository.findById(usuario.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                            "Aluno nao encontrado para validar permissao."));
            if (aluno.getTurma() != null && aluno.getTurma().getId().equals(disciplina.getTurma().getId())) {
                return disciplina;
            }
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Usuario sem permissao para acessar esta disciplina.");
    }

    public Disciplina buscarDisciplinaPermitidaParaUsuario(UUID id) {
        return buscarPermitidaParaUsuario(id);
    }

    public void validarPermissaoDisciplina(Disciplina disciplina) {
        if (disciplina == null || disciplina.getId() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Disciplina invalida para validar permissao.");
        }
        buscarPermitidaParaUsuario(disciplina.getId());
    }

    public List<Disciplina> listarMinhas() {
        Usuario usuario = usuarioAutenticado();

        if (usuario.getRole() == Role.GESTOR) {
            return listar();
        }

        if (usuario.getRole() == Role.PROFESSOR) {
            return disciplinaRepository.findByProfessorIdAndAtivaTrue(usuario.getId());
        }

        if (usuario.getRole() == Role.ALUNO) {
            Aluno aluno = alunoRepository.findById(usuario.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                            "Aluno nao encontrado para listar disciplinas."));
            if (aluno.getTurma() == null) return List.of();
            return disciplinaRepository.findByTurmaIdAndAtivaTrue(aluno.getTurma().getId());
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario sem permissao para listar disciplinas.");
    }

    public Disciplina atualizar(UUID id, DisciplinaRequestDTO dto) {
        Disciplina disciplina = buscarPorId(id);
        Professor professor = professorService.buscarPorId(dto.getProfessorId());
        Turma turma = turmaService.buscarPorId(dto.getTurmaId());

        boolean conflito = disciplinaRepository.existsByProfessorIdAndTurmaIdAndIdNotAndAtivaTrue(
                dto.getProfessorId(),
                dto.getTurmaId(),
                id
        );

        if (conflito) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Este professor ja possui uma disciplina nesta turma.");
        }

        disciplina.setNome(dto.getNome());
        disciplina.setCodigo(dto.getCodigo());
        disciplina.setTurma(turma);
        disciplina.setProfessor(professor);

        Disciplina salva = disciplinaRepository.save(disciplina);
        publicarEventoDisciplina(salva, "DISCIPLINA_ATUALIZADA");
        return salva;
    }

    public void deletar(UUID id) {
        Disciplina disciplina = buscarPorId(id);
        disciplina.setAtiva(false);
        disciplinaRepository.save(disciplina);
        publicarEventoDisciplina(disciplina, "DISCIPLINA_DELETADA");
    }

    public List<Disciplina> listar(){
        return disciplinaRepository.findByAtivaTrue();
    }

    public List<Disciplina> listarPorTurma(UUID turmaId) {
        Usuario usuario = usuarioAutenticado();

        if (usuario.getRole() == Role.GESTOR) {
            return disciplinaRepository.findByTurmaIdAndAtivaTrue(turmaId);
        }

        if (usuario.getRole() == Role.PROFESSOR
                && disciplinaRepository.existsByProfessorIdAndTurmaIdAndAtivaTrue(usuario.getId(), turmaId)) {
            return disciplinaRepository.findByTurmaIdAndAtivaTrue(turmaId);
        }

        if (usuario.getRole() == Role.ALUNO) {
            Aluno aluno = alunoRepository.findById(usuario.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                            "Aluno nao encontrado para listar disciplinas da turma."));
            if (aluno.getTurma() != null && aluno.getTurma().getId().equals(turmaId)) {
                return disciplinaRepository.findByTurmaIdAndAtivaTrue(turmaId);
            }
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Usuario sem permissao para listar disciplinas desta turma.");
    }

    public List<Disciplina> listarPorTurmaSemFiltro(UUID turmaId) {
        return disciplinaRepository.findByTurmaIdAndAtivaTrue(turmaId);
    }

    public List<Disciplina> listarPorProfessor(UUID professorId) {
        Usuario usuario = usuarioAutenticado();

        if (usuario.getRole() == Role.PROFESSOR && !usuario.getId().equals(professorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Professor nao pode listar disciplinas de outro professor.");
        }

        return disciplinaRepository.findByProfessorIdAndAtivaTrue(professorId);
    }

    public List<Disciplina> listarPorProfessorPermitido(UUID professorId) {
        return listarPorProfessor(professorId);
    }

    private void publicarEventoDisciplina(Disciplina disciplina, String tipo) {
        realtimeEventPublisher.gestor(tipo, "DISCIPLINA", disciplina.getId());
        realtimeEventPublisher.disciplina(disciplina.getId(), tipo);
        if (disciplina.getTurma() != null) {
            realtimeEventPublisher.turma(disciplina.getTurma().getId(), tipo);
        }
    }
}

