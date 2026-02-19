package com.oficinadafesta.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

public class DesktopApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/auth/login.fxml"));

        AppContext ctx = new AppContext("http://localhost:8080");

        loader.setControllerFactory(type -> {
            try {
                if (type.getName().equals("com.oficinadafesta.ui.auth.AuthController")) {
                    return type.getConstructor(AppContext.class).newInstance(ctx);
                }
                return type.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        Parent root = loader.load();

        Scene scene = new Scene(root, 1280, 720);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.F11) stage.setFullScreen(!stage.isFullScreen());
        });

        stage.setTitle("Sistema - Oficina da Festa");
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
