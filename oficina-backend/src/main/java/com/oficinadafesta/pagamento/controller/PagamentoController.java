package com.oficinadafesta.pagamento.controller;

import com.oficinadafesta.pagamento.dto.PagamentoRequestDTO;
import com.oficinadafesta.pagamento.dto.PagamentoResponseDTO;
import com.oficinadafesta.pagamento.service.PagamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/pagamentos")
@RequiredArgsConstructor
public class PagamentoController {

    private final PagamentoService pagamentoService;

    // ===============================
    // Criar pagamento
    // ===============================
    @PostMapping
    public ResponseEntity<PagamentoResponseDTO> criar(
            @RequestBody @Valid PagamentoRequestDTO dto) {

        PagamentoResponseDTO response = pagamentoService.criarPagamento(dto);

        return ResponseEntity
                .created(URI.create("/pagamentos/" + response.id()))
                .body(response);
    }

    // ===============================
    // Buscar pagamento por ID
    // ===============================
    @GetMapping("/{id}")
    public ResponseEntity<PagamentoResponseDTO> buscarPorId(
            @PathVariable Long id) {

        PagamentoResponseDTO response = pagamentoService.buscarPorId(id);

        return ResponseEntity.ok(response);
    }

    // ===============================
    // Listar todos pagamentos
    // ===============================
    @GetMapping
    public ResponseEntity<List<PagamentoResponseDTO>> listar() {

        List<PagamentoResponseDTO> pagamentos =
                pagamentoService.listarTodos();

        return ResponseEntity.ok(pagamentos);
    }
}