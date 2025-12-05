package com.hanoi.model;

public enum Difficulty {
    EASY(3),
    MEDIUM(4),
    HARD(10);

    private final int disks;

    Difficulty(int disks) {
        this.disks = disks;
    }

    public int getDisks() {
        return disks;
    }
}