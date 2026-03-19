package com.unicheck.Unicheckapi.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@DiscriminatorValue("professor")
@EqualsAndHashCode(callSuper = true)
public class Professor extends Usuario {

    private String departamento;

}