package com.unicheck.Unicheckapi.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OfflineAlunosSyncDTO {
    private List<OfflineAlunoCriarDTO> criar = new ArrayList<>();
    private List<OfflineAlunoEditarDTO> editar = new ArrayList<>();
    private List<OfflineAlunoDeletarDTO> deletar = new ArrayList<>();
}
