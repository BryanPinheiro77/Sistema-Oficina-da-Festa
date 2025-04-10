package com.oficinadafesta.comanda;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ComandaRepository extends JpaRepository<Comanda, String> {

    // Busca a próxima comanda desativada (disponível) em ordem crescente
    Optional<Comanda> findFirstByAtivaFalseOrderByCodigoAsc();

    // Busca comanda pelo código
    Optional<Comanda> findByCodigo(String codigo);
}
