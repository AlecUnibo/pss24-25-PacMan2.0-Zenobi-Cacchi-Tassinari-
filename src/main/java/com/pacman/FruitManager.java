package com.pacman;

import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FruitManager {

    private final PacMan game;
    private final ImageLoader imageLoader;
    private final List<Fruit> fruits = new ArrayList<>();
    private final Random rand = new Random();
    private Thread worker;
    private volatile boolean running = false;
    private int phase = 0;
    private long remainingDelay;
    private long lastPhaseStart;
    private static final int MAX_FRUITS_PER_LEVEL  = 2;
    private int spawnCount = 0;
    private static final int FRUIT_VISIBLE_MS      = 8000;
    private static final int FIRST_DELAY_MS        = 5000;
    private static final int SECOND_DELAY_MS       = 5000;

    public FruitManager(PacMan game, ImageLoader loader) {
        this.game = game;
        this.imageLoader = loader;
        resetTimers();
    }

    private void resetTimers() {
        phase = 0;
        remainingDelay = FIRST_DELAY_MS;
        lastPhaseStart = 0;
    }

    public synchronized void startFruitTimer() {
        if (running) return;
        running = true;
        lastPhaseStart = System.currentTimeMillis();
        worker = new Thread(this::runLoop);
        worker.setDaemon(true);
        worker.start();
    }

    public synchronized void pauseFruitTimer() {
        if (!running) return;
        running = false;
        long elapsed = System.currentTimeMillis() - lastPhaseStart;
        remainingDelay = Math.max(0, remainingDelay - elapsed);
        worker.interrupt();
    }

    private void runLoop() {
        while (running && phase < 3) {
            if (phase == 0 || phase == 2) {
                try {
                    sleepWithPause(remainingDelay);
                } catch (InterruptedException e) {
                    continue;
                }
                if (!running) break;
                spawnFruit();
                phase = (phase == 0) ? 1 : 3;
                lastPhaseStart = System.currentTimeMillis();
                remainingDelay = FRUIT_VISIBLE_MS;
            } else if (phase == 1) {
                long start = System.currentTimeMillis();
                while (running && !fruits.isEmpty() &&
                       System.currentTimeMillis() - start < FRUIT_VISIBLE_MS) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                if (!running) break;
                removeLastFruit();
                phase = 2;
                lastPhaseStart = System.currentTimeMillis();
                remainingDelay = SECOND_DELAY_MS;
            }
        }
        if (phase == 3 && running) {
            long start = System.currentTimeMillis();
            while (running && !fruits.isEmpty() &&
                   System.currentTimeMillis() - start < FRUIT_VISIBLE_MS) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {}
            }
            removeLastFruit();
        }
    }

    private void spawnFruit() {
        if (spawnCount >= MAX_FRUITS_PER_LEVEL) return;
        int col = PacMan.COLUMN_COUNT / 2;
        int x = col * PacMan.TILE_SIZE;
        int y = game.getReadyRow() * PacMan.TILE_SIZE;
        int lvl = game.getCurrentLevel();
        FruitType type = FruitType.values()[(lvl - 1) % FruitType.values().length];
        fruits.add(new Fruit(x, y, type));
        spawnCount++;
    }

    private void removeLastFruit() {
        if (!fruits.isEmpty()) {
            fruits.remove(fruits.size() - 1);
        }
    }

    public FruitType collectFruit(Block pacman) {
        for (int i = 0; i < fruits.size(); i++) {
            Fruit f = fruits.get(i);
            if (pacman.x < f.getX() + PacMan.TILE_SIZE &&
                pacman.x + pacman.width > f.getX() &&
                pacman.y < f.getY() + PacMan.TILE_SIZE &&
                pacman.y + pacman.height > f.getY()) {
                fruits.remove(i);
                if (phase == 1) {
                    phase = 2;
                    remainingDelay = SECOND_DELAY_MS;
                    lastPhaseStart = System.currentTimeMillis();
                    if (worker != null) worker.interrupt();
                }
                if (rand.nextDouble() < 0.33) {
                    if (rand.nextBoolean()) {
                        activateSpeedPower();
                    } else {
                        activateFreezePower();
                    }
                }
                return f.getType();
            }
        }
        return null;
    }

    // Metodo per supportare la CollisionStrategy
    public FruitType getCollidingFruit(Block pacman) {
        for (Fruit f : fruits) {
            if (pacman.x < f.getX() + PacMan.TILE_SIZE &&
                pacman.x + pacman.width > f.getX() &&
                pacman.y < f.getY() + PacMan.TILE_SIZE &&
                pacman.y + pacman.height > f.getY()) {
                return f.getType();
            }
        }
        return null;
    }

    private void activateSpeedPower() {
        game.setSpeedMultiplier(2.0);
        new Thread(() -> {
            try { Thread.sleep(5_000); } catch (InterruptedException ignored) {}
            Platform.runLater(() -> game.setSpeedMultiplier(1.0));
        }, "SpeedPowerTimer").start();
    }

    private void activateFreezePower() {
        game.freezeGhosts(5_000);
    }

    public void draw(GraphicsContext gc) {
        for (Fruit f : fruits) {
            Image img = switch (f.getType()) {
                case CHERRY     -> imageLoader.getCherryImage();
                case APPLE      -> imageLoader.getAppleImage();
                default         -> imageLoader.getStrawberryImage();
            };
            gc.drawImage(img, f.getX(), f.getY(), PacMan.TILE_SIZE, PacMan.TILE_SIZE);
        }
    }

    public synchronized void resetLevel() {
        fruits.clear();
        spawnCount = 0;
        if (running) {
            worker.interrupt();
            running = false;
        }
        resetTimers();
    }

    public synchronized void resetAfterLifeLost() {
        fruits.clear();
        if (running) {
            worker.interrupt();
            running = false;
        }
        resetTimers();
    }

    private static class Fruit {
        private final int x, y;
        private final FruitType type;
        Fruit(int x, int y, FruitType t) {
            this.x = x;
            this.y = y;
            this.type = t;
        }
        int getX() { return x; }
        int getY() { return y; }
        FruitType getType() { return type; }
    }

    public enum FruitType {
        CHERRY(200), APPLE(400), STRAWBERRY(800);
        private final int score;
        FruitType(int s) { score = s; }
        public int getScore() { return score; }
    }   

    public void consumeFruit(FruitType type) {
        fruits.removeIf(f -> f.getType() == type);
    }


    private void sleepWithPause(long duration) throws InterruptedException {
        long target = System.currentTimeMillis() + duration;
        while (running) {
            long remain = target - System.currentTimeMillis();
            if (remain <= 0) return;
            Thread.sleep(Math.min(remain, 200));
        }
    }
}
