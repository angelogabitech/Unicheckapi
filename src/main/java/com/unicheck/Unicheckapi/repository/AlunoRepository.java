
package com.unicheck.Unicheckapi.repository;

import com.unicheck.Unicheckapi.model.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlunoRepository extends JpaRepository<Aluno, UUID> {
    long countByTurmaId(UUID turmaId);
    List<Aluno> findByTurmaId(UUID turmaId);
    Optional<Aluno> findByClientId(UUID clientId);
}
