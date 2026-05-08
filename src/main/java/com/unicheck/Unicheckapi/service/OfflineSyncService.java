package com.unicheck.Unicheckapi.service;

import com.unicheck.Unicheckapi.dto.OfflineAulaSyncDTO;
import com.unicheck.Unicheckapi.dto.OfflineEncerramentoAulaSyncDTO;
import com.unicheck.Unicheckapi.dto.OfflinePresencaSyncDTO;
import com.unicheck.Unicheckapi.dto.OfflineSyncErroDTO;
import com.unicheck.Unicheckapi.dto.OfflineSyncMapDTO;
import com.unicheck.Unicheckapi.dto.OfflineSyncRequestDTO;
import com.unicheck.Unicheckapi.dto.OfflineSyncResponseDTO;
import com.unicheck.Unicheckapi.model.Aluno;
import com.unicheck.Unicheckapi.model.Aula;
import com.unicheck.Unicheckapi.model.Disciplina;
import com.unicheck.Unicheckapi.model.Presenca;
import com.unicheck.Unicheckapi.repository.AlunoRepository;
import com.unicheck.Unicheckapi.repository.AulaRepository;
import com.unicheck.Unicheckapi.repository.PresencaRepository;
import lombok.RequiredArgsConstructor;
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
    private final DisciplinaService disciplinaService;

    @Transactional
    public OfflineSyncResponseDTO sincronizar(OfflineSyncRequestDTO request) {
        if (request == null) {
            request = new OfflineSyncRequestDTO();
        }

        OfflineSyncResponseDTO response = new OfflineSyncResponseDTO();
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
                response.getEncerramentos().add(new OfflineSyncMapDTO(dto.getClientId(), aula.getId()));
            } catch (Exception e) {
                response.getErros().add(erro(dto.getClientId(), "ENCERRAR_AULA", e));
            }
        }

        return response;
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
            return aulaRepository.save(aula);
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

        return presencaRepository.save(presenca);
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

