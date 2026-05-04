package com.unicheck.Unicheckapi.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.unicheck.Unicheckapi.model.Matricula;
import com.unicheck.Unicheckapi.repository.MatriculaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QrCodeService {
    private final MatriculaRepository matriculaRepository;

    public byte[] gerarQrCode(String conteudo) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            com.google.zxing.common.BitMatrix matrix = writer.encode(
                    conteudo,
                    BarcodeFormat.QR_CODE,
                    300,
                    300
            );

            BufferedImage image = new BufferedImage(
                    300,
                    300,
                    BufferedImage.TYPE_INT_RGB
            );

            for (int x = 0; x < 300; x++) {
                for (int y = 0; y < 300; y++) {
                    image.setRGB(x, y,
                            matrix.get(x, y) ? 0x000000 : 0xFFFFFF);
                }
            }

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", output);

            return output.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar QR Code");
        }
    }

    public Matricula validarQrCode(String qrCode) {
        String id = qrCode.replace("MATRICULA_ID:", "");
        UUID matriculaId = UUID.fromString(id); // usa id (valor limpo)
        return matriculaRepository.findById(matriculaId)
                .orElseThrow(() -> new RuntimeException("QR Code inválido"));
    }
}