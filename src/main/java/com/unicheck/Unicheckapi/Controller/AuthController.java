package com.unicheck.Unicheckapi.Controller;

import com.unicheck.Unicheckapi.dto.LoginRequestDTO;
import com.unicheck.Unicheckapi.dto.LoginResponseDTO;
import com.unicheck.Unicheckapi.dto.MeResponseDTO;
import com.unicheck.Unicheckapi.dto.RegisterRequestDTO;
import com.unicheck.Unicheckapi.service.AuthService;
import com.unicheck.Unicheckapi.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;


    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @RequestBody LoginRequestDTO dto){

        String token = authService.login(dto);

        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponseDTO> me(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtService.extrairEmail(token);
        return ResponseEntity.ok(authService.me(email));
    }

}
