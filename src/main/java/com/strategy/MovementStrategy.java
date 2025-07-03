package com.strategy;

import com.pacman.Block;
import com.pacman.GameMap;
import javafx.scene.input.KeyCode;

public interface MovementStrategy {

    /** Muove il blocco di Pac-Man in base alla direzione e collisioni.*/
    void move(
            Block pacman,
            KeyCode currentDir,
            GameMap gameMap,
            double speedMultiplier
    );
}
