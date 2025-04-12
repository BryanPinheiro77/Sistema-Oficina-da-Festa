package com.oficinadafesta.login;

import com.oficinadafesta.enums.AreaTipo;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import javafx.scene.Parent;


public class LoginController {

    @FXML
    private TextField usuarioField;

    @FXML
    private PasswordField senhaField;

    @FXML
    private Label lblErroLogin;

    @Autowired
    private LoginService loginService;

    @FXML
    private void handleLogin(ActionEvent event) {
        String usuario = usuarioField.getText();
        String senha = senhaField.getText();

        if (usuario.isEmpty() || senha.isEmpty()) {
            lblErroLogin.setText("Usuário ou senha não podem estar vazios.");
        } else {
            AreaTipo setor = loginService.autenticar(usuario, senha);
            if (setor != null) {
                lblErroLogin.setText(""); // Limpa a mensagem de erro
                // Redireciona para a tela específica
                redirecionarParaTelaPorSetor(setor);
            } else {
                lblErroLogin.setText("Usuário ou senha inválidos.");
            }
        }
    }

    private void redirecionarParaTelaPorSetor(AreaTipo setor) {
        try {
            Parent root = null;

            switch (setor) {
                case COMUNICACAO:
                    root = FXMLLoader.load(getClass().getResource("/fxml/comunicacaoScreen.fxml"));
                    break;
                case CAFE:
                    root = FXMLLoader.load(getClass().getResource("/fxml/cafeScreen.fxml"));
                    break;
                case CONFEITARIA:
                    root = FXMLLoader.load(getClass().getResource("/fxml/confeitariaScreen.fxml"));
                    break;
                case CAIXA:
                    root = FXMLLoader.load(getClass().getResource("/fxml/caixaScreen.fxml"));
                    break;
                case PRODUCAO_DOCINHOS:
                    root = FXMLLoader.load(getClass().getResource("/fxml/docinhosScreen.fxml"));
                    break;
                case PRODUCAO_SALGADOS:
                    root = FXMLLoader.load(getClass().getResource("/fxml/salgadosScreen.fxml"));
                    break;
                case FRITURA:
                    root = FXMLLoader.load(getClass().getResource("/fxml/frituraScreen.fxml"));
                    break;
                case SOBREMESAS:
                    root = FXMLLoader.load(getClass().getResource("/fxml/sobremesaScreen.fxml"));
                    break;

            }
         if (root != null) {
    Stage stage = (Stage) usuarioField.getScene().getWindow();
    stage.setScene(new Scene(root));
    stage.show();
        }
        } catch (Exception e){
            e.printStackTrace();
            lblErroLogin.setText("Falha ao abrir a tela do setor.");
        }
    }
}