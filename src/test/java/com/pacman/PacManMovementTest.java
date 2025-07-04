package com.pacman;

import com.strategy.KeyboardMovementStrategy;
import com.strategy.MovementStrategy;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

class PacManMovementTest {

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
    private Block pac;

    @BeforeEach
    void setup() {
        map = new GameMap(new ImageLoader());
        pac = map.getPacman();
    }

    @Test
    void canMove_right_movesXPositive() {
        int originalX = pac.x;
        MovementStrategy strat = new KeyboardMovementStrategy();
        strat.move(pac, KeyCode.RIGHT, map, 1.0);
        assertTrue(pac.x > originalX, "Pac-Man should move right");
    }

    @Test
    void cannotMove_intoWall_stopsAtBoundary() {
        pac.x = 0; pac.y = 0;
        MovementStrategy strat = new KeyboardMovementStrategy();
        strat.move(pac, KeyCode.LEFT, map, 1.0);
        assertEquals(0, pac.x, "Pac-Man should not move into wall at x=0");
    }
}
