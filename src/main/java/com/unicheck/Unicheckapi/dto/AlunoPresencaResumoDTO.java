package com.unicheck.Unicheckapi.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AlunoPresencaResumoDTO {
    private UUID alunoId;
    private String nome;
    private String matricula;
    private String email;
    private String fotoUrl;
    private UUID turmaId;
    private String nomeTurma;
    private UUID disciplinaId;
    private long totalAulas;
    private long presencas;
    private long faltas;
    private double percentual;
}

