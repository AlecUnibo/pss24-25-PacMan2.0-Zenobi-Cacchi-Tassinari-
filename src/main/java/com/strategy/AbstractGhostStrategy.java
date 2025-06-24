package com.strategy;

import com.pacman.Block;
import com.pacman.Direction;
import com.pacman.GameMap;
import com.pacman.PacMan;

import java.util.*;

/**
 * Strategia astratta con metodi comuni per il movimento dei fantasmi.
 * Copia la logica esistente in GhostManager per collisioni, direzioni disponibili, random timer, chase, predizione Pac-Man.
 */
public abstract class AbstractGhostStrategy implements GhostMovementStrategy {
    protected final GameMap map;
    protected final PacMan game;
    protected final Random rand;
    protected final Map<Block, Long> nextChangeTime;

    // Costanti copiate da GhostManager
    public static final int GHOST_SPEED = 2;
    protected static final long ORANGE_PHASE_MS = 5_000L;
    protected static final long PINK_PHASE_MS = 10_000L;
    protected static final int PINK_PREDICT_TILES = 4;

    public AbstractGhostStrategy(GameMap map, PacMan game) {
        this.map = map;
        this.game = game;
        this.rand = new Random();
        this.nextChangeTime = new HashMap<>();
    }

    /**
     * Intervallo casuale tra 4 e 6 secondi.
     */
    protected long randomInterval() {
        return (4 + rand.nextInt(3)) * 1000L;
    }

    /**
     * Ritorna la lista di direzioni libere (senza muro) da una posizione centrata su griglia.
     */
    protected List<Direction> availableDirections(Block g) {
        List<Direction> ok = new ArrayList<>();
        for (Direction d : Direction.values()) {
            int nx = g.x + d.dx * GHOST_SPEED;
            int ny = g.y + d.dy * GHOST_SPEED;
            if (!collidesWithWall(nx, ny)) ok.add(d);
        }
        return ok;
    }

    /**
     * Direzione random libera o mantenimento direzione corrente se nessuna disponibile.
     */
    protected Direction randomAvailable(Block g) {
        List<Direction> ok = availableDirections(g);
        if (ok.isEmpty()) return g.direction;
        return ok.get(rand.nextInt(ok.size()));
    }

    /**
     * Verifica collisione con muro o porta.
     */
    protected boolean collidesWithWall(int x, int y) {
        Block test = new Block(null, x, y, PacMan.TILE_SIZE, PacMan.TILE_SIZE, null);
        return map.isCollisionWithWallOrPortal(test);
    }

    /**
     * Sceglie direzione basata su timer: se scaduto, ricalcola random e aggiorna nextChangeTime.
     */
    protected Direction timedRandom(Block g, long now) {
        Long t = nextChangeTime.getOrDefault(g, 0L);
        if (now >= t) {
            Direction d = randomAvailable(g);
            nextChangeTime.put(g, now + randomInterval());
            return d;
        }
        return g.direction;
    }

    /**
     * Modalità chase: direzione verso Pac-Man.
     */
    protected Direction chase(Block g) {
        Block pac = game.getPacmanBlock();
        Point target = new Point(pac.x, pac.y);
        return bestAvailableDirection(g, target);
    }

    /**
     * Predice la posizione di Pac-Man per Pink.
     */
    protected Point predictedPacmanTarget() {
        Block pac = game.getPacmanBlock();
        Direction pd = game.getPacmanDirection();
        return new Point(
            pac.x + pd.dx * PINK_PREDICT_TILES * PacMan.TILE_SIZE,
            pac.y + pd.dy * PINK_PREDICT_TILES * PacMan.TILE_SIZE
        );
    }

    /**
     * Sceglie la direzione che minimizza la distanza dal target.
     */
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

    /**
     * Classe di supporto per coordinate.
     */
    protected static class Point {
        final int x, y;
        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
