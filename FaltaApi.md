# FaltaApi

Arquivo reescrito em 2026-05-08. Mantem somente SQL/verificacoes de banco e o codigo Java final implementado na API Spring Boot. Ajustes do front nao estao detalhados aqui.

## SQL para adicionar/verificar no banco

```sql
-- Colunas de idempotencia do sync offline
ALTER TABLE aulas ADD COLUMN IF NOT EXISTS client_id UUID;
ALTER TABLE presencas ADD COLUMN IF NOT EXISTS client_id UUID;

-- Fotos/base64 precisam suportar textos longos
ALTER TABLE usuarios ALTER COLUMN foto_url TYPE TEXT;

-- Remover duplicidades antes de criar a regra unica por aluno/aula
DELETE FROM presencas p
USING presencas d
WHERE p.id > d.id
  AND p.aluno_id = d.aluno_id
  AND p.aula_id = d.aula_id;

-- Idempotencia e integridade
CREATE UNIQUE INDEX IF NOT EXISTS uk_aulas_client_id
ON aulas(client_id)
WHERE client_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_presencas_client_id
ON presencas(client_id)
WHERE client_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_presencas_aluno_aula
ON presencas(aluno_id, aula_id);

-- Evitar matricula duplicada entre alunos
CREATE UNIQUE INDEX IF NOT EXISTS uk_usuarios_aluno_matricula
ON usuarios(matricula)
WHERE tipo_usuario = 'aluno' AND matricula IS NOT NULL;
```

## Configuracao obrigatoria

Adicionar/manter em `src/main/resources/application.properties`:

```properties
app.jwt.secret=${JWT_SECRET:unicheck-local-dev-secret-32-bytes-minimum-2026}
```

## Codigo Java implementado

### src/main/java/com/unicheck/Unicheckapi/dto/DashboardDisciplinaDTO.java

```java
package com.unicheck.Unicheckapi.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class DashboardDisciplinaDTO {
    private UUID disciplinaId;
    private UUID turmaId;
    private UUID professorId;
    private String nomeDisciplina;
    private String nomeTurma;
    private String nomeProfessor;
    private long totalAlunos;
    private long totalPresencas;
    private long totalFaltas;
    private double percentualPresenca;
}

```

### src/main/java/com/unicheck/Unicheckapi/dto/SincronizacaoPresencaDTO.java

```java
package com.unicheck.Unicheckapi.dto;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class SincronizacaoPresencaDTO {
    private UUID clientId;
    private UUID alunoId;
    private UUID aulaId;
    private OffsetDateTime dataHoraLocal; // horario em que foi registrado offline
}

```

### src/main/java/com/unicheck/Unicheckapi/dto/OfflineAulaSyncDTO.java

```java
package com.unicheck.Unicheckapi.dto;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class OfflineAulaSyncDTO {
    private UUID clientId;
    private UUID disciplinaId;
    private String titulo;
    private OffsetDateTime dataHoraLocal;
}

```

### src/main/java/com/unicheck/Unicheckapi/dto/OfflinePresencaSyncDTO.java

```java
package com.unicheck.Unicheckapi.dto;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class OfflinePresencaSyncDTO {
    private UUID clientId;
    private UUID alunoId;
    private UUID aulaId;
    private UUID aulaClientId;
    private OffsetDateTime dataHoraLocal;
}

```

### src/main/java/com/unicheck/Unicheckapi/dto/OfflineEncerramentoAulaSyncDTO.java

```java
package com.unicheck.Unicheckapi.dto;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class OfflineEncerramentoAulaSyncDTO {
    private UUID clientId;
    private UUID aulaId;
    private UUID aulaClientId;
    private OffsetDateTime dataHoraLocal;
}

```

### src/main/java/com/unicheck/Unicheckapi/dto/PresencaRegistroResponseDTO.java

```java
package com.unicheck.Unicheckapi.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PresencaRegistroResponseDTO {
    private UUID presencaId;
    private UUID alunoId;
    private String nome;
    private String matricula;
    private String fotoUrl;
    private LocalDateTime dataHora;
}

```

### src/main/java/com/unicheck/Unicheckapi/dto/AlunoPresencaResumoDTO.java

```java
package com.unicheck.Unicheckapi.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AlunoPresencaResumoDTO {
    private UUID alunoId;
    private String nome;
    private String matricula;
    private String email;
    private String fotoUrl;
    private UUID turmaId;
    private String nomeTurma;
    private UUID disciplinaId;
    private long totalAulas;
    private long presencas;
    private long faltas;
    private double percentual;
}

```

### src/main/java/com/unicheck/Unicheckapi/dto/OfflineBootstrapDTO.java

```java
package com.unicheck.Unicheckapi.dto;

import com.unicheck.Unicheckapi.model.Aluno;
import com.unicheck.Unicheckapi.model.Aula;
import com.unicheck.Unicheckapi.model.Disciplina;
import com.unicheck.Unicheckapi.model.HorarioAula;
import com.unicheck.Unicheckapi.model.Presenca;
import com.unicheck.Unicheckapi.model.Professor;
import com.unicheck.Unicheckapi.model.Turma;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OfflineBootstrapDTO {
    private List<Turma> turmas;
    private List<Professor> professores;
    private List<Aluno> alunos;
    private List<Disciplina> disciplinas;
    private List<Aula> aulas;
    private List<HorarioAula> horarios;
    private List<Presenca> presencas;
    private String qrCodeBase64;
}

```

### src/main/java/com/unicheck/Unicheckapi/Controller/AlunoController.java

```java
package com.unicheck.Unicheckapi.Controller;

import com.unicheck.Unicheckapi.dto.AlunoRequestDTO;
import com.unicheck.Unicheckapi.dto.AtualizarPerfilDTO;
import com.unicheck.Unicheckapi.model.Aluno;
import com.unicheck.Unicheckapi.service.AlunoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/alunos")
@RequiredArgsConstructor
public class AlunoController {

    private final AlunoService alunoService;

    @PostMapping
    public ResponseEntity<Aluno> criar(@RequestBody AlunoRequestDTO dto) {
        return ResponseEntity.ok(alunoService.criar(dto));
    }

    @GetMapping
    public List<Aluno> listar(){
        return alunoService.listar();
    }

    @GetMapping("/turma/{turmaId}")
    public List<Aluno> listarPorTurma(@PathVariable UUID turmaId) {
        return alunoService.listarPorTurmaPermitida(turmaId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Aluno> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(alunoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Aluno> atualizar(@PathVariable UUID id, @RequestBody AlunoRequestDTO dto) {
        return ResponseEntity.ok(alunoService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        alunoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{id}/perfil")
    public ResponseEntity<Aluno> atualizarPerfil(
            @PathVariable UUID id,
            @RequestBody AtualizarPerfilDTO dto) {
        return ResponseEntity.ok(alunoService.atualizarPerfil(id, dto));
    }

    @PutMapping("/{id}/foto")
    public ResponseEntity<Void> uploadFoto(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        alunoService.salvarFoto(id, body.get("fotoBase64"));
        return ResponseEntity.ok().build();
    }

}

```

### src/main/java/com/unicheck/Unicheckapi/Controller/PresencaController.java

