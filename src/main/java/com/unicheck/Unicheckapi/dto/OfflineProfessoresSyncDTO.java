package com.unicheck.Unicheckapi.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OfflineProfessoresSyncDTO {
    private List<OfflineProfessorCriarDTO> criar = new ArrayList<>();
    private List<OfflineProfessorEditarDTO> editar = new ArrayList<>();
    private List<OfflineProfessorDeletarDTO> deletar = new ArrayList<>();
}
