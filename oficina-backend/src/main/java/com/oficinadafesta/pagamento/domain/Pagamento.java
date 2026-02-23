package com.oficinadafesta.pagamento.domain;

import com.oficinadafesta.comanda.domain.Comanda;
import com.oficinadafesta.pedido.domain.Pedido;
import com.oficinadafesta.enums.FormaPagamento;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    private FormaPagamento formaPagamento;

    private LocalDateTime pagoEm = LocalDateTime.now();

    @ManyToOne
    private Pedido pedido; // usado para pedido online

    @ManyToOne
    private Comanda comanda; // usado para presencial

    @PrePersist
    public void validarVinculo() {
        if ((pedido == null && comanda == null) ||
                (pedido != null && comanda != null)) {
            throw new IllegalStateException("Pagamento deve estar vinculado OU a pedido OU a comanda.");
        }
    }
}