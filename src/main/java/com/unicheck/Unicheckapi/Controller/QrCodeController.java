package com.unicheck.Unicheckapi.Controller;


import com.unicheck.Unicheckapi.service.QrCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/qrcode")
@RequiredArgsConstructor
public class QrCodeController {

    private final QrCodeService qrCodeService;

    @GetMapping("/disciplina/{id}")
    public ResponseEntity<byte[]> gerarQRCode(@PathVariable UUID id) throws Exception {

        String conteudo = id.toString();

        byte[] qrCode = qrCodeService.gerarQrCode(conteudo);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=qrcode.png")
                .contentType(MediaType.IMAGE_PNG)
                .body(qrCode);
    }
}