package com.oficinadafesta.comanda;

import com.oficinadafesta.dto.PagamentoComandaDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ComandaService {

    private final ComandaRepository comandaRepository;

    // ✅ Ativa a próxima comanda disponível
    public Comanda ativarProximaComanda() {
        Optional<Comanda> livre = comandaRepository.findFirstByAtivaFalseOrderByCodigoAsc();

        if (livre.isEmpty()) {
            throw new RuntimeException("Não há comandas disponíveis");
        }

        Comanda comanda = livre.get();
        comanda.setAtiva(true);
        comanda.setBloqueada(false);
        comanda.setPaga(false);

        return comandaRepository.save(comanda);
    }

    // ✅ Verifica se pode liberar saída
    public boolean podeLiberarSaida(String codigo) {
        Comanda comanda = comandaRepository.findById(codigo)
                .orElseThrow(() -> new RuntimeException("Comanda não encontrada"));

        // ⬇️ Pode liberar se estiver paga ou sem valor
        return comanda.isPaga() || comanda.getValorTotal().compareTo(BigDecimal.ZERO) == 0;
    }

    // ✅ Marca comanda como paga e desbloqueia
    public void marcarComoPaga(String codigo) {
        Comanda comanda = comandaRepository.findById(codigo)
                .orElseThrow(() -> new RuntimeException("Comanda não encontrada"));

        comanda.setPaga(true);
        comanda.setBloqueada(false);
        comandaRepository.save(comanda);
    }

    // ✅ Busca comanda pelo código
    public Comanda buscarPorcodigo(String codigo) {
        return comandaRepository.findById(codigo)
                .orElseThrow(() -> new RuntimeException("Comanda não encontrada"));
    }

    @Transactional
    public void pagarComanda(String codigo, PagamentoComandaDTO dto){
        Comanda comanda = buscarPorcodigo(codigo);

        if (comanda.isPaga()) {
            throw new RuntimeException("Comanda já foi paga.");
        }

        comanda.setPaga(true);
        comanda.setAtiva(false);
        comanda.setBloqueada(false);

        comanda.getPedidos().clear();

        comandaRepository.save(comanda);
    }
}
