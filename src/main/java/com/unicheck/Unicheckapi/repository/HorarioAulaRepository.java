package com.unicheck.Unicheckapi.repository;

import com.unicheck.Unicheckapi.model.HorarioAula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HorarioAulaRepository extends JpaRepository<HorarioAula, UUID> {

    List<HorarioAula> findByDisciplinaId(UUID disciplinaId);
}