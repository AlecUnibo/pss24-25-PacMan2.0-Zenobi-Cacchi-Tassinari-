package com.strategy;

import com.pacman.PacMan;
import com.pacman.Block;
import com.pacman.GhostManager;
import com.pacman.FruitManager;
import com.pacman.ScoreManager;
import com.pacman.GameMap;
import java.util.Iterator;
import com.pacman.SoundManager;

public class AllTypeCollisionsStrategy implements CollisionStrategy {

    private final GhostManager ghostManager;
    private final FruitManager fruitManager;
    private final ScoreManager scoreManager;
    private final GameMap gameMap;

    /**Inizializza con riferimenti ai manager coinvolti nelle collisioni.*/
    public AllTypeCollisionsStrategy(GhostManager ghostManager, FruitManager fruitManager, ScoreManager scoreManager, GameMap gameMap ) {
        this.ghostManager = ghostManager;
        this.fruitManager = fruitManager;
        this.scoreManager = scoreManager;
        this.gameMap = gameMap;
    }

    /** Rimuove il cibo normale e aggiunge 10 punti alla collisione.*/
    @Override
    public void handleFoodCollision(PacMan pacman, Block block) {
        Iterator<Block> it = gameMap.getFoods().iterator();
        while (it.hasNext()) {
            Block food = it.next();
            if (food.collidesWith(block)) {
                it.remove();
                scoreManager.addScore(10);
                break;
            }
        }
    }

    /** Rimuove la power-food, il cibo adiacente, assegna 50 punti e attiva scared mode.*/
    @Override
    public void handlePowerFoodCollision(PacMan pacman, Block block) {
        Iterator<Block> it = gameMap.getPowerFoods().iterator();
        while (it.hasNext()) {
            Block pf = it.next();
            if (pf.collidesWith(block)) {
                it.remove();
                break;
            }
        }
        Iterator<Block> foodIt = gameMap.getFoods().iterator();
        while (foodIt.hasNext()) {
            Block f = foodIt.next();
            if (f.collidesWith(block)) {
                foodIt.remove();
                break;
            }
        }
        scoreManager.addScore(50);
        ghostManager.activateScaredMode();
    }

    /** Annulla il movimento di Pac-Man contro un muro.*/
    @Override
    public void handleWallCollision(PacMan pacman, Block wallBlock) {
        pacman.undoMove();
    }

    /** Gestisce collisione con fantasma normale, sottraendo una vita.*/
    @Override
    public void handleGhostCollision(PacMan pacman, Block  ghost) {
        pacman.die();
    }

    /** Rimuove il fantasma spaventato, programma il respawn e assegna 200 punti.*/
    @Override
    public void handleScaredGhostCollision(PacMan pacman, Block ghost) {
        ghostManager.removeGhost(ghost);
        ghostManager.scheduleGhostRespawn(ghost);
        scoreManager.addScore(200);
        SoundManager.playSound("eat_ghost");
    }

    /** Consuma un frutto, aggiunge i punti e registra l'immagine.*/
    @Override
    public void handleFruitCollision(PacMan pacman, FruitManager.FruitType fruit) {
        fruitManager.consumeFruit(fruit);
        scoreManager.addScore(fruit.getScore());
        scoreManager.addCollectedFruit(fruit);
    }
}
