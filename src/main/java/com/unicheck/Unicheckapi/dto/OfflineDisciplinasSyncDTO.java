package com.unicheck.Unicheckapi.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OfflineDisciplinasSyncDTO {
    private List<OfflineDisciplinaCriarDTO> criar = new ArrayList<>();
    private List<OfflineDisciplinaEditarDTO> editar = new ArrayList<>();
    private List<OfflineDisciplinaDeletarDTO> deletar = new ArrayList<>();
}
