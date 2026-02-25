package com.oficinadafesta.comanda.service;

import com.oficinadafesta.comanda.domain.Comanda;
import com.oficinadafesta.comanda.dto.ComandaDetalhadaResponseDTO;
import com.oficinadafesta.comanda.dto.PagamentoComandaDTO;
import com.oficinadafesta.comanda.repository.ComandaRepository;
import com.oficinadafesta.device.dto.CatracaSaidaValidarResponseDTO;
import com.oficinadafesta.pedido.domain.Pedido;
import com.oficinadafesta.pedido.repository.PedidoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComandaService {

    private static final Logger log = LoggerFactory.getLogger(ComandaService.class);

    private final ComandaRepository comandaRepository;
    private final PedidoRepository pedidoRepository;

    // =========================================================
    // Helpers
    // =========================================================

    private Integer parseCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            log.warn("Código de comanda inválido (vazio/nulo)");
            throw new IllegalArgumentException("Código da comanda inválido");
        }
        try {
            return Integer.parseInt(codigo.trim()); // aceita "001"
        } catch (NumberFormatException e) {
            log.warn("Código de comanda inválido (não numérico): {}", codigo);
            throw new IllegalArgumentException("Código da comanda inválido: " + codigo);
        }
    }

    public Comanda buscarPorCodigo(String codigoStr) {
        Integer codigo = parseCodigo(codigoStr);

        return comandaRepository.findByCodigo(codigo)
                .orElseThrow(() -> {
                    log.warn("Comanda não encontrada: codigo={}", codigoStr);
                    return new RuntimeException("Comanda não encontrada: " + codigoStr);
                });
    }

    private String formatCodigo(Integer codigo) {
        return codigo == null ? null : String.format("%03d", codigo);
    }

    // =========================================================
    // Fluxo de comanda (interno)
    // =========================================================

    public Comanda ativarProximaComanda() {
        log.info("Ativando próxima comanda disponível (ativa=false)");

        Comanda comanda = comandaRepository.findFirstByAtivaFalseOrderByCodigoAsc()
                .orElseThrow(() -> {
                    log.warn("Não há comandas disponíveis para ativação");
                    return new RuntimeException("Não há comandas disponíveis");
                });

        comanda.setAtiva(true);
        Comanda salva = comandaRepository.save(comanda);

        log.info("Comanda ativada: id={}, codigo={}", salva.getId(), formatCodigo(salva.getCodigo()));
        return salva;
    }

    public boolean podeLiberarSaida(String codigo) {
        log.debug("Verificando liberação de saída: codigo={}", codigo);

        Comanda comanda = buscarPorCodigo(codigo);

        BigDecimal devido = comanda.calcularTotal();
        boolean paga = comanda.estaPaga();
        boolean liberar = paga || devido.compareTo(BigDecimal.ZERO) == 0;

        log.debug("Liberação saída: codigo={}, liberar={}, paga={}, devido={}",
                codigo, liberar, paga, devido);

        return liberar;
    }

    /**
     * ⚠️ No seu domínio, "pagar" deveria gerar Pagamento(s) e associar.
     * Esse método está aqui só como placeholder do que você tinha.
     * Ideal: migrar esse fluxo para PagamentoService e chamar aqui.
     */
    @Transactional
    public void pagarComanda(String codigo, PagamentoComandaDTO dto) {
        log.info("Solicitação pagar comanda: codigo={} (dto recebido)", codigo);

        Comanda comanda = buscarPorCodigo(codigo);

        if (!comanda.estaPaga()) {
            log.warn("Tentativa de pagar/fechar comanda sem estar paga (pagamentos insuficientes): codigo={}, total={}",
                    codigo, comanda.calcularTotal());
            throw new RuntimeException("Comanda não está paga ainda. Registre pagamento antes.");
        }

        // política atual: desassociar pedidos e fechar
        fecharComanda(codigo);

        log.info("Comanda paga/fechada com sucesso: codigo={}", codigo);
    }

    @Transactional
    public Comanda fecharComanda(String codigo) {
        log.info("Fechando comanda: codigo={}", codigo);

        Comanda comanda = buscarPorCodigo(codigo);

        // regra de domínio (vai lançar se não estiver paga)
        comanda.fechar();

        // desassocia pedidos (sua política atual)
        List<Pedido> pedidos = comanda.getPedidos();
        int totalPedidos = pedidos != null ? pedidos.size() : 0;

        if (pedidos != null && !pedidos.isEmpty()) {
            for (Pedido pedido : pedidos) {
                pedido.setComanda(null);
                pedidoRepository.save(pedido);
            }
            comanda.getPedidos().clear();
        }

        Comanda salva = comandaRepository.save(comanda);

        log.info("Comanda fechada: codigo={}, pedidosRemovidos={}, ativaAgora={}",
                codigo, totalPedidos, salva.isAtiva());

        return salva;
    }

    public ComandaDetalhadaResponseDTO buscarComandaCompleta(String codigo) {
        log.info("Buscando comanda completa: codigo={}", codigo);

        Comanda comanda = buscarPorCodigo(codigo);

        ComandaDetalhadaResponseDTO dto = new ComandaDetalhadaResponseDTO();
        dto.setCodigo(formatCodigo(comanda.getCodigo()));
        dto.setPaga(comanda.estaPaga());
        dto.setTotal(comanda.calcularTotal());

        List<ComandaDetalhadaResponseDTO.PedidoDTO> pedidosDTO = comanda.getPedidos().stream().map(p -> {
            ComandaDetalhadaResponseDTO.PedidoDTO pedidoDTO = new ComandaDetalhadaResponseDTO.PedidoDTO();
            pedidoDTO.setId(p.getId());
            pedidoDTO.setFormaPagamento(p.getFormaPagamento() != null ? p.getFormaPagamento().name() : null);

            List<ComandaDetalhadaResponseDTO.ItemDTO> itensDTO = p.getItens().stream().map(i -> {
                ComandaDetalhadaResponseDTO.ItemDTO itemDTO = new ComandaDetalhadaResponseDTO.ItemDTO();
                itemDTO.setNomeProduto(i.getProduto() != null ? i.getProduto().getNome() : null);
                itemDTO.setQuantidade(i.getQuantidade());
                itemDTO.setPreco(i.getProduto() != null ? i.getProduto().getPreco() : null);
                itemDTO.setStatus(i.getStatus() != null ? i.getStatus().name() : null);
                return itemDTO;
            }).collect(Collectors.toList());

            pedidoDTO.setItens(itensDTO);
            return pedidoDTO;
        }).collect(Collectors.toList());

        dto.setPedidos(pedidosDTO);

        log.info("Comanda completa retornada: codigo={}, paga={}, total={}, pedidos={}",
                dto.getCodigo(), dto.isPaga(), dto.getTotal(), pedidosDTO.size());

        return dto;
    }

    // =========================================================
    // Device / Catraca
    // =========================================================

    @Transactional
    public void ativarComanda(String codigo) {
        log.info("Ativando comanda via catraca: codigo={}", codigo);

        Comanda comanda = buscarPorCodigo(codigo);

        if (comanda.isAtiva()) {
            log.warn("Comanda já estava ativa (catraca): codigo={}", codigo);
            return;
        }

        comanda.setAtiva(true);
        comandaRepository.save(comanda);

        log.info("Comanda ativada via catraca: codigo={}", codigo);
    }

    public CatracaSaidaValidarResponseDTO validarSaida(String codigo) {
        log.info("Validando saída via catraca: codigo={}", codigo);

        Comanda comanda = buscarPorCodigo(codigo);

        BigDecimal devido = comanda.calcularTotal();
        boolean paga = comanda.estaPaga();
        boolean liberar = paga || devido.compareTo(BigDecimal.ZERO) == 0;

        String motivo = liberar ? "LIBERADO" : "PAGAMENTO_PENDENTE";

        log.info("Resultado validar saída: codigo={}, liberar={}, paga={}, devido={}",
                codigo, liberar, paga, devido);

        return new CatracaSaidaValidarResponseDTO(liberar, motivo, devido);
    }

    @Transactional
    public void confirmarSaida(String codigo) {
        log.info("Confirmando saída via catraca: codigo={}", codigo);

        CatracaSaidaValidarResponseDTO validar = validarSaida(codigo);

        if (!validar.liberar()) {
            log.warn("Saída negada: codigo={}, motivo={}, devido={}",
                    codigo, validar.motivo(), validar.valorDevido());
            throw new RuntimeException("Saída não liberada: " + validar.motivo());
        }

        // política atual: sair = fechar
        fecharComanda(codigo);

        log.info("Saída confirmada e comanda fechada: codigo={}", codigo);
    }
}