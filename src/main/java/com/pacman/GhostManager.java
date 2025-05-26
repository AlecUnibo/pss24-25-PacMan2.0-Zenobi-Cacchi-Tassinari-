package com.pacman;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestisce il comportamento dei fantasmi (ghosts) nel gioco:
 * - modalità spaventata (scared)
 * - collisioni con Pac-Man
 * - respawn dopo essere stati mangiati
 */
public class GhostManager {

    private final List<Block> ghosts;               // Lista dei fantasmi
    private final ImageLoader imageLoader;          // Caricatore immagini
    private final ScoreManager scoreManager;        // Gestore punteggio

    // Mappa che associa ogni fantasma alla sua posizione iniziale
    private final Map<Block, Position> spawnPositions = new HashMap<>();

    // Stato della modalità spaventata
    private boolean scared = false;
    private long scaredEndTime;

    // Classe interna per memorizzare le coordinate di respawn
    private static class Position {
        final int x, y;
        Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    // Costruttore: salva i fantasmi, le risorse e le posizioni di respawn
    public GhostManager(List<Block> ghosts, ImageLoader loader, ScoreManager scoreManager) {
        this.ghosts = ghosts;
        this.imageLoader = loader;
        this.scoreManager = scoreManager;
        for (Block g : ghosts) {
            spawnPositions.put(g, new Position(g.x, g.y));
        }
    }

    /**
     * Attiva la modalità spaventata per 6 secondi (chiamata quando Pac-Man mangia un power-food)
     */
    public void triggerScaredMode() {
        scared = true;
        scaredEndTime = System.currentTimeMillis() + 6_000;
        for (Block g : ghosts) {
            g.isScared = true;
            g.image = imageLoader.getScaredGhostImage();
        }
    }

    /**
     * Aggiorna lo stato dei fantasmi (chiamare ogni frame)
     * Disattiva la modalità spaventata al termine
     */
    public void update() {
        if (scared && System.currentTimeMillis() > scaredEndTime) {
            scared = false;
            for (Block g : ghosts) {
                g.isScared = false;
                g.image = g.originalImage;
            }
        }
    }

    /**
     * Controlla se Pac-Man ha mangiato un fantasma (durante la modalità spaventata)
     */
    public void checkCollisions(Block pacman) {
        for (Block g : ghosts) {
            if (g.isScared && collision(pacman, g)) {
                scoreManager.addPoints(200);  // Aggiunge punti al punteggio
                respawn(g);                   // Respawn del fantasma
            }
        }
    }

    // Riporta un fantasma nella sua posizione iniziale
    private void respawn(Block g) {
        Position p = spawnPositions.get(g);
        g.x = p.x;
        g.y = p.y;
        g.isScared = false;
        g.image = g.originalImage;
    }

    // Rileva collisione tra due blocchi (bounding box)
    private boolean collision(Block a, Block b) {
        return a.x < b.x + b.width
                && a.x + a.width > b.x
                && a.y < b.y + b.height
                && a.y + a.height > b.y;
    }
}
