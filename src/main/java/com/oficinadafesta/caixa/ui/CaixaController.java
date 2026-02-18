package com.oficinadafesta.caixa.ui;

import com.oficinadafesta.caixa.dto.ItemPedidoRow;
import com.oficinadafesta.cliente.domain.Cliente;
import com.oficinadafesta.cliente.service.ClienteService;
import com.oficinadafesta.enums.FormaPagamento;
import com.oficinadafesta.enums.TipoEntrega;
import com.oficinadafesta.login.UsuarioLogado;
import com.oficinadafesta.pedido.dto.PedidoItemRequestDTO;
import com.oficinadafesta.pedido.dto.PedidoRequestDTO;
import com.oficinadafesta.pedido.service.PedidoService;
import com.oficinadafesta.produto.domain.Produto;
import com.oficinadafesta.produto.service.ProdutoService;
import com.oficinadafesta.shared.config.SpringFXMLLoader;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@Component
public class CaixaController {

    private final ClienteService clienteService;
    private final ProdutoService produtoService;
    private final PedidoService pedidoService;

    private Cliente clienteAtual;
    private List<Produto> produtosDisponiveis;

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

    @Autowired
    private SpringFXMLLoader springFXMLLoader;

    @FXML private Button logoutButton;
    @FXML private Label lblErroLogin;

    // Aba finalizar comanda (mantive como estava; depois a gente alinha com DTO específico da comanda)
    @FXML private TextField codigoComandaField;
    @FXML private TableView<ItemPedidoRow> comandaItensTable;
    @FXML private TableColumn<ItemPedidoRow, String> comandaProdutoColumn;
    @FXML private TableColumn<ItemPedidoRow, Integer> comandaQuantidadeColumn;
    @FXML private TableColumn<ItemPedidoRow, String> comandaPrecoColumn;
    @FXML private Label totalComandaLabel;
    @FXML private ComboBox<String> formaPagamentoFinalComboBox;

    @FXML
    public void initialize() {
        // Mantido por enquanto, como tu pediu
        UsuarioLogado.logout();

        produtoColumn.setCellValueFactory(new PropertyValueFactory<>("nomeProduto"));
        quantidadeColumn.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        precoColumn.setCellValueFactory(new PropertyValueFactory<>("precoFormatado"));
        observacaoColumn.setCellValueFactory(new PropertyValueFactory<>("observacao"));

        comandaProdutoColumn.setCellValueFactory(new PropertyValueFactory<>("nomeProduto"));
        comandaQuantidadeColumn.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        comandaPrecoColumn.setCellValueFactory(new PropertyValueFactory<>("precoFormatado"));

        produtosDisponiveis = produtoService.listarTodos();
        for (Produto p : produtosDisponiveis) {
            produtoComboBox.getItems().add(p.getNome());
        }

        formaPagamentoComboBox.getItems().addAll("PIX", "CARTAO", "DINHEIRO");
        formaPagamentoFinalComboBox.getItems().addAll("PIX", "CARTAO", "DINHEIRO");

        retiradaToggleGroup = new ToggleGroup();
        retiradaImediataRadio.setToggleGroup(retiradaToggleGroup);
        retiradaAgendadaRadio.setToggleGroup(retiradaToggleGroup);
        entregaRadio.setToggleGroup(retiradaToggleGroup);
    }

    @FXML
    private void onBuscarCliente(ActionEvent event) {
        String telefone = telefoneField.getText();
        Optional<Cliente> clienteOptional = clienteService.buscarPorTelefone(telefone);

        if (clienteOptional.isPresent()) {
            clienteAtual = clienteOptional.get();
            nomeField.setText(clienteAtual.getNome());
            cepField.setText(clienteAtual.getCep());
            enderecoField.setText(clienteAtual.getEnderecoCompleto());
        } else {
            clienteAtual = null;
            mostrarErro("Cliente não encontrado. Preencha os dados para criar novo.");
        }
    }

