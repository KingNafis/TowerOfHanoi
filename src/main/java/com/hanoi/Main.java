package com.hanoi;

import com.hanoi.controller.GameController;
import com.hanoi.controller.WelcomeController;
import com.hanoi.model.Difficulty;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {

    private Stage primaryStage;
    private static Main instance;

    // Dark theme colors consistent with the web app (Slate 900)
    public static final String BG_COLOR = "#0f172a";
    public static final String TEXT_COLOR = "#f8fafc";
    public static final String ACCENT_COLOR = "#7c3aed"; // Purple
    public static final String SUCCESS_COLOR = "#10b981"; // Emerald

    @Override
    public void start(Stage primaryStage) {
        instance = this;
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Tower of Hanoi");

        showWelcomeScreen();

        this.primaryStage.show();
    }

    public static Main getInstance() {
        return instance;
    }

    public void showWelcomeScreen() {
        WelcomeController welcomeController = new WelcomeController(this);
        Scene scene = new Scene(welcomeController.getView(), 900, 600);
        applyStyles(scene);
        primaryStage.setScene(scene);
    }

    public void showGameScreen(String playerName, Difficulty difficulty) {
        GameController gameController = new GameController(this, playerName, difficulty);
        Scene scene = new Scene(gameController.getView(), 1024, 768);
        applyStyles(scene);
        primaryStage.setScene(scene);
    }

    private void applyStyles(Scene scene) {
        scene.setFill(Color.web(BG_COLOR));
        // Global CSS for components
        String css = """
            .root { -fx-background-color: #0f172a; -fx-font-family: 'Segoe UI', sans-serif; }
            .label { -fx-text-fill: #f8fafc; }
            .button {
                -fx-background-color: #334155;
                -fx-text-fill: white;
                -fx-background-radius: 8;
                -fx-padding: 10 20;
                -fx-cursor: hand;
                -fx-font-weight: bold;
            }
            .button:hover { -fx-background-color: #475569; }
            .button:disabled { -fx-opacity: 0.5; }
            .primary-button { -fx-background-color: #10b981; }
            .primary-button:hover { -fx-background-color: #059669; }
            .accent-button { -fx-background-color: #7c3aed; }
            .accent-button:hover { -fx-background-color: #6d28d9; }
            .text-field {
                -fx-background-color: #1e293b;
                -fx-text-fill: white;
                -fx-border-color: #475569;
                -fx-border-radius: 4;
                -fx-background-radius: 4;
            }
            .list-cell {
                -fx-background-color: transparent;
                -fx-text-fill: white;
                -fx-padding: 5;
            }
            .list-view {
                -fx-background-color: #1e293b;
                -fx-control-inner-background: #1e293b;
                -fx-background-radius: 8;
            }
        """;
        if (scene.getStylesheets().isEmpty()) {
            // In a real app, load from file. Here we inject inline for portability.
            scene.getStylesheets().add("data:text/css," + css.replace("\n", "").replace(" ", "%20"));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}