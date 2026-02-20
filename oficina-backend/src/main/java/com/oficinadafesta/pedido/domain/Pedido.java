package com.oficinadafesta.pedido.domain;

import com.oficinadafesta.cliente.domain.Cliente;
import com.oficinadafesta.comanda.domain.Comanda;
import com.oficinadafesta.pagamento.domain.Pagamento;
import com.oficinadafesta.enums.StatusPedido;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Cliente cliente;

    private boolean paraEntrega;
    private String enderecoEntrega;
    private LocalDateTime horarioEntrega;

    @Column(nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPedido status = StatusPedido.PENDENTE;

    @Column(precision = 10, scale = 2)
    private BigDecimal taxaEntrega = BigDecimal.ZERO;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();

    @ManyToOne
    private Comanda comanda; // null para pedidos online

    @OneToMany(mappedBy = "pedido")
    private List<Pagamento> pagamentos = new ArrayList<>();

    // =========================
    // Regras de domínio
    // =========================

    public BigDecimal calcularTotal() {
        BigDecimal total = BigDecimal.ZERO;

        for (ItemPedido item : itens) {
            BigDecimal preco = item.getProduto().getPreco();
            BigDecimal quantidade = BigDecimal.valueOf(item.getQuantidade());
            total = total.add(preco.multiply(quantidade));
        }

        return total.add(taxaEntrega != null ? taxaEntrega : BigDecimal.ZERO);
    }

    public boolean possuiComanda() {
        return this.comanda != null;
    }

    public boolean estaPago() {
        return pagamentos.stream()
                .map(Pagamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .compareTo(calcularTotal()) >= 0;
    }
}