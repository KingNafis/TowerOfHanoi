package com.hanoi.model;

import java.util.Stack;

public class GameLogic {
    private final Stack<Integer>[] towers;
    private final int totalDisks;
    private int moves;

    @SuppressWarnings("unchecked")
    public GameLogic(int totalDisks) {
        this.totalDisks = totalDisks;
        this.towers = new Stack[3];
        for (int i = 0; i < 3; i++) {
            this.towers[i] = new Stack<>();
        }
        reset();
    }

    public void reset() {
        for (Stack<Integer> tower : towers) {
            tower.clear();
        }
        // Initialize Peg 0 (Source) with disks.
        // Larger numbers = larger disks. Pushing N down to 1.
        for (int i = totalDisks; i >= 1; i--) {
            towers[0].push(i);
        }
        moves = 0;
    }

    public boolean canMove(int fromIndex, int toIndex) {
        if (towers[fromIndex].isEmpty()) return false;
        if (towers[toIndex].isEmpty()) return true;

        // Cannot place larger disk on smaller disk
        return towers[fromIndex].peek() < towers[toIndex].peek();
    }

    public void move(int fromIndex, int toIndex) {
        if (canMove(fromIndex, toIndex)) {
            Integer disk = towers[fromIndex].pop();
            towers[toIndex].push(disk);
            moves++;
        }
    }

    public boolean isSolved() {
        // Solved if all disks are on the last peg (index 2)
        // or potentially the middle one, but standard is far right.
        return towers[2].size() == totalDisks;
    }

    public Stack<Integer> getTower(int index) {
        return towers[index];
    }

    public int getMoves() {
        return moves;
    }

    public int getMinMoves() {
        return (int) (Math.pow(2, totalDisks) - 1);
    }

    public double calculateScore() {
        if (moves == 0) return 0;
        return ((double) getMinMoves() / moves) * 100.0;
    }
}