package com.unicheck.Unicheckapi.model;

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
