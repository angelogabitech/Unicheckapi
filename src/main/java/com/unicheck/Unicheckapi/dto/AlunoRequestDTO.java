package com.unicheck.Unicheckapi.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class AlunoRequestDTO {
    private String nome;
    private String matricula;
    private String email;
    private String senha;
    private UUID turmaId;
}