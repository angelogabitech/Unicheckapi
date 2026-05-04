package com.unicheck.Unicheckapi.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SincronizacaoPresencaDTO {
    private UUID alunoId;
    private UUID aulaId;
    private LocalDateTime dataHoraLocal; // horário em que foi registrado offline
}