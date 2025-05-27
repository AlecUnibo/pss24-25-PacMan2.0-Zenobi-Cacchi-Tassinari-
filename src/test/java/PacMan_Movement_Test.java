//Sostituire questo codice a quello presente nel file PacMan.java, per testare il movimento di pacman
package com.pacman;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class PacMan_Movement_Test extends Pane {
    public static final int TILE_SIZE    = 32;
    public static final int ROW_COUNT    = 22;
    public static final int COLUMN_COUNT = 19;
    public static final int BOARD_WIDTH  = COLUMN_COUNT * TILE_SIZE;
    public static final int BOARD_HEIGHT = ROW_COUNT * TILE_SIZE;
    private static final int pacmanSPEED = 2;

    private final GraphicsContext gc;
    private AnimationTimer gameLoop;
    private Block pacman;
    private KeyCode currentDirection = null;
    private KeyCode storedDirection  = null;
    private final ImageLoader imageLoader;
    final GameMap gameMap;
    private int animationCounter = 0;
    private boolean mouthOpen = true;
    private boolean inTunnel = false;

    public PacMan_Movement_Test() {
        Canvas canvas = new Canvas(BOARD_WIDTH, BOARD_HEIGHT + TILE_SIZE);
        gc = canvas.getGraphicsContext2D();
        getChildren().add(canvas);

        imageLoader = new ImageLoader();
        gameMap = new GameMap(imageLoader);
        pacman = gameMap.getPacman();

        setFocusTraversable(true);

        setOnKeyPressed(e -> {
            KeyCode key = e.getCode();
            if (currentDirection == null) {
                startAfterReady(key);
            } else {
                // se già in movimento, memorizza la direzione da applicare appena possibile
                if (keyToDir(key) != null) {
                    storedDirection = key;
                }
            }
        });

        draw();
    }

    // Avvia il gioco al primo input valido
    private void startAfterReady(KeyCode initialDir) {
        if (keyToDir(initialDir) == null) return;
        currentDirection = initialDir;
        applyImage(currentDirection);
        startGameLoop();
    }

    // Loop di gioco: tenta di cambiare direzione e muovere Pac-Man
    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // se c'è una direzione in memoria e Pac-Man può muovercisi, la applico
                if (storedDirection != null && gameMap.canMove(pacman, storedDirection)) {
                    currentDirection = storedDirection;
                    applyImage(currentDirection);
                    storedDirection = null;
                }
                movePacman();
                draw();
            }
        };
        gameLoop.start();
    }

    // Muove Pac-Man con collisioni e wrap-around tunnel
    private void movePacman() {
        if (currentDirection == null) return;
        Direction dir = keyToDir(currentDirection);
        if (dir == null) return;

        // Muovo di pacmanSPEED pixel, rispettando i muri
        int nx = pacman.x + dir.dx * pacmanSPEED;
        int ny = pacman.y + dir.dy * pacmanSPEED;
        Block test = new Block(null, nx, ny, pacman.width, pacman.height, null);
        if (!gameMap.isCollisionWithWallOrPortal(test)) {
            pacman.x = nx;
            pacman.y = ny;
        }

        // Gestione animazione apertura/chiusura bocca
        animationCounter++;
        if (animationCounter >= 10) {
            mouthOpen = !mouthOpen;
            animationCounter = 0;
        }
        applyImage(currentDirection);

        // Logica tunnel (wrap-around)
        if (!inTunnel) {
            for (Block t : gameMap.getTunnels()) {
                if (collision(pacman, t)) {
                    inTunnel = true;
                    KeyCode prevDir    = currentDirection;
                    KeyCode prevStored = storedDirection;
                    gameMap.wrapAround(pacman);
                    currentDirection = prevDir;
                    storedDirection  = prevStored;
                    break;
                }
            }
        } else {
            boolean still = false;
            for (Block t : gameMap.getTunnels()) {
                if (collision(pacman, t)) { still = true; break; }
            }
            if (!still) inTunnel = false;
        }
    }

    // Converte KeyCode in Direction
    private Direction keyToDir(KeyCode k) {
        return switch (k) {
            case UP    -> Direction.UP;
            case DOWN  -> Direction.DOWN;
            case LEFT  -> Direction.LEFT;
            case RIGHT -> Direction.RIGHT;
            default    -> null;
        };
    }

    // Cambia l'immagine di Pac-Man in base alla direzione e allo stato della bocca
    private void applyImage(KeyCode dir) {
        if (!mouthOpen) {
            pacman.image = imageLoader.getPacmanClosedImage();
            return;
        }
        switch (dir) {
            case UP    -> pacman.image = imageLoader.getPacmanUpImage();
            case DOWN  -> pacman.image = imageLoader.getPacmanDownImage();
            case LEFT  -> pacman.image = imageLoader.getPacmanLeftImage();
            case RIGHT -> pacman.image = imageLoader.getPacmanRightImage();
            default    -> {}
        }
    }

    // Disegna lo sfondo, la mappa e Pac-Man
    private void draw() {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT + TILE_SIZE);

        gc.save();
        gc.translate(0, TILE_SIZE);
        gameMap.draw(gc);
        gc.drawImage(pacman.image, pacman.x, pacman.y, TILE_SIZE, TILE_SIZE);
        gc.restore();
    }

    // Collisione rettangolare generica
    private boolean collision(Block a, Block c) {
        return a.x < c.x + c.width &&
               a.x + a.width > c.x &&
               a.y < c.y + c.height &&
               a.y + a.height > c.y;
    }

    public Block getPacmanBlock() {
        return pacman;
    }
}
