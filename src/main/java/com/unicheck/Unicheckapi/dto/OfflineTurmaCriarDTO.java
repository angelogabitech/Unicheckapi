package com.unicheck.Unicheckapi.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class OfflineTurmaCriarDTO {
    private UUID clientId;
    private String identificacao;
    private String curso;
    private String periodo;
}
