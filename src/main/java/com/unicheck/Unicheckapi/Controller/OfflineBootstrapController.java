package com.unicheck.Unicheckapi.Controller;

import com.unicheck.Unicheckapi.dto.OfflineBootstrapDTO;
import com.unicheck.Unicheckapi.service.OfflineBootstrapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/offline")
@RequiredArgsConstructor
public class OfflineBootstrapController {

    private final OfflineBootstrapService offlineBootstrapService;

    @GetMapping("/bootstrap")
    public ResponseEntity<OfflineBootstrapDTO> bootstrap() {
        return ResponseEntity.ok(offlineBootstrapService.carregar());
    }
}

