package com.unicheck.Unicheckapi.model;



import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "alunos")
@EqualsAndHashCode(callSuper = true)
public class Aluno extends Usuario {
    private String matricula;
}
