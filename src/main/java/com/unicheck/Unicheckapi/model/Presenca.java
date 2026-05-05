package com.unicheck.Unicheckapi.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "presencas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Presenca {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "client_id", unique = true)
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