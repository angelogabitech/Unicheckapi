package com.unicheck.Unicheckapi.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@DiscriminatorValue("aluno")
@EqualsAndHashCode(callSuper = true)
public class Aluno extends Usuario {

    private String matricula;
}