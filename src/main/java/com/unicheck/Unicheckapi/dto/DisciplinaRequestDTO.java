package com.unicheck.Unicheckapi.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
//RequisiÃ§Ã£o disciplina
@Data
public class DisciplinaRequestDTO {

    private String nome;
    private String codigo;
    private UUID turmaId;
    private UUID professorId;

}