```java
package com.unicheck.Unicheckapi.Controller;

import com.unicheck.Unicheckapi.dto.AlunoPresencaResumoDTO;
import com.unicheck.Unicheckapi.dto.DashboardDisciplinaDTO;
import com.unicheck.Unicheckapi.dto.PresencaRegistroResponseDTO;
import com.unicheck.Unicheckapi.dto.SincronizacaoPresencaDTO;
import com.unicheck.Unicheckapi.model.Presenca;
import com.unicheck.Unicheckapi.service.PresencaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/presencas")
@RequiredArgsConstructor
public class PresencaController {

    private final PresencaService presencaService;

    @PostMapping("/registrar")
    public ResponseEntity<PresencaRegistroResponseDTO> registrar(@RequestBody Map<String, String> body) {
        String qrCode = body.get("qrCode");
        UUID aulaId = UUID.fromString(body.get("aulaId"));
        return ResponseEntity.ok(presencaService.registrarPresenca(qrCode, aulaId));
    }
    @GetMapping("/aluno/{id}")
    public List<Presenca> buscarPorAluno(@PathVariable UUID id){
        return presencaService.buscarPorAluno(id);
    }
    @GetMapping("/disciplina/{id}")
    public List<Presenca> porDisciplina(@PathVariable UUID id){
        return presencaService.buscarPorDisciplina(id);
    }
    @GetMapping("/disciplina/{id}/alunos")
    public List<AlunoPresencaResumoDTO> resumoAlunosPorDisciplina(@PathVariable UUID id) {
        return presencaService.resumoAlunosPorDisciplina(id);
    }

    @GetMapping("/aula/{id}")
    public List<Presenca> porAula(@PathVariable UUID id) {
        return presencaService.buscarPorAula(id);
    }

    @GetMapping
    public List<Presenca> listar(){
        return presencaService.listar();

    }
    // Dashboard do Gestor — todas as disciplinas
    @GetMapping("/dashboard")
    public ResponseEntity<List<DashboardDisciplinaDTO>> dashboard() {
        return ResponseEntity.ok(presencaService.gerarDashboard());
    }

    // Dashboard do Professor — apenas disciplinas dele
    @GetMapping("/dashboard/professor/{professorId}")
    public ResponseEntity<List<DashboardDisciplinaDTO>> dashboardProfessor(
            @PathVariable UUID professorId) {
        return ResponseEntity.ok(presencaService.gerarDashboardProfessor(professorId));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        presencaService.deletarPresenca(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sincronizar")
    public ResponseEntity<String> sincronizar(
            @RequestBody List<SincronizacaoPresencaDTO> lista) {
        presencaService.sincronizar(lista);
        return ResponseEntity.ok("Sincronizado com sucesso");
    }
}

```

### src/main/java/com/unicheck/Unicheckapi/Controller/HorarioAulaController.java

```java
package com.unicheck.Unicheckapi.controller;

import com.unicheck.Unicheckapi.dto.HorarioAulaRequestDTO;
import com.unicheck.Unicheckapi.model.HorarioAula;
import com.unicheck.Unicheckapi.service.HorarioAulaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/horarios")
@RequiredArgsConstructor
public class HorarioAulaController {

    private final HorarioAulaService horarioAulaService;


    @PostMapping
    public ResponseEntity<HorarioAula> criar(@Valid @RequestBody HorarioAulaRequestDTO dto) {
        HorarioAula criado = horarioAulaService.criar(dto);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.getId())
                .toUri();
        return ResponseEntity.created(location).body(criado);
    }


    @GetMapping("/disciplina/{disciplinaId}")
    public ResponseEntity<List<HorarioAula>> listarPorDisciplina(@PathVariable UUID disciplinaId) {
        return ResponseEntity.ok(horarioAulaService.listarPorDisciplina(disciplinaId));
    }

    @GetMapping("/turma/{turmaId}")
    public ResponseEntity<List<HorarioAula>> listarPorTurma(@PathVariable UUID turmaId) {
        return ResponseEntity.ok(horarioAulaService.listarPorTurma(turmaId));
    }


    @PutMapping("/{id}")
    public ResponseEntity<HorarioAula> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody HorarioAulaRequestDTO dto) {
        return ResponseEntity.ok(horarioAulaService.atualizar(id, dto));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        horarioAulaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}

```

### src/main/java/com/unicheck/Unicheckapi/Controller/OfflineBootstrapController.java

```java
package com.unicheck.Unicheckapi.Controller;

import com.unicheck.Unicheckapi.dto.OfflineBootstrapDTO;
import com.unicheck.Unicheckapi.service.OfflineBootstrapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/offline")
@RequiredArgsConstructor
public class OfflineBootstrapController {

    private final OfflineBootstrapService offlineBootstrapService;

    @GetMapping("/bootstrap")
    public ResponseEntity<OfflineBootstrapDTO> bootstrap() {
        return ResponseEntity.ok(offlineBootstrapService.carregar());
    }
}

```

### src/main/java/com/unicheck/Unicheckapi/model/Aula.java

```java
package com.unicheck.Unicheckapi.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "aulas",
        uniqueConstraints = @UniqueConstraint(name = "uk_aulas_client_id", columnNames = "client_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Aula {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "client_id")
    private UUID clientId;

    @ManyToOne
    private Disciplina disciplina;


    @Column(nullable = false)
    private String titulo;

    private String qrToken;

    private LocalDateTime dataHora;

    private boolean ativa;
}

```

### src/main/java/com/unicheck/Unicheckapi/model/Presenca.java

```java
package com.unicheck.Unicheckapi.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "presencas",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_presencas_client_id", columnNames = "client_id"),
                @UniqueConstraint(name = "uk_presencas_aluno_aula", columnNames = {"aluno_id", "aula_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Presenca {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "client_id")
    private UUID clientId;

    @ManyToOne
    private Aluno aluno;

    @ManyToOne
    @JoinColumn(name = "aula_id", nullable = false)
    private Aula aula;

    @ManyToOne
    private Disciplina disciplina;

    private LocalDateTime dataHora;

    @PrePersist
    public void prePersist() {
        if (dataHora == null) {
            dataHora = LocalDateTime.now();
        }
    }
}

```

### src/main/java/com/unicheck/Unicheckapi/repository/AulaRepository.java

```java
package com.unicheck.Unicheckapi.repository;

import com.unicheck.Unicheckapi.model.Aula;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AulaRepository extends JpaRepository<Aula, UUID> {
    List<Aula> findByDisciplinaId(UUID disciplinaId);
    List<Aula> findByDisciplinaIdIn(List<UUID> disciplinaIds);
    List<Aula> findByDisciplinaIdAndAtivaTrue(UUID disciplinaId);
    long countByDisciplinaId(UUID disciplinaId);
    Optional<Aula> findByClientId(UUID clientId);
}

```

### src/main/java/com/unicheck/Unicheckapi/repository/HorarioAulaRepository.java

```java
package com.unicheck.Unicheckapi.repository;

import com.unicheck.Unicheckapi.model.HorarioAula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HorarioAulaRepository extends JpaRepository<HorarioAula, UUID> {

    List<HorarioAula> findByDisciplinaId(UUID disciplinaId);
    List<HorarioAula> findByDisciplinaIdIn(List<UUID> disciplinaIds);
}

```

### src/main/java/com/unicheck/Unicheckapi/repository/PresencaRepository.java

