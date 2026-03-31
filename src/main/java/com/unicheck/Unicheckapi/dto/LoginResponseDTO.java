package com.unicheck.Unicheckapi.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

//Resposta do Login
@Data
@AllArgsConstructor
public class LoginResponseDTO {
    private String token;
}