package com.unicheck.Unicheckapi.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OfflineGestorEntidadeResponse {
    private List<OfflineSyncMapDTO> criadas = new ArrayList<>();
    private List<OfflineSyncMapDTO> editadas = new ArrayList<>();
    private List<OfflineSyncMapDTO> deletadas = new ArrayList<>();
}
