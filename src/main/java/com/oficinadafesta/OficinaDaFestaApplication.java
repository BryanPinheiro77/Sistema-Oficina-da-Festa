package com.oficinadafesta;

import com.oficinadafesta.shared.config.SpringFXMLLoader;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class OficinaDaFestaApplication extends Application {

    private static ConfigurableApplicationContext context;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() {
        context = SpringApplication.run(OficinaDaFestaApplication.class);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        SpringFXMLLoader loader = context.getBean(SpringFXMLLoader.class);
        Parent root = loader.load("ui/auth/login.fxml");

        // Cria a cena com tamanho base (opcional, pois a tela será cheia)
        Scene scene = new Scene(root, 1280, 720); // tamanho inicial para fallback

        // Adiciona o ouvinte para capturar pressionamento de tecla
        scene.setOnKeyPressed(event -> {
            // Verifica se a tecla pressionada foi F11
            if (event.getCode() == KeyCode.F11) {
                // Alterna entre tela cheia e normal
                if (primaryStage.isFullScreen()) {
                    primaryStage.setFullScreen(false);  // Sai da tela cheia
                } else {
                    primaryStage.setFullScreen(true);   // Vai para a tela cheia
                }
            }
        });

        // Configura a janela
        primaryStage.setTitle("Sistema - Oficina da Festa");
        primaryStage.setScene(scene);

        // Ativa o modo tela cheia
        primaryStage.setFullScreen(true);

        // Exibe a janela
        primaryStage.show();
    }

    @Override
    public void stop() {
        context.close();
    }
}
