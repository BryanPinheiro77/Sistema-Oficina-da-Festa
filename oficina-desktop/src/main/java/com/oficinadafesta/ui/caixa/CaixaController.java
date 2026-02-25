package com.oficinadafesta.ui.caixa;

import com.oficinadafesta.api.CaixaApi;
import com.oficinadafesta.api.ClienteApi;
import com.oficinadafesta.api.ProdutoApi;
import com.oficinadafesta.api.dto.ClienteDTO;
import com.oficinadafesta.api.dto.ComandaDetalhadaResponseDTO;
import com.oficinadafesta.api.dto.PagamentoComandaDTO;
import com.oficinadafesta.api.dto.PedidoItemRequestDTO;
import com.oficinadafesta.api.dto.PedidoRequestDTO;
import com.oficinadafesta.api.dto.ProdutoDTO;
import com.oficinadafesta.app.AppContext;
import com.oficinadafesta.ui.model.ItemPedidoRow;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;

public class CaixaController {

    private final AppContext ctx;
    private final ClienteApi clienteApi;
    private final ProdutoApi produtoApi;
    private final CaixaApi caixaApi;

    public CaixaController(AppContext ctx) {
        this.ctx = ctx;
        this.clienteApi = ctx.clienteApi;
        this.produtoApi = ctx.produtoApi;

        // cria CaixaApi usando o mesmo Http/token do AppContext
        this.caixaApi = new CaixaApi(ctx.http);
    }

    private ClienteDTO clienteAtual;
    private List<ProdutoDTO> produtosDisponiveis = new ArrayList<>();

    // Aba novo pedido
    @FXML private TextField telefoneField, nomeField, cepField, enderecoField;
    @FXML private ComboBox<String> produtoComboBox;
    @FXML private TextField quantidadeField;

    @FXML private TableView<ItemPedidoRow> itensTable;
    @FXML private TableColumn<ItemPedidoRow, String> produtoColumn;
    @FXML private TableColumn<ItemPedidoRow, Integer> quantidadeColumn;
    @FXML private TableColumn<ItemPedidoRow, String> precoColumn;
    @FXML private TableColumn<ItemPedidoRow, String> observacaoColumn;

    @FXML private ComboBox<String> formaPagamentoComboBox;
    @FXML private ToggleGroup retiradaToggleGroup;
    @FXML private DatePicker dataRetiradaPicker;
    @FXML private TextField horaRetiradaField;
    @FXML private RadioButton retiradaImediataRadio;
    @FXML private RadioButton retiradaAgendadaRadio;
    @FXML private RadioButton entregaRadio;
    @FXML private TextField campoObservacaoItem;

    @FXML private Button logoutButton;

    // Aba finalizar comanda
    @FXML private TextField codigoComandaField;
    @FXML private TableView<ItemPedidoRow> comandaItensTable;
    @FXML private TableColumn<ItemPedidoRow, String> comandaProdutoColumn;
    @FXML private TableColumn<ItemPedidoRow, Integer> comandaQuantidadeColumn;
    @FXML private TableColumn<ItemPedidoRow, String> comandaPrecoColumn;
    @FXML private Label totalComandaLabel;
    @FXML private ComboBox<String> formaPagamentoFinalComboBox;

    private ComandaDetalhadaResponseDTO comandaAtual;

    @FXML
    public void initialize() {
        produtoColumn.setCellValueFactory(new PropertyValueFactory<>("nomeProduto"));
        quantidadeColumn.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        precoColumn.setCellValueFactory(new PropertyValueFactory<>("precoFormatado"));
        observacaoColumn.setCellValueFactory(new PropertyValueFactory<>("observacao"));

        comandaProdutoColumn.setCellValueFactory(new PropertyValueFactory<>("nomeProduto"));
        comandaQuantidadeColumn.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        comandaPrecoColumn.setCellValueFactory(new PropertyValueFactory<>("precoFormatado"));

        formaPagamentoComboBox.getItems().addAll("PIX", "CARTAO", "DINHEIRO");
        formaPagamentoFinalComboBox.getItems().addAll("PIX", "CARTAO", "DINHEIRO");

        retiradaToggleGroup = new ToggleGroup();
        retiradaImediataRadio.setToggleGroup(retiradaToggleGroup);
        retiradaAgendadaRadio.setToggleGroup(retiradaToggleGroup);
        entregaRadio.setToggleGroup(retiradaToggleGroup);

        // defaults (opcional)
        retiradaImediataRadio.setSelected(true);

        carregarProdutos();
    }

