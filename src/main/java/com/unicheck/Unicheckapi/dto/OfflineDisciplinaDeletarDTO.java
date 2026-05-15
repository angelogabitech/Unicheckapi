package com.unicheck.Unicheckapi.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class OfflineDisciplinaDeletarDTO {
    private UUID clientId;
    private UUID serverId;
}
