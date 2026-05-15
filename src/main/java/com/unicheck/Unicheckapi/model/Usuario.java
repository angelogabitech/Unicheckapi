package com.unicheck.Unicheckapi.model;

import jakarta.persistence.*;
import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

@JsonIgnoreProperties(ignoreUnknown = true)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_usuario")
public class Usuario {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "client_id", unique = true)
    private UUID clientId;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(columnDefinition = "TEXT")
    private String fotoUrl;

    @Builder.Default
    private boolean ativo = true;         // â† corrigido

    private LocalDateTime criado;
    private LocalDateTime atualizado;

    @Builder.Default
    private boolean sincronizado = false; // â† corrigido

    @PrePersist
    public void prePersist() {
        criado = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        atualizado = LocalDateTime.now();
    }
}
