
package com.unicheck.Unicheckapi.repository;

import com.unicheck.Unicheckapi.model.Matricula;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MatriculaRepository
        extends JpaRepository<Matricula, UUID> {
}