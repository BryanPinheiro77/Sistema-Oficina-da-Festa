package com.oficinadafesta.front.controller;

import com.oficinadafesta.dto.ItemPedidoDTO;
import com.oficinadafesta.pedido.ItemPedido;
import com.oficinadafesta.pedido.PedidoService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.Data;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

@Data
public class CaixaController {

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
    }

    @FXML
    private void onBuscarCliente(ActionEvent event){
       // TODO: buscar cliente por telefone
    }

    @FXML
    private void onAdicionarItem(ActionEvent event){
        // TODO: adicionar item a tabela de itens
    }

    @FXML
    private void onCriarPedido(ActionEvent event){
        // TODO: montar pedido e enviar para backend
    }

    @FXML
    private void onBuscarComanda(ActionEvent event) {
        // TODO: buscar itens da comanda
    }

    @FXML
    private void onConfirmarPagamento(ActionEvent event) {
        // TODO: confirmar pagamento e limpar comanda
    }
}
