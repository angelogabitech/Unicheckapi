package com.unicheck.Unicheckapi.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class OfflineProfessorEditarDTO {
    private UUID clientId;
    private UUID serverId;
    private String nome;
    private String email;
    private String senha;
}
