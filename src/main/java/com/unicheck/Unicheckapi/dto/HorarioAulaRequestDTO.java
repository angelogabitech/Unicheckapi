package com.unicheck.Unicheckapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record HorarioAulaRequestDTO(

        @NotNull(message = "O ID da disciplina é obrigatório")
        UUID disciplinaId,

        @NotBlank(message = "O dia da semana é obrigatório")
        String diaSemana,

        @NotBlank(message = "A hora de início é obrigatória")
        String horaInicio,

        @NotBlank(message = "A hora de fim é obrigatória")
        String horaFim
) {}