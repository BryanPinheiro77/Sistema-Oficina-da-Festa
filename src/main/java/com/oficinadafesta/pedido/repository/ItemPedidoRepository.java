package com.oficinadafesta.pedido.repository;

import com.oficinadafesta.pedido.domain.ItemPedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {
}
