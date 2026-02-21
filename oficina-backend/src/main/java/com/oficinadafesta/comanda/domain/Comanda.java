package com.oficinadafesta.comanda.domain;

import com.oficinadafesta.pedido.domain.Pedido;
import com.oficinadafesta.pagamento.domain.Pagamento;
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
public class Comanda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Integer codigo;

    private boolean ativa = true;

    private LocalDateTime abertaEm = LocalDateTime.now();

    private LocalDateTime fechadaEm;

    @OneToMany(mappedBy = "comanda")
    private List<Pedido> pedidos = new ArrayList<>();

    @OneToMany(mappedBy = "comanda")
    private List<Pagamento> pagamentos = new ArrayList<>();

    // =========================
    // Regras de domínio
    // =========================

    public BigDecimal calcularTotal() {
        return pedidos.stream()
                .map(Pedido::calcularTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean estaPaga() {
        BigDecimal totalPago = pagamentos.stream()
                .map(Pagamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalPago.compareTo(calcularTotal()) >= 0;
    }

    public void fechar() {
        if (!estaPaga()) {
            throw new IllegalStateException("Comanda não pode ser fechada sem pagamento.");
        }
        this.ativa = false;
        this.fechadaEm = LocalDateTime.now();
    }
}