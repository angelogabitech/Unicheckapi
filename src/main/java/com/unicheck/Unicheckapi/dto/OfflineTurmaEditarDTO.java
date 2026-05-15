package com.unicheck.Unicheckapi.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class OfflineTurmaEditarDTO {
    private UUID clientId;
    private UUID serverId;
    private String identificacao;
    private String curso;
    private String periodo;
}
