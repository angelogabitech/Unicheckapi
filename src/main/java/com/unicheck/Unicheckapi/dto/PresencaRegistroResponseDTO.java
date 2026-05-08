package com.unicheck.Unicheckapi.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PresencaRegistroResponseDTO {
    private UUID presencaId;
    private UUID alunoId;
    private String nome;
    private String matricula;
    private String fotoUrl;
    private LocalDateTime dataHora;
}

