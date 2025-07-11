package com.pacman;

import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import com.strategy.CollisionStrategy;
import com.strategy.AllTypeCollisionsStrategy;
import com.strategy.MovementStrategy;
import com.strategy.KeyboardMovementStrategy;

public class PacMan extends Pane {
    private CollisionStrategy collisionStrategy;
    private MovementStrategy movementStrategy;
    public static final int TILE_SIZE    = 32;
    public static final int ROW_COUNT    = 22;
    public static final int COLUMN_COUNT = 19;
    public static final int BOARD_WIDTH  = COLUMN_COUNT * TILE_SIZE;
    public static final int BOARD_HEIGHT = ROW_COUNT * TILE_SIZE;
    private boolean started = false;
    private final GraphicsContext gc;
    private final AnimationTimer gameLoop = new AnimationTimer() {
        @Override
        public void handle(long now) {
            if (!gameOver && !flashing) {
                // 1) input buffering (prima dei movimenti)
                if (storedDirection != null &&
                        gameMap.canMove(pacman, storedDirection)) {
                    currentDirection = storedDirection;
                    storedDirection  = null;
                }

                // 2) DELEGA SPAZIO + animazione bocca + tunnel
                movementStrategy.move(
                        pacman,
                        currentDirection,
                        gameMap,
                        speedMultiplier
                );

                // 3) raccolta cibo
                Block foodBlock = gameMap.getCollidingFoodBlock(pacman);
                if (foodBlock != null) {
                    collisionStrategy.handleFoodCollision(PacMan.this, foodBlock);
                    SoundManager.playSound("dot");
                }

                Block powerFoodBlock = gameMap.getCollidingPowerFoodBlock(pacman);
                if (powerFoodBlock != null) {
                    boolean wasPresent = gameMap.getPowerFoods().contains(powerFoodBlock);
                    collisionStrategy.handlePowerFoodCollision(PacMan.this, powerFoodBlock);
                    boolean isRemoved = !gameMap.getPowerFoods().contains(powerFoodBlock);
                    if (wasPresent && isRemoved) {
                        SoundManager.playSound("fruit");
                    }
                }


                // 4) raccolta frutta
                FruitManager.FruitType type = fruitManager.collectFruit(pacman);
                if (type != null) {
                    collisionStrategy.handleFruitCollision(PacMan.this, type);
                    SoundManager.playSound("fruit");
                }

                // 5) fantasmi & collisioni
                ghostManager.moveGhosts();
                ghostManager.handleGhostCollisionsWithStrategy(PacMan.this, collisionStrategy, PacMan.this::loseLife);
                score = scoreManager.getScore(); // 🔥 sincronizza lo score visualizzato



                // 6) disegno e avanzamento livello
                draw();
                if (gameMap.getFoods().isEmpty() &&
                        gameMap.getPowerFoodCount() == 0) {
                    nextLevel();
                }
            }
        }
    };
    private Block pacman;
    private int   score  = 0;
    private int   lives  = 3;
    private int   level  = 1;
    private boolean gameOver          = false;
    private boolean flashing          = false;
    private boolean waitingForRestart = false;
    private boolean waitingForLifeKey = false;
    private double speedMultiplier = 1.0;
    private KeyCode currentDirection = null;
    private KeyCode storedDirection  = null;
    private final ImageLoader   imageLoader;
    final GameMap               gameMap;
    private final FruitManager fruitManager;
    private final GhostManager  ghostManager;
    private final ScoreManager  scoreManager;
    private final Font scoreFont;
    private final Font gameOverFont;
    private final Font returnKeyFont;
    private final MainMenu mainMenu;
    private final SoundManager soundManager = new SoundManager();
    private boolean waitingForStartSound = false;
    private boolean waitingForDeathSound = false;

