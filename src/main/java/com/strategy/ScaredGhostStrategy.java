package com.strategy;

import com.pacman.Block;
import com.pacman.Direction;
import com.pacman.GameMap;
import com.pacman.PacMan;

public class ScaredGhostStrategy extends AbstractGhostStrategy {

    /** La strategia del fantasma spaventato muove in direzione casuale a intervalli.*/
    public ScaredGhostStrategy(GameMap map, PacMan game) {
        super(map, game);
    }

    @Override
    public Direction getNextDirection(Block ghost, long now) {
        return timedRandom(ghost, now);
    }
}