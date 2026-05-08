package com.unicheck.Unicheckapi.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "aulas",
        uniqueConstraints = @UniqueConstraint(name = "uk_aulas_client_id", columnNames = "client_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Aula {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "client_id")
    private UUID clientId;

    @ManyToOne
    private Disciplina disciplina;


    @Column(nullable = false)
    private String titulo;

    private String qrToken;

    private LocalDateTime dataHora;

    private boolean ativa;
}

