package com.strategy;

import com.pacman.Block;
import com.pacman.Direction;
import com.pacman.GameMap;
import com.pacman.PacMan;

/**
 * Strategia per il fantasma PINK: primi 6s modalità predittiva, poi random.
 */
public class PinkGhostStrategy extends AbstractGhostStrategy {
    public PinkGhostStrategy(GameMap map, PacMan game) {
        super(map, game);
    }

    @Override
    public Direction getNextDirection(Block ghost, long now) {
        long phase = now % PINK_PHASE_MS;
        if (phase < 6_000L) {
            Point target = predictedPacmanTarget();
            return bestAvailableDirection(ghost, target);
        } else {
            return timedRandom(ghost, now);
        }
    }
}