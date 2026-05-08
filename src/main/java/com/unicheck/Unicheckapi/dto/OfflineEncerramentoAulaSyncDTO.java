package com.unicheck.Unicheckapi.dto;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class OfflineEncerramentoAulaSyncDTO {
    private UUID clientId;
    private UUID aulaId;
    private UUID aulaClientId;
    private OffsetDateTime dataHoraLocal;
}

