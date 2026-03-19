package com.unicheck.Unicheckapi.repository;

import com.unicheck.Unicheckapi.model.Disciplina;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DisciplinaRepository extends JpaRepository<Disciplina, UUID> {
}