package com.oficinadafesta.cliente.service;

import com.oficinadafesta.cliente.domain.Cliente;
import com.oficinadafesta.cliente.repository.ClienteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ClienteService {

    private static final Logger log = LoggerFactory.getLogger(ClienteService.class);

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository){
        this.clienteRepository = clienteRepository;
    }

    public Cliente salvar(Cliente cliente){
        log.debug("Salvando cliente: nome={}, telefone={}", cliente.getNome(), cliente.getTelefone());
        Cliente salvo = clienteRepository.save(cliente);
        log.debug("Cliente salvo: id={}", salvo.getId());
        return salvo;
    }

    public Optional<Cliente> buscarPorTelefone(String telefone){
        log.debug("Buscando cliente por telefone={}", telefone);
        return clienteRepository.findByTelefone(telefone);
    }

    public Optional<Cliente> buscarPorId(Long id){
        log.debug("Buscando cliente por id={}", id);
        return clienteRepository.findById(id);
    }
}