package com.strategy;

import com.pacman.Block;
import com.pacman.Direction;

/**
 * Interfaccia principale per la strategia di movimento dei fantasmi.
 */
public interface GhostMovementStrategy {
    /**
     * Calcola la direzione successiva per il fantasma in base allo stato attuale.
     *
     * @param ghost "Block" del fantasma da muovere
     * @param now   timestamp corrente in millisecondi
     * @return la direzione scelta per il prossimo step
     */
    Direction getNextDirection(Block ghost, long now);
}
