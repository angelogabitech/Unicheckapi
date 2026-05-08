package com.unicheck.Unicheckapi.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class DashboardDisciplinaDTO {
    private UUID disciplinaId;
    private UUID turmaId;
    private UUID professorId;
    private String nomeDisciplina;
    private String nomeTurma;
    private String nomeProfessor;
    private long totalAlunos;
    private long totalPresencas;
    private long totalFaltas;
    private double percentualPresenca;
}

