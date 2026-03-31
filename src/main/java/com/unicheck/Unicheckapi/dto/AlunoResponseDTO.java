package com.unicheck.Unicheckapi.dto;

import lombok.Builder;
import lombok.Data;
//Resposta da Requisição aluno
@Data
@Builder
public class AlunoResponseDTO {

    private String nome;
    private String matricula;
    private String fotoUrl;
    private String disciplina;
}