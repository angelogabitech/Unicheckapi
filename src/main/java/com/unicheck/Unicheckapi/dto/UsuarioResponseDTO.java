
package com.unicheck.Unicheckapi.dto;

import com.unicheck.Unicheckapi.model.Role;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;
//Resposta Usuario
@Data
@Builder
public class UsuarioResponseDTO {

    private UUID id;
    private String nome;
    private String email;
    private Role role;
}
