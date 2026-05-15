package com.unicheck.Unicheckapi.repository;

import com.unicheck.Unicheckapi.model.Turma;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TurmaRepository extends JpaRepository<Turma, UUID> {
    Optional<Turma> findByClientId(UUID clientId);
}
