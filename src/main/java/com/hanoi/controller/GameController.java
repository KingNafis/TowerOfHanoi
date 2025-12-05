package com.hanoi.controller;

import com.hanoi.Main;
import com.hanoi.db.DatabaseManager;
import com.hanoi.model.Difficulty;
import com.hanoi.model.GameLogic;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.Optional;
import java.util.Stack;

public class GameController {

    private final Main mainApp;
    private final String playerName;
    private final Difficulty difficulty;
    private final GameLogic logic;
    private final DatabaseManager dbManager;

    private BorderPane view;
    private Label movesLabel;
    private Pane gameArea;

    // UI Constants
    private static final double PEG_WIDTH = 15;
    private static final double PEG_HEIGHT = 200;
    private static final double DISK_HEIGHT = 25;
    private static final double BASE_WIDTH = 250;
    private static final double MIN_DISK_WIDTH = 60;
    private static final double MAX_DISK_WIDTH = 200;

    // Colors for disks (from smallest index to largest)
    private static final Color[] DISK_COLORS = {
            Color.web("#ef4444"), // Red
            Color.web("#f97316"), // Orange
            Color.web("#fbbf24"), // Amber
            Color.web("#10b981"), // Emerald
            Color.web("#06b6d4"), // Cyan
            Color.web("#3b82f6"), // Blue
            Color.web("#8b5cf6")  // Purple
    };

    public GameController(Main mainApp, String playerName, Difficulty difficulty) {
        this.mainApp = mainApp;
        this.playerName = playerName;
        this.difficulty = difficulty;
        this.logic = new GameLogic(difficulty.getDisks());
        this.dbManager = DatabaseManager.getInstance();
        initView();
        drawGame();
    }

