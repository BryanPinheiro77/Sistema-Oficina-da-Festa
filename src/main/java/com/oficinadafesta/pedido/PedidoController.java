package com.oficinadafesta.pedido;

import com.oficinadafesta.dto.*;
import com.oficinadafesta.enums.AreaTipo;
import com.oficinadafesta.enums.StatusItemPedido;
import com.oficinadafesta.enums.StatusPedido;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService){
        this.pedidoService = pedidoService;
    }

    @PostMapping
    public Pedido criar(@RequestBody PedidoRequestDTO dto){
        return pedidoService.criarPedido(dto);
    }

    @GetMapping
    public List<Pedido> listarTodos() {
        return pedidoService.listarTodos();
    }

    @GetMapping("/status/{status}")
    public List<Pedido> listarPorStatus(@PathVariable StatusPedido status){
        return pedidoService.listarPorStatus(status);
    }

    @PatchMapping("/{id}/status")
    public Pedido atualizarStatus(@PathVariable Long id, @RequestParam StatusPedido status){
        return pedidoService.atualizarStatus(id, status);
    }

    @PostMapping("/{id}/confirmar-pagamento")
    public Pedido confirmarPagamento(@PathVariable Long id, @RequestParam BigDecimal valorPago){
        return pedidoService.confirmarPagamento(id, valorPago);
    }

    @GetMapping("/{id}/itens-por-setor")
    public List<ItensPorSetorResponseDTO> getItensPorSetor(@PathVariable Long id) {
        Pedido pedido = pedidoService.buscarPorId(id);

        Map<AreaTipo, List<ItemPedido>> itensPorSetor = pedidoService.separarItensPorSetor(pedido);

        return itensPorSetor.entrySet().stream()
                .map(entry -> new ItensPorSetorResponseDTO(
                        entry.getKey().name(),
                        entry.getValue().stream()
                                .map(item -> new ItensPorSetorResponseDTO.ItemDTO(
                                        item.getProduto().getNome(),
                                        item.getQuantidade()))
                                .toList()
                ))
                .toList();
    }

    @GetMapping("/setor/{setor}/pedidos")
    public List<PedidoSetorResponseDTO> listarPedidosPorSetor(@PathVariable AreaTipo setor){
        return pedidoService.listarPedidosPorSetor(setor);
    }

    @PatchMapping("/itens/{idItem}/status")
    public void atualizarStatusItem(@PathVariable Long idItem, @RequestParam StatusItemPedido status) {
        pedidoService.atualizarStatusItem(idItem, status);
    }

    @PostMapping("/caixa")
    public ResponseEntity<?> criarPedidoNoCaixa(@RequestBody PedidoCaixaDTO dto){
        pedidoService.criarPedidoNoCaixa(dto);
        return ResponseEntity.ok("Pedido criado com sucesso");
    }

    @PostMapping("cafe/comandas/{codigo}/adicionar")
    public ResponseEntity<PedidoResumoDTO> adicionarPedidoCafe(@PathVariable String codigo, @RequestBody AdicionarPedidoCafeDTO dto){
        PedidoResumoDTO resumo = pedidoService.adicionarPedidoCafe(codigo, dto);
        return ResponseEntity.ok(resumo);
    }
}
