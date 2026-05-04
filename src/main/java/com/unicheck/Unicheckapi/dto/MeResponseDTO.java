package com.unicheck.Unicheckapi.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class MeResponseDTO {

    private UUID id;
    private String nome;
    private String email;
    private String role;
    private String fotoUrl;
}
