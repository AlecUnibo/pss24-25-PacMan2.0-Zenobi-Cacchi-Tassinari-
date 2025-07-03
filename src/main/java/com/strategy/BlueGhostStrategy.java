package com.strategy;

import com.pacman.Block;
import com.pacman.Direction;
import com.pacman.GameMap;
import com.pacman.PacMan;

public class BlueGhostStrategy extends AbstractGhostStrategy {
    /** La strategia del fantasma blu muove in direzione casuale a intervalli.*/
    public BlueGhostStrategy(GameMap map, PacMan game) {
        super(map, game);
    }

    @Override
    public Direction getNextDirection(Block ghost, long now) {
        return timedRandom(ghost, now);
    }
}