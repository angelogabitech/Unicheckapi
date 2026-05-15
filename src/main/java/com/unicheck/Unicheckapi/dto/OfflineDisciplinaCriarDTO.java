package com.unicheck.Unicheckapi.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class OfflineDisciplinaCriarDTO {
    private UUID clientId;
    private String nome;
    private String codigo;
    private UUID turmaServerId;
    private UUID turmaClientId;
    private UUID professorServerId;
    private UUID professorClientId;
}
