
package com.unicheck.Unicheckapi.repository;

import com.unicheck.Unicheckapi.model.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AlunoRepository extends JpaRepository<Aluno, UUID> {
}