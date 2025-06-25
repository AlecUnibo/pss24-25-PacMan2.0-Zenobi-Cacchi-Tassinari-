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

    public AllTypeCollisionsStrategy(GhostManager ghostManager, FruitManager fruitManager, ScoreManager scoreManager, GameMap gameMap ) {
        this.ghostManager = ghostManager;
        this.fruitManager = fruitManager;
        this.scoreManager = scoreManager;
        this.gameMap = gameMap;

    }

    @Override
    public void handleFoodCollision(PacMan pacman, Block block) {
        Iterator<Block> it = gameMap.getFoods().iterator();
        while (it.hasNext()) {
            Block food = it.next();
            if (food.collidesWith(block)) {
                it.remove();
                scoreManager.addScore(10); // 🔥 assicurati che ci sia
                break;
            }
        }
    }


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

    @Override
    public void handleWallCollision(PacMan pacman, Block wallBlock) {
        // Muro: annulla il movimento
        pacman.undoMove();
    }

    @Override
    public void handleGhostCollision(PacMan pacman, Block  ghost) {
        pacman.die(); // o pacman.setAlive(false);
    }

    @Override
    public void handleScaredGhostCollision(PacMan pacman, Block ghost) {
        ghostManager.removeGhost(ghost); // toglie dalla lista dei fantasmi attivi
        ghostManager.scheduleGhostRespawn(ghost); // lo fa riapparire nella gabbia
        scoreManager.addScore(200);
        SoundManager.playSound("eat_ghost");

    }




    @Override
    public void handleFruitCollision(PacMan pacman, FruitManager.FruitType fruit) {
        fruitManager.consumeFruit(fruit);
        scoreManager.addScore(fruit.getScore());
        // Effetti bonus casuali già gestiti nel fruitManager
    }
}
