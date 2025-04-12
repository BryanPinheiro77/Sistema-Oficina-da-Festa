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

    @Override
    public void init() {
        context = SpringApplication.run(OficinaDaFestaApplication.class);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        SpringFXMLLoader loader = context.getBean(SpringFXMLLoader.class);
        Parent root = loader.load("/fxml/login.fxml");

        primaryStage.setTitle("Login - Oficina da Festa");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    @Override
    public void stop() {
        context.close();
    }

    public static void main(String[] args) {
        launch(args); // Inicia JavaFX
    }
}
