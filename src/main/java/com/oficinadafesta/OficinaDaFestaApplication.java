package com.oficinadafesta;

import com.oficinadafesta.config.SpringFXMLLoader;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
        Parent root = loader.load("com/oficinadafesta/login/login.fxml");

        // Cria a cena com tamanho base (opcional, pois a tela será cheia)
        Scene scene = new Scene(root, 1280, 720); // tamanho inicial para fallback

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
