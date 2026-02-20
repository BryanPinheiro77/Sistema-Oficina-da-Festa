package com.oficinadafesta.ui.auth;

import com.oficinadafesta.api.dto.AuthResponseDTO;
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
            AuthResponseDTO resp = ctx.authApi.login(usuario, senha);

            if (resp == null || resp.accessToken == null || resp.accessToken.isBlank()) {
                lblErroLogin.setText("Usuário ou senha inválidos.");
                return;
            }

            // salva sessão
            ctx.accessToken = resp.accessToken;
            ctx.setor = resp.setor;

            // injeta Bearer para todas as próximas chamadas
            ctx.http.setBearerToken(ctx.accessToken);

            String setorNorm = (ctx.setor == null ? "" : ctx.setor.trim().toUpperCase());
            if (setorNorm.isBlank()) {
                lblErroLogin.setText("Setor inválido retornado pelo servidor.");
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
            case "ADMIN" -> "/ui/setores/caixaScreen.fxml"; // provisório (ou cria tela admin)
            default -> null;
        };

        if (fxmlPath == null) {
            lblErroLogin.setText("Setor não reconhecido: " + setorNorm);
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));

        // Só CAIXA/ADMIN precisa controller com ctx (injeção)
        if ("CAIXA".equals(setorNorm) || "ADMIN".equals(setorNorm)) {
            loader.setController(new CaixaController(ctx));
        }

        Parent root = loader.load();

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