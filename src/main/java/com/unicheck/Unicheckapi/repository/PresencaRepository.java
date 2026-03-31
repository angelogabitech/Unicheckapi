package com.unicheck.Unicheckapi.repository;

import com.unicheck.Unicheckapi.model.Presenca;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PresencaRepository extends JpaRepository<Presenca, UUID> {
   //Verificação se já existe alguma aluno com o mesmo id pra evitar presença duplicada
    boolean existsByAlunoIdAndDisciplinaId(UUID alunoId, UUID disciplinaId);
    List<Presenca> findByAlunoId(UUID alunoId);
    List<Presenca> findByDisciplinaId(UUID disciplinaId);

}
