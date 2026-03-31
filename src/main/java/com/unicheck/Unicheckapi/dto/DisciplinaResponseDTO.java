package com.unicheck.Unicheckapi.dto;

//Resposta da Requisição de disciplina
public record DisciplinaResponseDTO(
        Long id,
        String nome,
        String codigo,
        String professorNome
){}
