package com.unicheck.Unicheckapi.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OfflineTurmasSyncDTO {
    private List<OfflineTurmaCriarDTO> criar = new ArrayList<>();
    private List<OfflineTurmaEditarDTO> editar = new ArrayList<>();
    private List<OfflineTurmaDeletarDTO> deletar = new ArrayList<>();
}
