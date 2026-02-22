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

import java.math.BigDecimal;
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
        comanda.setCodigo("001");
        comanda.setAtiva(false);
        comanda.setBloqueada(true);
        comanda.setPaga(true);

        when(comandaRepository.findFirstByAtivaFalseOrderByCodigoAsc())
                .thenReturn(Optional.of(comanda));
        when(comandaRepository.save(any(Comanda.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Comanda ativada = comandaService.ativarProximaComanda();

        assertTrue(ativada.isAtiva());
        assertFalse(ativada.isBloqueada());
        assertFalse(ativada.isPaga());

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
    void podeLiberarSaida_deveLiberarSePaga() {
        Comanda comanda = new Comanda();
        comanda.setCodigo("010");
        comanda.setPaga(true);

        // pode estar com pedidos ou não; paga=true já libera
        comanda.setPedidos(new ArrayList<>());

        when(comandaRepository.findById("010"))
                .thenReturn(Optional.of(comanda));

        assertTrue(comandaService.podeLiberarSaida("010"));
    }

    @Test
    void podeLiberarSaida_deveLiberarSeSemPedidos_totalZero() {
        Comanda comanda = new Comanda();
        comanda.setCodigo("010");
        comanda.setPaga(false);

        // Sem pedidos => getValorTotal() = 0
        comanda.setPedidos(new ArrayList<>());

        when(comandaRepository.findById("010"))
                .thenReturn(Optional.of(comanda));

        assertTrue(comandaService.podeLiberarSaida("010"));
    }

    @Test
    void podeLiberarSaida_deveNegarSeNaoPagaEComPedidos_totalMaiorQueZero() {
        Comanda comanda = new Comanda();
        comanda.setCodigo("010");
        comanda.setPaga(false);

        // cria um pedido fake com total > 0
        Pedido pedido = mock(Pedido.class);
        when(pedido.getTotal()).thenReturn(new BigDecimal("0.01"));

        comanda.setPedidos(List.of(pedido));

        when(comandaRepository.findById("010"))
                .thenReturn(Optional.of(comanda));

        assertFalse(comandaService.podeLiberarSaida("010"));
    }

    @Test
    void pagarComanda_deveFalharSeJaPaga() {
        Comanda comanda = new Comanda();
        comanda.setCodigo("123");
        comanda.setPaga(true);

        when(comandaRepository.findById("123"))
                .thenReturn(Optional.of(comanda));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                comandaService.pagarComanda("123", null)
        );

        assertTrue(ex.getMessage().contains("Comanda já foi paga"));
        verify(comandaRepository, never()).save(any());
    }

    @Test
    void pagarComanda_deveDesvincularPedidos_limparLista_eSalvar() {
        Comanda comanda = new Comanda();
        comanda.setCodigo("123");
        comanda.setPaga(false);
        comanda.setAtiva(true);
        comanda.setBloqueada(true);
        comanda.setPedidos(new ArrayList<>());

        Pedido p1 = new Pedido();
        p1.setId(1L);
        p1.setComanda(comanda);

        Pedido p2 = new Pedido();
        p2.setId(2L);
        p2.setComanda(comanda);

        comanda.getPedidos().addAll(List.of(p1, p2));

        when(comandaRepository.findById("123")).thenReturn(Optional.of(comanda));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));
        when(comandaRepository.save(any(Comanda.class))).thenAnswer(inv -> inv.getArgument(0));

        comandaService.pagarComanda("123", null);

        assertTrue(comanda.isPaga());
        assertFalse(comanda.isAtiva());
        assertFalse(comanda.isBloqueada());
        assertTrue(comanda.getPedidos().isEmpty());

        assertNull(p1.getComanda());
        assertNull(p2.getComanda());

        verify(pedidoRepository, times(2)).save(any(Pedido.class));
        verify(comandaRepository, times(1)).save(any(Comanda.class));
    }
}