```java
package com.unicheck.Unicheckapi.repository;

import com.unicheck.Unicheckapi.model.Presenca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PresencaRepository extends JpaRepository<Presenca, UUID> {
   //Verificação se já existe alguma aluno com o mesmo id pra evitar presença duplicada
    boolean existsByAlunoIdAndDisciplinaId(UUID alunoId, UUID disciplinaId);
    List<Presenca> findByAlunoId(UUID alunoId);
    @Query("SELECT p FROM Presenca p WHERE p.aula.disciplina.id = :disciplinaId")
    List<Presenca> findByAulaDisciplinaId(@Param("disciplinaId") UUID disciplinaId);
    @Query("SELECT p FROM Presenca p WHERE p.aula.disciplina.id IN :disciplinaIds")
    List<Presenca> findByAulaDisciplinaIdIn(@Param("disciplinaIds") List<UUID> disciplinaIds);
    List<Presenca> findByAulaId(UUID aulaId);
    @Query("SELECT COUNT(p) FROM Presenca p WHERE p.aula.disciplina.id = :disciplinaId")
    long countByAulasDisciplinaId(@Param("disciplinaId") UUID disciplinaId);
    @Query("SELECT COUNT(DISTINCT p.aula.id) FROM Presenca p WHERE p.aluno.id = :alunoId AND p.aula.disciplina.id = :disciplinaId")
    long countAulasPresentesPorAlunoDisciplina(@Param("alunoId") UUID alunoId, @Param("disciplinaId") UUID disciplinaId);
    boolean existsByAlunoIdAndAulaId(UUID alunoId, UUID aulaId);
    Optional<Presenca> findByClientId(UUID clientId);
    Optional<Presenca> findByAlunoIdAndAulaId(UUID alunoId, UUID aulaId);

}

```

### src/main/java/com/unicheck/Unicheckapi/service/AlunoService.java

```java
package com.unicheck.Unicheckapi.service;


import com.unicheck.Unicheckapi.dto.AlunoRequestDTO;
import com.unicheck.Unicheckapi.dto.AtualizarPerfilDTO;
import com.unicheck.Unicheckapi.model.Aluno;
import com.unicheck.Unicheckapi.model.Role;
import com.unicheck.Unicheckapi.model.Turma;
import com.unicheck.Unicheckapi.model.Usuario;
import com.unicheck.Unicheckapi.repository.AlunoRepository;
import com.unicheck.Unicheckapi.repository.DisciplinaRepository;
import com.unicheck.Unicheckapi.repository.MatriculaRepository;
import com.unicheck.Unicheckapi.repository.PresencaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlunoService {

    private final AlunoRepository alunoRepository;
    private final PresencaRepository presencaRepository;
    private final MatriculaRepository matriculaRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final DisciplinaService disciplinaService;
    private final TurmaService turmaService;
    private final PasswordEncoder passwordEncoder;


    public Aluno salvar(Aluno aluno) {
        return alunoRepository.save(aluno);
    }

    public List<Aluno> listar() {
        return alunoRepository.findAll();
    }

    public Aluno buscarPorId(UUID id) {
        return alunoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado"));
    }

    public List<Aluno> listarPorTurmaPermitida(UUID turmaId) {
        Usuario usuario = disciplinaService.usuarioAutenticado();

        if (usuario.getRole() == Role.GESTOR) {
            return alunoRepository.findByTurmaId(turmaId);
        }

        if (usuario.getRole() == Role.PROFESSOR
                && disciplinaRepository.existsByProfessorIdAndTurmaIdAndAtivaTrue(usuario.getId(), turmaId)) {
            return alunoRepository.findByTurmaId(turmaId);
        }

        if (usuario.getRole() == Role.ALUNO) {
            Aluno aluno = buscarPorId(usuario.getId());
            if (aluno.getTurma() != null && aluno.getTurma().getId().equals(turmaId)) {
                return alunoRepository.findByTurmaId(turmaId);
            }
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Usuario sem permissao para listar alunos desta turma.");
    }

    public Aluno criar(AlunoRequestDTO dto) {
        Turma turma = turmaService.buscarPorId(dto.getTurmaId());

        Aluno aluno = new Aluno();
        aluno.setNome(dto.getNome());
        aluno.setEmail(dto.getEmail());
        aluno.setSenha(passwordEncoder.encode(dto.getSenha()));
        aluno.setMatricula(dto.getMatricula());
        aluno.setTurma(turma);
        aluno.setRole(Role.ALUNO);
        return alunoRepository.save(aluno);
    }

    public Aluno atualizar(UUID id, AlunoRequestDTO dto) {
        Aluno aluno = buscarPorId(id);
        aluno.setNome(dto.getNome());
        aluno.setEmail(dto.getEmail());
        aluno.setMatricula(dto.getMatricula());
        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            aluno.setSenha(passwordEncoder.encode(dto.getSenha()));
        }
        if (dto.getTurmaId() != null) {
            aluno.setTurma(turmaService.buscarPorId(dto.getTurmaId()));
        }
        return alunoRepository.save(aluno);
    }

    public void deletar(UUID id) {
        Aluno aluno = buscarPorId(id);

        presencaRepository.deleteAll(presencaRepository.findByAlunoId(id));
        matriculaRepository.deleteAll(matriculaRepository.findByAlunoId(id));

        alunoRepository.delete(aluno);
    }

    public Aluno atualizarPerfil(UUID id, AtualizarPerfilDTO dto) {
        Aluno aluno = buscarPorId(id);
        if (dto.getNome() != null) aluno.setNome(dto.getNome());
        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            aluno.setSenha(passwordEncoder.encode(dto.getSenha()));
        }
        if (dto.getFotoUrl() != null) aluno.setFotoUrl(dto.getFotoUrl());
        return alunoRepository.save(aluno);
    }

    public void salvarFoto(UUID id, String fotoBase64) {
        Aluno aluno = buscarPorId(id);
        aluno.setFotoUrl(fotoBase64);
        alunoRepository.save(aluno);
    }
}

```

### src/main/java/com/unicheck/Unicheckapi/service/DisciplinaService.java

