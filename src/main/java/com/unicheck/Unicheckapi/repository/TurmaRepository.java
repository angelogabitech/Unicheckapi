package com.unicheck.Unicheckapi.repository;

import com.unicheck.Unicheckapi.model.Turma;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TurmaRepository extends JpaRepository<Turma, UUID> {
}