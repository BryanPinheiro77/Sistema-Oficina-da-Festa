package com.oficinadafesta.cliente.controller;


import com.oficinadafesta.cliente.domain.Cliente;
import com.oficinadafesta.cliente.service.ClienteService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/clientes")
public class ClienteController {
    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService){
        this.clienteService = clienteService;
    }

    @PostMapping
    public Cliente criar(@RequestBody Cliente cliente){
        return clienteService.salvar(cliente);
    }

    @GetMapping("/telefone/{telefone}")
    public Optional<Cliente> buscarPorTelefone(@PathVariable String telefone){
        return clienteService.buscarPorTelefone(telefone);
    }

    @GetMapping("/{id}")
    public Optional<Cliente> buscarPorId(@PathVariable Long id){
        return clienteService.buscarPorId(id);
    }
}
