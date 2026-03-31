package com.unicheck.Unicheckapi.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
//Requisição disciplina
@Data
public class DisciplinaRequestDTO {

    private String nome;
    private String codigo;
    private UUID professorId;

}
