package com.oficinadafesta.pagamento.domain;

import com.oficinadafesta.comanda.domain.Comanda;
import com.oficinadafesta.enums.FormaPagamento;
import com.oficinadafesta.pedido.domain.Pedido;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormaPagamento formaPagamento;

    @Column(precision = 10, scale = 2)
    private BigDecimal troco;

    @Column(nullable = false)
    private LocalDateTime pagoEm = LocalDateTime.now();

    @ManyToOne(optional = true)
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    @ManyToOne(optional = true)
    @JoinColumn(name = "comanda_id")
    private Comanda comanda;

    @PrePersist
    public void validarVinculo() {
        if (pagoEm == null) pagoEm = LocalDateTime.now();
        if ((pedido == null && comanda == null) ||
                (pedido != null && comanda != null)) {
            throw new IllegalStateException(
                    "Pagamento deve estar vinculado OU a pedido OU a comanda."
            );
        }
    }
}