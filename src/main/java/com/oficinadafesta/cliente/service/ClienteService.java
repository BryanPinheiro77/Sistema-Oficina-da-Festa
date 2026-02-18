package com.oficinadafesta.cliente.service;

import com.oficinadafesta.cliente.domain.Cliente;
import com.oficinadafesta.cliente.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository){
        this.clienteRepository = clienteRepository;
    }

    public Cliente salvar(Cliente cliente){
        return clienteRepository.save(cliente);
    }

    public Optional<Cliente> buscarPorTelefone(String telefone){
        return clienteRepository.findByTelefone(telefone);
    }

    public Optional<Cliente> buscarPorId(long id){
        return clienteRepository.findById(id);
    }
}
