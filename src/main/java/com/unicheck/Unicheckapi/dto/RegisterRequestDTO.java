package com.unicheck.Unicheckapi.dto;
import lombok.Data;

@Data
public class RegisterRequestDTO {

    private String nome;
    private String email;
    private String senha;
}