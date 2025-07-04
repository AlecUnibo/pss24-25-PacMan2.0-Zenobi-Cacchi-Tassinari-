package com.pacman;

import com.strategy.BlueGhostStrategy;
import com.strategy.CageGhostStrategy;
import com.strategy.GhostMovementStrategy;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

class GhostMovementTest {

    @BeforeAll
    static void initToolkit() throws Exception {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(latch::countDown);
            latch.await();
        } catch (IllegalStateException e) {
        }
    }

    private GameMap map;
    private Block ghost;
    private long now;

    @BeforeEach
    void init() {
        map = new GameMap(new ImageLoader());
        ghost = new Block(null,
                          PacMan.TILE_SIZE, PacMan.TILE_SIZE,
                          PacMan.TILE_SIZE, PacMan.TILE_SIZE,
                          Block.GhostType.BLUE);
        now = System.currentTimeMillis();
    }

    @Test
    void blueStrategy_returnsValidDirection() {
        GhostMovementStrategy strat = new BlueGhostStrategy(map, null);
        assertNotNull(strat.getNextDirection(ghost, now));
    }

    @Test
    void cageStrategy_alwaysUp() {
        GhostMovementStrategy cage = new CageGhostStrategy();
        assertEquals(Direction.UP, cage.getNextDirection(ghost, now));
    }
}
