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

<<<<<<< HEAD

=======
    private String departamento;
>>>>>>> 0fe0c1eff8687d7baa9153ab44cce2e9923c8612

}