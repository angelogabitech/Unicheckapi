package com.unicheck.Unicheckapi.model;



import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@DiscriminatorValue("aluno")
@EqualsAndHashCode(callSuper = true)
public class Aluno extends Usuario {

    private String matricula;

    @ManyToOne
    @JoinColumn(name = "turma_id")
    private Turma turma;
}