```java
package com.unicheck.Unicheckapi.service;

import com.unicheck.Unicheckapi.dto.DisciplinaBulkRequestDTO;
import com.unicheck.Unicheckapi.dto.DisciplinaRequestDTO;
import com.unicheck.Unicheckapi.model.Aluno;
import com.unicheck.Unicheckapi.model.Disciplina;
import com.unicheck.Unicheckapi.model.Professor;
import com.unicheck.Unicheckapi.model.Role;
import com.unicheck.Unicheckapi.model.Turma;
import com.unicheck.Unicheckapi.model.Usuario;
import com.unicheck.Unicheckapi.repository.AlunoRepository;
import com.unicheck.Unicheckapi.repository.DisciplinaRepository;
import com.unicheck.Unicheckapi.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DisciplinaService {

    private final DisciplinaRepository disciplinaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AlunoRepository alunoRepository;
    private final ProfessorService professorService;
    private final TurmaService turmaService;

    public Disciplina criar(DisciplinaRequestDTO dto) {
        Professor professor = professorService.buscarPorId(dto.getProfessorId());
        Turma turma = turmaService.buscarPorId(dto.getTurmaId());

        boolean conflito = disciplinaRepository.existsByProfessorIdAndTurmaIdAndAtivaTrue(dto.getProfessorId(), dto.getTurmaId());
        if (conflito) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Este professor ja possui uma disciplina nesta turma.");
        }

        Disciplina disciplina = Disciplina.builder()
                .nome(dto.getNome())
                .codigo(dto.getCodigo())
                .turma(turma)
                .professor(professor)
                .build();

        return disciplinaRepository.save(disciplina);
    }

    public List<Disciplina> criarEmLote(DisciplinaBulkRequestDTO dto) {
        return dto.getTurmaIds().stream()
                .map(turmaId -> {
                    DisciplinaRequestDTO request = new DisciplinaRequestDTO();
                    request.setNome(dto.getNome());
                    request.setCodigo(dto.getCodigo());
                    request.setProfessorId(dto.getProfessorId());
                    request.setTurmaId(turmaId);
                    return criar(request);
                })
                .toList();
    }

    public Disciplina buscarPorId(UUID id) {
        return disciplinaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Disciplina nao encontrada"));
    }

    public Usuario usuarioAutenticado() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario nao autenticado");
        }

        return usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario nao encontrado"));
    }

    public Disciplina buscarPermitidaParaUsuario(UUID id) {
        Disciplina disciplina = buscarPorId(id);
        Usuario usuario = usuarioAutenticado();

        if (usuario.getRole() == Role.GESTOR) {
            return disciplina;
        }

        if (usuario.getRole() == Role.PROFESSOR
                && disciplina.getProfessor() != null
                && disciplina.getProfessor().getId().equals(usuario.getId())) {
            return disciplina;
        }

        if (usuario.getRole() == Role.ALUNO
                && disciplina.getTurma() != null) {
            Aluno aluno = alunoRepository.findById(usuario.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                            "Aluno nao encontrado para validar permissao."));
            if (aluno.getTurma() != null && aluno.getTurma().getId().equals(disciplina.getTurma().getId())) {
                return disciplina;
            }
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Usuario sem permissao para acessar esta disciplina.");
    }

    public List<Disciplina> listarMinhas() {
        Usuario usuario = usuarioAutenticado();

        if (usuario.getRole() == Role.GESTOR) {
            return listar();
        }

        if (usuario.getRole() == Role.PROFESSOR) {
            return disciplinaRepository.findByProfessorIdAndAtivaTrue(usuario.getId());
        }

        if (usuario.getRole() == Role.ALUNO) {
            Aluno aluno = alunoRepository.findById(usuario.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                            "Aluno nao encontrado para listar disciplinas."));
            if (aluno.getTurma() == null) return List.of();
            return disciplinaRepository.findByTurmaIdAndAtivaTrue(aluno.getTurma().getId());
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario sem permissao para listar disciplinas.");
    }

    public Disciplina atualizar(UUID id, DisciplinaRequestDTO dto) {
        Disciplina disciplina = buscarPorId(id);
        Professor professor = professorService.buscarPorId(dto.getProfessorId());
        Turma turma = turmaService.buscarPorId(dto.getTurmaId());

        boolean conflito = disciplinaRepository.existsByProfessorIdAndTurmaIdAndIdNotAndAtivaTrue(
                dto.getProfessorId(),
                dto.getTurmaId(),
                id
        );

        if (conflito) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Este professor ja possui uma disciplina nesta turma.");
        }

        disciplina.setNome(dto.getNome());
        disciplina.setCodigo(dto.getCodigo());
        disciplina.setTurma(turma);
        disciplina.setProfessor(professor);

        return disciplinaRepository.save(disciplina);
    }

    public void deletar(UUID id) {
        Disciplina disciplina = buscarPorId(id);
        disciplina.setAtiva(false);
        disciplinaRepository.save(disciplina);
    }

    public List<Disciplina> listar(){
        return disciplinaRepository.findByAtivaTrue();
    }

    public List<Disciplina> listarPorTurma(UUID turmaId) {
        Usuario usuario = usuarioAutenticado();

        if (usuario.getRole() == Role.GESTOR) {
            return disciplinaRepository.findByTurmaIdAndAtivaTrue(turmaId);
        }

        if (usuario.getRole() == Role.PROFESSOR
                && disciplinaRepository.existsByProfessorIdAndTurmaIdAndAtivaTrue(usuario.getId(), turmaId)) {
            return disciplinaRepository.findByTurmaIdAndAtivaTrue(turmaId);
        }

        if (usuario.getRole() == Role.ALUNO) {
            Aluno aluno = alunoRepository.findById(usuario.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                            "Aluno nao encontrado para listar disciplinas da turma."));
            if (aluno.getTurma() != null && aluno.getTurma().getId().equals(turmaId)) {
                return disciplinaRepository.findByTurmaIdAndAtivaTrue(turmaId);
            }
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Usuario sem permissao para listar disciplinas desta turma.");
    }

    public List<Disciplina> listarPorTurmaSemFiltro(UUID turmaId) {
        return disciplinaRepository.findByTurmaIdAndAtivaTrue(turmaId);
    }

    public List<Disciplina> listarPorProfessor(UUID professorId) {
        Usuario usuario = usuarioAutenticado();

        if (usuario.getRole() == Role.PROFESSOR && !usuario.getId().equals(professorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Professor nao pode listar disciplinas de outro professor.");
        }

        return disciplinaRepository.findByProfessorIdAndAtivaTrue(professorId);
    }
}

```

### src/main/java/com/unicheck/Unicheckapi/service/HorarioAulaService.java

```java
package com.unicheck.Unicheckapi.service;

import com.unicheck.Unicheckapi.dto.HorarioAulaRequestDTO;
import com.unicheck.Unicheckapi.model.Disciplina;
import com.unicheck.Unicheckapi.model.HorarioAula;
import com.unicheck.Unicheckapi.repository.HorarioAulaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HorarioAulaService {

    private final HorarioAulaRepository horarioAulaRepository;
    private final DisciplinaService disciplinaService;

    public HorarioAula criar(HorarioAulaRequestDTO dto) {
        Disciplina disciplina = disciplinaService.buscarPermitidaParaUsuario(dto.disciplinaId());

        HorarioAula horario = HorarioAula.builder()
                .disciplina(disciplina)
                .diaSemana(DayOfWeek.valueOf(dto.diaSemana().toUpperCase()))
                .horaInicio(LocalTime.parse(dto.horaInicio()))
                .horaFim(LocalTime.parse(dto.horaFim()))
                .build();

        return horarioAulaRepository.save(horario);
    }

    public List<HorarioAula> listarPorDisciplina(UUID disciplinaId) {
        disciplinaService.buscarPermitidaParaUsuario(disciplinaId);
        return horarioAulaRepository.findByDisciplinaId(disciplinaId);
    }

    public List<HorarioAula> listarPorTurma(UUID turmaId) {
        List<UUID> disciplinaIds = disciplinaService.listarPorTurma(turmaId).stream()
                .map(Disciplina::getId)
                .toList();

        if (disciplinaIds.isEmpty()) {
            return List.of();
        }

        return horarioAulaRepository.findByDisciplinaIdIn(disciplinaIds);
    }

    public HorarioAula atualizar(UUID id, HorarioAulaRequestDTO dto) {
        HorarioAula horario = horarioAulaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Horário não encontrado com id: " + id));

        disciplinaService.buscarPermitidaParaUsuario(horario.getDisciplina().getId());
        Disciplina disciplina = disciplinaService.buscarPermitidaParaUsuario(dto.disciplinaId());

        horario.setDisciplina(disciplina);
        horario.setDiaSemana(DayOfWeek.valueOf(dto.diaSemana().toUpperCase()));
        horario.setHoraInicio(LocalTime.parse(dto.horaInicio()));
        horario.setHoraFim(LocalTime.parse(dto.horaFim()));

        return horarioAulaRepository.save(horario);
    }

    public void deletar(UUID id) {
        HorarioAula horario = horarioAulaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Horário não encontrado com id: " + id));
        disciplinaService.buscarPermitidaParaUsuario(horario.getDisciplina().getId());
        horarioAulaRepository.delete(horario);
    }
}

```