    private void initView() {
        view = new BorderPane();
        view.setPadding(new Insets(20));
        view.setStyle("-fx-background-color: " + Main.BG_COLOR + ";");

        // --- Top Bar ---
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0, 0, 20, 0));

        Button backBtn = new Button("← Menu");
        backBtn.setOnAction(e -> mainApp.showWelcomeScreen());

        VBox infoBox = new VBox(2);
        Label playerLabel = new Label("Player: " + playerName + " | Difficulty: " + difficulty);
        playerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        infoBox.getChildren().add(playerLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox statsBox = new VBox(2);
        statsBox.setAlignment(Pos.CENTER_RIGHT);
        Label minMovesLabel = new Label("Min Moves: " + logic.getMinMoves());
        movesLabel = new Label("Moves: 0");
        movesLabel.setTextFill(Color.web(Main.SUCCESS_COLOR));
        movesLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 18));
        statsBox.getChildren().addAll(minMovesLabel, movesLabel);

        topBar.getChildren().addAll(backBtn, infoBox, spacer, statsBox);
        view.setTop(topBar);

        // --- Game Area ---
        gameArea = new Pane();
        gameArea.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 12;");
        view.setCenter(gameArea);
    }

    private void drawGame() {
        gameArea.getChildren().clear();

        double areaWidth = 984; // approximate, based on scene width - padding
        double areaHeight = 600;
        double pegSpacing = areaWidth / 3;

        // Draw 3 Pegs
        for (int i = 0; i < 3; i++) {
            double centerX = (i * pegSpacing) + (pegSpacing / 2);
            double bottomY = areaHeight - 50;

            // Draw Base
            Rectangle base = new Rectangle(centerX - (BASE_WIDTH / 2), bottomY, BASE_WIDTH, 20);
            base.setFill(Color.web("#475569"));
            base.setArcWidth(10); base.setArcHeight(10);

            // Draw Pole
            Rectangle pole = new Rectangle(centerX - (PEG_WIDTH / 2), bottomY - PEG_HEIGHT, PEG_WIDTH, PEG_HEIGHT);
            pole.setFill(Color.web("#475569"));
            pole.setArcWidth(10); pole.setArcHeight(10);

            // Add drop handlers to pole area (invisible large rect for easier drop)
            Rectangle dropZone = new Rectangle(centerX - (BASE_WIDTH/2), bottomY - PEG_HEIGHT - 50, BASE_WIDTH, PEG_HEIGHT + 70);
            dropZone.setFill(Color.TRANSPARENT);
            setupDropTarget(dropZone, i);

            gameArea.getChildren().addAll(base, pole, dropZone);

            // Draw Label
            Text label = new Text(String.valueOf((char)('A' + i)));
            label.setFill(Color.GRAY);
            label.setFont(Font.font(24));
            label.setX(centerX - 7);
            label.setY(bottomY + 50);
            gameArea.getChildren().add(label);

            // Draw Disks
            Stack<Integer> tower = logic.getTower(i);
            for (int j = 0; j < tower.size(); j++) {
                int diskSize = tower.get(j); // 1 is smallest
                // Logic stores N as largest at bottom.
                // Wait, in GameLogic constructor:
                // for (int k = totalDisks; k >= 1; k--) towers[0].push(k);
                // Stack index 0 is bottom.

                double width = MIN_DISK_WIDTH + ((double) (diskSize - 1) / (difficulty.getDisks())) * (MAX_DISK_WIDTH - MIN_DISK_WIDTH);

                Rectangle disk = new Rectangle();
                disk.setWidth(width);
                disk.setHeight(DISK_HEIGHT);
                disk.setX(centerX - (width / 2));
                // Stack grows up. J=0 is bottom.
                disk.setY(bottomY - ((j + 1) * (DISK_HEIGHT + 2)));

                disk.setArcWidth(8);
                disk.setArcHeight(8);

                int colorIndex = (diskSize - 1) % DISK_COLORS.length;
                disk.setFill(DISK_COLORS[colorIndex]);
                disk.setStroke(Color.WHITE.deriveColor(0, 1, 1, 0.2));

                // Add drag handler if it's the top disk
                if (j == tower.size() - 1) {
                    setupDragSource(disk, i);
                    disk.setCursor(javafx.scene.Cursor.HAND);
                }

                gameArea.getChildren().add(disk);
            }
        }
    }

    private void setupDragSource(Rectangle disk, int sourceIndex) {
        disk.setOnDragDetected(event -> {
            Dragboard db = disk.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            // Store source peg index
            content.putString(String.valueOf(sourceIndex));
            db.setContent(content);

            // Optional: Set drag view image
            db.setDragView(disk.snapshot(null, null));

            event.consume();
        });
    }

    private void setupDropTarget(Rectangle zone, int targetIndex) {
        zone.setOnDragOver(event -> {
            if (event.getGestureSource() != zone && event.getDragboard().hasString()) {
                int sourceIndex = Integer.parseInt(event.getDragboard().getString());
                if (logic.canMove(sourceIndex, targetIndex)) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
            }
            event.consume();
        });

        zone.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                int sourceIndex = Integer.parseInt(db.getString());
                logic.move(sourceIndex, targetIndex);
                movesLabel.setText("Moves: " + logic.getMoves());
                drawGame(); // Re-render
                checkWin();
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private void checkWin() {
        if (logic.isSolved()) {
            double score = logic.calculateScore();
            int moves = logic.getMoves();

            // Save to DB
            dbManager.addScore(playerName, difficulty, moves, score);

            showWinDialog(moves, score);
        }
    }

    private void showWinDialog(int moves, double score) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("You Win!");
        alert.setHeaderText("Great job, " + playerName + "!");

        String content = String.format("""
            Moves: %d
            Min Moves: %d
            Score: %.1f
            """, moves, logic.getMinMoves(), score);
        alert.setContentText(content);

        ButtonType btnPlayAgain = new ButtonType("Play Again", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnExit = new ButtonType("Exit", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().addAll(btnPlayAgain, btnExit);

        // Check if we can offer next level
        boolean canNextLevel = score >= 50.0 && difficulty != Difficulty.HARD;
        ButtonType btnNextLevel = null;
        if (canNextLevel) {
            btnNextLevel = new ButtonType("Next Level →", ButtonBar.ButtonData.NEXT_FORWARD);
            alert.getButtonTypes().add(1, btnNextLevel);
        }

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == btnPlayAgain) {
                // Restart same config
                logic.reset();
                movesLabel.setText("Moves: 0");
                drawGame();
            } else if (btnNextLevel != null && result.get() == btnNextLevel) {
                // Determine next difficulty
                Difficulty nextDiff = (difficulty == Difficulty.EASY) ? Difficulty.MEDIUM : Difficulty.HARD;
                mainApp.showGameScreen(playerName, nextDiff);
            } else {
                mainApp.showWelcomeScreen();
            }
        }
    }

    public Parent getView() {
        return view;
    }
}