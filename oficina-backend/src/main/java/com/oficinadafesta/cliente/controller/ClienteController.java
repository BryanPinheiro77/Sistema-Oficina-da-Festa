package com.oficinadafesta.cliente.controller;

import com.oficinadafesta.cliente.domain.Cliente;
import com.oficinadafesta.cliente.service.ClienteService;
import com.oficinadafesta.shared.security.Roles;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private static final Logger log = LoggerFactory.getLogger(ClienteController.class);

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService){
        this.clienteService = clienteService;
    }

    @PreAuthorize(Roles.CLIENTE_CRIAR)
    @PostMapping
    public ResponseEntity<Cliente> criar(@Valid @RequestBody Cliente cliente){
        log.info("POST /clientes - criando cliente: nome={}, telefone={}", cliente.getNome(), cliente.getTelefone());
        Cliente salvo = clienteService.salvar(cliente);
        log.info("Cliente criado: id={}, nome={}", salvo.getId(), salvo.getNome());
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @PreAuthorize(Roles.CLIENTE_LER)
    @GetMapping("/telefone/{telefone}")
    public ResponseEntity<Cliente> buscarPorTelefone(@PathVariable String telefone){
        log.debug("GET /clientes/telefone/{} - buscando cliente", telefone);
        return clienteService.buscarPorTelefone(telefone)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.debug("Cliente não encontrado por telefone={}", telefone);
                    return ResponseEntity.notFound().build();
                });
    }

    @PreAuthorize(Roles.CLIENTE_LER)
    @GetMapping("/{id}")
    public ResponseEntity<Cliente> buscarPorId(@PathVariable Long id){
        log.debug("GET /clientes/{} - buscando cliente", id);
        return clienteService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.debug("Cliente não encontrado por id={}", id);
                    return ResponseEntity.notFound().build();
                });
    }
}