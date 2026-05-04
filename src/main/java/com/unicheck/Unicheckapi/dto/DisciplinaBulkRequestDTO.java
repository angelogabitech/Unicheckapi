package com.unicheck.Unicheckapi.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class DisciplinaBulkRequestDTO {

    private String nome;
    private String codigo;
    private UUID professorId;
    private List<UUID> turmaIds;
}
