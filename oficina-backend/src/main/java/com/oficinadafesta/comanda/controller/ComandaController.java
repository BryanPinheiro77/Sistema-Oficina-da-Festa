package com.oficinadafesta.comanda.controller;


import com.oficinadafesta.comanda.domain.Comanda;
import com.oficinadafesta.comanda.service.ComandaService;
import com.oficinadafesta.comanda.dto.ComandaDetalhadaResponseDTO;
import com.oficinadafesta.comanda.dto.ComandaResponseDTO;
import com.oficinadafesta.comanda.dto.PagamentoComandaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comandas")
@RequiredArgsConstructor
public class ComandaController {

    private final ComandaService comandaService;

    @PostMapping("/entrada")
    public Comanda registrarEntrada(){
        return comandaService.ativarProximaComanda();
    }

    @GetMapping("/{codigo}/resumo")
    public ComandaResponseDTO getResumoComanda(@PathVariable String codigo){
        Comanda comanda = comandaService.buscarPorcodigo(codigo);
        ComandaResponseDTO dto = new ComandaResponseDTO();

        dto.setCodigo(comanda.getCodigo());
        dto.setAtiva(comanda.isAtiva());
        dto.setBloqueada(comanda.isBloqueada());
        dto.setPaga(comanda.isPaga());
        dto.setValorTotal(comanda.getValorTotal());

        dto.setPedidos(comanda.getPedidos().stream().map(pedido ->{
            ComandaResponseDTO.PedidoResumoDTO resumo = new ComandaResponseDTO.PedidoResumoDTO();
            resumo.setId(pedido.getId());
            resumo.setStatus(pedido.getStatus());
            resumo.setTotal(pedido.getTotal());
            return resumo;
        }) .toList());

        return dto;
    }

    @GetMapping("/com/oficinadafesta/caixa/{codigo}")
    public ComandaResponseDTO visualizarComandaNoCaixa(@PathVariable String codigo){

        Comanda comanda = comandaService.buscarPorcodigo(codigo);
        ComandaResponseDTO dto = new ComandaResponseDTO();
        dto.setCodigo(comanda.getCodigo());
        dto.setAtiva(comanda.isAtiva());
        dto.setBloqueada(comanda.isBloqueada());
        dto.setPaga(comanda.isPaga());
        dto.setValorTotal(comanda.getValorTotal());

        dto.setPedidos(comanda.getPedidos().stream().map(pedido -> {
            ComandaResponseDTO.PedidoResumoDTO resumo = new ComandaResponseDTO.PedidoResumoDTO();
            resumo.setId(pedido.getId());
            resumo.setStatus(pedido.getStatus());
            resumo.setTotal(pedido.getTotal());
            return resumo;
        }) .toList());

        return dto;
    }


    @GetMapping("/{codigo}/verificar-saida")
    public boolean podeSair(@PathVariable String codigo){
        return comandaService.podeLiberarSaida(codigo);
    }

    @PostMapping("/{codigo}/fechar")
    public void fecharComanda(@PathVariable String codigo){
        comandaService.fecharComanda(codigo);
    }

    @PostMapping("/com/oficinadafesta/caixa/{codigo}/pagar")
    public void pagarComanda(@PathVariable String codigo, @RequestBody PagamentoComandaDTO dto){
        comandaService.pagarComanda(codigo, dto);
    }

    @GetMapping("/{codigo}")
    public ComandaDetalhadaResponseDTO buscarDetalhesComanda(@PathVariable String codigo){
        return comandaService.buscarComandaCompleta(codigo);
    }
}