### src/main/java/com/unicheck/Unicheckapi/service/PresencaService.java

```java

package com.unicheck.Unicheckapi.service;
import com.unicheck.Unicheckapi.dto.AlunoPresencaResumoDTO;
import com.unicheck.Unicheckapi.dto.DashboardDisciplinaDTO;
import com.unicheck.Unicheckapi.dto.PresencaRegistroResponseDTO;
import com.unicheck.Unicheckapi.dto.SincronizacaoPresencaDTO;
import com.unicheck.Unicheckapi.model.Aluno;

import com.unicheck.Unicheckapi.model.*;
import com.unicheck.Unicheckapi.repository.AlunoRepository;
import com.unicheck.Unicheckapi.repository.AulaRepository;
import com.unicheck.Unicheckapi.repository.DisciplinaRepository;
import com.unicheck.Unicheckapi.repository.PresencaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PresencaService {

    private final PresencaRepository presencaRepository;
    private final AlunoRepository alunoRepository;
    private final AulaRepository aulaRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final DisciplinaService disciplinaService;

    @Transactional
    public PresencaRegistroResponseDTO registrarPresenca(String alunoIdStr, UUID aulaId) {
        UUID alunoId = UUID.fromString(alunoIdStr);

        Aluno aluno = alunoRepository.findById(alunoId)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado"));

        Aula aula = aulaRepository.findById(aulaId)
                .orElseThrow(() -> new RuntimeException("Aula não encontrada"));
        disciplinaService.buscarPermitidaParaUsuario(aula.getDisciplina().getId());

        var existente = presencaRepository.findByAlunoIdAndAulaId(alunoId, aulaId);
        if (existente.isPresent()) {
            return respostaPresenca(existente.get(), aluno);
        }

        if (!aula.isAtiva()) {
            throw new RuntimeException("A aula já foi encerrada");
        }

        Presenca presenca = Presenca.builder()
                .aluno(aluno)
                .aula(aula)
                .disciplina(aula.getDisciplina())
                .build();

        presencaRepository.save(presenca);

        return respostaPresenca(presenca, aluno);
    }
    public List<Presenca> listar(){
        return presencaRepository.findAll();
    }
    public List<Presenca> buscarPorAluno(UUID id){
        Usuario usuario = disciplinaService.usuarioAutenticado();
        if (usuario.getRole() != Role.GESTOR && !usuario.getId().equals(id)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "Usuario sem permissao para consultar presencas deste aluno.");
        }
        return presencaRepository.findByAlunoId(id);
    }
    public List<Presenca> buscarPorDisciplina(UUID disciplinaId){
        disciplinaService.buscarPermitidaParaUsuario(disciplinaId);
        return presencaRepository.findByAulaDisciplinaId(disciplinaId);
    }
    public List<Presenca> buscarPorAula(UUID aulaId) {
        Aula aula = aulaRepository.findById(aulaId)
                .orElseThrow(() -> new RuntimeException("Aula não encontrada"));
        disciplinaService.buscarPermitidaParaUsuario(aula.getDisciplina().getId());
        return presencaRepository.findByAulaId(aulaId);
    }
    public List<DashboardDisciplinaDTO> gerarDashboard() {
        List<Disciplina> disciplinas = disciplinaRepository.findByAtivaTrue();
        return calcularDashboard(disciplinas);
    }

    public List<DashboardDisciplinaDTO> gerarDashboardProfessor(UUID professorId) {
        List<Disciplina> disciplinas = disciplinaService.listarPorProfessor(professorId);
        return calcularDashboard(disciplinas);
    }

    private List<DashboardDisciplinaDTO> calcularDashboard(List<Disciplina> disciplinas) {
        return disciplinas.stream().map(d -> {
            boolean possuiTurma = d.getTurma() != null;
            List<Aluno> alunos = possuiTurma ? alunoRepository.findByTurmaId(d.getTurma().getId()) : List.of();
            long totalAlunos = alunos.size();
            long totalAulas = aulaRepository.countByDisciplinaId(d.getId());
            long totalPresencas = alunos.stream()
                    .mapToLong(aluno -> presencaRepository.countAulasPresentesPorAlunoDisciplina(aluno.getId(), d.getId()))
                    .sum();
            long totalFaltas = (totalAlunos * totalAulas) - totalPresencas;
            double percentual = totalAulas == 0 || totalAlunos == 0 ? 0 :
                    (double) totalPresencas / (totalAlunos * totalAulas) * 100;

            return DashboardDisciplinaDTO.builder()
                    .disciplinaId(d.getId())
                    .turmaId(possuiTurma ? d.getTurma().getId() : null)
                    .professorId(d.getProfessor() != null ? d.getProfessor().getId() : null)
                    .nomeDisciplina(d.getNome())
                    .nomeTurma(possuiTurma ? d.getTurma().getIdentificacao() : "Sem turma")
                    .nomeProfessor(d.getProfessor() != null ? d.getProfessor().getNome() : null)
                    .totalAlunos(totalAlunos)
                    .totalPresencas(totalPresencas)
                    .totalFaltas(Math.max(0, totalFaltas))
                    .percentualPresenca(percentual)
                    .build();
        }).collect(java.util.stream.Collectors.toList());
    }

    public List<AlunoPresencaResumoDTO> resumoAlunosPorDisciplina(UUID disciplinaId) {
        Disciplina disciplina = disciplinaService.buscarPermitidaParaUsuario(disciplinaId);
        long totalAulas = aulaRepository.countByDisciplinaId(disciplinaId);

        if (disciplina.getTurma() == null) {
            return List.of();
        }

        return alunoRepository.findByTurmaId(disciplina.getTurma().getId()).stream()
                .map(aluno -> {
                    long presencas = presencaRepository.countAulasPresentesPorAlunoDisciplina(aluno.getId(), disciplinaId);
                    long faltas = Math.max(totalAulas - presencas, 0);
                    double percentual = totalAulas > 0 ? ((double) presencas / totalAulas) * 100 : 0;

                    return AlunoPresencaResumoDTO.builder()
                            .alunoId(aluno.getId())
                            .nome(aluno.getNome())
                            .matricula(aluno.getMatricula())
                            .email(aluno.getEmail())
                            .fotoUrl(aluno.getFotoUrl())
                            .turmaId(disciplina.getTurma().getId())
                            .nomeTurma(disciplina.getTurma().getIdentificacao())
                            .disciplinaId(disciplinaId)
                            .totalAulas(totalAulas)
                            .presencas(presencas)
                            .faltas(faltas)
                            .percentual(percentual)
                            .build();
                })
                .toList();
    }
    public void deletarPresenca(UUID id) {
        Presenca presenca = presencaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Presença não encontrada"));
        disciplinaService.buscarPermitidaParaUsuario(presenca.getAula().getDisciplina().getId());
        presencaRepository.delete(presenca);
    }

    public void sincronizar(List<SincronizacaoPresencaDTO> lista) {
        for (SincronizacaoPresencaDTO dto : lista) {
            if (dto.getClientId() != null && presencaRepository.findByClientId(dto.getClientId()).isPresent()) {
                continue;
            }
            // Evitar duplicata: verifica se já existe presença para o par (aluno, aula)
            boolean jaExiste = presencaRepository.existsByAlunoIdAndAulaId(
                    dto.getAlunoId(), dto.getAulaId());

            if (!jaExiste) {
                Aluno aluno = alunoRepository.findById(dto.getAlunoId())
                        .orElseThrow(() -> new RuntimeException("Aluno não encontrado: " + dto.getAlunoId()));
                Aula aula = aulaRepository.findById(dto.getAulaId())
                        .orElseThrow(() -> new RuntimeException("Aula não encontrada: " + dto.getAulaId()));
                disciplinaService.buscarPermitidaParaUsuario(aula.getDisciplina().getId());

                Presenca presenca = Presenca.builder()
                        .clientId(dto.getClientId())
                        .aluno(aluno)
                        .aula(aula)
                        .disciplina(aula.getDisciplina())
                        .dataHora(dto.getDataHoraLocal() != null ? dto.getDataHoraLocal().toLocalDateTime() : null)
                        .build();
                presencaRepository.save(presenca);
            }
        }
    }

    private PresencaRegistroResponseDTO respostaPresenca(Presenca presenca, Aluno aluno) {
        return PresencaRegistroResponseDTO.builder()
                .presencaId(presenca.getId())
                .alunoId(aluno.getId())
                .nome(aluno.getNome())
                .matricula(aluno.getMatricula())
                .fotoUrl(aluno.getFotoUrl())
                .dataHora(presenca.getDataHora())
                .build();
    }
}

```

