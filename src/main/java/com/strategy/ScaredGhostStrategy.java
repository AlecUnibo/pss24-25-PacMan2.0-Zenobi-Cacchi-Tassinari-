package com.strategy;

import com.pacman.Block;
import com.pacman.Direction;
import com.pacman.GameMap;
import com.pacman.PacMan;

/**
 * Strategia per fantasma in stato spaventato: movimento casuale via timer.
 */
public class ScaredGhostStrategy extends AbstractGhostStrategy {
    public ScaredGhostStrategy(GameMap map, PacMan game) {
        super(map, game);
    }

    @Override
    public Direction getNextDirection(Block ghost, long now) {
        return timedRandom(ghost, now);
    }
}