    private void carregarProdutos() {
        try {
            ProdutoDTO[] produtos = produtoApi.listarTodos();
            produtosDisponiveis = produtos != null ? Arrays.asList(produtos) : new ArrayList<>();
            produtoComboBox.getItems().clear();
            for (ProdutoDTO p : produtosDisponiveis) {
                produtoComboBox.getItems().add(p.nome);
            }
        } catch (Exception e) {
            mostrarErro("Erro ao carregar produtos do servidor.");
            e.printStackTrace();
        }
    }

    // =========================================================
    // CLIENTE
    // =========================================================

    @FXML
    private void onBuscarCliente(ActionEvent event) {
        String telefone = telefoneField.getText();
        if (telefone == null || telefone.isBlank()) {
            mostrarErro("Informe o telefone.");
            return;
        }

        try {
            ClienteDTO c = clienteApi.buscarPorTelefone(telefone);

            if (c != null && c.id != null) {
                clienteAtual = c;
                nomeField.setText(clienteAtual.nome);
                cepField.setText(clienteAtual.cep);
                enderecoField.setText(clienteAtual.enderecoCompleto);
            } else {
                clienteAtual = null;
                mostrarErro("Cliente não encontrado. Preencha os dados para criar novo.");
            }
        } catch (Exception e) {
            clienteAtual = null;
            mostrarErro("Cliente não encontrado. Preencha os dados para criar novo.");
        }
    }

    @FXML
    private void onCriarNovoCliente(ActionEvent event) {
        String nome = nomeField.getText();
        String telefone = telefoneField.getText();
        String cep = cepField.getText();
        String endereco = enderecoField.getText();

        if (nome == null || nome.isBlank() ||
                telefone == null || telefone.isBlank() ||
                cep == null || cep.isBlank() ||
                endereco == null || endereco.isBlank()) {
            mostrarErro("Preencha todos os campos para cadastrar o cliente.");
            return;
        }

        ClienteDTO novo = new ClienteDTO();
        novo.nome = nome;
        novo.telefone = telefone;
        novo.cep = cep;
        novo.enderecoCompleto = endereco;

        try {
            ClienteDTO criado = clienteApi.criar(novo);
            clienteAtual = criado;
            mostrarSucesso("Novo cliente criado com sucesso!");
        } catch (Exception e) {
            mostrarErro("Erro ao criar o cliente no servidor.");
            e.printStackTrace();
        }
    }

    // =========================================================
    // ITENS DO PEDIDO (tabela)
    // =========================================================

    @FXML
    private void onAdicionarItem(ActionEvent event) {
        String nomeProduto = produtoComboBox.getValue();
        String textoQuantidade = quantidadeField.getText();
        String observacao = campoObservacaoItem.getText();

        if (nomeProduto == null || textoQuantidade == null || textoQuantidade.isBlank()) {
            mostrarErro("Selecione um produto e informe a quantidade.");
            return;
        }

        int quantidade;
        try {
            quantidade = Integer.parseInt(textoQuantidade);
        } catch (NumberFormatException e) {
            mostrarErro("Quantidade inválida.");
            return;
        }

        ProdutoDTO produto = buscarProdutoPorNome(nomeProduto);
        if (produto == null) {
            mostrarErro("Produto não encontrado.");
            return;
        }

        ItemPedidoRow item = new ItemPedidoRow(
                produto.id,
                produto.nome,
                quantidade,
                produto.preco,
                observacao
        );

        itensTable.getItems().add(item);

        produtoComboBox.setValue(null);
        quantidadeField.clear();
        campoObservacaoItem.clear();
    }

    private ProdutoDTO buscarProdutoPorNome(String nomeProduto) {
        for (ProdutoDTO produto : produtosDisponiveis) {
            if (produto.nome != null && produto.nome.equalsIgnoreCase(nomeProduto)) {
                return produto;
            }
        }
        return null;
    }

