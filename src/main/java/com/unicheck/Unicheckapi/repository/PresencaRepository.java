package com.unicheck.Unicheckapi.repository;

import com.unicheck.Unicheckapi.model.Presenca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PresencaRepository extends JpaRepository<Presenca, UUID> {
   //Verificação se já existe alguma aluno com o mesmo id pra evitar presença duplicada
    boolean existsByAlunoIdAndDisciplinaId(UUID alunoId, UUID disciplinaId);
    List<Presenca> findByAlunoId(UUID alunoId);
    List<Presenca> findByDisciplinaId(UUID disciplinaId);
    @Query("SELECT p FROM Presenca p WHERE p.aula.disciplina.id = :disciplinaId")
    List<Presenca> findByAulaDisciplinaId(@Param("disciplinaId") UUID disciplinaId);
    @Query("SELECT COUNT(p) FROM Presenca p WHERE p.aula.disciplina.id = :disciplinaId")
    long countByAulasDisciplinaId(@Param("disciplinaId") UUID disciplinaId);
    boolean existsByAlunoIdAndAulaId(UUID alunoId, UUID aulaId);

}
