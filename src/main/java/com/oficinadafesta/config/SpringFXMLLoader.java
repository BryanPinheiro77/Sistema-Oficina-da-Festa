package com.oficinadafesta.config;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import lombok.Data;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;

@Data
@Component
public class SpringFXMLLoader {

    private final ApplicationContext context;

    public SpringFXMLLoader(ApplicationContext context) {
        this.context = context;
    }

    public Parent load(String fxmlPath) throws IOException {
        // Usando o ClassLoader para garantir que o recurso seja carregado corretamente
        URL location = getClass().getClassLoader().getResource(fxmlPath);
        if (location == null) {
            throw new IOException("FXML file not found: " + fxmlPath);
        }

        FXMLLoader loader = new FXMLLoader(location);
        loader.setControllerFactory(context::getBean); // Injeção de dependências com Spring
        return loader.load();
    }
}