    // =========================================================
    // CRIAR PEDIDO NORMAL (CAIXA -> /caixa/pedidos)
    // =========================================================

    @FXML
    private void onCriarPedido(ActionEvent event) {
        if (clienteAtual == null || clienteAtual.id == null) {
            mostrarErro("Busque um cliente válido antes de criar o pedido.");
            return;
        }

        if (itensTable.getItems().isEmpty()) {
            mostrarErro("Adicione ao menos um item.");
            return;
        }

        PedidoRequestDTO pedido = new PedidoRequestDTO();
        pedido.clienteId = clienteAtual.id;

        String formaPagamentoStr = formaPagamentoComboBox.getValue();
        if (formaPagamentoStr == null) {
            mostrarErro("Selecione uma forma de pagamento.");
            return;
        }
        pedido.formaPagamento = formaPagamentoStr.toUpperCase();

        RadioButton selecionado = (RadioButton) retiradaToggleGroup.getSelectedToggle();
        if (selecionado == null) {
            mostrarErro("Selecione o tipo: retirada imediata, retirada agendada ou entrega.");
            return;
        }

        // ✅ Mapeamento correto dos 3 radios
        if (selecionado == retiradaImediataRadio) {
            pedido.tipoEntrega = "IMEDIATA";
            pedido.paraEntrega = false;
            pedido.enderecoEntrega = null;
            pedido.dataRetirada = null;

        } else if (selecionado == retiradaAgendadaRadio) {
            pedido.tipoEntrega = "RETIRADA";
            pedido.paraEntrega = false;
            pedido.enderecoEntrega = null;

            LocalDate data = dataRetiradaPicker.getValue();
            String horaTexto = horaRetiradaField.getText();

            if (data == null || horaTexto == null || horaTexto.isBlank()) {
                mostrarErro("Informe a data e a hora da retirada.");
                return;
            }

            try {
                LocalTime hora = LocalTime.parse(horaTexto); // HH:mm
                LocalDateTime dataHora = LocalDateTime.of(data, hora);
                pedido.dataRetirada = dataHora;
            } catch (DateTimeParseException e) {
                mostrarErro("Hora inválida. Use o formato HH:mm.");
                return;
            }

        } else if (selecionado == entregaRadio) {
            pedido.tipoEntrega = "ENTREGA";
            pedido.paraEntrega = true;

            String endereco = enderecoField.getText();
            if (endereco == null || endereco.isBlank()) {
                mostrarErro("Informe o endereço de entrega.");
                return;
            }
            pedido.enderecoEntrega = endereco;

            // se você já tiver distância no DTO, você preenche aqui (ex: pedido.distanciaEntregaKm)
            // senão deixa o backend calcular depois.

        } else {
            mostrarErro("Tipo de entrega inválido.");
            return;
        }

        List<PedidoItemRequestDTO> itens = new ArrayList<>();
        for (ItemPedidoRow item : itensTable.getItems()) {
            itens.add(new PedidoItemRequestDTO(
                    item.getProdutoId(),
                    item.getQuantidade(),
                    item.getObservacao()
            ));
        }
        pedido.itens = itens;

        try {
            // ✅ agora chama /caixa/pedidos
            caixaApi.criarPedidoNormal(pedido);

            mostrarSucesso("Pedido criado com sucesso!");
            limparCamposPedido();
        } catch (Exception e) {
            mostrarErro("Erro ao criar pedido no servidor.");
            e.printStackTrace();
        }
    }

    // =========================================================
    // COMANDA (aba finalizar comanda)
    // =========================================================

