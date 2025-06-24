// File: src/main/java/com/strategy/MovementStrategy.java
package com.strategy;

import com.pacman.Block;
import com.pacman.GameMap;
import javafx.scene.input.KeyCode;

/**
 * Strategy interface for Pac-Man movement logic:
 * spostamento pixel-by-pixel, animazione bocca e wrap tunnel.
 */
public interface MovementStrategy {
    /**
     * Muove il blocco di Pac-Man di un frame.
     *
     * @param pacman          il block di Pac-Man su cui operare
     * @param currentDir      la direzione corrente (tasto freccia) o null
     * @param gameMap         la mappa di gioco per collisioni e tunnel
     * @param speedMultiplier moltiplicatore di velocità (power-up)
     */
    void move(
        Block pacman,
        KeyCode currentDir,
        GameMap gameMap,
        double speedMultiplier
    );
}