    public PacMan(MainMenu menu) {
        this.mainMenu = menu;

        SoundManager.loadSound("start",     "sounds/start.wav");
        SoundManager.loadSound("death",     "sounds/death.wav");
        SoundManager.loadSound("dot",       "sounds/dot.wav");
        SoundManager.loadSound("fruit",     "sounds/fruit.wav");
        SoundManager.loadSound("eat_ghost", "sounds/eat_ghost.wav");
        SoundManager.loadSound("siren_ghost", "sounds/siren_ghost.wav");

        Canvas canvas = new Canvas(BOARD_WIDTH, BOARD_HEIGHT + TILE_SIZE);
        gc = canvas.getGraphicsContext2D();
        getChildren().add(canvas);
        scoreFont     = Font.loadFont(getClass().getResource("/assets/fonts/PressStart2P.ttf").toExternalForm(), 12);
        gameOverFont  = Font.loadFont(getClass().getResource("/assets/fonts/PressStart2P.ttf").toExternalForm(), 48);
        returnKeyFont = Font.loadFont(getClass().getResource("/assets/fonts/PressStart2P.ttf").toExternalForm(), 16);
        imageLoader  = new ImageLoader();
        gameMap      = new GameMap(imageLoader);
        fruitManager = new FruitManager(this, imageLoader);
        ghostManager = new GhostManager(
                gameMap.getGhosts(),
                gameMap.getGhostPortal(),
                gameMap.getPowerFoods(),
                gameMap,
                this,
                soundManager
        );

        gameMap.resetEntities();
        ghostManager.resetGhosts(gameMap.getGhosts(), gameMap.getGhostPortal(), gameMap.getPowerFoods());
        scoreManager = new ScoreManager(scoreFont, imageLoader);
        this.collisionStrategy = new AllTypeCollisionsStrategy(ghostManager, fruitManager, scoreManager, gameMap);
        movementStrategy = new KeyboardMovementStrategy();
        pacman       = gameMap.getPacman();

        setFocusTraversable(true);

        setOnKeyPressed(e -> {
            KeyCode code = e.getCode();

            if (waitingForStartSound || waitingForDeathSound || flashing) {
                return;
            }
            // START iniziale
            if (!started) {
                if (keyToDir(code) != null) {
                    started = true;
                    gameMap.setFirstLoad(false);
                    currentDirection = code;
                    applyImage(currentDirection);
                    fruitManager.startFruitTimer();
                    ghostManager.startCageTimers();
                    startGameLoop();
                }
                return;
            }

            // RIPRESA dopo vita persa
            if (waitingForLifeKey) {
                if (keyToDir(code) == null) return;
                waitingForLifeKey = false;
                started = true;
                currentDirection = code;
                applyImage(currentDirection);
                fruitManager.startFruitTimer();
                ghostManager.startCageTimers();
                startGameLoop();
                return;
            }

            // RIPRESA dopo flashWalls / cambio livello
            if (waitingForRestart) {
                if (keyToDir(code) == null) return;
                waitingForRestart = false;
                started = true;
                currentDirection = code;
                applyImage(currentDirection);
                fruitManager.startFruitTimer();
                ghostManager.startCageTimers();
                startGameLoop();
                return;
            }

            if (waitingForRestart) {
                if (keyToDir(code) == null) return;
                waitingForRestart = false;
                started = true;
                currentDirection = code;
                applyImage(currentDirection);
                // Ora che l'utente ha premuto la prima freccia:
                fruitManager.startFruitTimer();
                ghostManager.startCageTimers();
                gameLoop.start();
                return;
            }

            // GAME OVER -> menu
            if (gameOver) {
                mainMenu.returnToMenu();
                return;
            }

            // GIOCO ATTIVO: bufferizzo
            if (keyToDir(code) != null) {
                storedDirection = code;
            }
        });

        draw();

        waitingForStartSound = true;
        Clip startClip = SoundManager.getClip("start");
        if (startClip != null) {
            startClip.addLineListener(evt -> {
                if (evt.getType() == LineEvent.Type.STOP) {
                    Platform.runLater(() -> waitingForStartSound = false);
                }
            });
            startClip.setFramePosition(0);
            startClip.start();
        }
        setOnMouseClicked(e -> {
            requestFocus();

            double mouseX = e.getX();
            double mouseY = e.getY();

            double iconSize = TILE_SIZE * 0.8;
            double iconX = BOARD_WIDTH - iconSize - 5;
            double iconY    = BOARD_HEIGHT + TILE_SIZE - iconSize - 5;

            if (mouseX >= iconX && mouseX <= iconX + iconSize &&
                    mouseY >= iconY && mouseY <= iconY + iconSize) {
                scoreManager.toggleMute();
                if (scoreManager.isMuted()) {
                    SoundManager.muteAll();
                } else {
                    SoundManager.unmuteAll();
                }
                draw();
            }
        });
    }

    // Crea e avvia il ciclo principale del gioco (chiamato ogni frame).
    private void startGameLoop() {
        gameLoop.start();
    }

    public int getReadyRow() {
        return 11;
    }

    // Converte un tasto premuto nella direzione corrispondente
    private Direction keyToDir(KeyCode k) {
        return switch (k) {
            case UP    -> Direction.UP;
            case DOWN  -> Direction.DOWN;
            case LEFT  -> Direction.LEFT;
            case RIGHT -> Direction.RIGHT;
            default    -> null;
        };
    }

    // Restituisce il blocco di Pac-Man (posizione e dimensione).
    public Block getPacmanBlock() {
        return pacman;
    }

    public Direction getPacmanDirection() {
        if (currentDirection != null) {
            Direction d = keyToDir(currentDirection);
            if (d != null) return d;
        }
        return Direction.randomDirection();
    }
    // Disegna tutti gli elementi grafici
    private void draw() {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT + TILE_SIZE);

