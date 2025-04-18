package com.oficinadafesta.front.controller;

import com.oficinadafesta.cliente.Cliente;
import com.oficinadafesta.cliente.ClienteService;
import com.oficinadafesta.dto.ItemPedidoDTO;
import com.oficinadafesta.dto.PedidoRequestDTO;
import com.oficinadafesta.enums.FormaPagamento;
import com.oficinadafesta.enums.TipoEntrega;
import com.oficinadafesta.pedido.ItemPedido;
import com.oficinadafesta.pedido.PedidoService;
import com.oficinadafesta.produto.Produto;
import com.oficinadafesta.produto.ProdutoService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.Data;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.springframework.stereotype.Component;

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

    //Aba novo pedido
    @FXML private TextField telefoneField, nomeField, cepField, enderecoField;
    @FXML private ComboBox<String> produtoComboBox;
    @FXML private TextField quantidadeField;
    @FXML private TableView<ItemPedidoDTO> itensTable;
    @FXML private TableColumn<ItemPedidoDTO, String> produtoColumn;
    @FXML private TableColumn<ItemPedidoDTO, Integer> quantidadeColumn;
    @FXML private TableColumn<ItemPedidoDTO, String> precoColumn;
    @FXML private ComboBox<String> formaPagamentoComboBox;
    @FXML private ToggleGroup retiradaToggleGroup;
    @FXML private DatePicker dataRetiradaPicker;
    @FXML private TextField horaRetiradaField;
    @FXML private RadioButton retiradaImediataRadio;
    @FXML private RadioButton retiradaAgendadaRadio;
    @FXML private RadioButton entregaRadio;

    // Aba finalizar comanda
    @FXML private TextField codigoComandaField;
    @FXML private TableView<ItemPedidoDTO> comandaItensTable;
    @FXML private TableColumn<ItemPedidoDTO, String> comandaProdutoColumn;
    @FXML private TableColumn<ItemPedidoDTO, Integer> comandaQuantidadeColumn;
    @FXML private TableColumn<ItemPedidoDTO, String> comandaPrecoColumn;
    @FXML private Label totalComandaLabel;
    @FXML private ComboBox<String> formaPagamentoFinalComboBox;




    @FXML
    public void initialize(){
        produtoColumn.setCellValueFactory(new PropertyValueFactory<>("nomeProduto"));
        quantidadeColumn.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        precoColumn.setCellValueFactory(new PropertyValueFactory<>("precoFormatado"));

        comandaProdutoColumn.setCellValueFactory(new PropertyValueFactory<>("nomeProduto"));
        comandaQuantidadeColumn.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        comandaPrecoColumn.setCellValueFactory(new PropertyValueFactory<>("precoFormatado"));

        produtosDisponiveis = produtoService.listarTodos();
        for(Produto p : produtosDisponiveis){
            produtoComboBox.getItems().add(p.getNome());
        }

        formaPagamentoComboBox.getItems().addAll("PIX", "CARTAO", "DINHEIRO");
        formaPagamentoFinalComboBox.getItems().addAll("PIX", "CARTAO", "DINHEIRO");
    }

    @FXML
    private void onBuscarCliente(ActionEvent event) {
        String telefone = telefoneField.getText();

        // Usando Optional corretamente
        Optional<Cliente> clienteOptional = clienteService.buscarPorTelefone(telefone);
        if (clienteOptional.isPresent()) {
            clienteAtual = clienteOptional.get(); // Obter cliente se presente
            nomeField.setText(clienteAtual.getNome());
            cepField.setText(clienteAtual.getCep());
            enderecoField.setText(clienteAtual.getEnderecoCompleto());
        } else {
            clienteAtual = null;
            mostrarErro("Cliente não encontrado. Preencha os dados para criar novo.");
        }
    }


    @FXML
    private void onAdicionarItem(ActionEvent event){
        String nomeProduto = produtoComboBox.getValue();
        int quantidade = Integer.parseInt(quantidadeField.getText());

        Produto produto = buscarProdutoPorNome(nomeProduto);
        if(produto == null) {
            mostrarErro("Produto não encontrado!");
            return;
        }

        ItemPedidoDTO item = new ItemPedidoDTO();
        item.setProdutoId(produto.getId());
        item.setNomeProduto(produto.getNome());
        item.setQuantidade(quantidade);
        item.setPreco(produto.getPreco());

        itensTable.getItems().add(item);


        quantidadeField.clear();
        produtoComboBox.getEditor().clear();
    }



    @FXML
    private void onCriarPedido(ActionEvent event){
        if (clienteAtual == null){
            mostrarErro("Busque um cliente válido antes de criar o pedido.");
            return;
        }

        if (itensTable.getItems().isEmpty()){
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

        RadioButton selecionado = (RadioButton) retiradaToggleGroup.getSelectedToggle();
        boolean agendar = selecionado != null && "Agendar".equalsIgnoreCase(selecionado.getText());

        if (agendar){
            pedido.setTipoEntrega(TipoEntrega.RETIRADA);
            pedido.setParaEntrega(false);
            LocalDate data = dataRetiradaPicker.getValue();
            String horaTexto = horaRetiradaField.getText(); // Exemplo: "14:30"

            if (data == null || horaTexto == null || horaTexto.isBlank()) {
                mostrarErro("Informe a data e a hora da retirada.");
                return;
            }

            try {
                LocalTime hora = LocalTime.parse(horaTexto); // Formato esperado: HH:mm
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

        // Convertendo os itens para PedidoItemDTO
        List<PedidoRequestDTO.PedidoItemDTO> itens = new ArrayList<>();
        for (ItemPedidoDTO item : itensTable.getItems()) {
            PedidoRequestDTO.PedidoItemDTO dto = new PedidoRequestDTO.PedidoItemDTO();
            dto.setProdutoId(item.getProdutoId());
            dto.setQuantidade(item.getQuantidade());
            itens.add(dto);
        }

        pedido.setItens(itens);

        pedidoService.criarPedido(pedido);
        mostrarSucesso("Pedido criado com sucesso!");
        limparCamposPedido();
    }

    // Método para buscar produto por nome
    private Produto buscarProdutoPorNome(String nomeProduto) {
        for (Produto produto : produtosDisponiveis) {
            if (produto.getNome().equalsIgnoreCase(nomeProduto)) {
                return produto;
            }
        }
        return null;
    }

    // Exibe a mensagem de erro
    private void mostrarErro(String msg) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Erro");
        alerta.setHeaderText(null);
        alerta.setContentText(msg);
        alerta.showAndWait();
    }

    // Exibe a mensagem de sucesso
    private void mostrarSucesso(String msg) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Sucesso");
        alerta.setHeaderText(null);
        alerta.setContentText(msg);
        alerta.showAndWait();
    }

    // Limpa os campos após criar o pedido
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
        String codigoComanda = codigoComandaField.getText();
        mostrarSucesso("Comanda encontrada!");
    }

    @FXML
    private void onConfirmarPagamento(ActionEvent event) {
        mostrarSucesso("Pagamento confirmado com sucesso!");
    }
    @FXML
    private void onCriarNovoCliente(ActionEvent event) {
        // Verifica se os campos de cliente estão preenchidos
        String nome = nomeField.getText();
        String telefone = telefoneField.getText();
        String cep = cepField.getText();
        String endereco = enderecoField.getText();

        if (nome.isEmpty() || telefone.isEmpty() || cep.isEmpty() || endereco.isEmpty()) {
            mostrarErro("Preencha todos os campos para cadastrar o cliente.");
            return;
        }

        // Cria um novo cliente
        Cliente novoCliente = new Cliente();
        novoCliente.setNome(nome);
        novoCliente.setTelefone(telefone);
        novoCliente.setCep(cep);
        novoCliente.setEnderecoCompleto(endereco);

        // Salva o cliente usando o ClienteService
        try {
            clienteService.salvar(novoCliente); // Supondo que exista um método para salvar o cliente
            clienteAtual = novoCliente; // Define o cliente atual para o novo cliente criado
            mostrarSucesso("Novo cliente criado com sucesso!");
        } catch (Exception e) {
            mostrarErro("Erro ao criar o cliente: " + e.getMessage());
        }
    }

    @FXML
    private void onTipoEntregaSelecionado(ActionEvent event) {
        if (retiradaImediataRadio.isSelected()) {
            // Lógica para retirada imediata
        } else if (retiradaAgendadaRadio.isSelected()) {
            // Lógica para retirada agendada
        } else if (entregaRadio.isSelected()) {
            // Lógica para entrega
        }
    }

}