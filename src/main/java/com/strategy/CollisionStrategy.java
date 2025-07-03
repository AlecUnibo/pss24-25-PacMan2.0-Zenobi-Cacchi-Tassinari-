package com.strategy;

import com.pacman.Block;
import com.pacman.PacMan;
import com.pacman.FruitManager.FruitType;

public interface CollisionStrategy {

    /** Gestisce collisioni con cibo normale*/
    void handleFoodCollision(PacMan pacman, Block foodBlock);

    /** Gestisce collisioni con power-food.*/
    void handlePowerFoodCollision(PacMan pacman, Block powerFoodBlock);

    /** Gestisce collisioni con muri.*/
    void handleWallCollision(PacMan pacman, Block wallBlock);

    /** Gestisce collisioni con fantasmi normali*/
    void handleGhostCollision(PacMan pacman, Block ghost);

    /** Gestisce collisioni con fantasmi spaventati.*/
    void handleScaredGhostCollision(PacMan pacman, Block ghost);

    /** Gestisce collisioni con frutti.*/
    void handleFruitCollision(PacMan pacman, FruitType fruit);
}
