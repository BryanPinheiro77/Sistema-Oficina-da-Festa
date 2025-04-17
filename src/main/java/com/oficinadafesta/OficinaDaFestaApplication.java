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
        // Lança o JavaFX
        launch(args);
    }

    @Override
    public void init() {
        // Inicializa o Spring Boot antes de iniciar o JavaFX
        context = SpringApplication.run(OficinaDaFestaApplication.class);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Carrega a tela FXML usando o SpringFXMLLoader
        SpringFXMLLoader loader = context.getBean(SpringFXMLLoader.class);
        Parent root = loader.load("com/oficinadafesta/login/login.fxml");

        // Configura a cena do JavaFX
        primaryStage.setTitle("Login - Oficina da Festa");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    @Override
    public void stop() {
        // Fecha o contexto do Spring Boot
        context.close();
    }
}
