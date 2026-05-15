package com.unicheck.Unicheckapi.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class OfflineAlunoDeletarDTO {
    private UUID clientId;
    private UUID serverId;
}
