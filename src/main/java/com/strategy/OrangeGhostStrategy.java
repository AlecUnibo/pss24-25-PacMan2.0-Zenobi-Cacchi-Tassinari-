package com.strategy;

import com.pacman.Block;
import com.pacman.Direction;
import com.pacman.GameMap;
import com.pacman.PacMan;
import java.util.*;

public class OrangeGhostStrategy extends AbstractGhostStrategy {
    private final Map<Block, Boolean> orangeChaseState = new HashMap<>();

    /** Alterna fase di inseguimento e casuale ogni ORANGE_PHASE_MS.*/
    public OrangeGhostStrategy(GameMap map, PacMan game) {
        super(map, game);
    }

    @Override
    public Direction getNextDirection(Block ghost, long now) {
        boolean chasePhase = ((now / ORANGE_PHASE_MS) % 2) == 1;
        Boolean last = orangeChaseState.get(ghost);
        Direction next;
        if (last == null || last != chasePhase) {
            next = chasePhase ? chase(ghost) : randomAvailable(ghost);
            orangeChaseState.put(ghost, chasePhase);
        } else {
            next = ghost.direction;
        }
        return next;
    }
}