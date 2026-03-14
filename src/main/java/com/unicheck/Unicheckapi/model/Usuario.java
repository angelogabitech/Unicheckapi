package com.unicheck.Unicheckapi.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String fotoUrl;

    private boolean ativo = true;

    private LocalDateTime criado;
    private LocalDateTime atualizado;

    private boolean sincronizado = false;

    @PrePersist
    public void prePersist() {
        criado = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        atualizado = LocalDateTime.now();
    }
}
