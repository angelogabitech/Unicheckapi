package com.unicheck.Unicheckapi.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "presencas",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_presencas_client_id", columnNames = "client_id"),
                @UniqueConstraint(name = "uk_presencas_aluno_aula", columnNames = {"aluno_id", "aula_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Presenca {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "client_id")
    private UUID clientId;

    @ManyToOne
    private Aluno aluno;

    @ManyToOne
    @JoinColumn(name = "aula_id", nullable = false)
    private Aula aula;

    @ManyToOne
    private Disciplina disciplina;

    private LocalDateTime dataHora;

    @PrePersist
    public void prePersist() {
        if (dataHora == null) {
            dataHora = LocalDateTime.now();
        }
    }
}

