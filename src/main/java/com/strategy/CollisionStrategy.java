package com.strategy;

import com.pacman.Block;
import com.pacman.PacMan;
import com.pacman.FruitManager.FruitType;


public interface CollisionStrategy {

    void handleFoodCollision(PacMan pacman, Block foodBlock);
    
    void handlePowerFoodCollision(PacMan pacman, Block powerFoodBlock);
    
    void handleWallCollision(PacMan pacman, Block wallBlock);
    
    void handleGhostCollision(PacMan pacman, Block ghost);
    
    void handleScaredGhostCollision(PacMan pacman, Block ghost);
    
    void handleFruitCollision(PacMan pacman, FruitType fruit);

}
