package com.oficinadafesta.config;

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
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(fxmlPath));
        loader.setControllerFactory(context::getBean); // importante!
        return loader.load();
    }
}

