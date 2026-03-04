package com.oficinadafesta.comanda.service;

import com.oficinadafesta.comanda.domain.Comanda;
import com.oficinadafesta.comanda.repository.ComandaRepository;
import com.oficinadafesta.pedido.domain.Pedido;
import com.oficinadafesta.pedido.repository.PedidoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComandaServiceTest {

    @Mock private ComandaRepository comandaRepository;
    @Mock private PedidoRepository pedidoRepository;

    @InjectMocks private ComandaService comandaService;

    @Test
    void ativarProximaComanda_deveAtivarPrimeiraLivre() {
        Comanda comanda = new Comanda();
        comanda.setCodigo(1);
        comanda.setAtiva(false);

        when(comandaRepository.findFirstByAtivaFalseOrderByCodigoAsc())
                .thenReturn(Optional.of(comanda));
        when(comandaRepository.save(any(Comanda.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Comanda ativada = comandaService.ativarProximaComanda();

        assertTrue(ativada.isAtiva());
        verify(comandaRepository, times(1)).save(any(Comanda.class));
    }

    @Test
    void ativarProximaComanda_deveFalharSeNaoHouverDisponivel() {
        when(comandaRepository.findFirstByAtivaFalseOrderByCodigoAsc())
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                comandaService.ativarProximaComanda()
        );

        assertTrue(ex.getMessage().contains("Não há comandas disponíveis"));
        verify(comandaRepository, never()).save(any());
    }

    @Test
    void podeLiberarSaida_deveLiberarSeSemPedidos_totalZero() {
        Comanda comanda = new Comanda();
        comanda.setCodigo(10);
        comanda.setAtiva(true);
        comanda.setPedidos(new ArrayList<>());

        when(comandaRepository.findByCodigo(10))
                .thenReturn(Optional.of(comanda));

        assertTrue(comandaService.podeLiberarSaida("010"));
    }

    @Test
    void fecharComanda_deveFalharSeNaoPaga() {
        Comanda comanda = new Comanda();
        comanda.setCodigo(123);
        comanda.setAtiva(true);
        comanda.setPedidos(new ArrayList<>());

        when(comandaRepository.findByCodigo(123))
                .thenReturn(Optional.of(comanda));

        assertThrows(IllegalStateException.class, () ->
                comandaService.fecharComanda("123")
        );

        verify(comandaRepository, never()).save(any());
    }

    @Test
    void fecharComanda_deveDesvinuclarPedidos_eSalvar() {
        Comanda comanda = new Comanda();
        comanda.setCodigo(123);
        comanda.setAtiva(true);

        Pedido p1 = new Pedido();
        p1.setId(1L);
        p1.setComanda(comanda);

        Pedido p2 = new Pedido();
        p2.setId(2L);
        p2.setComanda(comanda);

        comanda.setPedidos(new ArrayList<>(List.of(p1, p2)));

        when(comandaRepository.findByCodigo(123)).thenReturn(Optional.of(comanda));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));
        when(comandaRepository.save(any(Comanda.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThrows(IllegalStateException.class, () ->
                comandaService.fecharComanda("123")
        );
    }
}