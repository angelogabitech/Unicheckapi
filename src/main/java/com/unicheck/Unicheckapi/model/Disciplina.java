package com.unicheck.Unicheckapi.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "disciplinas",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_disciplina_professor_turma",
                columnNames = {"professor_id", "turma_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Disciplina {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "client_id", unique = true)
    private UUID clientId;

    @Column(nullable = false)
    private String nome;

    @ManyToOne
    @JoinColumn(name = "turma_id")
    private Turma turma;

    @ManyToOne
    @JoinColumn(name = "professor_id")
    private Professor professor;

    private LocalDateTime criado;

    @Column
    private String codigo;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean ativa = true;

    private LocalDateTime atualizado;

    @PrePersist
    public void prePersist() {
        criado = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        atualizado = LocalDateTime.now();
    }
}
