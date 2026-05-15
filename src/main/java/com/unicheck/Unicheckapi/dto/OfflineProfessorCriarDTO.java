package com.unicheck.Unicheckapi.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class OfflineProfessorCriarDTO {
    private UUID clientId;
    private String nome;
    private String email;
    private String senha;
}
