package com.unicheck.Unicheckapi.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class OfflineAlunoCriarDTO {
    private UUID clientId;
    private String nome;
    private String matricula;
    private String email;
    private String senha;
    private UUID turmaServerId;
    private UUID turmaClientId;
}
