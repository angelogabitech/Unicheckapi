package com.unicheck.Unicheckapi.model;

<<<<<<< HEAD
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "turmas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Turma {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @Column(nullable = false)
    private String periodo;


    @Column(nullable = false)
    private String curso;

//turma
    @Column(nullable = false)
    private String identificacao;
}
=======
public class Turma {
}
>>>>>>> 0fe0c1eff8687d7baa9153ab44cce2e9923c8612
