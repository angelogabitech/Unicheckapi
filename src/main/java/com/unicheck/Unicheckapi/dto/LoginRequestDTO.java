package com.unicheck.Unicheckapi.dto;

import lombok.Data;
//Requisição Login
@Data
public class LoginRequestDTO {
    private String email;
    private String senha;
}
