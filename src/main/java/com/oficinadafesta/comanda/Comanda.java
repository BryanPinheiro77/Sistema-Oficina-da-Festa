package com.oficinadafesta.comanda;


import com.oficinadafesta.pedido.Pedido;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Comanda {

    @Id
    private String codigo;

    private boolean ativa;

    private boolean bloqueada;

    private boolean paga = false;

    @OneToMany(mappedBy = "comanda")
    private List<Pedido> pedidos;

    public BigDecimal getValorTotal(){
        if (pedidos == null || pedidos.isEmpty()){
            return BigDecimal.ZERO;
        }
        return pedidos.stream()
                .map(Pedido::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
