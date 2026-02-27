package com.oficinadafesta.app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.util.concurrent.atomic.AtomicBoolean;

public class DesktopApp extends Application {

    private final AtomicBoolean loggingOut = new AtomicBoolean(false);

    @Override
    public void start(Stage stage) throws Exception {

        AppContext ctx = new AppContext("http://localhost:8080");

        // ✅ handler global de 401 -> força logout e volta pro login
        ctx.onUnauthorized((status, body) -> {
            if (!loggingOut.compareAndSet(false, true)) return;

            Platform.runLater(() -> {
                try {
                    ctx.clearSession();
                    loadLogin(stage, ctx); // volta pro login
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    loggingOut.set(false);
                }
            });
        });

        loadLogin(stage, ctx);

        stage.setTitle("Sistema - Oficina da Festa");
        stage.setFullScreen(true);
        stage.show();
    }

    private void loadLogin(Stage stage, AppContext ctx) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/auth/login.fxml"));

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

        stage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}