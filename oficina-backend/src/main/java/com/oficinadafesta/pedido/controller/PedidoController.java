package com.oficinadafesta.pedido.controller;

import com.oficinadafesta.enums.AreaTipo;
import com.oficinadafesta.enums.StatusItemPedido;
import com.oficinadafesta.enums.StatusPedido;
import com.oficinadafesta.pedido.domain.ItemPedido;
import com.oficinadafesta.pedido.domain.Pedido;
import com.oficinadafesta.pedido.dto.*;
import com.oficinadafesta.pedido.service.PedidoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    // Helpers (roles)
    // =========================================================

    private Authentication auth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private boolean isAdmin(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private boolean isCaixa(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CAIXA"));
    }

    private boolean isCafe(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CAFE"));
    }

    private boolean canSeeAll(Authentication auth) {
        return isAdmin(auth) || isCaixa(auth) || isCafe(auth);
    }

    private boolean hasRole(Authentication auth, String role) {
        String authority = "ROLE_" + role;
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(authority));
    }

    // =========================================================
    // 1) PEDIDOS GERAIS (Caixa/Admin)
    // =========================================================

    @PreAuthorize("hasAnyRole('CAIXA','ADMIN')")
    @PostMapping
    public ResponseEntity<Pedido> criar(@RequestBody PedidoRequestDTO dto){
        Pedido criado = pedidoService.criarPedido(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PreAuthorize("hasAnyRole('CAIXA','ADMIN')")
    @GetMapping
    public ResponseEntity<List<Pedido>> listarTodos() {
        return ResponseEntity.ok(pedidoService.listarTodos());
    }

    @PreAuthorize("hasAnyRole('CAIXA','ADMIN')")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Pedido>> listarPorStatus(@PathVariable StatusPedido status){
        return ResponseEntity.ok(pedidoService.listarPorStatus(status));
    }

    @PreAuthorize("hasAnyRole('CAIXA','ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<Pedido> atualizarStatus(@PathVariable Long id, @RequestParam StatusPedido status){
        return ResponseEntity.ok(pedidoService.atualizarStatus(id, status));
    }

    @PreAuthorize("hasAnyRole('CAIXA','ADMIN')")
    @PostMapping("/{id}/confirmar-pagamento")
    public ResponseEntity<Pedido> confirmarPagamento(@PathVariable Long id, @RequestParam BigDecimal valorPago){
        return ResponseEntity.ok(pedidoService.confirmarPagamento(id, valorPago));
    }

    // =========================================================
    // 2) ITENS POR SETOR (Opção B)
    // - ADMIN e CAIXA veem tudo
    // - Setor vê apenas os itens do setor dele
    // =========================================================

    @PreAuthorize("hasAnyRole('ADMIN','CAIXA','CAFE','CONFEITARIA','PRODUCAO_DOCINHOS','PRODUCAO_SALGADOS','FRITURA','SOBREMESAS','COMUNICACAO')")
    @GetMapping("/{id}/itens-por-setor")
    public ResponseEntity<List<ItensPorSetorResponseDTO>> getItensPorSetor(@PathVariable Long id) {
        Authentication a = auth();
        Pedido pedido = pedidoService.buscarPorId(id);
        Map<AreaTipo, List<ItemPedido>> itensPorSetor = pedidoService.separarItensPorSetor(pedido);

        // ADMIN, CAIXA e CAFE veem tudo
        if (canSeeAll(a)) {
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
        // (como JWT gera ROLE_<SETOR>, usamos as roles para descobrir qual setor ele é)
        AreaTipo setorDoUsuario = detectarSetorDoUsuario(a);
        if (setorDoUsuario == null) {
            log.warn("Acesso negado: user={} sem setor/role válida tentou acessar itens-por-setor do pedido={}",
                    a != null ? a.getName() : "anon", id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<ItemPedido> itens = itensPorSetor.getOrDefault(setorDoUsuario, List.of());

        // retorna apenas 1 bloco (do setor dele)
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

    private AreaTipo detectarSetorDoUsuario(Authentication auth) {
        if (auth == null) return null;

        // tenta mapear a primeira ROLE_ que bater em um AreaTipo
        for (AreaTipo setor : AreaTipo.values()) {
            if (hasRole(auth, setor.name())) {
                return setor;
            }
        }
        return null;
    }

    // =========================================================
    // 3) FILA POR SETOR (Setor só vê o dele; ADMIN vê qualquer)
    // =========================================================

    @PreAuthorize("hasAnyRole('ADMIN','CAFE','CONFEITARIA','PRODUCAO_DOCINHOS','PRODUCAO_SALGADOS','FRITURA','SOBREMESAS')")
    @GetMapping("/setor/{setor}")
    public ResponseEntity<List<PedidoSetorResponseDTO>> listarPedidosPorSetor(@PathVariable AreaTipo setor){
        Authentication a = auth();

        // ADMIN pode tudo
        if (!isAdmin(a)) {
            // precisa ter a role do setor do path
            if (!hasRole(a, setor.name())) {
                log.warn("Acesso negado: user={} tentou acessar setor={} sem permissão",
                        a != null ? a.getName() : "anon", setor.name());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        return ResponseEntity.ok(pedidoService.listarPedidosPorSetor(setor));
    }

    // =========================================================
    // 4) SETORES atualizam status do item (produção)
    // =========================================================

    @PreAuthorize("hasAnyRole('ADMIN','CAFE','CONFEITARIA','PRODUCAO_DOCINHOS','PRODUCAO_SALGADOS','FRITURA','SOBREMESAS')")
    @PatchMapping("/itens/{idItem}/status")
    public ResponseEntity<Void> atualizarStatusItem(@PathVariable Long idItem, @RequestParam StatusItemPedido status) {
        pedidoService.atualizarStatusItem(idItem, status);
        return ResponseEntity.noContent().build();
    }

    // =========================================================
    // 5) FLUXO DO CAIXA (comanda / consumo local)
    // =========================================================

    @PreAuthorize("hasAnyRole('CAIXA','ADMIN')")
    @PostMapping("/caixa")
    public ResponseEntity<Void> criarPedidoNoCaixa(@RequestBody PedidoCaixaDTO dto){
        pedidoService.criarPedidoNoCaixa(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // =========================================================
    // 6) CAFÉ adiciona pedido na comanda
    // =========================================================

    @PreAuthorize("hasAnyRole('CAFE','CAIXA','ADMIN')")
    @PostMapping("/cafe/comandas/{codigo}/adicionar")
    public ResponseEntity<PedidoResumoDTO> adicionarPedidoCafe(@PathVariable String codigo, @RequestBody AdicionarPedidoCafeDTO dto){
        PedidoResumoDTO resumo = pedidoService.adicionarPedidoCafe(codigo, dto);
        return ResponseEntity.ok(resumo);
    }
}