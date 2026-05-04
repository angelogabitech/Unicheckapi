package com.unicheck.Unicheckapi.repository;

import com.unicheck.Unicheckapi.model.Disciplina;
import org.springframework.data.jpa.repository.JpaRepository;

<<<<<<< HEAD
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
=======
import java.util.UUID;

public interface DisciplinaRepository extends JpaRepository<Disciplina, UUID> {
}
>>>>>>> 0fe0c1eff8687d7baa9153ab44cce2e9923c8612
