package com.unicheck.Unicheckapi.Controller;

import com.unicheck.Unicheckapi.dto.LoginRequestDTO;
import com.unicheck.Unicheckapi.dto.LoginResponseDTO;
import com.unicheck.Unicheckapi.dto.MeResponseDTO;
import com.unicheck.Unicheckapi.dto.RefreshRequestDTO;
import com.unicheck.Unicheckapi.service.AuthService;
import com.unicheck.Unicheckapi.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refresh(@RequestBody RefreshRequestDTO dto) {
        return ResponseEntity.ok(authService.refresh(dto.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshRequestDTO dto) {
        authService.logout(dto.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponseDTO> me(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtService.extrairEmail(token);
        return ResponseEntity.ok(authService.me(email));
    }
}
