package com.unicheck.Unicheckapi.dto;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class SincronizacaoPresencaDTO {
    private UUID clientId;
    private UUID alunoId;
    private UUID aulaId;
    private OffsetDateTime dataHoraLocal; // horario em que foi registrado offline
}