### src/main/java/com/unicheck/Unicheckapi/service/OfflineBootstrapService.java

```java
package com.unicheck.Unicheckapi.service;

import com.unicheck.Unicheckapi.dto.OfflineBootstrapDTO;
import com.unicheck.Unicheckapi.model.Aluno;
import com.unicheck.Unicheckapi.model.Disciplina;
import com.unicheck.Unicheckapi.model.Professor;
import com.unicheck.Unicheckapi.model.Role;
import com.unicheck.Unicheckapi.model.Turma;
import com.unicheck.Unicheckapi.model.Usuario;
import com.unicheck.Unicheckapi.repository.AlunoRepository;
import com.unicheck.Unicheckapi.repository.AulaRepository;
import com.unicheck.Unicheckapi.repository.DisciplinaRepository;
import com.unicheck.Unicheckapi.repository.HorarioAulaRepository;
import com.unicheck.Unicheckapi.repository.PresencaRepository;
import com.unicheck.Unicheckapi.repository.ProfessorRepository;
import com.unicheck.Unicheckapi.repository.TurmaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OfflineBootstrapService {

    private final DisciplinaService disciplinaService;
    private final TurmaRepository turmaRepository;
    private final ProfessorRepository professorRepository;
    private final AlunoRepository alunoRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final AulaRepository aulaRepository;
    private final HorarioAulaRepository horarioAulaRepository;
    private final PresencaRepository presencaRepository;
    private final QrCodeService qrCodeService;

    public OfflineBootstrapDTO carregar() {
        Usuario usuario = disciplinaService.usuarioAutenticado();

        if (usuario.getRole() == Role.GESTOR) {
            return montarGestor();
        }

        if (usuario.getRole() == Role.PROFESSOR) {
            return montarProfessor(usuario.getId());
        }

        if (usuario.getRole() == Role.ALUNO) {
            return montarAluno(usuario.getId());
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Perfil sem permissao para bootstrap offline.");
    }

    private OfflineBootstrapDTO montarGestor() {
        List<Disciplina> disciplinas = disciplinaRepository.findByAtivaTrue();
        List<UUID> disciplinaIds = idsDisciplinas(disciplinas);

        return OfflineBootstrapDTO.builder()
                .turmas(turmaRepository.findAll())
                .professores(professorRepository.findAll())
                .alunos(alunoRepository.findAll())
                .disciplinas(disciplinas)
                .aulas(aulasPorDisciplinas(disciplinaIds))
                .horarios(horariosPorDisciplinas(disciplinaIds))
                .presencas(presencasPorDisciplinas(disciplinaIds))
                .build();
    }

    private OfflineBootstrapDTO montarProfessor(UUID professorId) {
        Professor professor = professorRepository.findById(professorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Professor nao encontrado."));
        List<Disciplina> disciplinas = disciplinaRepository.findByProfessorIdAndAtivaTrue(professorId);
        List<UUID> disciplinaIds = idsDisciplinas(disciplinas);
        List<Turma> turmas = disciplinas.stream()
                .map(Disciplina::getTurma)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        List<UUID> turmaIds = turmas.stream().map(Turma::getId).toList();
        List<Aluno> alunos = turmaIds.stream()
                .flatMap(turmaId -> alunoRepository.findByTurmaId(turmaId).stream())
                .distinct()
                .toList();

        return OfflineBootstrapDTO.builder()
                .turmas(turmas)
                .professores(List.of(professor))
                .alunos(alunos)
                .disciplinas(disciplinas)
                .aulas(aulasPorDisciplinas(disciplinaIds))
                .horarios(horariosPorDisciplinas(disciplinaIds))
                .presencas(presencasPorDisciplinas(disciplinaIds))
                .build();
    }

    private OfflineBootstrapDTO montarAluno(UUID alunoId) {
        Aluno aluno = alunoRepository.findById(alunoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Aluno nao encontrado."));
        List<Disciplina> disciplinas = aluno.getTurma() == null
                ? List.of()
                : disciplinaRepository.findByTurmaIdAndAtivaTrue(aluno.getTurma().getId());
        List<UUID> disciplinaIds = idsDisciplinas(disciplinas);
        List<Professor> professores = disciplinas.stream()
                .map(Disciplina::getProfessor)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        return OfflineBootstrapDTO.builder()
                .turmas(aluno.getTurma() == null ? List.of() : List.of(aluno.getTurma()))
                .professores(professores)
                .alunos(List.of(aluno))
                .disciplinas(disciplinas)
                .aulas(aulasPorDisciplinas(disciplinaIds))
                .horarios(horariosPorDisciplinas(disciplinaIds))
                .presencas(presencasPorDisciplinas(disciplinaIds).stream()
                        .filter(presenca -> presenca.getAluno() != null
                                && presenca.getAluno().getId().equals(alunoId))
                        .toList())
                .qrCodeBase64(Base64.getEncoder().encodeToString(qrCodeService.gerarQrCode(alunoId.toString())))
                .build();
    }

    private List<UUID> idsDisciplinas(List<Disciplina> disciplinas) {
        return disciplinas.stream().map(Disciplina::getId).toList();
    }

    private List<com.unicheck.Unicheckapi.model.Aula> aulasPorDisciplinas(List<UUID> disciplinaIds) {
        return disciplinaIds.isEmpty() ? List.of() : aulaRepository.findByDisciplinaIdIn(disciplinaIds);
    }

    private List<com.unicheck.Unicheckapi.model.HorarioAula> horariosPorDisciplinas(List<UUID> disciplinaIds) {
        return disciplinaIds.isEmpty() ? List.of() : horarioAulaRepository.findByDisciplinaIdIn(disciplinaIds);
    }

    private List<com.unicheck.Unicheckapi.model.Presenca> presencasPorDisciplinas(List<UUID> disciplinaIds) {
        return disciplinaIds.isEmpty() ? List.of() : presencaRepository.findByAulaDisciplinaIdIn(disciplinaIds);
    }
}

```

