package com.oficinadafesta.login;

import com.oficinadafesta.config.SpringFXMLLoader;
import com.oficinadafesta.enums.AreaTipo;
import jakarta.annotation.PostConstruct;
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
import org.springframework.stereotype.Controller;

@Controller
public class LoginController {

    @FXML
    private TextField usuarioField;

    @FXML
    private PasswordField senhaField;

    @FXML
    private Label lblErroLogin;

    @Autowired
    private LoginService loginService;

    @Autowired
    private SpringFXMLLoader springFXMLLoader;  // Injetando o SpringFXMLLoader

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
            // Definindo o caminho do FXML com base no setor
            String fxmlPath = "";
            switch (setor) {
                case COMUNICACAO:
                    fxmlPath = "com/oficinadafesta/login/comunicacaoScreen.fxml";
                    break;
                case CAFE:
                    fxmlPath = "com/oficinadafesta/login/cafeScreen.fxml";
                    break;
                case CONFEITARIA:
                    fxmlPath = "com/oficinadafesta/login/confeitariaScreen.fxml";
                    break;
                case CAIXA:
                    fxmlPath = "com/oficinadafesta/login/caixaScreen.fxml";
                    break;
                case PRODUCAO_DOCINHOS:
                    fxmlPath = "com/oficinadafesta/login/docinhosScreen.fxml";
                    break;
                case PRODUCAO_SALGADOS:
                    fxmlPath = "com/oficinadafesta/login/salgadosScreen.fxml";
                    break;
                case FRITURA:
                    fxmlPath = "com/oficinadafesta/login/frituraScreen.fxml";
                    break;
                case SOBREMESAS:
                    fxmlPath = "com/oficinadafesta/login/sobremesaScreen.fxml";
                    break;
            }

            // Carregando a tela
            if (!fxmlPath.isEmpty()) {
                root = springFXMLLoader.load(fxmlPath); // Usando o caminho correto
                if (root != null) {
                    Stage stage = (Stage) usuarioField.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            lblErroLogin.setText("Falha ao abrir a tela do setor.");
        }
    }
}


