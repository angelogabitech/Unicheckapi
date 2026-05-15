package com.unicheck.Unicheckapi.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class OfflineAlunoEditarDTO {
    private UUID clientId;
    private UUID serverId;
    private String nome;
    private String matricula;
    private String email;
    private String senha;
    private UUID turmaServerId;
    private UUID turmaClientId;
}
