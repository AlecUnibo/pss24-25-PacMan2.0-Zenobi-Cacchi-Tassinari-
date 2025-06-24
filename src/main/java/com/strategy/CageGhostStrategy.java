package com.strategy;

import com.pacman.Block;
import com.pacman.Direction;

/**
 * Strategia per gestire i fantasmi in gabbia: escono verso l'alto fino a liberazione.
 */
public class CageGhostStrategy implements GhostMovementStrategy {
    @Override
    public Direction getNextDirection(Block ghost, long now) {
        // Il movimento di uscita è sempre verso l'alto.
        return Direction.UP;
    }
}