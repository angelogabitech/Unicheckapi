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

