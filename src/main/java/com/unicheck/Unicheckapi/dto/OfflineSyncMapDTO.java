package com.unicheck.Unicheckapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfflineSyncMapDTO {
    private UUID clientId;
    private UUID serverId;
}
