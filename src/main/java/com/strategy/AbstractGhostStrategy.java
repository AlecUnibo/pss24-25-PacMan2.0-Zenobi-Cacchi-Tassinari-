package com.strategy;

import com.pacman.Block;
import com.pacman.Direction;
import com.pacman.GameMap;
import com.pacman.PacMan;
import java.util.*;

public abstract class AbstractGhostStrategy implements GhostMovementStrategy {
    protected final GameMap map;
    protected final PacMan game;
    protected final Random rand;
    protected final Map<Block, Long> nextChangeTime;

    public static final int GHOST_SPEED = 2;
    protected static final long ORANGE_PHASE_MS = 5_000L;
    protected static final long PINK_PHASE_MS = 10_000L;
    protected static final int PINK_PREDICT_TILES = 4;

    /** Inizializza la strategia con riferimenti alla mappa e al gioco.*/
    public AbstractGhostStrategy(GameMap map, PacMan game) {
        this.map = map;
        this.game = game;
        this.rand = new Random();
        this.nextChangeTime = new HashMap<>();
    }

    protected long randomInterval() {
        return (4 + rand.nextInt(3)) * 1000L;
    }

    /** Restituisce le direzioni libere (senza muro) dal blocco dato.*/
    protected List<Direction> availableDirections(Block g) {
        List<Direction> ok = new ArrayList<>();
        for (Direction d : Direction.values()) {
            int nx = g.x + d.dx * GHOST_SPEED;
            int ny = g.y + d.dy * GHOST_SPEED;
            if (!collidesWithWall(nx, ny)) ok.add(d);
        }
        return ok;
    }

    /** Sceglie casualmente una direzione libera.*/
    protected Direction randomAvailable(Block g) {
        List<Direction> ok = availableDirections(g);
        if (ok.isEmpty()) return g.direction;
        return ok.get(rand.nextInt(ok.size()));
    }

    /** Verifica se la posizione data collide con un muro o portale*/
    protected boolean collidesWithWall(int x, int y) {
        Block test = new Block(null, x, y, PacMan.TILE_SIZE, PacMan.TILE_SIZE, null);
        return map.isCollisionWithWallOrPortal(test);
    }

    /** Cambia direzione a intervalli casuali basandosi sul timestamp.*/
    protected Direction timedRandom(Block g, long now) {
        Long t = nextChangeTime.getOrDefault(g, 0L);
        if (now >= t) {
            Direction d = randomAvailable(g);
            nextChangeTime.put(g, now + randomInterval());
            return d;
        }
        return g.direction;
    }

    /** Insegui Pac-Man calcolando la direzione che minimizza la distanza.*/
    protected Direction chase(Block g) {
        Block pac = game.getPacmanBlock();
        Point target = new Point(pac.x, pac.y);
        return bestAvailableDirection(g, target);
    }

    /** Previsione della posizione futura di Pac-Man per il fantasma rosa.*/
    protected Point predictedPacmanTarget() {
        Block pac = game.getPacmanBlock();
        Direction pd = game.getPacmanDirection();
        return new Point(
                pac.x + pd.dx * PINK_PREDICT_TILES * PacMan.TILE_SIZE,
                pac.y + pd.dy * PINK_PREDICT_TILES * PacMan.TILE_SIZE
        );
    }

    /** Seleziona la direzione verso il punto target con distanza minima.*/
    protected Direction bestAvailableDirection(Block g, Point target) {
        double bestDist = Double.MAX_VALUE;
        Direction best = g.direction;
        for (Direction d : availableDirections(g)) {
            double nx = g.x + d.dx * GHOST_SPEED;
            double ny = g.y + d.dy * GHOST_SPEED;
            double dist = hypot(nx - target.x, ny - target.y);
            if (dist < bestDist) {
                bestDist = dist;
                best = d;
            }
        }
        return best;
    }

    private static double hypot(double dx, double dy) {
        return Math.hypot(dx, dy);
    }

    /** Rappresenta un punto 2D (pixel) per il calcolo del pathfinding.*/
    protected static class Point {
        final int x, y;
        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
