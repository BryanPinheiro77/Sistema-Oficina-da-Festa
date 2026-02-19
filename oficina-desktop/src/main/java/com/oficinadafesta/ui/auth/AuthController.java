package com.oficinadafesta.ui.auth;

import com.oficinadafesta.app.AppContext;
import com.oficinadafesta.ui.caixa.CaixaController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

public class AuthController {

    private final AppContext ctx;

    public AuthController(AppContext ctx) {
        this.ctx = ctx;
    }

    @FXML private TextField usuarioField;
    @FXML private PasswordField senhaField;
    @FXML private Label lblErroLogin;

    @FXML
    private void handleLogin(ActionEvent event) {
        String usuario = usuarioField.getText();
        String senha = senhaField.getText();

        if (usuario == null || usuario.isBlank() || senha == null || senha.isBlank()) {
            lblErroLogin.setText("Usuário ou senha não podem estar vazios.");
            return;
        }

        try {
            String setor = ctx.authApi.login(usuario, senha);
            String setorNorm = setor == null ? "" : setor.replace("\"", "").trim().toUpperCase();

            if (setorNorm.isBlank()) {
                lblErroLogin.setText("Usuário ou senha inválidos.");
                return;
            }

            lblErroLogin.setText("");
            redirecionarParaTelaPorSetor(setorNorm);

        } catch (Exception e) {
            lblErroLogin.setText("Erro ao conectar no servidor.");
            e.printStackTrace();
        }
    }

    private void redirecionarParaTelaPorSetor(String setorNorm) throws Exception {
        String fxmlPath = switch (setorNorm) {
            case "CAIXA" -> "/ui/setores/caixaScreen.fxml";
            case "CAFE" -> "/ui/setores/cafeScreen.fxml";
            case "COMUNICACAO" -> "/ui/setores/comunicacaoScreen.fxml";
            case "CONFEITARIA" -> "/ui/setores/confeitariaScreen.fxml";
            case "PRODUCAO_DOCINHOS" -> "/ui/setores/docinhosScreen.fxml";
            case "PRODUCAO_SALGADOS" -> "/ui/setores/salgadosScreen.fxml";
            case "FRITURA" -> "/ui/setores/frituraScreen.fxml";
            case "SOBREMESAS" -> "/ui/setores/sobremesaScreen.fxml";
            default -> null;
        };

        if (fxmlPath == null) {
            lblErroLogin.setText("Setor não reconhecido: " + setorNorm);
            return;
        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));

        if ("CAIXA".equals(setorNorm)) {
            fxmlLoader.setControllerFactory(type -> {
                if (type == CaixaController.class) {
                    return new CaixaController(ctx.clienteApi, ctx.produtoApi, ctx.pedidoApi);
                }
                try {
                    return type.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        Parent root = fxmlLoader.load();

        Stage stage = (Stage) usuarioField.getScene().getWindow();
        Scene novaCena = new Scene(root);

        novaCena.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.F11) stage.setFullScreen(!stage.isFullScreen());
        });

        stage.setScene(novaCena);
        stage.setFullScreen(true);
        stage.show();
    }
}