        gc.save();
        gc.translate(0, TILE_SIZE);
        gameMap.draw(gc);
        fruitManager.draw(gc);
        gc.drawImage(pacman.image, pacman.x, pacman.y, TILE_SIZE, TILE_SIZE);
        ghostManager.draw(gc);
        ghostManager.drawPortal(gc);
        gc.restore();
        scoreManager.drawScoreboard(gc,lives,score,level);
        if (gameOver) {
            drawGameOver();
        }
    }

    private void applyImage(KeyCode dir) {
        switch (dir) {
            case UP    -> pacman.image = imageLoader.getPacmanUpImage();
            case DOWN  -> pacman.image = imageLoader.getPacmanDownImage();
            case LEFT  -> pacman.image = imageLoader.getPacmanLeftImage();
            case RIGHT -> pacman.image = imageLoader.getPacmanRightImage();
            default    -> { }
        }
    }

    private double getTextWidth(String text, Font font) {
        javafx.scene.text.Text t = new javafx.scene.text.Text(text);
        t.setFont(font);
        return t.getLayoutBounds().getWidth();
    }
    /* Gestisce la perdita di una vita: blocca il gioco, riproduce suono death, imposta lo stato per
       continuare o terminare.*/
    private void loseLife() {
        SoundManager.stopSound("siren_ghost");
        gameLoop.stop();
        speedMultiplier = 1.0;
        ghostManager.unfreeze();
        fruitManager.pauseFruitTimer();
        currentDirection = null;
        storedDirection  = null;
        lives--;
        waitingForDeathSound = true;
        Clip deathClip = SoundManager.getClip("death");
        if (deathClip != null) {
            deathClip.addLineListener(evt -> {
                if (evt.getType() == LineEvent.Type.STOP) {
                    Platform.runLater(this::proceedAfterDeathSound);
                }
            });
            deathClip.setFramePosition(0);
            deathClip.start();
        }
    }

    private void drawGameOver() {
        gc.setFill(Color.ORANGE);
        gc.setFont(gameOverFont);
        String msg = "GAME OVER";
        double w = getTextWidth(msg, gameOverFont);
        gc.fillText(msg, (BOARD_WIDTH - w) / 2, (BOARD_HEIGHT + TILE_SIZE) / 2);
        String prompt = "PRESS ANY KEY TO RETURN";
        gc.setFill(Color.YELLOW);
        gc.setFont(returnKeyFont);
        double pw = getTextWidth(prompt, returnKeyFont);
        gc.fillText(prompt, (BOARD_WIDTH - pw) / 2, (BOARD_HEIGHT + TILE_SIZE) / 2 + 40);
    }

    private void nextLevel() {
        SoundManager.stopSound("siren_ghost");
        ghostManager.unfreeze();            // fermo ogni freeze residuo
        speedMultiplier = 1.0;
        level++;
        if (level % 3 == 1 && lives < 3) lives++;
        flashing = true;
        gameLoop.stop();
        started = false;
        storedDirection = null;
        currentDirection = null;
        flashWalls();
    }



    public int getCurrentLevel() { return level; }
    public void setSpeedMultiplier(double m) { speedMultiplier = m; }
    // Restituisce il moltiplicatore di velocità attuale
    public double getSpeedMultiplier()   { return speedMultiplier; }
    public void freezeGhosts(long durationMs) { ghostManager.freeze(durationMs); }

    private void flashWalls() {
        new Thread(() -> {
            try {
                for (int i = 0; i < 3; i++) {
                    gameMap.setWallImage(imageLoader.getWallWhiteImage()); draw(); Thread.sleep(500);
                    gameMap.setWallImage(imageLoader.getWallImage());      draw(); Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                // Termino l’effetto lampeggio e ricarico lo stato per il nuovo livello
                flashing = false;
                fruitManager.resetLevel();
                gameMap.reload();
                gameMap.resetEntities();
                pacman = gameMap.getPacman();
                ghostManager.resetGhosts(
                        gameMap.getGhosts(),
                        gameMap.getGhostPortal(),
                        gameMap.getPowerFoods()
                );
                // Aspetto il primo input per far ripartire il gioco
                waitingForRestart = true;
                draw();  // mostro READY! finché non parte il primo input
            }
        }).start();
    }

    public void die() {
        loseLife();
    }

    public void undoMove(){
    }

    private void proceedAfterDeathSound() {
        waitingForDeathSound = false;
        if (lives <= 0) {
            gameOver = true;
            draw();
            return;
        }
        // Ripristino stato di gioco per la nuova vita
        started = false;                  // permettere di ri-avviare con una direzione
        currentDirection = null;
        storedDirection  = null;
        gameMap.resetEntities();
        pacman = gameMap.getPacman();
        ghostManager.resetGhosts(
                gameMap.getGhosts(),
                gameMap.getGhostPortal(),
                gameMap.getPowerFoods()
        );
        ghostManager.startCageTimers();
        fruitManager.resetAfterLifeLost();
        // Richiedo nuovamente un tasto direzionale per ripartire
        waitingForLifeKey = false;
        draw();
    }
}