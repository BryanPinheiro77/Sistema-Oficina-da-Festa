package com.oficinadafesta.pedido;

import com.oficinadafesta.enums.StatusItemPedido;
import com.oficinadafesta.enums.StatusPedido;
import com.oficinadafesta.produto.Produto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    private Pedido pedido;

    @ManyToOne
    private Produto produto;

    private int quantidade;

    @Enumerated(EnumType.STRING)
    private StatusItemPedido status = StatusItemPedido.PENDENTE;
}