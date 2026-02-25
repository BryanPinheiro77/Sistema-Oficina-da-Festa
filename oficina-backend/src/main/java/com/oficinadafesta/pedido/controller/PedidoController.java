package com.oficinadafesta.pedido.controller;

import com.oficinadafesta.enums.AreaTipo;
import com.oficinadafesta.enums.StatusItemPedido;
import com.oficinadafesta.enums.StatusPedido;
import com.oficinadafesta.enums.TipoEntrega;
import com.oficinadafesta.pedido.domain.ItemPedido;
import com.oficinadafesta.pedido.domain.Pedido;
import com.oficinadafesta.pedido.dto.*;
import com.oficinadafesta.pedido.service.PedidoService;
import com.oficinadafesta.shared.security.Roles;
import com.oficinadafesta.shared.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private static final Logger log = LoggerFactory.getLogger(PedidoController.class);

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService){
        this.pedidoService = pedidoService;
    }

    // =========================================================
    // 1) PEDIDOS GERAIS (Caixa/Admin)
    // =========================================================

    @PreAuthorize(Roles.CAIXA_OU_ADMIN)
    @PostMapping
    public ResponseEntity<Pedido> criar(@RequestBody PedidoRequestDTO dto){
        Pedido criado = pedidoService.criarPedido(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PreAuthorize(Roles.CAIXA_OU_ADMIN)
    @GetMapping
    public ResponseEntity<List<Pedido>> listarTodos() {
        return ResponseEntity.ok(pedidoService.listarTodos());
    }

    @PreAuthorize(Roles.CAIXA_OU_ADMIN)
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Pedido>> listarPorStatus(@PathVariable StatusPedido status){
        return ResponseEntity.ok(pedidoService.listarPorStatus(status));
    }

    @PreAuthorize(Roles.CAIXA_OU_ADMIN)
    @PatchMapping("/{id}/status")
    public ResponseEntity<Pedido> atualizarStatus(@PathVariable Long id, @RequestParam StatusPedido status){
        return ResponseEntity.ok(pedidoService.atualizarStatus(id, status));
    }

    @PreAuthorize(Roles.CAIXA_OU_ADMIN)
    @PostMapping("/{id}/confirmar-pagamento")
    public ResponseEntity<Pedido> confirmarPagamento(@PathVariable Long id, @RequestParam BigDecimal valorPago){
        return ResponseEntity.ok(pedidoService.confirmarPagamento(id, valorPago));
    }

    // =========================================================
    // 2) ITENS POR SETOR (Opção B)
    // - ADMIN e CAIXA veem tudo
    // - Setor vê apenas os itens do setor dele
    // =========================================================

    @PreAuthorize(Roles.TODOS_SETORES)
    @GetMapping("/{id}/itens-por-setor")
    public ResponseEntity<List<ItensPorSetorResponseDTO>> getItensPorSetor(@PathVariable Long id) {
        Authentication a = SecurityUtils.auth();

        Pedido pedido = pedidoService.buscarPorId(id);
        Map<AreaTipo, List<ItemPedido>> itensPorSetor = pedidoService.separarItensPorSetor(pedido);

        // ADMIN, CAIXA e CAFE veem tudo
        if (SecurityUtils.canSeeAll(a)) {
            List<ItensPorSetorResponseDTO> resp = itensPorSetor.entrySet().stream()
                    .map(entry -> new ItensPorSetorResponseDTO(
                            entry.getKey().name(),
                            entry.getValue().stream()
                                    .map(item -> new ItensPorSetorResponseDTO.ItemDTO(
                                            item.getProduto().getNome(),
                                            item.getQuantidade()))
                                    .toList()
                    ))
                    .toList();

            return ResponseEntity.ok(resp);
        }

        // Setor: só vê o próprio
        AreaTipo setorDoUsuario = SecurityUtils.getSetor(a);
        if (setorDoUsuario == null) {
            log.warn("Acesso negado: user={} sem setor/role válida tentou acessar itens-por-setor do pedido={}",
                    a != null ? a.getName() : "anon", id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<ItemPedido> itens = itensPorSetor.getOrDefault(setorDoUsuario, List.of());

        List<ItensPorSetorResponseDTO> resp = List.of(
                new ItensPorSetorResponseDTO(
                        setorDoUsuario.name(),
                        itens.stream()
                                .map(item -> new ItensPorSetorResponseDTO.ItemDTO(
                                        item.getProduto().getNome(),
                                        item.getQuantidade()
                                ))
                                .toList()
                )
        );

        return ResponseEntity.ok(resp);
    }

    // =========================================================
    // 3) FILA POR SETOR (Setor só vê o dele; ADMIN vê qualquer)
    // =========================================================

    @PreAuthorize(Roles.SETORES_PRODUCAO)
    @GetMapping("/setor/{setor}")
    public ResponseEntity<List<PedidoSetorResponseDTO>> listarPedidosPorSetor(@PathVariable AreaTipo setor){
        Authentication a = SecurityUtils.auth();

        if (!SecurityUtils.isAdmin(a)) {
            if (!SecurityUtils.hasRole(a, setor.name())) {
                log.warn("Acesso negado: user={} tentou acessar setor={} sem permissão",
                        a != null ? a.getName() : "anon", setor.name());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        return ResponseEntity.ok(pedidoService.listarPedidosPorSetor(setor));
    }

    // =========================================================
    // 4) SETORES atualizam status do item (produção)
    // - validação forte está no service (setor só altera item do próprio setor)
    // =========================================================

    @PreAuthorize(Roles.SETORES_PRODUCAO)
    @PatchMapping("/itens/{idItem}/status")
    public ResponseEntity<Void> atualizarStatusItem(@PathVariable Long idItem, @RequestParam StatusItemPedido status) {
        pedidoService.atualizarStatusItem(idItem, status);
        return ResponseEntity.noContent().build();
    }

    // =========================================================
    // 5) FLUXO DO CAIXA (comanda / consumo local)
    // =========================================================

    @PreAuthorize(Roles.CAIXA_OU_ADMIN)
    @PostMapping("/caixa")
    public ResponseEntity<Void> criarPedidoNoCaixa(@RequestBody PedidoCaixaDTO dto){
        pedidoService.criarPedidoNoCaixa(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // =========================================================
    // 6) CAFÉ adiciona pedido na comanda
    // =========================================================

    @PreAuthorize(Roles.CAFE_CAIXA_ADMIN)
    @PostMapping("/cafe/comandas/{codigo}/adicionar")
    public ResponseEntity<PedidoResumoDTO> adicionarPedidoCafe(@PathVariable String codigo,
                                                               @RequestBody AdicionarPedidoCafeDTO dto){
        PedidoResumoDTO resumo = pedidoService.adicionarPedidoCafe(codigo, dto);
        return ResponseEntity.ok(resumo);
    }
    // =========================================================
    // 7) Pedidos para entrega ou retirada
    // =========================================================

    @PreAuthorize(Roles.EXPEDICAO)
    @GetMapping("/expedicao")
    public ResponseEntity<List<PedidoExpedicaoDTO>> listarPedidosExpedicao(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) StatusPedido status
    ) {
        TipoEntrega tipoEntrega = null;
        if (tipo != null && !tipo.isBlank() && !tipo.equalsIgnoreCase("TODOS")) {
            tipoEntrega = TipoEntrega.valueOf(tipo.toUpperCase());
        }
        return ResponseEntity.ok(pedidoService.listarPedidosExpedicao(tipoEntrega, status));
    }
}