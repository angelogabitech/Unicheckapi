package com.unicheck.Unicheckapi.service;

import com.unicheck.Unicheckapi.dto.DisciplinaBulkRequestDTO;
import com.unicheck.Unicheckapi.dto.DisciplinaRequestDTO;
import com.unicheck.Unicheckapi.model.Disciplina;
import com.unicheck.Unicheckapi.model.Professor;
import com.unicheck.Unicheckapi.model.Role;
import com.unicheck.Unicheckapi.model.Turma;
import com.unicheck.Unicheckapi.model.Usuario;
import com.unicheck.Unicheckapi.repository.DisciplinaRepository;
import com.unicheck.Unicheckapi.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
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
    private final ProfessorService professorService;
    private final TurmaService turmaService;

    public Disciplina criar(DisciplinaRequestDTO dto) {
        Professor professor = dto.getProfessorId() == null ? null : professorService.buscarPorId(dto.getProfessorId());
        Turma turma = dto.getTurmaId() == null ? null : turmaService.buscarPorId(dto.getTurmaId());

        boolean conflito = dto.getProfessorId() != null
                && dto.getTurmaId() != null
                && disciplinaRepository.existsByProfessorIdAndTurmaId(dto.getProfessorId(), dto.getTurmaId());
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

        return disciplinaRepository.save(disciplina);
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

    public Disciplina buscarDisciplinaPermitidaParaUsuario(UUID disciplinaId) {
        Disciplina disciplina = buscarPorId(disciplinaId);
        validarPermissaoDisciplina(disciplina);
        return disciplina;
    }

    public void validarPermissaoDisciplina(Disciplina disciplina) {
        Usuario usuario = usuarioAutenticado();

        if (usuario.getRole() == Role.GESTOR) {
            return;
        }

        if (usuario.getRole() == Role.PROFESSOR
                && disciplina.getProfessor() != null
                && disciplina.getProfessor().getId().equals(usuario.getId())) {
            return;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Esta disciplina nao pertence ao professor autenticado.");
    }

    public Usuario usuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    public List<Disciplina> listar(){
        return disciplinaRepository.findByAtivaTrue();
    }

    public List<Disciplina> listarPorTurma(UUID turmaId) {
        Usuario usuario = usuarioAutenticado();
        if (usuario.getRole() == Role.GESTOR) {
            return disciplinaRepository.findByTurmaIdAndAtivaTrue(turmaId);
        }
        if (usuario.getRole() == Role.PROFESSOR) {
            return disciplinaRepository.findByTurmaIdAndProfessorIdAndAtivaTrue(turmaId, usuario.getId());
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Usuario autenticado nao pode listar disciplinas por turma.");
    }

    public List<Disciplina> listarPorProfessor(UUID professorId) {
        return disciplinaRepository.findByProfessorIdAndAtivaTrue(professorId);
    }

    public List<Disciplina> listarPorProfessorPermitido(UUID professorId) {
        Usuario usuario = usuarioAutenticado();
        if (usuario.getRole() == Role.GESTOR) {
            return listarPorProfessor(professorId);
        }
        if (usuario.getRole() == Role.PROFESSOR && usuario.getId().equals(professorId)) {
            return listarPorProfessor(professorId);
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Professor autenticado nao pode listar disciplinas de outro professor.");
    }

    public List<Disciplina> listarMinhas() {
        Usuario usuario = usuarioAutenticado();
        if (usuario.getRole() == Role.GESTOR) {
            return listar();
        }
        if (usuario.getRole() == Role.PROFESSOR) {
            return disciplinaRepository.findByProfessorIdAndAtivaTrue(usuario.getId());
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Usuario autenticado nao pode listar disciplinas de professor.");
    }

    public Disciplina atualizar(UUID id, DisciplinaRequestDTO dto) {
        Disciplina disciplina = buscarPorId(id);
        Professor professor = dto.getProfessorId() == null ? null : professorService.buscarPorId(dto.getProfessorId());
        Turma turma = dto.getTurmaId() == null ? null : turmaService.buscarPorId(dto.getTurmaId());

        boolean conflito = dto.getProfessorId() != null
                && dto.getTurmaId() != null
                && disciplinaRepository.existsByProfessorIdAndTurmaIdAndIdNot(dto.getProfessorId(), dto.getTurmaId(), id);
        if (conflito) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Este professor ja possui uma disciplina nesta turma.");
        }

        disciplina.setNome(dto.getNome());
        disciplina.setCodigo(dto.getCodigo());
        disciplina.setProfessor(professor);
        disciplina.setTurma(turma);

        return disciplinaRepository.save(disciplina);
    }

    public void deletar(UUID id) {
        Disciplina disciplina = buscarPorId(id);
        disciplina.setAtiva(false);
        disciplinaRepository.save(disciplina);
    }
}