### src/main/java/com/unicheck/Unicheckapi/service/OfflineSyncService.java

```java
package com.unicheck.Unicheckapi.service;

import com.unicheck.Unicheckapi.dto.OfflineAulaSyncDTO;
import com.unicheck.Unicheckapi.dto.OfflineEncerramentoAulaSyncDTO;
import com.unicheck.Unicheckapi.dto.OfflinePresencaSyncDTO;
import com.unicheck.Unicheckapi.dto.OfflineSyncErroDTO;
import com.unicheck.Unicheckapi.dto.OfflineSyncMapDTO;
import com.unicheck.Unicheckapi.dto.OfflineSyncRequestDTO;
import com.unicheck.Unicheckapi.dto.OfflineSyncResponseDTO;
import com.unicheck.Unicheckapi.model.Aluno;
import com.unicheck.Unicheckapi.model.Aula;
import com.unicheck.Unicheckapi.model.Disciplina;
import com.unicheck.Unicheckapi.model.Presenca;
import com.unicheck.Unicheckapi.repository.AlunoRepository;
import com.unicheck.Unicheckapi.repository.AulaRepository;
import com.unicheck.Unicheckapi.repository.PresencaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OfflineSyncService {

    private final AulaRepository aulaRepository;
    private final AlunoRepository alunoRepository;
    private final PresencaRepository presencaRepository;
    private final DisciplinaService disciplinaService;

    @Transactional
    public OfflineSyncResponseDTO sincronizar(OfflineSyncRequestDTO request) {
        if (request == null) {
            request = new OfflineSyncRequestDTO();
        }

        OfflineSyncResponseDTO response = new OfflineSyncResponseDTO();
        Map<UUID, Aula> aulasPorClientId = new HashMap<>();

        for (OfflineAulaSyncDTO dto : safeList(request.getAulas())) {
            try {
                Aula aula = sincronizarAula(dto);
                aulasPorClientId.put(dto.getClientId(), aula);
                response.getAulas().add(new OfflineSyncMapDTO(dto.getClientId(), aula.getId()));
            } catch (Exception e) {
                response.getErros().add(erro(dto.getClientId(), "AULA", e));
            }
        }

        for (OfflinePresencaSyncDTO dto : safeList(request.getPresencas())) {
            try {
                Presenca presenca = sincronizarPresenca(dto, aulasPorClientId);
                response.getPresencas().add(new OfflineSyncMapDTO(dto.getClientId(), presenca.getId()));
            } catch (Exception e) {
                response.getErros().add(erro(dto.getClientId(), "PRESENCA", e));
            }
        }

        for (OfflineEncerramentoAulaSyncDTO dto : safeList(request.getEncerramentos())) {
            try {
                Aula aula = resolverAula(dto.getAulaId(), dto.getAulaClientId(), aulasPorClientId);
                disciplinaService.buscarPermitidaParaUsuario(aula.getDisciplina().getId());
                aula.setAtiva(false);
                aulaRepository.save(aula);
                response.getEncerramentos().add(new OfflineSyncMapDTO(dto.getClientId(), aula.getId()));
            } catch (Exception e) {
                response.getErros().add(erro(dto.getClientId(), "ENCERRAR_AULA", e));
            }
        }

        return response;
    }

    private Aula sincronizarAula(OfflineAulaSyncDTO dto) {
        validar(dto.getClientId() != null, "clientId da aula e obrigatorio");
        validar(dto.getDisciplinaId() != null, "disciplinaId e obrigatorio");
        validar(dto.getTitulo() != null && !dto.getTitulo().isBlank(), "titulo da aula e obrigatorio");

        return aulaRepository.findByClientId(dto.getClientId()).orElseGet(() -> {
            Disciplina disciplina = disciplinaService.buscarPermitidaParaUsuario(dto.getDisciplinaId());
            Aula aula = Aula.builder()
                    .clientId(dto.getClientId())
                    .disciplina(disciplina)
                    .titulo(dto.getTitulo())
                    .dataHora(dto.getDataHoraLocal() != null ? dto.getDataHoraLocal().toLocalDateTime() : LocalDateTime.now())
                    .ativa(true)
                    .build();
            return aulaRepository.save(aula);
        });
    }

    private Presenca sincronizarPresenca(OfflinePresencaSyncDTO dto, Map<UUID, Aula> aulasPorClientId) {
        validar(dto.getClientId() != null, "clientId da presenca e obrigatorio");
        validar(dto.getAlunoId() != null, "alunoId e obrigatorio");

        var existentePorClientId = presencaRepository.findByClientId(dto.getClientId());
        if (existentePorClientId.isPresent()) {
            return existentePorClientId.get();
        }

        Aula aula = resolverAula(dto.getAulaId(), dto.getAulaClientId(), aulasPorClientId);
        disciplinaService.buscarPermitidaParaUsuario(aula.getDisciplina().getId());

        var existentePorAlunoAula = presencaRepository.findByAlunoIdAndAulaId(dto.getAlunoId(), aula.getId());
        if (existentePorAlunoAula.isPresent()) {
            return existentePorAlunoAula.get();
        }

        Aluno aluno = alunoRepository.findById(dto.getAlunoId())
                .orElseThrow(() -> new RuntimeException("Aluno nao encontrado: " + dto.getAlunoId()));

        Presenca presenca = Presenca.builder()
                .clientId(dto.getClientId())
                .aluno(aluno)
                .aula(aula)
                .disciplina(aula.getDisciplina())
                .dataHora(dto.getDataHoraLocal() != null ? dto.getDataHoraLocal().toLocalDateTime() : LocalDateTime.now())
                .build();

        return presencaRepository.save(presenca);
    }

    private Aula resolverAula(UUID aulaId, UUID aulaClientId, Map<UUID, Aula> aulasPorClientId) {
        if (aulaId != null) {
            return aulaRepository.findById(aulaId)
                    .orElseThrow(() -> new RuntimeException("Aula nao encontrada: " + aulaId));
        }

        if (aulaClientId != null) {
            Aula aulaDoPacote = aulasPorClientId.get(aulaClientId);
            if (aulaDoPacote != null) return aulaDoPacote;

            return aulaRepository.findByClientId(aulaClientId)
                    .orElseThrow(() -> new RuntimeException("Aula offline nao sincronizada: " + aulaClientId));
        }

        throw new RuntimeException("aulaId ou aulaClientId e obrigatorio");
    }

    private void validar(boolean condicao, String mensagem) {
        if (!condicao) throw new RuntimeException(mensagem);
    }

    private OfflineSyncErroDTO erro(UUID clientId, String tipo, Exception e) {
        String mensagem = e.getMessage() != null ? e.getMessage() : "Erro ao sincronizar item offline";
        return new OfflineSyncErroDTO(clientId, tipo, mensagem);
    }

    private <T> List<T> safeList(List<T> value) {
        return value == null ? Collections.emptyList() : value;
    }
}

```

### src/main/java/com/unicheck/Unicheckapi/service/QrCodeService.java

```java
package com.unicheck.Unicheckapi.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

@Service
public class QrCodeService {

    public byte[] gerarQrCode(String conteudo) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            com.google.zxing.common.BitMatrix matrix = writer.encode(
                    conteudo,
                    BarcodeFormat.QR_CODE,
                    300,
                    300
            );

            BufferedImage image = new BufferedImage(
                    300,
                    300,
                    BufferedImage.TYPE_INT_RGB
            );

            for (int x = 0; x < 300; x++) {
                for (int y = 0; y < 300; y++) {
                    image.setRGB(x, y,
                            matrix.get(x, y) ? 0x000000 : 0xFFFFFF);
                }
            }

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", output);

            return output.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar QR Code");
        }
    }
}

```

