package com.unicheck.Unicheckapi.dto;


import lombok.Data;

import java.time.LocalDateTime;
//Resposta Presença
@Data
public class PresencaResponseDTO {
    private String alunoNome;
    private String disciplinaNome;
    private LocalDateTime dataHora;
}
