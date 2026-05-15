package com.unicheck.Unicheckapi.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class OfflineDisciplinaEditarDTO {
    private UUID clientId;
    private UUID serverId;
    private String nome;
    private String codigo;
    private UUID turmaServerId;
    private UUID turmaClientId;
    private UUID professorServerId;
    private UUID professorClientId;
}