### src/main/java/com/unicheck/Unicheckapi/service/JwtService.java

```java
package com.unicheck.Unicheckapi.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;

    public JwtService(@Value("${app.jwt.secret}") String secret) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("app.jwt.secret deve ter pelo menos 32 bytes para HS256.");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // GERAR TOKEN com email e role
    public String gerarToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(key, SignatureAlgorithm.HS256)  // ← usa "key" diretamente, não getChave()
                .compact();
    }

    // EXTRAIR EMAIL
    public String extrairEmail(String token) {
        return extrairClaims(token).getSubject();
    }

    // EXTRAIR ROLE
    public String extrairRole(String token) {
        return extrairClaims(token).get("role", String.class);
    }

    // VALIDAR TOKEN
    public boolean tokenValido(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims extrairClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

```

### src/main/java/com/unicheck/Unicheckapi/Exception/GlobalExceptionHandler.java

```java
package com.unicheck.Unicheckapi.Exception;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleStatus(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handle(RuntimeException ex){
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}

```

### src/main/java/com/unicheck/Unicheckapi/security/SecurityConfig.java

```java
package com.unicheck.Unicheckapi.security;

import com.unicheck.Unicheckapi.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    SecurityFilterChain security(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // Apenas GESTOR pode gerenciar usuários, turmas e disciplinas
                        .requestMatchers(HttpMethod.GET, "/disciplinas").hasRole("GESTOR")
                        .requestMatchers(HttpMethod.GET, "/disciplinas/minhas", "/disciplinas/turma/**").hasAnyRole("ALUNO", "PROFESSOR", "GESTOR")
                        .requestMatchers(HttpMethod.GET, "/disciplinas/professor/**").hasAnyRole("PROFESSOR", "GESTOR")
                        .requestMatchers(HttpMethod.GET, "/alunos").hasRole("GESTOR")
                        .requestMatchers(HttpMethod.GET, "/alunos/turma/**").hasAnyRole("ALUNO", "PROFESSOR", "GESTOR")
                        .requestMatchers(HttpMethod.POST, "/professores/**", "/alunos/**", "/turmas/**", "/disciplinas/**").hasRole("GESTOR")
                        .requestMatchers(HttpMethod.PUT, "/turmas/**", "/disciplinas/**").hasRole("GESTOR")
                        .requestMatchers(HttpMethod.DELETE, "/turmas/**", "/disciplinas/**", "/alunos/**", "/professores/**").hasRole("GESTOR")
                        .requestMatchers("/alunos/*/turma").hasRole("GESTOR")

                        // Professor pode iniciar/encerrar aulas e registrar/invalidar presenças
                        .requestMatchers(HttpMethod.GET, "/aulas/disciplina/**").hasAnyRole("ALUNO", "PROFESSOR", "GESTOR")
                        .requestMatchers(HttpMethod.POST, "/aulas/**").hasAnyRole("PROFESSOR", "GESTOR")
                        .requestMatchers(HttpMethod.PATCH, "/aulas/**").hasAnyRole("PROFESSOR", "GESTOR")
                        .requestMatchers(HttpMethod.GET, "/horarios/disciplina/**", "/horarios/turma/**").hasAnyRole("ALUNO", "PROFESSOR", "GESTOR")
                        .requestMatchers(HttpMethod.GET, "/presencas/dashboard").hasRole("GESTOR")
                        .requestMatchers(HttpMethod.GET, "/presencas/aluno/**").hasAnyRole("ALUNO", "GESTOR")
                        .requestMatchers(HttpMethod.GET, "/presencas/dashboard/professor/**", "/presencas/disciplina/**", "/presencas/aula/**").hasAnyRole("PROFESSOR", "GESTOR")
                        .requestMatchers("/presencas/registrar").hasRole("PROFESSOR")
                        .requestMatchers("/presencas/sincronizar").hasAnyRole("PROFESSOR", "GESTOR")
                        .requestMatchers("/sync/offline").hasAnyRole("PROFESSOR", "GESTOR")
                        .requestMatchers(HttpMethod.GET, "/offline/bootstrap").hasAnyRole("ALUNO", "PROFESSOR", "GESTOR")
                        .requestMatchers(HttpMethod.GET, "/presencas").hasRole("GESTOR")
                        .requestMatchers(HttpMethod.DELETE, "/presencas/**").hasAnyRole("PROFESSOR", "GESTOR")

                        // Aluno pode ver seu QR Code
                        .requestMatchers("/qrcode/aluno/**").hasRole("ALUNO")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

```

## Arquivos da API Spring Boot mexidos

### Adicionados

```text
src/main/java/com/unicheck/Unicheckapi/Controller/OfflineBootstrapController.java
src/main/java/com/unicheck/Unicheckapi/dto/AlunoPresencaResumoDTO.java
src/main/java/com/unicheck/Unicheckapi/dto/OfflineBootstrapDTO.java
src/main/java/com/unicheck/Unicheckapi/dto/PresencaRegistroResponseDTO.java
src/main/java/com/unicheck/Unicheckapi/service/OfflineBootstrapService.java
```

### Alterados

```text
src/main/java/com/unicheck/Unicheckapi/Controller/AlunoController.java
src/main/java/com/unicheck/Unicheckapi/Controller/HorarioAulaController.java
src/main/java/com/unicheck/Unicheckapi/Controller/PresencaController.java
src/main/java/com/unicheck/Unicheckapi/Exception/GlobalExceptionHandler.java
src/main/java/com/unicheck/Unicheckapi/dto/DashboardDisciplinaDTO.java
src/main/java/com/unicheck/Unicheckapi/dto/OfflineAulaSyncDTO.java
src/main/java/com/unicheck/Unicheckapi/dto/OfflineEncerramentoAulaSyncDTO.java
src/main/java/com/unicheck/Unicheckapi/dto/OfflinePresencaSyncDTO.java
src/main/java/com/unicheck/Unicheckapi/dto/SincronizacaoPresencaDTO.java
src/main/java/com/unicheck/Unicheckapi/model/Aula.java
src/main/java/com/unicheck/Unicheckapi/model/Presenca.java
src/main/java/com/unicheck/Unicheckapi/repository/AulaRepository.java
src/main/java/com/unicheck/Unicheckapi/repository/HorarioAulaRepository.java
src/main/java/com/unicheck/Unicheckapi/repository/PresencaRepository.java
src/main/java/com/unicheck/Unicheckapi/security/SecurityConfig.java
src/main/java/com/unicheck/Unicheckapi/service/AlunoService.java
src/main/java/com/unicheck/Unicheckapi/service/DisciplinaService.java
src/main/java/com/unicheck/Unicheckapi/service/HorarioAulaService.java
src/main/java/com/unicheck/Unicheckapi/service/JwtService.java
src/main/java/com/unicheck/Unicheckapi/service/OfflineSyncService.java
src/main/java/com/unicheck/Unicheckapi/service/PresencaService.java
src/main/java/com/unicheck/Unicheckapi/service/QrCodeService.java
src/main/resources/application.properties
```

### Removidos

```text
Nenhum arquivo foi removido nesta alteracao.
```

