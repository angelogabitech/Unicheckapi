package com.unicheck.Unicheckapi.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OfflineSyncResponseDTO {
    private List<OfflineSyncMapDTO> aulas = new ArrayList<>();
    private List<OfflineSyncMapDTO> presencas = new ArrayList<>();
    private List<OfflineSyncMapDTO> encerramentos = new ArrayList<>();
    private List<OfflineSyncErroDTO> erros = new ArrayList<>();
    private OfflineGestorEntidadeResponse turmas = new OfflineGestorEntidadeResponse();
    private OfflineGestorEntidadeResponse professores = new OfflineGestorEntidadeResponse();
    private OfflineGestorEntidadeResponse alunos = new OfflineGestorEntidadeResponse();
    private OfflineGestorEntidadeResponse disciplinas = new OfflineGestorEntidadeResponse();
}
