package com.pacman;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.skin.TextInputControlSkin.Direction;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class PacMan extends StackPane {

    public static final int TILE_SIZE    = 32;
    public static final int ROW_COUNT    = 22;
    public static final int COLUMN_COUNT = 19;
    public static final int BOARD_WIDTH  = COLUMN_COUNT * TILE_SIZE;
    public static final int BOARD_HEIGHT = ROW_COUNT * TILE_SIZE;
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final ImageLoader imageLoader;
    private final GameMap gameMap;
    private Block pacman;
    private KeyCode currentDirection = null;
    private KeyCode storedDirection = null;
    private boolean mouthOpen = true;
    private int animationCounter = 0;
    private boolean inTunnel = false;
    private final ScoreManager scoreManager = new ScoreManager();
    private final GhostManager ghostManager;

    public PacMan(MainMenu menu) {
        // dimensioni del pannello
        setPrefSize(BOARD_WIDTH, BOARD_HEIGHT);

        // inizializza il canvas
        canvas = new Canvas(BOARD_WIDTH, BOARD_HEIGHT);
        gc     = canvas.getGraphicsContext2D();
        getChildren().add(canvas);

        // carica risorse e mappa
        imageLoader = new ImageLoader();
        gameMap     = new GameMap(imageLoader);
        ghostManager = new GhostManager(gameMap.getGhosts(), imageLoader, scoreManager);


        pacman = gameMap.getPacman();
        setFocusTraversable(true);
        setOnKeyPressed(e -> {
            if (keyToDir(e.getCode()) != null) {
                storedDirection = e.getCode();
            }
        });

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                render();
            }
        };
        timer.start();

    }

    private void render() {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
        gameMap.draw(gc);
        movePacman();
        gameMap.getGhosts().forEach(g -> {
            if (g.image != null) {
                gc.drawImage(g.image, g.x, g.y, TILE_SIZE, TILE_SIZE);
            }
        });

        if (pacman != null && pacman.image != null) {
            gc.drawImage(pacman.image, pacman.x, pacman.y, TILE_SIZE, TILE_SIZE);
        }
        gc.setFill(Color.YELLOW);
        gc.fillText("Score: " + scoreManager.getScore(), 10, 20);
        gc.fillText("Lives: " + scoreManager.getLives(), 10, 40);

    }

    private void movePacman() {
        if (storedDirection != null && gameMap.canMove(pacman, storedDirection)) {
            currentDirection = storedDirection;
            storedDirection = null;
        }

        if (currentDirection == null || !gameMap.canMove(pacman, currentDirection)) {
            return;
        }

        switch (currentDirection) {
            case UP -> pacman.y -= 4;
            case DOWN -> pacman.y += 4;
            case LEFT -> pacman.x -= 4;
            case RIGHT -> pacman.x += 4;
        }

        animationCounter++;
        if (animationCounter >= 10) {
            mouthOpen = !mouthOpen;
            animationCounter = 0;
        }

        applyImage(currentDirection);
    }

    private void applyImage(KeyCode dir) {
        if (!mouthOpen) {
            pacman.image = imageLoader.getPacmanClosedImage();
            return;
        }
        switch (dir) {
            case UP -> pacman.image = imageLoader.getPacmanUpImage();
            case DOWN -> pacman.image = imageLoader.getPacmanDownImage();
            case LEFT -> pacman.image = imageLoader.getPacmanLeftImage();
            case RIGHT -> pacman.image = imageLoader.getPacmanRightImage();
        }
    }

    private Direction keyToDir(KeyCode k) {
        return switch (k) {
            case UP -> Direction.UP;
            case DOWN -> Direction.DOWN;
            case LEFT -> Direction.LEFT;
            case RIGHT -> Direction.RIGHT;
            default -> null;
        };
    }

    private void update() {
        Block pacman = gameMap.getPacman();

        // Gestione tunnel
        if (!inTunnel) {
            for (Block t : gameMap.getTunnels()) {
                if (collision(pacman, t)) {
                    inTunnel = true;
                    gameMap.wrapAround(pacman);
                    break;
                }
            }
        } else {
            boolean stillInTunnel = false;
            for (Block t : gameMap.getTunnels()) {
                if (collision(pacman, t)) {
                    stillInTunnel = true;
                    break;
                }
            }
            if (!stillInTunnel) inTunnel = false;
        }

        // Collisione con food
        int points = gameMap.collectFood(pacman);
        if (points > 0) scoreManager.addPoints(points);

        // Collisione con powerfood
        if (gameMap.collectPowerFood(pacman)) {
            scoreManager.addPoints(50);
            ghostManager.triggerScaredMode();

        }

        // Collisione con fantasmi
        for (Block ghost : gameMap.getGhosts()) {
            if (collision(pacman, ghost)) {
                ghostManager.update();
                ghostManager.checkCollisions(pacman);
                scoreManager.loseLife();
                if (scoreManager.isGameOver()) {
                    System.out.println("!Game Over!");
                    // eventualmente mostrare schermata di fine gioco
                } else {
                    // Resetta solo posizione di pacman e fantasmi
                    gameMap.resetEntities();
                }
                break;
            }
        }
    }


    private boolean collision(Block a, Block b) {
        return a.x < b.x + b.width &&
                a.x + a.width > b.x &&
                a.y < b.y + b.height &&
                a.y + a.height > b.y;
    }

     int foodScore = gameMap.collectFood(pacman);
        if (foodScore > 0) {
            SoundManager.playSound("dot");
            score += foodScore;
        }
        if (gameMap.collectPowerFood(pacman)) {
            score += 50;
            ghostManager.activateScaredMode();
        }
        int prevScore = score;
        score += fruitManager.collectFruit(pacman);
        if (score > prevScore) {
            int gained = score - prevScore;
            FruitManager.FruitType type = switch (gained) {
                case 200 -> FruitManager.FruitType.CHERRY;
                case 400 -> FruitManager.FruitType.APPLE;
                case 800 -> FruitManager.FruitType.STRAWBERRY;
                default  -> null;
            };
            if (type != null) scoreManager.addCollectedFruit(type);
            SoundManager.playSound("fruit");
        }
    }
