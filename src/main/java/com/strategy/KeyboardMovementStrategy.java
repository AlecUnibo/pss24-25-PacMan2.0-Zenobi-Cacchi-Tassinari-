package com.strategy;

import com.pacman.Block;
import com.pacman.Direction;
import com.pacman.GameMap;
import com.pacman.ImageLoader;
import javafx.scene.input.KeyCode;

public class KeyboardMovementStrategy implements MovementStrategy {
    private static final int BASE_SPEED = 4;
    private final ImageLoader loader = new ImageLoader();
    private boolean mouthOpen = true;
    private int animationCounter = 0;
    private boolean inTunnel = false;

    /** Muove Pac-Man in base al tasto premuto, gestendo animazione e tunnel.*/
    @Override
    public void move(
            Block pacman,
            KeyCode currentDir,
            GameMap gameMap,
            double speedMultiplier
    ) {
        if (currentDir == null) return;
        int steps = Math.max(1, (int)Math.round(speedMultiplier));
        Direction dir = keyToDir(currentDir);
        if (dir == null) return;
        for (int s = 0; s < steps; s++) {
            // allineamento su griglia orizz/vert
            if (dir == Direction.LEFT || dir == Direction.RIGHT) {
                int targetY = Math.round((float)pacman.y / com.pacman.PacMan.TILE_SIZE)
                        * com.pacman.PacMan.TILE_SIZE;
                int deltaY = targetY - pacman.y;
                if (deltaY != 0) {
                    pacman.y += Integer.signum(deltaY)
                            * Math.min(BASE_SPEED, Math.abs(deltaY));
                    if (pacman.y != targetY) break;
                }
            } else {
                int targetX = Math.round((float)pacman.x / com.pacman.PacMan.TILE_SIZE)
                        * com.pacman.PacMan.TILE_SIZE;
                int deltaX = targetX - pacman.x;
                if (deltaX != 0) {
                    pacman.x += Integer.signum(deltaX)
                            * Math.min(BASE_SPEED, Math.abs(deltaX));
                    if (pacman.x != targetX) break;
                }
            }

            int nx = pacman.x + dir.dx * BASE_SPEED;
            int ny = pacman.y + dir.dy * BASE_SPEED;
            Block test = new Block(null, nx, ny, pacman.width, pacman.height, null);
            if (!gameMap.isCollisionWithWallOrPortal(test)) {
                pacman.x = nx;
                pacman.y = ny;
            } else {
                break;
            }
        }

        // animazione bocca
        animationCounter++;
        if (animationCounter >= 10) {
            mouthOpen = !mouthOpen;
            animationCounter = 0;
        }
        pacman.image = mouthOpen
                ? imageForDirection(dir)
                : loader.getPacmanClosedImage();

        // gestione tunnel
        if (!inTunnel && isOnTunnel(pacman, gameMap)) {
            inTunnel = true;
            gameMap.wrapAround(pacman);
        } else if (inTunnel && !isOnTunnel(pacman, gameMap)) {
            inTunnel = false;
        }
    }

    private Direction keyToDir(KeyCode k) {
        return switch (k) {
            case UP    -> Direction.UP;
            case DOWN  -> Direction.DOWN;
            case LEFT  -> Direction.LEFT;
            case RIGHT -> Direction.RIGHT;
            default    -> null;
        };
    }

    /** Restituisce l'immagine di Pac-Man per la direzione data.*/
    private javafx.scene.image.Image imageForDirection(Direction d) {
        return switch (d) {
            case UP    -> loader.getPacmanUpImage();
            case DOWN  -> loader.getPacmanDownImage();
            case LEFT  -> loader.getPacmanLeftImage();
            case RIGHT -> loader.getPacmanRightImage();
        };
    }

    /** Controlla se Pac-Man si trova in un tunnel per wrap-around.*/
    private boolean isOnTunnel(Block pacman, GameMap map) {
        return map.getTunnels().stream()
                .anyMatch(t -> pacman.x < t.x + t.width
                        && pacman.x + pacman.width  > t.x
                        && pacman.y < t.y + t.height
                        && pacman.y + pacman.height > t.y);
    }
}
