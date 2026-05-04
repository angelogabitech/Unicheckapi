package com.unicheck.Unicheckapi.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
<<<<<<< HEAD
@Table(
        name = "disciplinas",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_disciplina_professor_turma",
                columnNames = {"professor_id", "turma_id"}
        )
)
=======
@Table(name = "disciplinas")
>>>>>>> 0fe0c1eff8687d7baa9153ab44cce2e9923c8612
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Disciplina {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nome;

<<<<<<< HEAD
    @ManyToOne
    @JoinColumn(name = "turma_id")
    private Turma turma;
=======
    @Column(nullable = false, unique = true)
    private String codigo;
>>>>>>> 0fe0c1eff8687d7baa9153ab44cce2e9923c8612

    @ManyToOne
    @JoinColumn(name = "professor_id")
    private Professor professor;

    private LocalDateTime criado;

<<<<<<< HEAD
    @Column
    private String codigo;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean ativa = true;

=======
>>>>>>> 0fe0c1eff8687d7baa9153ab44cce2e9923c8612
    private LocalDateTime atualizado;

    @PrePersist
    public void prePersist() {
        criado = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        atualizado = LocalDateTime.now();
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> 0fe0c1eff8687d7baa9153ab44cce2e9923c8612
