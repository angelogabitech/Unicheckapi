package com.unicheck.Unicheckapi.model;

<<<<<<< HEAD
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "aulas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Aula {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private Disciplina disciplina;


    @Column(nullable = false)
    private String titulo;

    private String qrToken;

    private LocalDateTime dataHora;

    private boolean ativa;
}
=======
public class Aula {
}
>>>>>>> 0fe0c1eff8687d7baa9153ab44cce2e9923c8612
