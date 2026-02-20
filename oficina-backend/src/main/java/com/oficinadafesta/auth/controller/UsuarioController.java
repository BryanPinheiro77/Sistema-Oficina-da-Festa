package com.oficinadafesta.auth.controller;

import com.oficinadafesta.auth.dto.CriarUsuarioDTO;
import com.oficinadafesta.auth.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/criar")
    public ResponseEntity<?> criar(@Valid @RequestBody CriarUsuarioDTO dto) {
        var criado = usuarioService.criarUsuario(dto);

        // por enquanto simples (depois fazemos UsuarioResponseDTO)
        return ResponseEntity.ok(
                "Usuário criado: " + criado.getUsuario() + " (" + criado.getSetor().name() + ")"
        );
    }
}