package com.unicheck.Unicheckapi.model;

<<<<<<< HEAD


import jakarta.persistence.*;
=======
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
>>>>>>> 0fe0c1eff8687d7baa9153ab44cce2e9923c8612
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@DiscriminatorValue("aluno")
@EqualsAndHashCode(callSuper = true)
public class Aluno extends Usuario {

    private String matricula;
<<<<<<< HEAD

    @ManyToOne
    @JoinColumn(name = "turma_id")
    private Turma turma;
=======
>>>>>>> 0fe0c1eff8687d7baa9153ab44cce2e9923c8612
}