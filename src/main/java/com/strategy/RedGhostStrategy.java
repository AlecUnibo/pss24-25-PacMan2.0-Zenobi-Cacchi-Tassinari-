package com.strategy;

import com.pacman.Block;
import com.pacman.Direction;
import com.pacman.GameMap;
import com.pacman.PacMan;

/**
 * Strategia per il fantasma RED: esplorazione casuale via timer.
 */
public class RedGhostStrategy extends AbstractGhostStrategy {
    public RedGhostStrategy(GameMap map, PacMan game) {
        super(map, game);
    }

    @Override
    public Direction getNextDirection(Block ghost, long now) {
        return timedRandom(ghost, now);
    }
}