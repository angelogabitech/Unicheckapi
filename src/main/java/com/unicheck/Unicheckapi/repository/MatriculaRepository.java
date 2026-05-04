
package com.unicheck.Unicheckapi.repository;

import com.unicheck.Unicheckapi.model.Matricula;
import org.springframework.data.jpa.repository.JpaRepository;

<<<<<<< HEAD
import java.util.List;
=======
>>>>>>> 0fe0c1eff8687d7baa9153ab44cce2e9923c8612
import java.util.UUID;

public interface MatriculaRepository
        extends JpaRepository<Matricula, UUID> {
<<<<<<< HEAD
    List<Matricula> findByAlunoId(UUID alunoId);
}
=======
}
>>>>>>> 0fe0c1eff8687d7baa9153ab44cce2e9923c8612
