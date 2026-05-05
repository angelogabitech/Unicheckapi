package com.unicheck.Unicheckapi.Controller;

import com.unicheck.Unicheckapi.dto.OfflineSyncRequestDTO;
import com.unicheck.Unicheckapi.dto.OfflineSyncResponseDTO;
import com.unicheck.Unicheckapi.service.OfflineSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sync")
@RequiredArgsConstructor
public class OfflineSyncController {

    private final OfflineSyncService offlineSyncService;

    @PostMapping("/offline")
    public ResponseEntity<OfflineSyncResponseDTO> sincronizarOffline(
            @RequestBody OfflineSyncRequestDTO request) {
        return ResponseEntity.ok(offlineSyncService.sincronizar(request));
    }
}
