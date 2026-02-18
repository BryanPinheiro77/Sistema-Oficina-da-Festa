package com.oficinadafesta.shared.config;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;


@Component

public class SpringFXMLLoader {

    @Autowired
    private ApplicationContext context;

    public Parent load(String fxmlPath) throws IOException {
        String normalized = fxmlPath.startsWith("/") ? fxmlPath.substring(1) : fxmlPath;
        URL url = getClass().getClassLoader().getResource(normalized);
        if (url == null) throw new IllegalArgumentException("FXML não encontrado: " + fxmlPath);
        FXMLLoader loader = new FXMLLoader(url);
        loader.setControllerFactory(context::getBean);
        return loader.load();
    }
}

