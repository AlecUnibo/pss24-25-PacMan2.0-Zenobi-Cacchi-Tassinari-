package com.strategy;

import com.pacman.Block;
import com.pacman.Direction;

public interface GhostMovementStrategy {

    /** Calcola la prossima direzione del fantasma in base allo stato.*/
    Direction getNextDirection(Block ghost, long now);
}
