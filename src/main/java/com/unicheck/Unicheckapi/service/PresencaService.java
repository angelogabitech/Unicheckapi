
package com.unicheck.Unicheckapi.service;
import com.unicheck.Unicheckapi.dto.AlunoPresencaResumoDTO;
import com.unicheck.Unicheckapi.dto.DashboardDisciplinaDTO;
import com.unicheck.Unicheckapi.dto.PresencaRegistroResponseDTO;
import com.unicheck.Unicheckapi.dto.SincronizacaoPresencaDTO;
import com.unicheck.Unicheckapi.model.Aluno;

import com.unicheck.Unicheckapi.model.*;
import com.unicheck.Unicheckapi.repository.AlunoRepository;
import com.unicheck.Unicheckapi.repository.AulaRepository;
import com.unicheck.Unicheckapi.repository.DisciplinaRepository;
import com.unicheck.Unicheckapi.repository.PresencaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PresencaService {

    private final PresencaRepository presencaRepository;
    private final AlunoRepository alunoRepository;
    private final AulaRepository aulaRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final DisciplinaService disciplinaService;

    @Transactional
    public PresencaRegistroResponseDTO registrarPresenca(String alunoIdStr, UUID aulaId) {
        UUID alunoId = UUID.fromString(alunoIdStr);

        Aluno aluno = alunoRepository.findById(alunoId)
                .orElseThrow(() -> new RuntimeException("Aluno nÃ£o encontrado"));

        Aula aula = aulaRepository.findById(aulaId)
                .orElseThrow(() -> new RuntimeException("Aula nÃ£o encontrada"));
        disciplinaService.buscarPermitidaParaUsuario(aula.getDisciplina().getId());

        var existente = presencaRepository.findByAlunoIdAndAulaId(alunoId, aulaId);
        if (existente.isPresent()) {
            return respostaPresenca(existente.get(), aluno);
        }

        if (!aula.isAtiva()) {
            throw new RuntimeException("A aula jÃ¡ foi encerrada");
        }

        Presenca presenca = Presenca.builder()
                .aluno(aluno)
                .aula(aula)
                .disciplina(aula.getDisciplina())
                .build();

        presencaRepository.save(presenca);

        return respostaPresenca(presenca, aluno);
    }
    public List<Presenca> listar(){
        return presencaRepository.findAll();
    }
    public List<Presenca> buscarPorAluno(UUID id){
        Usuario usuario = disciplinaService.usuarioAutenticado();
        if (usuario.getRole() != Role.GESTOR && !usuario.getId().equals(id)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "Usuario sem permissao para consultar presencas deste aluno.");
        }
        return presencaRepository.findByAlunoId(id);
    }
    public List<Presenca> buscarPorDisciplina(UUID disciplinaId){
        disciplinaService.buscarPermitidaParaUsuario(disciplinaId);
        return presencaRepository.findByAulaDisciplinaId(disciplinaId);
    }
    public List<Presenca> buscarPorAula(UUID aulaId) {
        Aula aula = aulaRepository.findById(aulaId)
                .orElseThrow(() -> new RuntimeException("Aula nÃ£o encontrada"));
        disciplinaService.buscarPermitidaParaUsuario(aula.getDisciplina().getId());
        return presencaRepository.findByAulaId(aulaId);
    }
    public List<DashboardDisciplinaDTO> gerarDashboard() {
        List<Disciplina> disciplinas = disciplinaRepository.findByAtivaTrue();
        return calcularDashboard(disciplinas);
    }

    public List<DashboardDisciplinaDTO> gerarDashboardProfessor(UUID professorId) {
        List<Disciplina> disciplinas = disciplinaService.listarPorProfessor(professorId);
        return calcularDashboard(disciplinas);
    }

    private List<DashboardDisciplinaDTO> calcularDashboard(List<Disciplina> disciplinas) {
        return disciplinas.stream().map(d -> {
            boolean possuiTurma = d.getTurma() != null;
            List<Aluno> alunos = possuiTurma ? alunoRepository.findByTurmaId(d.getTurma().getId()) : List.of();
            long totalAlunos = alunos.size();
            long totalAulas = aulaRepository.countByDisciplinaId(d.getId());
            long totalPresencas = alunos.stream()
                    .mapToLong(aluno -> presencaRepository.countAulasPresentesPorAlunoDisciplina(aluno.getId(), d.getId()))
                    .sum();
            long totalFaltas = (totalAlunos * totalAulas) - totalPresencas;
            double percentual = totalAulas == 0 || totalAlunos == 0 ? 0 :
                    (double) totalPresencas / (totalAlunos * totalAulas) * 100;

            return DashboardDisciplinaDTO.builder()
                    .disciplinaId(d.getId())
                    .turmaId(possuiTurma ? d.getTurma().getId() : null)
                    .professorId(d.getProfessor() != null ? d.getProfessor().getId() : null)
                    .nomeDisciplina(d.getNome())
                    .nomeTurma(possuiTurma ? d.getTurma().getIdentificacao() : "Sem turma")
                    .nomeProfessor(d.getProfessor() != null ? d.getProfessor().getNome() : null)
                    .totalAlunos(totalAlunos)
                    .totalPresencas(totalPresencas)
                    .totalFaltas(Math.max(0, totalFaltas))
                    .percentualPresenca(percentual)
                    .build();
        }).collect(java.util.stream.Collectors.toList());
    }

    public List<AlunoPresencaResumoDTO> resumoAlunosPorDisciplina(UUID disciplinaId) {
        Disciplina disciplina = disciplinaService.buscarPermitidaParaUsuario(disciplinaId);
        long totalAulas = aulaRepository.countByDisciplinaId(disciplinaId);

        if (disciplina.getTurma() == null) {
            return List.of();
        }

        return alunoRepository.findByTurmaId(disciplina.getTurma().getId()).stream()
                .map(aluno -> {
                    long presencas = presencaRepository.countAulasPresentesPorAlunoDisciplina(aluno.getId(), disciplinaId);
                    long faltas = Math.max(totalAulas - presencas, 0);
                    double percentual = totalAulas > 0 ? ((double) presencas / totalAulas) * 100 : 0;

                    return AlunoPresencaResumoDTO.builder()
                            .alunoId(aluno.getId())
                            .nome(aluno.getNome())
                            .matricula(aluno.getMatricula())
                            .email(aluno.getEmail())
                            .fotoUrl(aluno.getFotoUrl())
                            .turmaId(disciplina.getTurma().getId())
                            .nomeTurma(disciplina.getTurma().getIdentificacao())
                            .disciplinaId(disciplinaId)
                            .totalAulas(totalAulas)
                            .presencas(presencas)
                            .faltas(faltas)
                            .percentual(percentual)
                            .build();
                })
                .toList();
    }
    public void deletarPresenca(UUID id) {
        Presenca presenca = presencaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PresenÃ§a nÃ£o encontrada"));
        disciplinaService.buscarPermitidaParaUsuario(presenca.getAula().getDisciplina().getId());
        presencaRepository.delete(presenca);
    }

    public void sincronizar(List<SincronizacaoPresencaDTO> lista) {
        for (SincronizacaoPresencaDTO dto : lista) {
            if (dto.getClientId() != null && presencaRepository.findByClientId(dto.getClientId()).isPresent()) {
                continue;
            }
            // Evitar duplicata: verifica se jÃ¡ existe presenÃ§a para o par (aluno, aula)
            boolean jaExiste = presencaRepository.existsByAlunoIdAndAulaId(
                    dto.getAlunoId(), dto.getAulaId());

            if (!jaExiste) {
                Aluno aluno = alunoRepository.findById(dto.getAlunoId())
                        .orElseThrow(() -> new RuntimeException("Aluno nÃ£o encontrado: " + dto.getAlunoId()));
                Aula aula = aulaRepository.findById(dto.getAulaId())
                        .orElseThrow(() -> new RuntimeException("Aula nÃ£o encontrada: " + dto.getAulaId()));
                disciplinaService.buscarPermitidaParaUsuario(aula.getDisciplina().getId());

                Presenca presenca = Presenca.builder()
                        .clientId(dto.getClientId())
                        .aluno(aluno)
                        .aula(aula)
                        .disciplina(aula.getDisciplina())
                        .dataHora(dto.getDataHoraLocal() != null ? dto.getDataHoraLocal().toLocalDateTime() : null)
                        .build();
                presencaRepository.save(presenca);
            }
        }
    }

    private PresencaRegistroResponseDTO respostaPresenca(Presenca presenca, Aluno aluno) {
        return PresencaRegistroResponseDTO.builder()
                .presencaId(presenca.getId())
                .alunoId(aluno.getId())
                .nome(aluno.getNome())
                .matricula(aluno.getMatricula())
                .fotoUrl(aluno.getFotoUrl())
                .dataHora(presenca.getDataHora())
                .build();
    }
}

