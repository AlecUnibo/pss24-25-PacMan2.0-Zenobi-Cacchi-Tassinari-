package com.strategy;

import com.pacman.Block;
import com.pacman.Direction;

public class CageGhostStrategy implements GhostMovementStrategy {
    /** Nel recinto i fantasmi si muovono sempre verso l'alto.*/
    @Override
    public Direction getNextDirection(Block ghost, long now) {
        return Direction.UP;
    }
}