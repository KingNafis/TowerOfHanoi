package com.hanoi.db;

import com.hanoi.model.Difficulty;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:hanoi_scores.db";
    private static DatabaseManager instance;

    public record ScoreRecord(String name, String difficulty, int moves, double score, String date) {}

    private DatabaseManager() {
        try {
            // Ensure driver is loaded
            Class.forName("org.sqlite.JDBC");
            createTable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void createTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS scores (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                difficulty TEXT,
                moves INTEGER,
                score REAL,
                date_played TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        """;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addScore(String name, Difficulty difficulty, int moves, double score) {
        String sql = "INSERT INTO scores(name, difficulty, moves, score) VALUES(?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, difficulty.name());
            pstmt.setInt(3, moves);
            pstmt.setDouble(4, score);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<ScoreRecord> getTopScores() {
        List<ScoreRecord> scores = new ArrayList<>();
        String sql = "SELECT name, difficulty, moves, score, date_played FROM scores ORDER BY score DESC LIMIT 5";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                scores.add(new ScoreRecord(
                        rs.getString("name"),
                        rs.getString("difficulty"),
                        rs.getInt("moves"),
                        rs.getDouble("score"),
                        rs.getString("date_played")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return scores;
    }

    /**
     * Returns the highest score achieved for a specific difficulty.
     * Used to determine if the next level should be unlocked.
     */
    public double getBestScore(Difficulty difficulty) {
        String sql = "SELECT MAX(score) as max_score FROM scores WHERE difficulty = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, difficulty.name());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("max_score");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
}