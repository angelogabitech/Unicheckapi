package com.unicheck.Unicheckapi.Controller;

import com.unicheck.Unicheckapi.dto.LoginRequestDTO;
import com.unicheck.Unicheckapi.dto.LoginResponseDTO;
import com.unicheck.Unicheckapi.dto.RegisterRequestDTO;
import com.unicheck.Unicheckapi.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @RequestBody LoginRequestDTO dto){

        String token = authService.login(dto);

        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO dto){
        authService.register(dto);
        return ResponseEntity.ok("Usuário criado");
    }
}