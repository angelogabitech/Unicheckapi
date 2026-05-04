package com.unicheck.Unicheckapi.service;

import com.unicheck.Unicheckapi.dto.HorarioAulaRequestDTO;
import com.unicheck.Unicheckapi.model.Disciplina;
import com.unicheck.Unicheckapi.model.HorarioAula;
import com.unicheck.Unicheckapi.repository.HorarioAulaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HorarioAulaService {

    private final HorarioAulaRepository horarioAulaRepository;
    private final DisciplinaService disciplinaService;

    public HorarioAula criar(HorarioAulaRequestDTO dto) {
        Disciplina disciplina = disciplinaService.buscarDisciplinaPermitidaParaUsuario(dto.disciplinaId());

        HorarioAula horario = HorarioAula.builder()
                .disciplina(disciplina)
                .diaSemana(DayOfWeek.valueOf(dto.diaSemana().toUpperCase()))
                .horaInicio(LocalTime.parse(dto.horaInicio()))
                .horaFim(LocalTime.parse(dto.horaFim()))
                .build();

        return horarioAulaRepository.save(horario);
    }

    public List<HorarioAula> listarPorDisciplina(UUID disciplinaId) {
        disciplinaService.buscarDisciplinaPermitidaParaUsuario(disciplinaId);
        return horarioAulaRepository.findByDisciplinaId(disciplinaId);
    }

    public HorarioAula atualizar(UUID id, HorarioAulaRequestDTO dto) {
        HorarioAula horario = horarioAulaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Horario nao encontrado com id: " + id));
        disciplinaService.validarPermissaoDisciplina(horario.getDisciplina());

        Disciplina disciplina = disciplinaService.buscarDisciplinaPermitidaParaUsuario(dto.disciplinaId());

        horario.setDisciplina(disciplina);
        horario.setDiaSemana(DayOfWeek.valueOf(dto.diaSemana().toUpperCase()));
        horario.setHoraInicio(LocalTime.parse(dto.horaInicio()));
        horario.setHoraFim(LocalTime.parse(dto.horaFim()));

        return horarioAulaRepository.save(horario);
    }

    public void deletar(UUID id) {
        HorarioAula horario = horarioAulaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Horario nao encontrado com id: " + id));
        disciplinaService.validarPermissaoDisciplina(horario.getDisciplina());
        horarioAulaRepository.delete(horario);
    }
}
