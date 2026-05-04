package com.unicheck.Unicheckapi.model;

import jakarta.persistence.*;
import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
<<<<<<< HEAD
=======
import java.util.List;
>>>>>>> 0fe0c1eff8687d7baa9153ab44cce2e9923c8612
import java.util.UUID;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
<<<<<<< HEAD

=======
>>>>>>> 0fe0c1eff8687d7baa9153ab44cce2e9923c8612
@JsonIgnoreProperties(ignoreUnknown = true)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_usuario")
public class Usuario {
<<<<<<< HEAD

=======
>>>>>>> 0fe0c1eff8687d7baa9153ab44cce2e9923c8612
    @Id
    @GeneratedValue
    private UUID id;

<<<<<<< HEAD

=======
>>>>>>> 0fe0c1eff8687d7baa9153ab44cce2e9923c8612
    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Enumerated(EnumType.STRING)
    private Role role;

<<<<<<< HEAD
    @Column(columnDefinition = "TEXT")
    private String fotoUrl;

    @Builder.Default
    private boolean ativo = true;         // ← corrigido
=======
    private String fotoUrl;

    private boolean ativo = true;
>>>>>>> 0fe0c1eff8687d7baa9153ab44cce2e9923c8612

    private LocalDateTime criado;
    private LocalDateTime atualizado;

<<<<<<< HEAD
    @Builder.Default
    private boolean sincronizado = false; // ← corrigido
=======
    private boolean sincronizado = false;
>>>>>>> 0fe0c1eff8687d7baa9153ab44cce2e9923c8612

    @PrePersist
    public void prePersist() {
        criado = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        atualizado = LocalDateTime.now();
    }
<<<<<<< HEAD
=======

    @ManyToMany
    @JoinTable(
            name = "usuario_disciplinas",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "disciplina_id")
    )
    private List<Disciplina> disciplinas;
>>>>>>> 0fe0c1eff8687d7baa9153ab44cce2e9923c8612
}
