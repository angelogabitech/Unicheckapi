package com.unicheck.Unicheckapi.service;

<<<<<<< HEAD
import com.unicheck.Unicheckapi.Exception.ApiException;
import com.unicheck.Unicheckapi.dto.AtualizarPerfilDTO;
import com.unicheck.Unicheckapi.dto.ProfessorRequestDTO;
import com.unicheck.Unicheckapi.model.Disciplina;
import com.unicheck.Unicheckapi.model.Professor;
import com.unicheck.Unicheckapi.repository.DisciplinaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.unicheck.Unicheckapi.model.Role;
=======
import com.unicheck.Unicheckapi.model.Professor;
>>>>>>> 0fe0c1eff8687d7baa9153ab44cce2e9923c8612
import com.unicheck.Unicheckapi.repository.ProfessorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service

public class ProfessorService {

    private final ProfessorRepository repository;
<<<<<<< HEAD
    private final DisciplinaRepository disciplinaRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfessorService(ProfessorRepository repository, DisciplinaRepository disciplinaRepository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.disciplinaRepository = disciplinaRepository;
        this.passwordEncoder = passwordEncoder;
    }
=======

    public ProfessorService(ProfessorRepository repository) {
        this.repository = repository;
    }

>>>>>>> 0fe0c1eff8687d7baa9153ab44cce2e9923c8612
    public Professor salvar(Professor professor) {
        return repository.save(professor);
    }

    public List<Professor> listar() {
        return repository.findAll();
    }

    public Professor buscarPorId(UUID id) {
        return repository.findById(id)
<<<<<<< HEAD
                .orElseThrow(() -> new ApiException("Professor não encontrado"));
    }
    public Professor criar(ProfessorRequestDTO dto) {
        Professor professor = new Professor();
        professor.setNome(dto.getNome());
        professor.setEmail(dto.getEmail());
        professor.setSenha(passwordEncoder.encode(dto.getSenha()));
        professor.setRole(Role.PROFESSOR);
        return repository.save(professor);
    }

    public Professor atualizar(UUID id, ProfessorRequestDTO dto){
        Professor professor = buscarPorId(id);
        professor.setNome(dto.getNome());
        professor.setEmail(dto.getEmail());
        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            professor.setSenha(passwordEncoder.encode(dto.getSenha()));
        }
        return repository.save(professor);
    }
    public void deletar(UUID id) {
        Professor professor = buscarPorId(id);
        List<Disciplina> disciplinas = disciplinaRepository.findByProfessorId(id);
        disciplinas.forEach(disciplina -> disciplina.setProfessor(null));
        disciplinaRepository.saveAll(disciplinas);
        repository.delete(professor);
    }
    public  Professor atualizarPerfil(UUID id, AtualizarPerfilDTO dto) {
        Professor professor = buscarPorId(id);
        if (dto.getNome() != null) professor.setNome(dto.getNome());
        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            professor.setSenha(passwordEncoder.encode(dto.getSenha()));
        }
        if (dto.getFotoUrl() != null) professor.setFotoUrl(dto.getFotoUrl());
        return repository.save(professor);
    }
    public void salvarFoto(UUID id, String fotoBase64) {
        Professor professor = buscarPorId(id);
        professor.setFotoUrl(fotoBase64);
        repository.save(professor); // ← repository minúsculo, não ProfessorRepository
    }
}
=======
                .orElseThrow(() -> new RuntimeException("Professor não encontrado"));
    }
}
>>>>>>> 0fe0c1eff8687d7baa9153ab44cce2e9923c8612
