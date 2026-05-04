
package com.unicheck.Unicheckapi.service;
import com.unicheck.Unicheckapi.dto.DashboardDisciplinaDTO;
import com.unicheck.Unicheckapi.dto.SincronizacaoPresencaDTO;
import com.unicheck.Unicheckapi.model.Aluno;

import com.unicheck.Unicheckapi.dto.AlunoResponseDTO;
import com.unicheck.Unicheckapi.model.*;
import com.unicheck.Unicheckapi.repository.AlunoRepository;
import com.unicheck.Unicheckapi.repository.AulaRepository;
import com.unicheck.Unicheckapi.repository.DisciplinaRepository;
import com.unicheck.Unicheckapi.repository.PresencaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
    private final QrCodeService qrService;

    public AlunoResponseDTO registrarPresenca(String alunoIdStr, UUID aulaId) {
        UUID alunoId = UUID.fromString(alunoIdStr);

        Aluno aluno = alunoRepository.findById(alunoId)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado"));

        Aula aula = aulaRepository.findById(aulaId)      // ← minúsculo
                .orElseThrow(() -> new RuntimeException("Aula não encontrada"));
        disciplinaService.validarPermissaoDisciplina(aula.getDisciplina());

        if (!aula.isAtiva()) {
            throw new RuntimeException("A aula já foi encerrada");
        }

        if (presencaRepository.existsByAlunoIdAndAulaId(aluno.getId(), aula.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Presenca ja registrada para este aluno nesta aula.");
        }

        Presenca presenca = Presenca.builder()
                .aluno(aluno)
                .aula(aula)
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
        disciplinaService.buscarDisciplinaPermitidaParaUsuario(disciplinaId);
        return presencaRepository.findByAulaDisciplinaId(disciplinaId);
    }
    public List<DashboardDisciplinaDTO> gerarDashboard() {
        List<Disciplina> disciplinas = disciplinaRepository.findByAtivaTrue();
        return calcularDashboard(disciplinas);
    }

    public List<DashboardDisciplinaDTO> gerarDashboardProfessor(UUID professorId) {
        Usuario usuario = disciplinaService.usuarioAutenticado();
        if (usuario.getRole() == Role.PROFESSOR && !usuario.getId().equals(professorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Professor autenticado nao pode acessar dashboard de outro professor.");
        }
        List<Disciplina> disciplinas = disciplinaRepository.findByProfessorIdAndAtivaTrue(professorId);
        return calcularDashboard(disciplinas);
    }

    private List<DashboardDisciplinaDTO> calcularDashboard(List<Disciplina> disciplinas) {
        return disciplinas.stream().filter(d -> d.getTurma() != null).map(d -> {
            long totalAlunos = alunoRepository.countByTurmaId(d.getTurma().getId());
            long totalAulas = aulaRepository.countByDisciplinaId(d.getId());
            long totalPresencas = presencaRepository.countByAulasDisciplinaId(d.getId());
            long totalFaltas = (totalAlunos * totalAulas) - totalPresencas;
            double percentual = totalAulas == 0 || totalAlunos == 0 ? 0 :
                    (double) totalPresencas / (totalAlunos * totalAulas) * 100;

            return DashboardDisciplinaDTO.builder()
                    .nomeDisciplina(d.getNome())
                    .nomeTurma(d.getTurma().getIdentificacao())
                    .totalAlunos(totalAlunos)
                    .totalPresencas(totalPresencas)
                    .totalFaltas(Math.max(0, totalFaltas))
                    .percentualPresenca(percentual)
                    .build();
        }).collect(java.util.stream.Collectors.toList());
    }
    public void sincronizar(List<SincronizacaoPresencaDTO> lista) {
        for (SincronizacaoPresencaDTO dto : lista) {
            // Evitar duplicata: verifica se já existe presença para o par (aluno, aula)
            boolean jaExiste = presencaRepository.existsByAlunoIdAndAulaId(
                    dto.getAlunoId(), dto.getAulaId());

            if (!jaExiste) {
                Aluno aluno = alunoRepository.findById(dto.getAlunoId())
                        .orElseThrow(() -> new RuntimeException("Aluno não encontrado: " + dto.getAlunoId()));
                Aula aula = aulaRepository.findById(dto.getAulaId())
                        .orElseThrow(() -> new RuntimeException("Aula não encontrada: " + dto.getAulaId()));

                Presenca presenca = Presenca.builder()
                        .aluno(aluno)
                        .aula(aula)
                        .build();
                presencaRepository.save(presenca);
            }
        }
    }

    public void deletarPresenca(UUID id) {
        Presenca presenca = presencaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Presenca nao encontrada"));
        disciplinaService.validarPermissaoDisciplina(presenca.getAula().getDisciplina());
        presencaRepository.delete(presenca);
    }
}
