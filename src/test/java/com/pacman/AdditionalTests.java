package com.pacman;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import javafx.application.Platform;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import static org.junit.jupiter.api.Assertions.*;

class AdditionalTests {

    @BeforeAll
    static void initToolkit() throws Exception {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(latch::countDown);
            latch.await();
        } catch (IllegalStateException e) {
            // già inizializzato
        }
    }

    /** 1) Collisione Pac‑Man vs fantasma in normal mode invoca onHit */
    @Test
    void pacmanGhostCollision_invokesLoseLife() {
        GameMap map = new GameMap(new ImageLoader());
        Block pac = map.getPacman();
        // posizioniamo un fantasma esattamente dove c'è pacman
        Block ghost = new Block(null, pac.x, pac.y, PacMan.TILE_SIZE, PacMan.TILE_SIZE, Block.GhostType.RED);
        // costruisco il GhostManager con un solo fantasma
        GhostManager gm = new GhostManager(
            List.of(ghost),
            map.getGhostPortal(),
            map.getPowerFoods(),
            map,
            null,
            new SoundManager()
        );
        boolean[] lifeLost = {false};
        // richiamo il metodo di collisione
        gm.handleGhostCollisions(pac, () -> lifeLost[0] = true);
        assertTrue(lifeLost[0], "onHit (loseLife) should be invoked on normal ghost collision");
    }

    /** 2) Collisione Pac‑Man vs cibo (puntino) restituisce 10 e rimuove il food */
    @Test
    void collectFood_returns10AndRemovesDot() {
        GameMap map = new GameMap(new ImageLoader());
        Block pac = map.getPacman();
        // posiziona pac esattamente sopra un food
        Block food = map.getFoods().iterator().next();
        pac.x = food.x; pac.y = food.y;
        int before = map.getFoods().size();
        int score = map.collectFood(pac);
        assertEquals(10, score, "collectFood should return 10");
        assertEquals(before - 1, map.getFoods().size(), "one food should be removed");
    }

    /** 3) Caricamento e riproduzione sound di avvio crea un Clip non null */
    @Test
    void startSound_loadsAndReturnsClip() throws Exception {
        SoundManager.loadSound("test-start", "sounds/start.wav");
        assertNotNull(SoundManager.getClip("test-start"), "Clip for 'test-start' must not be null");
        // Non si può realmente riprodurre in test, ma almeno non lancia eccezioni:
        assertDoesNotThrow(() -> SoundManager.playSound("test-start"));
    }
}
