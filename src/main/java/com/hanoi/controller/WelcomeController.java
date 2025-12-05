package com.hanoi.controller;

import com.hanoi.Main;
import com.hanoi.db.DatabaseManager;
import com.hanoi.model.Difficulty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.List;

public class WelcomeController {

    private final Main mainApp;
    private final DatabaseManager dbManager;
    private TextField nameField;
    private ToggleGroup difficultyGroup;
    private VBox view;

    public WelcomeController(Main mainApp) {
        this.mainApp = mainApp;
        this.dbManager = DatabaseManager.getInstance();
        initView();
    }

    private void initView() {
        view = new VBox(20);
        view.setAlignment(Pos.CENTER);
        view.setPadding(new Insets(40));
        view.setStyle("-fx-background-color: " + Main.BG_COLOR + ";");

        // Header
        Text title = new Text("Tower of Hanoi");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 36));
        title.setFill(Color.WHITE);

        // --- Left Panel: Input ---
        VBox inputPanel = new VBox(15);
        inputPanel.setMaxWidth(400);
        inputPanel.setStyle("-fx-background-color: #1e293b; -fx-padding: 30; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 4);");

        Label nameLabel = new Label("Player Name");
        nameField = new TextField();
        nameField.setPromptText("Enter your name");

        Label diffLabel = new Label("Select Difficulty");
        HBox diffBox = new HBox(10);
        difficultyGroup = new ToggleGroup();

        // Check locks
        double easyBest = dbManager.getBestScore(Difficulty.EASY);
        boolean unlockMedium = easyBest >= 50.0;
        // In a strictly sequential game, unlock Hard if Medium passed,
        // but prompt says "unlock when user scores >= 50% in easy level" for Medium AND Hard?
        // Prompt: "lock medium and hard level and unlock when user scores more or equal 50% in easy level"
        // This implies passing Easy unlocks everything.
        boolean unlockHard = unlockMedium;

        diffBox.getChildren().add(createDiffButton(Difficulty.EASY, false));
        diffBox.getChildren().add(createDiffButton(Difficulty.MEDIUM, !unlockMedium));
        diffBox.getChildren().add(createDiffButton(Difficulty.HARD, !unlockHard));

        Button startButton = new Button("Start Game");
        startButton.getStyleClass().add("primary-button");
        startButton.setMaxWidth(Double.MAX_VALUE);
        startButton.setOnAction(e -> handleStart());

        inputPanel.getChildren().addAll(nameLabel, nameField, diffLabel, diffBox, startButton);

        // --- Right Panel: High Scores ---
        VBox scorePanel = new VBox(10);
        scorePanel.setMaxWidth(400);
        scorePanel.setStyle("-fx-background-color: #1e293b; -fx-padding: 20; -fx-background-radius: 12;");

        Label scoreLabel = new Label("Top 5 High Scores");
        scoreLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        ListView<String> scoreList = new ListView<>();
        scoreList.setPrefHeight(200);

        List<DatabaseManager.ScoreRecord> scores = dbManager.getTopScores();
        if (scores.isEmpty()) {
            scoreList.getItems().add("No scores yet.");
        } else {
            for (DatabaseManager.ScoreRecord s : scores) {
                scoreList.getItems().add(String.format("%s - %s (%d moves) - %.1f pts", s.name(), s.difficulty(), s.moves(), s.score()));
            }
        }

        scorePanel.getChildren().addAll(scoreLabel, scoreList);

        // Layout container
        HBox mainLayout = new HBox(40);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.getChildren().addAll(inputPanel, scorePanel);

        view.getChildren().addAll(title, mainLayout);
    }

    private ToggleButton createDiffButton(Difficulty diff, boolean locked) {
        ToggleButton btn = new ToggleButton(diff.name());
        btn.setToggleGroup(difficultyGroup);
        btn.setUserData(diff);
        btn.setDisable(locked);
        btn.setPrefWidth(100);
        if (diff == Difficulty.EASY) btn.setSelected(true); // Default

        if (locked) {
            btn.setText("Locked 🔒");
            btn.setStyle("-fx-opacity: 0.6;");
        }
        return btn;
    }

    private void handleStart() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            showAlert("Validation Error", "Please enter your name.");
            return;
        }

        Toggle selected = difficultyGroup.getSelectedToggle();
        if (selected == null) return;

        Difficulty diff = (Difficulty) selected.getUserData();
        mainApp.showGameScreen(name, diff);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public Parent getView() {
        return view;
    }
}