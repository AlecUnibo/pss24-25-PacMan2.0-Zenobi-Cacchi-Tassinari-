package com.strategy;

import com.pacman.Block;
import com.pacman.Direction;
import com.pacman.GameMap;
import com.pacman.PacMan;

/**
 * Strategia per il fantasma BLUE: comportamento simile a RED (esplorazione casuale).
 */
public class BlueGhostStrategy extends AbstractGhostStrategy {
    public BlueGhostStrategy(GameMap map, PacMan game) {
        super(map, game);
    }

    @Override
    public Direction getNextDirection(Block ghost, long now) {
        return timedRandom(ghost, now);
    }
}