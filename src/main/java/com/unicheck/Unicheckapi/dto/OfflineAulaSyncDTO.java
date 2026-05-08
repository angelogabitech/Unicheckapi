package com.unicheck.Unicheckapi.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class OfflineAulaSyncDTO {
    private UUID clientId;
    private UUID disciplinaId;
    private String titulo;
    private LocalDateTime dataHoraLocal;
}
