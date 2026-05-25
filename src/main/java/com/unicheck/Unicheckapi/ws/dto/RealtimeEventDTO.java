package com.unicheck.Unicheckapi.ws.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RealtimeEventDTO {
    private String tipo;
    private String entidade;
    private UUID id;
    private LocalDateTime timestamp;
}
