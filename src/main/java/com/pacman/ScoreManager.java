package com.pacman;

public class ScoreManager {
    private int score;
    private int lives;

    public ScoreManager() {
        this.score = 0;
        this.lives = 3;
    }

    public void addPoints(int points) {
        this.score += points;
    }

    public void loseLife() {
        if (lives > 0) lives--;
    }

    public int getScore() {
        return score;
    }

    public int getLives() {
        return lives;
    }

    public boolean isGameOver() {
        return lives <= 0;
    }

    public void reset() {
        score = 0;
        lives = 3;
    }
}