    @FXML
    private void onBuscarComanda(ActionEvent event) {
        String codigo = codigoComandaField.getText();
        if (codigo == null || codigo.isBlank()) {
            mostrarErro("Informe o código da comanda.");
            return;
        }

        try {
            // ✅ detalhes inclui itens
            comandaAtual = caixaApi.detalhesComanda(codigo.trim());

            comandaItensTable.getItems().clear();

            if (comandaAtual != null && comandaAtual.pedidos != null) {
                for (ComandaDetalhadaResponseDTO.PedidoDTO p : comandaAtual.pedidos) {
                    if (p.itens == null) continue;

                    for (ComandaDetalhadaResponseDTO.ItemDTO i : p.itens) {
                        ItemPedidoRow row = new ItemPedidoRow(
                                null, // sem produtoId no detalhe (ok)
                                i.nomeProduto,
                                i.quantidade != null ? i.quantidade : 0,
                                i.preco,
                                null
                        );
                        comandaItensTable.getItems().add(row);
                    }
                }
            }

            String totalStr = (comandaAtual != null && comandaAtual.total != null)
                    ? "R$ " + comandaAtual.total
                    : "R$ 0.00";
            totalComandaLabel.setText(totalStr);

            mostrarSucesso("Comanda carregada: " + (comandaAtual != null ? comandaAtual.codigo : codigo));

        } catch (Exception e) {
            mostrarErro("Erro ao buscar comanda no servidor.");
            e.printStackTrace();
        }
    }

    @FXML
    private void onConfirmarPagamento(ActionEvent event) {
        String codigo = codigoComandaField.getText();
        if (codigo == null || codigo.isBlank()) {
            mostrarErro("Informe o código da comanda.");
            return;
        }

        String forma = formaPagamentoFinalComboBox.getValue();
        if (forma == null || forma.isBlank()) {
            mostrarErro("Selecione a forma de pagamento.");
            return;
        }

        try {
            // ⚠️ depende do seu PagamentoComandaDTO real (desktop)
            PagamentoComandaDTO dto = new PagamentoComandaDTO();
            dto.formaPagamento = forma.toUpperCase();

            caixaApi.pagarComanda(codigo.trim(), dto);

            mostrarSucesso("Pagamento enviado com sucesso!");
            limparCamposComanda();

        } catch (Exception e) {
            mostrarErro("Erro ao confirmar pagamento no servidor.");
            e.printStackTrace();
        }
    }

    private void limparCamposComanda() {
        codigoComandaField.clear();
        comandaItensTable.getItems().clear();
        totalComandaLabel.setText("R$ 0.00");
        formaPagamentoFinalComboBox.getSelectionModel().clearSelection();
        comandaAtual = null;
    }

    @FXML
    private void onTipoEntregaSelecionado(ActionEvent event) {
        // UX: habilita/desabilita campos de data/hora conforme rádio
        RadioButton selecionado = (RadioButton) retiradaToggleGroup.getSelectedToggle();

        boolean agendada = selecionado == retiradaAgendadaRadio;
        dataRetiradaPicker.setDisable(!agendada);
        horaRetiradaField.setDisable(!agendada);

        boolean entrega = selecionado == entregaRadio;
        enderecoField.setDisable(!entrega);
    }

    // =========================================================
    // LOGOUT
    // =========================================================

    @FXML
    private void deslogar() {
        try {
            ctx.clearSession();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/auth/login.fxml"));
            loader.setControllerFactory(type -> {
                try {
                    if (type == com.oficinadafesta.ui.auth.AuthController.class) {
                        return type.getConstructor(com.oficinadafesta.app.AppContext.class).newInstance(ctx);
                    }
                    return type.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Parent root = loader.load();

            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setFullScreen(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Erro ao deslogar.");
        }
    }

    // =========================================================
    // Alerts / limpeza
    // =========================================================

    private void mostrarErro(String msg) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Erro");
        alerta.setHeaderText(null);
        alerta.setContentText(msg);
        alerta.showAndWait();
    }

    private void mostrarSucesso(String msg) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Sucesso");
        alerta.setHeaderText(null);
        alerta.setContentText(msg);
        alerta.showAndWait();
    }

    private void limparCamposPedido() {
        telefoneField.clear();
        nomeField.clear();
        cepField.clear();
        enderecoField.clear();
        produtoComboBox.setValue(null);
        quantidadeField.clear();
        itensTable.getItems().clear();
        formaPagamentoComboBox.getSelectionModel().clearSelection();
        retiradaToggleGroup.selectToggle(null);
        dataRetiradaPicker.setValue(null);
        horaRetiradaField.clear();
        campoObservacaoItem.clear();
        clienteAtual = null;
    }
}