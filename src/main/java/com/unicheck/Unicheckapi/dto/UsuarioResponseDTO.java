
package com.unicheck.Unicheckapi.dto;

import com.unicheck.Unicheckapi.model.Role;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;
<<<<<<< HEAD
//Resposta Usuario
=======

>>>>>>> 0fe0c1eff8687d7baa9153ab44cce2e9923c8612
@Data
@Builder
public class UsuarioResponseDTO {

    private UUID id;
    private String nome;
    private String email;
    private Role role;
}