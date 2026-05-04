package com.unicheck.Unicheckapi.repository;

import com.unicheck.Unicheckapi.model.Aula;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AulaRepository extends JpaRepository<Aula, UUID> {
    List<Aula> findByDisciplinaId(UUID disciplinaId);
    List<Aula> findByDisciplinaIdAndAtivaTrue(UUID disciplinaId);
    long countByDisciplinaId(UUID disciplinaId);
}