    @FXML
    private void onAdicionarItem(ActionEvent event) {
        String nomeProduto = produtoComboBox.getValue();
        String textoQuantidade = quantidadeField.getText();
        String observacao = campoObservacaoItem.getText();

        if (nomeProduto == null || textoQuantidade.isEmpty()) {
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

        Produto produto = buscarProdutoPorNome(nomeProduto);
        if (produto == null) {
            mostrarErro("Produto não encontrado.");
            return;
        }

        ItemPedidoRow item = new ItemPedidoRow(
                produto.getId(),
                produto.getNome(),
                quantidade,
                produto.getPreco(),
                observacao
        );

        itensTable.getItems().add(item);

        produtoComboBox.setValue(null);
        quantidadeField.clear();
        campoObservacaoItem.clear();
    }

    @FXML
    private void onCriarPedido(ActionEvent event) {
        if (clienteAtual == null) {
            mostrarErro("Busque um cliente válido antes de criar o pedido.");
            return;
        }

        if (itensTable.getItems().isEmpty()) {
            mostrarErro("Adicione ao menos um item.");
            return;
        }

        PedidoRequestDTO pedido = new PedidoRequestDTO();
        pedido.setClienteId(clienteAtual.getId());

        String formaPagamentoStr = formaPagamentoComboBox.getValue();
        if (formaPagamentoStr == null) {
            mostrarErro("Selecione uma forma de pagamento");
            return;
        }
        pedido.setFormaPagamento(FormaPagamento.valueOf(formaPagamentoStr.toUpperCase()));

        // ⚠️ Aqui teu código original tinha lógica baseada em texto "Agendar"
        // Vou manter a estrutura, mas recomendo mudar para usar os RadioButtons diretamente.
        RadioButton selecionado = (RadioButton) retiradaToggleGroup.getSelectedToggle();
        boolean agendar = selecionado != null && selecionado == retiradaAgendadaRadio;

        if (agendar) {
            pedido.setTipoEntrega(TipoEntrega.RETIRADA);
            pedido.setParaEntrega(false);

            LocalDate data = dataRetiradaPicker.getValue();
            String horaTexto = horaRetiradaField.getText();

            if (data == null || horaTexto == null || horaTexto.isBlank()) {
                mostrarErro("Informe a data e a hora da retirada.");
                return;
            }

            try {
                LocalTime hora = LocalTime.parse(horaTexto); // HH:mm
                LocalDateTime dataHora = LocalDateTime.of(data, hora);
                pedido.setDataRetirada(dataHora);
            } catch (DateTimeParseException e) {
                mostrarErro("Hora inválida. Use o formato HH:mm.");
                return;
            }
        } else {
            pedido.setTipoEntrega(TipoEntrega.ENTREGA);
            pedido.setParaEntrega(true);
            pedido.setEnderecoEntrega(enderecoField.getText());
        }

        List<PedidoItemRequestDTO> itens = new ArrayList<>();
        for (ItemPedidoRow item : itensTable.getItems()) {
            itens.add(new PedidoItemRequestDTO(
                    item.getProdutoId(),
                    item.getQuantidade(),
                    item.getObservacao()
            ));
        }
        pedido.setItens(itens);

        pedidoService.criarPedido(pedido);
        mostrarSucesso("Pedido criado com sucesso!");
        limparCamposPedido();
    }

    private Produto buscarProdutoPorNome(String nomeProduto) {
        for (Produto produto : produtosDisponiveis) {
            if (produto.getNome().equalsIgnoreCase(nomeProduto)) {
                return produto;
            }
        }
        return null;
    }

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
        produtoComboBox.getEditor().clear();
        quantidadeField.clear();
        itensTable.getItems().clear();
        formaPagamentoComboBox.getSelectionModel().clearSelection();
        retiradaToggleGroup.selectToggle(null);
        dataRetiradaPicker.setValue(null);
        horaRetiradaField.clear();
    }

    @FXML
    private void onBuscarComanda(ActionEvent event) {
        mostrarSucesso("Comanda encontrada!");
    }

    @FXML
    private void onConfirmarPagamento(ActionEvent event) {
        mostrarSucesso("Pagamento confirmado com sucesso!");
    }

    @FXML
    private void onCriarNovoCliente(ActionEvent event) {
        String nome = nomeField.getText();
        String telefone = telefoneField.getText();
        String cep = cepField.getText();
        String endereco = enderecoField.getText();

        if (nome.isEmpty() || telefone.isEmpty() || cep.isEmpty() || endereco.isEmpty()) {
            mostrarErro("Preencha todos os campos para cadastrar o cliente.");
            return;
        }

        Optional<Cliente> clienteExistente = clienteService.buscarPorTelefone(telefone);
        if (clienteExistente.isPresent()) {
            mostrarErro("Cliente já cadastrado com esse telefone.");
            return;
        }

        Cliente novoCliente = new Cliente();
        novoCliente.setNome(nome);
        novoCliente.setTelefone(telefone);
        novoCliente.setCep(cep);
        novoCliente.setEnderecoCompleto(endereco);

        try {
            clienteService.salvar(novoCliente);
            clienteAtual = novoCliente;
            mostrarSucesso("Novo cliente criado com sucesso!");
        } catch (Exception e) {
            mostrarErro("Erro ao criar o cliente: " + e.getMessage());
        }
    }

    @FXML
    private void onTipoEntregaSelecionado(ActionEvent event) {
        // depois a gente melhora esse fluxo
    }

    @FXML
    private void deslogar() {
        UsuarioLogado.logout();

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ui/auth/login.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setFullScreen(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
