package com.unicheck.Unicheckapi.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OfflineSyncRequestDTO {
    private List<OfflineAulaSyncDTO> aulas = new ArrayList<>();
    private List<OfflinePresencaSyncDTO> presencas = new ArrayList<>();
    private List<OfflineEncerramentoAulaSyncDTO> encerramentos = new ArrayList<>();
}
