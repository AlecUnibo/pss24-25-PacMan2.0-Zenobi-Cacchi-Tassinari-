package com.strategy;

import com.pacman.Block;
import com.pacman.Direction;
import com.pacman.GameMap;
import com.pacman.PacMan;

public class PinkGhostStrategy extends AbstractGhostStrategy {

    /** Usa previsione delle mosse di Pac-Man nei primi 6s, poi movimento casuale per 4s.*/
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