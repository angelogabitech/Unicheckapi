package com.unicheck.Unicheckapi.service;

import com.unicheck.Unicheckapi.dto.*;
import com.unicheck.Unicheckapi.model.*;
import com.unicheck.Unicheckapi.repository.*;
import com.unicheck.Unicheckapi.ws.RealtimeEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OfflineSyncService {

    private final AulaRepository aulaRepository;
    private final AlunoRepository alunoRepository;
    private final PresencaRepository presencaRepository;
    private final TurmaRepository turmaRepository;
    private final ProfessorRepository professorRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final MatriculaRepository matriculaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final DisciplinaService disciplinaService;
    private final RealtimeEventPublisher realtimeEventPublisher;

    @Transactional
    public OfflineSyncResponseDTO sincronizar(OfflineSyncRequestDTO request) {
        if (request == null) {
            request = new OfflineSyncRequestDTO();
        }

        OfflineSyncResponseDTO response = new OfflineSyncResponseDTO();
        Map<UUID, UUID> turmaClientToServer = new HashMap<>();
        Map<UUID, UUID> professorClientToServer = new HashMap<>();

        if (temBlocosGestor(request)) {
            Usuario usuario = disciplinaService.usuarioAutenticado();
            if (usuario.getRole() == Role.GESTOR) {
                processarTurmasCriar(request, response, turmaClientToServer);
                processarTurmasEditar(request, response);
                processarProfessoresCriar(request, response, professorClientToServer);
                processarProfessoresEditar(request, response);
                processarAlunosCriar(request, response, turmaClientToServer);
                processarAlunosEditar(request, response, turmaClientToServer);
                processarDisciplinasCriar(request, response, turmaClientToServer, professorClientToServer);
                processarDisciplinasEditar(request, response, turmaClientToServer, professorClientToServer);
            } else {
                response.getErros().add(new OfflineSyncErroDTO(null, "GESTOR", "Usuario sem permissao para sincronizar cadastros do gestor"));
            }
        }

        Map<UUID, Aula> aulasPorClientId = new HashMap<>();

        for (OfflineAulaSyncDTO dto : safeList(request.getAulas())) {
            try {
                Aula aula = sincronizarAula(dto);
                aulasPorClientId.put(dto.getClientId(), aula);
                response.getAulas().add(new OfflineSyncMapDTO(dto.getClientId(), aula.getId()));
            } catch (Exception e) {
                response.getErros().add(erro(dto.getClientId(), "AULA", e));
            }
        }

        for (OfflinePresencaSyncDTO dto : safeList(request.getPresencas())) {
            try {
                Presenca presenca = sincronizarPresenca(dto, aulasPorClientId);
                response.getPresencas().add(new OfflineSyncMapDTO(dto.getClientId(), presenca.getId()));
            } catch (Exception e) {
                response.getErros().add(erro(dto.getClientId(), "PRESENCA", e));
            }
        }

        for (OfflineEncerramentoAulaSyncDTO dto : safeList(request.getEncerramentos())) {
            try {
                Aula aula = resolverAula(dto.getAulaId(), dto.getAulaClientId(), aulasPorClientId);
                disciplinaService.buscarPermitidaParaUsuario(aula.getDisciplina().getId());
                aula.setAtiva(false);
                aulaRepository.save(aula);
                publicarEventoAula(aula, "AULA_ENCERRADA");
                response.getEncerramentos().add(new OfflineSyncMapDTO(dto.getClientId(), aula.getId()));
            } catch (Exception e) {
                response.getErros().add(erro(dto.getClientId(), "ENCERRAR_AULA", e));
            }
        }

        if (temBlocosGestor(request) && disciplinaService.usuarioAutenticado().getRole() == Role.GESTOR) {
            processarDisciplinasDeletar(request, response);
            processarAlunosDeletar(request, response);
            processarProfessoresDeletar(request, response);
            processarTurmasDeletar(request, response);
        }

        return response;
    }

    private void processarTurmasCriar(OfflineSyncRequestDTO request, OfflineSyncResponseDTO response,
                                      Map<UUID, UUID> turmaClientToServer) {
        if (request.getTurmas() == null) return;

        for (OfflineTurmaCriarDTO dto : safeList(request.getTurmas().getCriar())) {
            try {
                validar(dto.getClientId() != null, "clientId da turma e obrigatorio");
                boolean jaExistia = turmaRepository.findByClientId(dto.getClientId()).isPresent();
                Turma turma = turmaRepository.findByClientId(dto.getClientId()).orElseGet(() -> {
                    Turma nova = Turma.builder()
                            .clientId(dto.getClientId())
                            .identificacao(dto.getIdentificacao())
                            .curso(dto.getCurso())
                            .periodo(dto.getPeriodo())
                            .build();
                    return turmaRepository.save(nova);
                });
                turmaClientToServer.put(dto.getClientId(), turma.getId());
                response.getTurmas().getCriadas().add(new OfflineSyncMapDTO(dto.getClientId(), turma.getId()));
                if (!jaExistia) {
                    realtimeEventPublisher.gestor("TURMA_CRIADA", "TURMA", turma.getId());
                    realtimeEventPublisher.turma(turma.getId(), "TURMA_CRIADA");
                }
            } catch (Exception e) {
                response.getErros().add(erro(dto.getClientId(), "TURMA_CRIAR", e));
            }
        }
    }

    private void processarTurmasEditar(OfflineSyncRequestDTO request, OfflineSyncResponseDTO response) {
        if (request.getTurmas() == null) return;

        for (OfflineTurmaEditarDTO dto : safeList(request.getTurmas().getEditar())) {
            try {
                Turma turma = turmaRepository.findById(dto.getServerId())
                        .orElseThrow(() -> new RuntimeException("Turma nao encontrada: " + dto.getServerId()));
                turma.setIdentificacao(dto.getIdentificacao());
                turma.setCurso(dto.getCurso());
                turma.setPeriodo(dto.getPeriodo());
                turmaRepository.save(turma);
                response.getTurmas().getEditadas().add(new OfflineSyncMapDTO(dto.getClientId(), turma.getId()));
                realtimeEventPublisher.gestor("TURMA_ATUALIZADA", "TURMA", turma.getId());
                realtimeEventPublisher.turma(turma.getId(), "TURMA_ATUALIZADA");
            } catch (Exception e) {
                response.getErros().add(erro(dto.getClientId(), "TURMA_EDITAR", e));
            }
        }
    }

    private void processarTurmasDeletar(OfflineSyncRequestDTO request, OfflineSyncResponseDTO response) {
        if (request.getTurmas() == null) return;

        for (OfflineTurmaDeletarDTO dto : safeList(request.getTurmas().getDeletar())) {
            try {
                turmaRepository.findById(dto.getServerId()).ifPresent(turma -> {
                    List<Aluno> alunos = alunoRepository.findByTurmaId(turma.getId());
                    alunos.forEach(aluno -> aluno.setTurma(null));
                    alunoRepository.saveAll(alunos);

                    List<Disciplina> disciplinas = disciplinaRepository.findByTurmaId(turma.getId());
                    disciplinas.forEach(disciplina -> disciplina.setTurma(null));
                    disciplinaRepository.saveAll(disciplinas);

                    turmaRepository.delete(turma);
                });
                response.getTurmas().getDeletadas().add(new OfflineSyncMapDTO(dto.getClientId(), dto.getServerId()));
                realtimeEventPublisher.gestor("TURMA_DELETADA", "TURMA", dto.getServerId());
                realtimeEventPublisher.turma(dto.getServerId(), "TURMA_DELETADA");
            } catch (Exception e) {
                response.getErros().add(erro(dto.getClientId(), "TURMA_DELETAR", e));
            }
        }
    }

    private void processarProfessoresCriar(OfflineSyncRequestDTO request, OfflineSyncResponseDTO response,
                                           Map<UUID, UUID> professorClientToServer) {
        if (request.getProfessores() == null) return;

        for (OfflineProfessorCriarDTO dto : safeList(request.getProfessores().getCriar())) {
            try {
                validar(dto.getClientId() != null, "clientId do professor e obrigatorio");
                boolean jaExistia = professorRepository.findByClientId(dto.getClientId()).isPresent();
                if (!jaExistia) {
                    validar(!usuarioRepository.existsByEmail(dto.getEmail()), "E-mail ja cadastrado: " + dto.getEmail());
                }
                Professor professor = professorRepository.findByClientId(dto.getClientId()).orElseGet(() -> {
                    Professor novo = new Professor();
                    novo.setClientId(dto.getClientId());
                    novo.setNome(dto.getNome());
                    novo.setEmail(dto.getEmail());
                    novo.setSenha(passwordEncoder.encode(dto.getSenha()));
                    novo.setRole(Role.PROFESSOR);
                    return professorRepository.save(novo);
                });
                professorClientToServer.put(dto.getClientId(), professor.getId());
                response.getProfessores().getCriadas().add(new OfflineSyncMapDTO(dto.getClientId(), professor.getId()));
                if (!jaExistia) {
                    realtimeEventPublisher.gestor("PROFESSOR_CRIADO", "PROFESSOR", professor.getId());
                }
            } catch (Exception e) {
                response.getErros().add(erro(dto.getClientId(), "PROFESSOR_CRIAR", e));
            }
        }
    }

    private void processarProfessoresEditar(OfflineSyncRequestDTO request, OfflineSyncResponseDTO response) {
        if (request.getProfessores() == null) return;

        for (OfflineProfessorEditarDTO dto : safeList(request.getProfessores().getEditar())) {
            try {
                Professor professor = professorRepository.findById(dto.getServerId())
                        .orElseThrow(() -> new RuntimeException("Professor nao encontrado: " + dto.getServerId()));
                validar(!usuarioRepository.existsByEmailAndIdNot(dto.getEmail(), professor.getId()), "E-mail ja cadastrado: " + dto.getEmail());
                professor.setNome(dto.getNome());
                professor.setEmail(dto.getEmail());
                if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
                    professor.setSenha(passwordEncoder.encode(dto.getSenha()));
                }
                professorRepository.save(professor);
                response.getProfessores().getEditadas().add(new OfflineSyncMapDTO(dto.getClientId(), professor.getId()));
                realtimeEventPublisher.gestor("PROFESSOR_ATUALIZADO", "PROFESSOR", professor.getId());
            } catch (Exception e) {
                response.getErros().add(erro(dto.getClientId(), "PROFESSOR_EDITAR", e));
            }
        }
    }

    private void processarProfessoresDeletar(OfflineSyncRequestDTO request, OfflineSyncResponseDTO response) {
        if (request.getProfessores() == null) return;

        for (OfflineProfessorDeletarDTO dto : safeList(request.getProfessores().getDeletar())) {
            try {
                professorRepository.findById(dto.getServerId()).ifPresent(professor -> {
                    List<Disciplina> disciplinas = disciplinaRepository.findByProfessorId(professor.getId());
                    disciplinas.forEach(disciplina -> disciplina.setProfessor(null));
                    disciplinaRepository.saveAll(disciplinas);
                    professorRepository.delete(professor);
                });
                response.getProfessores().getDeletadas().add(new OfflineSyncMapDTO(dto.getClientId(), dto.getServerId()));
                realtimeEventPublisher.gestor("PROFESSOR_DELETADO", "PROFESSOR", dto.getServerId());
            } catch (Exception e) {
                response.getErros().add(erro(dto.getClientId(), "PROFESSOR_DELETAR", e));
            }
        }
    }

    private void processarAlunosCriar(OfflineSyncRequestDTO request, OfflineSyncResponseDTO response,
                                      Map<UUID, UUID> turmaClientToServer) {
        if (request.getAlunos() == null) return;

        for (OfflineAlunoCriarDTO dto : safeList(request.getAlunos().getCriar())) {
            try {
                validar(dto.getClientId() != null, "clientId do aluno e obrigatorio");
                boolean jaExistia = alunoRepository.findByClientId(dto.getClientId()).isPresent();
                if (!jaExistia) {
                    validar(!usuarioRepository.existsByEmail(dto.getEmail()), "E-mail ja cadastrado: " + dto.getEmail());
                }
                Aluno aluno = alunoRepository.findByClientId(dto.getClientId()).orElseGet(() -> {
                    Aluno novo = new Aluno();
                    novo.setClientId(dto.getClientId());
                    novo.setNome(dto.getNome());
                    novo.setEmail(dto.getEmail());
                    novo.setMatricula(dto.getMatricula());
                    novo.setSenha(passwordEncoder.encode(dto.getSenha()));
                    novo.setRole(Role.ALUNO);
                    novo.setTurma(resolverTurma(dto.getTurmaServerId(), dto.getTurmaClientId(), turmaClientToServer));
                    return alunoRepository.save(novo);
                });
                response.getAlunos().getCriadas().add(new OfflineSyncMapDTO(dto.getClientId(), aluno.getId()));
                if (!jaExistia) {
                    publicarEventoAluno(aluno, "ALUNO_CRIADO");
                }
            } catch (Exception e) {
                response.getErros().add(erro(dto.getClientId(), "ALUNO_CRIAR", e));
            }
        }
    }

    private void processarAlunosEditar(OfflineSyncRequestDTO request, OfflineSyncResponseDTO response,
                                       Map<UUID, UUID> turmaClientToServer) {
        if (request.getAlunos() == null) return;

        for (OfflineAlunoEditarDTO dto : safeList(request.getAlunos().getEditar())) {
            try {
                Aluno aluno = alunoRepository.findById(dto.getServerId())
                        .orElseThrow(() -> new RuntimeException("Aluno nao encontrado: " + dto.getServerId()));
                validar(!usuarioRepository.existsByEmailAndIdNot(dto.getEmail(), aluno.getId()), "E-mail ja cadastrado: " + dto.getEmail());
                aluno.setNome(dto.getNome());
                aluno.setEmail(dto.getEmail());
                aluno.setMatricula(dto.getMatricula());
                if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
                    aluno.setSenha(passwordEncoder.encode(dto.getSenha()));
                }
                Turma turma = resolverTurma(dto.getTurmaServerId(), dto.getTurmaClientId(), turmaClientToServer);
                if (turma != null) {
                    aluno.setTurma(turma);
                }
                alunoRepository.save(aluno);
                response.getAlunos().getEditadas().add(new OfflineSyncMapDTO(dto.getClientId(), aluno.getId()));
                publicarEventoAluno(aluno, "ALUNO_ATUALIZADO");
            } catch (Exception e) {
                response.getErros().add(erro(dto.getClientId(), "ALUNO_EDITAR", e));
            }
        }
    }

    private void processarAlunosDeletar(OfflineSyncRequestDTO request, OfflineSyncResponseDTO response) {
        if (request.getAlunos() == null) return;

        for (OfflineAlunoDeletarDTO dto : safeList(request.getAlunos().getDeletar())) {
            try {
                alunoRepository.findById(dto.getServerId()).ifPresent(aluno -> {
                    UUID turmaId = aluno.getTurma() != null ? aluno.getTurma().getId() : null;
                    presencaRepository.deleteAll(presencaRepository.findByAlunoId(aluno.getId()));
                    matriculaRepository.deleteAll(matriculaRepository.findByAlunoId(aluno.getId()));
                    alunoRepository.delete(aluno);
                    realtimeEventPublisher.gestor("ALUNO_DELETADO", "ALUNO", aluno.getId());
                    realtimeEventPublisher.turma(turmaId, "ALUNO_DELETADO");
                });
                response.getAlunos().getDeletadas().add(new OfflineSyncMapDTO(dto.getClientId(), dto.getServerId()));
            } catch (Exception e) {
                response.getErros().add(erro(dto.getClientId(), "ALUNO_DELETAR", e));
            }
        }
    }

    private void processarDisciplinasCriar(OfflineSyncRequestDTO request, OfflineSyncResponseDTO response,
                                           Map<UUID, UUID> turmaClientToServer,
                                           Map<UUID, UUID> professorClientToServer) {
        if (request.getDisciplinas() == null) return;

        for (OfflineDisciplinaCriarDTO dto : safeList(request.getDisciplinas().getCriar())) {
            try {
                validar(dto.getClientId() != null, "clientId da disciplina e obrigatorio");
                boolean jaExistia = disciplinaRepository.findByClientId(dto.getClientId()).isPresent();
                Disciplina disciplina = disciplinaRepository.findByClientId(dto.getClientId()).orElseGet(() -> {
                    Disciplina nova = Disciplina.builder()
                            .clientId(dto.getClientId())
                            .nome(dto.getNome())
                            .codigo(dto.getCodigo())
                            .turma(resolverTurma(dto.getTurmaServerId(), dto.getTurmaClientId(), turmaClientToServer))
                            .professor(resolverProfessor(dto.getProfessorServerId(), dto.getProfessorClientId(), professorClientToServer))
                            .ativa(true)
                            .build();
                    return disciplinaRepository.save(nova);
                });
                response.getDisciplinas().getCriadas().add(new OfflineSyncMapDTO(dto.getClientId(), disciplina.getId()));
                if (!jaExistia) {
                    publicarEventoDisciplina(disciplina, "DISCIPLINA_CRIADA");
                }
            } catch (Exception e) {
                response.getErros().add(erro(dto.getClientId(), "DISCIPLINA_CRIAR", e));
            }
        }
    }

    private void processarDisciplinasEditar(OfflineSyncRequestDTO request, OfflineSyncResponseDTO response,
                                            Map<UUID, UUID> turmaClientToServer,
                                            Map<UUID, UUID> professorClientToServer) {
        if (request.getDisciplinas() == null) return;

        for (OfflineDisciplinaEditarDTO dto : safeList(request.getDisciplinas().getEditar())) {
            try {
                Disciplina disciplina = disciplinaRepository.findById(dto.getServerId())
                        .orElseThrow(() -> new RuntimeException("Disciplina nao encontrada: " + dto.getServerId()));
                disciplina.setNome(dto.getNome());
                disciplina.setCodigo(dto.getCodigo());

                Turma turma = resolverTurma(dto.getTurmaServerId(), dto.getTurmaClientId(), turmaClientToServer);
                if (turma != null) {
                    disciplina.setTurma(turma);
                }

                Professor professor = resolverProfessor(dto.getProfessorServerId(), dto.getProfessorClientId(), professorClientToServer);
                if (professor != null) {
                    disciplina.setProfessor(professor);
                }

                disciplinaRepository.save(disciplina);
                response.getDisciplinas().getEditadas().add(new OfflineSyncMapDTO(dto.getClientId(), disciplina.getId()));
                publicarEventoDisciplina(disciplina, "DISCIPLINA_ATUALIZADA");
            } catch (Exception e) {
                response.getErros().add(erro(dto.getClientId(), "DISCIPLINA_EDITAR", e));
            }
        }
    }

    private void processarDisciplinasDeletar(OfflineSyncRequestDTO request, OfflineSyncResponseDTO response) {
        if (request.getDisciplinas() == null) return;

        for (OfflineDisciplinaDeletarDTO dto : safeList(request.getDisciplinas().getDeletar())) {
            try {
                disciplinaRepository.findById(dto.getServerId()).ifPresent(disciplina -> {
                    disciplina.setAtiva(false);
                    disciplinaRepository.save(disciplina);
                    publicarEventoDisciplina(disciplina, "DISCIPLINA_DELETADA");
                });
                response.getDisciplinas().getDeletadas().add(new OfflineSyncMapDTO(dto.getClientId(), dto.getServerId()));
            } catch (Exception e) {
                response.getErros().add(erro(dto.getClientId(), "DISCIPLINA_DELETAR", e));
            }
        }
    }

    private Turma resolverTurma(UUID turmaServerId, UUID turmaClientId, Map<UUID, UUID> turmaClientToServer) {
        if (turmaServerId != null) {
            return turmaRepository.findById(turmaServerId)
                    .orElseThrow(() -> new RuntimeException("Turma nao encontrada: " + turmaServerId));
        }

        if (turmaClientId != null) {
            UUID turmaId = turmaClientToServer.get(turmaClientId);
            if (turmaId != null) {
                return turmaRepository.findById(turmaId)
                        .orElseThrow(() -> new RuntimeException("Turma nao encontrada: " + turmaId));
            }

            return turmaRepository.findByClientId(turmaClientId)
                    .orElseThrow(() -> new RuntimeException("Turma offline nao sincronizada: " + turmaClientId));
        }

        return null;
    }

    private Professor resolverProfessor(UUID professorServerId, UUID professorClientId,
                                        Map<UUID, UUID> professorClientToServer) {
        if (professorServerId != null) {
            return professorRepository.findById(professorServerId)
                    .orElseThrow(() -> new RuntimeException("Professor nao encontrado: " + professorServerId));
        }

        if (professorClientId != null) {
            UUID professorId = professorClientToServer.get(professorClientId);
            if (professorId != null) {
                return professorRepository.findById(professorId)
                        .orElseThrow(() -> new RuntimeException("Professor nao encontrado: " + professorId));
            }

            return professorRepository.findByClientId(professorClientId)
                    .orElseThrow(() -> new RuntimeException("Professor offline nao sincronizado: " + professorClientId));
        }

        return null;
    }

    private Aula sincronizarAula(OfflineAulaSyncDTO dto) {
        validar(dto.getClientId() != null, "clientId da aula e obrigatorio");
        validar(dto.getDisciplinaId() != null, "disciplinaId e obrigatorio");
        validar(dto.getTitulo() != null && !dto.getTitulo().isBlank(), "titulo da aula e obrigatorio");

        return aulaRepository.findByClientId(dto.getClientId()).orElseGet(() -> {
            Disciplina disciplina = disciplinaService.buscarPermitidaParaUsuario(dto.getDisciplinaId());
            Aula aula = Aula.builder()
                    .clientId(dto.getClientId())
                    .disciplina(disciplina)
                    .titulo(dto.getTitulo())
                    .dataHora(dto.getDataHoraLocal() != null ? dto.getDataHoraLocal().toLocalDateTime() : LocalDateTime.now())
                    .ativa(true)
                    .build();
            Aula salva = aulaRepository.save(aula);
            publicarEventoAula(salva, "AULA_INICIADA");
            return salva;
        });
    }

    private Presenca sincronizarPresenca(OfflinePresencaSyncDTO dto, Map<UUID, Aula> aulasPorClientId) {
        validar(dto.getClientId() != null, "clientId da presenca e obrigatorio");
        validar(dto.getAlunoId() != null, "alunoId e obrigatorio");

        var existentePorClientId = presencaRepository.findByClientId(dto.getClientId());
        if (existentePorClientId.isPresent()) {
            return existentePorClientId.get();
        }

        Aula aula = resolverAula(dto.getAulaId(), dto.getAulaClientId(), aulasPorClientId);
        disciplinaService.buscarPermitidaParaUsuario(aula.getDisciplina().getId());

        var existentePorAlunoAula = presencaRepository.findByAlunoIdAndAulaId(dto.getAlunoId(), aula.getId());
        if (existentePorAlunoAula.isPresent()) {
            return existentePorAlunoAula.get();
        }

        Aluno aluno = alunoRepository.findById(dto.getAlunoId())
                .orElseThrow(() -> new RuntimeException("Aluno nao encontrado: " + dto.getAlunoId()));

        Presenca presenca = Presenca.builder()
                .clientId(dto.getClientId())
                .aluno(aluno)
                .aula(aula)
                .disciplina(aula.getDisciplina())
                .dataHora(dto.getDataHoraLocal() != null ? dto.getDataHoraLocal().toLocalDateTime() : LocalDateTime.now())
                .build();

        Presenca salva = presencaRepository.save(presenca);
        publicarEventoPresenca(salva, "PRESENCA");
        return salva;
    }

    private Aula resolverAula(UUID aulaId, UUID aulaClientId, Map<UUID, Aula> aulasPorClientId) {
        if (aulaId != null) {
            return aulaRepository.findById(aulaId)
                    .orElseThrow(() -> new RuntimeException("Aula nao encontrada: " + aulaId));
        }

        if (aulaClientId != null) {
            Aula aulaDoPacote = aulasPorClientId.get(aulaClientId);
            if (aulaDoPacote != null) return aulaDoPacote;

            return aulaRepository.findByClientId(aulaClientId)
                    .orElseThrow(() -> new RuntimeException("Aula offline nao sincronizada: " + aulaClientId));
        }

        throw new RuntimeException("aulaId ou aulaClientId e obrigatorio");
    }

    private boolean temBlocosGestor(OfflineSyncRequestDTO request) {
        return temOperacoes(request.getTurmas())
                || temOperacoes(request.getProfessores())
                || temOperacoes(request.getAlunos())
                || temOperacoes(request.getDisciplinas());
    }

    private boolean temOperacoes(OfflineTurmasSyncDTO dto) {
        return dto != null && (!safeList(dto.getCriar()).isEmpty()
                || !safeList(dto.getEditar()).isEmpty()
                || !safeList(dto.getDeletar()).isEmpty());
    }

    private boolean temOperacoes(OfflineProfessoresSyncDTO dto) {
        return dto != null && (!safeList(dto.getCriar()).isEmpty()
                || !safeList(dto.getEditar()).isEmpty()
                || !safeList(dto.getDeletar()).isEmpty());
    }

    private boolean temOperacoes(OfflineAlunosSyncDTO dto) {
        return dto != null && (!safeList(dto.getCriar()).isEmpty()
                || !safeList(dto.getEditar()).isEmpty()
                || !safeList(dto.getDeletar()).isEmpty());
    }

    private boolean temOperacoes(OfflineDisciplinasSyncDTO dto) {
        return dto != null && (!safeList(dto.getCriar()).isEmpty()
                || !safeList(dto.getEditar()).isEmpty()
                || !safeList(dto.getDeletar()).isEmpty());
    }

    private void publicarEventoAula(Aula aula, String tipo) {
        if (aula.getDisciplina() != null) {
            realtimeEventPublisher.disciplina(aula.getDisciplina().getId(), tipo);
            if (aula.getDisciplina().getTurma() != null) {
                realtimeEventPublisher.turma(aula.getDisciplina().getTurma().getId(), tipo);
            }
        }
    }

    private void publicarEventoPresenca(Presenca presenca, String tipo) {
        if (presenca.getDisciplina() != null) {
            realtimeEventPublisher.disciplina(presenca.getDisciplina().getId(), tipo);
        }
        if (presenca.getAluno() != null) {
            realtimeEventPublisher.aluno(presenca.getAluno().getId(), tipo);
        }
    }

    private void publicarEventoAluno(Aluno aluno, String tipo) {
        realtimeEventPublisher.gestor(tipo, "ALUNO", aluno.getId());
        if (aluno.getTurma() != null) {
            realtimeEventPublisher.turma(aluno.getTurma().getId(), tipo);
        }
    }

    private void publicarEventoDisciplina(Disciplina disciplina, String tipo) {
        realtimeEventPublisher.gestor(tipo, "DISCIPLINA", disciplina.getId());
        realtimeEventPublisher.disciplina(disciplina.getId(), tipo);
        if (disciplina.getTurma() != null) {
            realtimeEventPublisher.turma(disciplina.getTurma().getId(), tipo);
        }
    }

    private void validar(boolean condicao, String mensagem) {
        if (!condicao) throw new RuntimeException(mensagem);
    }

    private OfflineSyncErroDTO erro(UUID clientId, String tipo, Exception e) {
        String mensagem = e.getMessage() != null ? e.getMessage() : "Erro ao sincronizar item offline";
        return new OfflineSyncErroDTO(clientId, tipo, mensagem);
    }

    private <T> List<T> safeList(List<T> value) {
        return value == null ? Collections.emptyList() : value;
    }
}
