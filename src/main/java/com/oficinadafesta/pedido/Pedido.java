package com.oficinadafesta.pedido;

import com.oficinadafesta.pedido.ItemPedido;
import com.oficinadafesta.cliente.Cliente;
import com.oficinadafesta.comanda.Comanda;
import com.oficinadafesta.enums.FormaPagamento;
import com.oficinadafesta.enums.StatusPedido;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    // Outros campos de entrega...
    private boolean paraEntrega;
    private String enderecoEntrega;
    private LocalDateTime horarioEntrega;

    @Enumerated(EnumType.STRING)
    private FormaPagamento formaPagamento;

    // Utilize BigDecimal para valores monetários
    private BigDecimal taxaEntrega;
    private BigDecimal valorTotal;
    private boolean pagamentoConfirmado;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusPedido status;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    private List<ItemPedido> itens;

    // Método para calcular o total do pedido
    public BigDecimal getTotal() {
        BigDecimal total = BigDecimal.ZERO;
        if (itens != null) {
            for (ItemPedido item : itens) {
                // Supondo que o preço do produto esteja em double, convertemos:
                BigDecimal preco = item.getProduto().getPreco();
                BigDecimal quantidade = BigDecimal.valueOf(item.getQuantidade());
                total = total.add(preco.multiply(quantidade));
            }
        }
        // Se taxaEntrega é BigDecimal, adicione diretamente:
        if (taxaEntrega != null) {
            total = total.add(taxaEntrega);
        }
        return total;
    }

    @Column(precision = 10, scale = 2)
    private BigDecimal valorPago = BigDecimal.ZERO;

    @ManyToOne
    @JoinColumn(name = "comanda_id")
    private Comanda comanda;
}
