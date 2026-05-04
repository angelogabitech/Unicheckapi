package com.unicheck.Unicheckapi.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardDisciplinaDTO {
    private String nomeDisciplina;
    private String nomeTurma;
    private long totalAlunos;
    private long totalPresencas;
    private long totalFaltas;
    private double percentualPresenca;
}
