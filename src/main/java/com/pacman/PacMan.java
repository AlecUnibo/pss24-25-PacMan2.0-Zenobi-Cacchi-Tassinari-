package com.pacman;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
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

        pacman = gameMap.getPacman();
        setFocusTraversable(true);

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                render();
            }
        };
        timer.start();
    }

    private void render() {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);

        gameMap.draw(gc);

        gameMap.getGhosts().forEach(g -> {
            if (g.image != null) {
                gc.drawImage(g.image, g.x, g.y, TILE_SIZE, TILE_SIZE);
            }
        });

        if (pacman != null && pacman.image != null) {
            gc.drawImage(pacman.image, pacman.x, pacman.y, TILE_SIZE, TILE_SIZE);
        }
    }
}
