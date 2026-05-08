package com.unicheck.Unicheckapi.dto;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class OfflineAulaSyncDTO {
    private UUID clientId;
    private UUID disciplinaId;
    private String titulo;
    private OffsetDateTime dataHoraLocal;
}

