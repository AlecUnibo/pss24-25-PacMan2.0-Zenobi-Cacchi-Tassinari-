package com.pacman;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GhostManager {

    private final List<Block> ghosts;
    private final ImageLoader imageLoader;
    private final ScoreManager scoreManager;

    // salva posizione iniziale di ogni ghost per il respawn
    private final Map<Block, Position> spawnPositions = new HashMap<>();

    // stato scared
    private boolean scared = false;
    private long scaredEndTime;

    private static class Position {
        final int x, y;

        Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public GhostManager(List<Block> ghosts, ImageLoader loader, ScoreManager scoreManager) {
        this.ghosts = ghosts;
        this.imageLoader = loader;
        this.scoreManager = scoreManager;
        // memorizza le posizioni di spawn
        for (Block g : ghosts) {
            spawnPositions.put(g, new Position(g.x, g.y));
        }
    }

    /**
     * Chiamare quando Pac-Man mangia un power‐food
     */
    public void triggerScaredMode() {
        scared = true;
        scaredEndTime = System.currentTimeMillis() + 6_000; // 6 secondi
        for (Block g : ghosts) {
            g.isScared = true;
            g.image = imageLoader.getScaredGhostImage();
        }
    }

    /**
     * Chiamare in ogni ciclo di update (prima di checkCollision)
     */
    public void update() {
        if (scared && System.currentTimeMillis() > scaredEndTime) {
            scared = false;
            // termina scared mode: ripristina immagini originali
            for (Block g : ghosts) {
                g.isScared = false;
                g.image = g.originalImage;
            }
        }
    }

    /**
     * Controlla eventuali collisioni “Pac-Man vs ghost” in scared mode
     */
    public void checkCollisions(Block pacman) {
        for (Block g : ghosts) {
            if (g.isScared && collision(pacman, g)) {
                // ghost mangiato
                scoreManager.addPoints(200);
                respawn(g);
            }
        }
    }

    private void respawn(Block g) {
        // riporta ghost nella gabbia con immagine originale
        Position p = spawnPositions.get(g);
        g.x = p.x;
        g.y = p.y;
        g.isScared = false;
        g.image = g.originalImage;
    }

    private boolean collision(Block a, Block b) {
        return a.x < b.x + b.width
                && a.x + a.width > b.x
                && a.y < b.y + b.height
                && a.y + a.height > b.y;
    }
}