
package com.unicheck.Unicheckapi.dto;

import com.unicheck.Unicheckapi.model.Role;
import lombok.Data;

@Data
public class UsuarioRequestDTO {

    private String nome;
    private String email;
    private String senha;
    private Role role;
}