package com.pacman;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;

class GameMapTest {

    @BeforeAll
    static void initToolkit() throws Exception {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(latch::countDown);
            latch.await();
        } catch (IllegalStateException e) {
        }
    }

    @Test
    void loadMap_populatesWallsFoodsAndPacman() {
        ImageLoader loader = new ImageLoader();
        GameMap map = new GameMap(loader);

        assertFalse(map.getWalls().isEmpty(),  "Walls should be loaded");
        assertFalse(map.getFoods().isEmpty(),  "Foods should be loaded");

        Block pac = map.getPacman();
        assertNotNull(pac, "Pacman block should be non-null");
        assertTrue(pac.x >= 0 && pac.y >= 0, "Pacman initial coordinates valid");

        assertTrue(map.getTunnels().size() >= 2, "At least two tunnel blocks");
    }
}
