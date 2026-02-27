package com.oficinadafesta.auth.controller;

import com.oficinadafesta.auth.dto.AuthResponseDTO;
import com.oficinadafesta.auth.dto.LoginDTO;
import com.oficinadafesta.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginDTO dto) {
        return ResponseEntity.ok(authService.login(dto.getUsuario(), dto.getSenha()));
    }
}