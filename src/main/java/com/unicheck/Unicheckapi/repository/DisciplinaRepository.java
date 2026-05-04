package com.unicheck.Unicheckapi.repository;

import com.unicheck.Unicheckapi.model.Disciplina;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DisciplinaRepository extends JpaRepository<Disciplina, UUID> {
    List<Disciplina> findByProfessorId(UUID professorId);
    List<Disciplina> findByTurmaId(UUID turmaId);
    List<Disciplina> findByAtivaTrue();
    List<Disciplina> findByTurmaIdAndAtivaTrue(UUID turmaId);
    List<Disciplina> findByTurmaIdAndProfessorIdAndAtivaTrue(UUID turmaId, UUID professorId);
    List<Disciplina> findByProfessorIdAndAtivaTrue(UUID professorId);
    boolean existsByProfessorIdAndTurmaId(UUID professorId, UUID turmaId);
    boolean existsByProfessorIdAndTurmaIdAndIdNot(UUID professorId, UUID turmaId, UUID id);
}
