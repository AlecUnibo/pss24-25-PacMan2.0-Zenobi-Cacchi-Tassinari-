package com.pacman;

import com.strategy.*;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.*;

public class GhostManager {

    private static final long SCARED_DURATION_MS = 6_000;
    private final List<Block> ghosts;
    private final List<Block> cagedGhosts;
    private final List<RespawnGhost> respawningGhosts;
    private Block ghostPortal;
    private final GameMap map;
    private final PacMan game;
    private final Map<Block, Long> cageReleaseTime = new HashMap<>();
    private static final long BLUE_DELAY_MS = 2_000;
    private static final long ORANGE_DELAY_MS = 4_000;
    private static final long PINK_DELAY_MS = 6_000;
    private boolean cageTimerStarted = false;
    private boolean ghostsAreScared = false;
    private long scaredEndTime = 0;
    private final Image scaredGhostImage;
    private final Image whiteGhostImage;
    private final ImageLoader imageLoader;
    private final Set<Block> ghostsInTunnel = new HashSet<>();
    private boolean frozen = false;
    private long frozenEndTime = 0;

    // Strategie base per ogni fantasma
    private final Map<Block, GhostMovementStrategy> baseStrategies = new HashMap<>();
    private final ScaredGhostStrategy scaredStrategy;

    public GhostManager(List<Block> allGhosts,
                        Block ghostPortal,
                        List<Block> powerFoods,
                        GameMap map,
                        PacMan game,
                        SoundManager soundManager) {
        this.map = map;
        this.imageLoader = new ImageLoader();
        this.scaredGhostImage = imageLoader.getScaredGhostImage();
        this.whiteGhostImage = imageLoader.getWhiteGhostImage();
        this.ghosts = new ArrayList<>();
        this.cagedGhosts = new ArrayList<>();
        this.respawningGhosts = new ArrayList<>();
        this.ghostPortal = ghostPortal;
        this.game = game;
        this.scaredStrategy = new ScaredGhostStrategy(map, game);

        // Inizializza fantasmi: RED parte subito, altri in gabbia
        Block red = allGhosts.stream()
            .filter(g -> g.ghostType == Block.GhostType.RED)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Manca il fantasma RED!"));
        ghosts.add(red);
        baseStrategies.put(red, createStrategyForType(red));
        allGhosts.stream()
                 .filter(g -> g != red)
                 .forEach(g -> {
                     cagedGhosts.add(g);
                     baseStrategies.put(g, createStrategyForType(g));
                 });
    }

    public void resetGhosts(List<Block> allGhosts,
                            Block newPortal,
                            List<Block> newPowerFoods) {
        ghosts.clear();
        cagedGhosts.clear();
        respawningGhosts.clear();
        cageReleaseTime.clear();
        baseStrategies.clear();
        this.ghostPortal = newPortal;
        ghostsAreScared = false;
        scaredEndTime = 0;
        cageTimerStarted = false;

        Block red = allGhosts.stream()
            .filter(g -> g.ghostType == Block.GhostType.RED)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Manca il fantasma RED!"));
        ghosts.add(red);
        baseStrategies.put(red, createStrategyForType(red));
        allGhosts.stream()
                 .filter(g -> g != red)
                 .forEach(g -> {
                     cagedGhosts.add(g);
                     baseStrategies.put(g, createStrategyForType(g));
                 });
    }

    // Avvia i timer per liberare progressivamente i fantasmi dalla gabbia
    public void startCageTimers() {
        if (cageTimerStarted) return;
        cageTimerStarted = true;
        long zero = System.currentTimeMillis();
        for (Block g : cagedGhosts) {
            long delay = switch (g.ghostType) {
                case BLUE -> BLUE_DELAY_MS;
                case ORANGE -> ORANGE_DELAY_MS;
                case PINK -> PINK_DELAY_MS;
                default -> 0L;
            };
            cageReleaseTime.put(g, zero + delay);
        }
    }

    // Disegna i fantasmi, gestendo l’effetto “spaventato” e il blinking
    public void draw(GraphicsContext gc) {
        long timeLeft = Math.max(0, scaredEndTime - System.currentTimeMillis());
        boolean blinking = timeLeft > 0 && timeLeft <= 3000 && ((timeLeft / 500) % 2 == 1);
        for (Block g : ghosts) {
            Image img = (!g.isScared) ? g.image : (blinking ? whiteGhostImage : scaredGhostImage);
            gc.drawImage(img, g.x, g.y, PacMan.TILE_SIZE, PacMan.TILE_SIZE);
        }
        for (Block g : cagedGhosts) {
            Image img = (!g.isScared) ? g.image : (blinking ? whiteGhostImage : scaredGhostImage);
            gc.drawImage(img, g.x, g.y, PacMan.TILE_SIZE, PacMan.TILE_SIZE);
        }
    }

    // Disegna il portale da cui escono i fantasmi
    public void drawPortal(GraphicsContext gc) {
        if (ghostPortal != null) {
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(4);
            gc.strokeLine(
                ghostPortal.x, ghostPortal.y + 2,
                ghostPortal.x + PacMan.TILE_SIZE, ghostPortal.y + 2
            );
        }
    }

    // Attiva la modalità spaventato per tutti i fantasmi
    public void activateScaredMode() {
        ghostsAreScared = true;
        scaredEndTime = System.currentTimeMillis() + SCARED_DURATION_MS;
        for (Block g : ghosts) {
            g.isScared = true;
            g.setScared(true); // <--- aggiunto
        }
        for (Block g : cagedGhosts) {
            g.isScared = true;
            g.setScared(true); // <--- aggiunto
        }

        SoundManager.loopSound("siren_ghost");
    }

    private void updateScaredState() {
        if (ghostsAreScared && System.currentTimeMillis() > scaredEndTime) {
            ghostsAreScared = false;
            for (Block g : ghosts) {
                g.isScared = false;
                g.setScared(false);
                g.image = g.originalImage;
            }
            for (Block g : cagedGhosts) {
                g.isScared = false;
                g.setScared(false);
                g.image = g.originalImage;
            }
            SoundManager.stopSound("siren_ghost");
        }
    }

    // Gestisce collisioni Pac-Man vs fantasmi, restituendo punti o invocando onHit
    public int handleGhostCollisions(Block pacman, Runnable onHit) {
        updateScaredState();
        int points = 0;
        List<Block> eaten = new ArrayList<>();
        for (Block g : ghosts) {
            boolean collided = pacman.x < g.x + g.width &&
                               pacman.x + pacman.width > g.x &&
                               pacman.y < g.y + g.height &&
                               pacman.y + pacman.height > g.y;
            if (!collided) continue;
            if (g.isScared) {
                points += 200;
                eaten.add(g);
                SoundManager.playSound("eat_ghost");
            } else {
                onHit.run();
                return 0;
            }
        }
        for (Block g : eaten) {
            ghosts.remove(g);
            scheduleGhostRespawn(g);
        }
        return points;
    }

    public void scheduleGhostRespawn(Block g) {
        g.isScared = false;
        g.image = g.originalImage;
        g.x = ghostPortal.x + (ghostPortal.width - g.width) / 2;
        g.y = ghostPortal.y + (ghostPortal.height - g.height) / 2;
        g.direction = Direction.UP;
        g.isExiting = true;
        respawningGhosts.add(new RespawnGhost(g, System.currentTimeMillis() + 4000));
    }

    private void checkRespawningGhosts() {
        long now = System.currentTimeMillis();
        for (Iterator<RespawnGhost> it = respawningGhosts.iterator(); it.hasNext();) {
            RespawnGhost rg = it.next();
            if (now >= rg.respawnTime) {
                boolean stillScared = now < scaredEndTime;
                rg.ghost.isScared = stillScared;
                rg.ghost.image = stillScared ? scaredGhostImage : rg.ghost.originalImage;
                rg.ghost.image = rg.ghost.isScared ? scaredGhostImage : rg.ghost.originalImage;
                ghosts.add(rg.ghost);
                it.remove();
            }
        }
    }


    private void checkCagedGhostsRelease() {
        if (!cageTimerStarted) return;
        long now = System.currentTimeMillis();
        Iterator<Block> it = cagedGhosts.iterator();
        while (it.hasNext()) {
            Block g = it.next();
            Long releaseAt = cageReleaseTime.get(g);
            if (releaseAt != null && now >= releaseAt) {
                it.remove();
                g.x = ghostPortal.x + (ghostPortal.width - g.width) / 2;
                g.y = ghostPortal.y + (ghostPortal.height - g.height) / 2;
                g.isExiting = true;
                g.direction = Direction.UP;
                g.isScared = ghostsAreScared;
                g.image = g.isScared ? scaredGhostImage : g.originalImage;
                ghosts.add(g);
            }
        }
    }

    public void moveGhosts() {
        long now = System.currentTimeMillis();
        if (frozen && now < frozenEndTime) return;
        frozen = false;
        
        updateScaredState();
        checkRespawningGhosts();
        checkCagedGhostsRelease();

        ghosts.sort(Comparator.comparingInt(g -> g.ghostType.ordinal()));

        for (Block g : ghosts) {
            if (g.isExiting) {
                // Uscita dalla gabbia: sempre verso l'alto
                g.y -= AbstractGhostStrategy.GHOST_SPEED;
                if (g.y + g.height < ghostPortal.y) {
                    g.isExiting = false;
                    g.image = g.originalImage;
                    g.direction = g.direction; // mantiene la direzione
                }
                handleWrap(g);
                continue;
            }

            GhostMovementStrategy strategy;
            if (g.isScared) {
                strategy = scaredStrategy;
            } else {
                strategy = baseStrategies.get(g);
            }
            Direction next = strategy.getNextDirection(g, now);
            moveAlong(g, next);
            handleWrap(g);
        }
    }


    private void moveAlong(Block g, Direction d) {
        int nx = g.x + d.dx * AbstractGhostStrategy.GHOST_SPEED;
        int ny = g.y + d.dy * AbstractGhostStrategy.GHOST_SPEED;
        boolean free = !collidesWithWall(nx, ny);
        boolean onGridX = (g.x % PacMan.TILE_SIZE) == 0;
        boolean onGridY = (g.y % PacMan.TILE_SIZE) == 0;
        if (!(onGridX && onGridY)) {
            Direction cur = g.direction;
            int cx = g.x + cur.dx * AbstractGhostStrategy.GHOST_SPEED;
            int cy = g.y + cur.dy * AbstractGhostStrategy.GHOST_SPEED;
            if (!collidesWithWall(cx, cy)) {
                g.x = cx;
                g.y = cy;
                return;
            }
        }
        if (free) {
            g.x = nx;
            g.y = ny;
            g.direction = d;
            return;
        }
        List<Direction> freeDirs = availableDirections(g);
        if (!freeDirs.isEmpty()) {
            Direction alt = freeDirs.get(new Random().nextInt(freeDirs.size()));
            g.x += alt.dx * AbstractGhostStrategy.GHOST_SPEED;
            g.y += alt.dy * AbstractGhostStrategy.GHOST_SPEED;
            g.direction = alt;
        }
    }

    private void handleWrap(Block g) {
        if (isOnTunnel(g)) {
            if (!ghostsInTunnel.contains(g)) {
                map.wrapAround(g);
                ghostsInTunnel.add(g);
            }
        } else {
            ghostsInTunnel.remove(g);
        }
    }

    public int handleGhostCollisionsWithStrategy(PacMan pacman, CollisionStrategy strategy, Runnable onLifeLost) {
        int score = 0;
        Block pacmanBlock = pacman.getPacmanBlock();  // fondamentale!

        for (Block g : ghosts) {
            if (g.isVisible() && g.collidesWith(pacmanBlock)) {
                if (g.isScared()) {
                    strategy.handleScaredGhostCollision(pacman, g);
                    score += 200;
                } else {
                    strategy.handleGhostCollision(pacman, g);
                    onLifeLost.run();
                }
            }
        }

        return score;
    }


    private boolean collidesWithWall(int x, int y) {
        Block test = new Block(null, x, y, PacMan.TILE_SIZE, PacMan.TILE_SIZE, null);
        return map.isCollisionWithWallOrPortal(test);
    }

    private List<Direction> availableDirections(Block g) {
        List<Direction> ok = new ArrayList<>();
        for (Direction d : Direction.values()) {
            int nx = g.x + d.dx * AbstractGhostStrategy.GHOST_SPEED;
            int ny = g.y + d.dy * AbstractGhostStrategy.GHOST_SPEED;
            if (!collidesWithWall(nx, ny)) ok.add(d);
        }
        return ok;
    }

    // Congela i fantasmi per un certo intervallo
    public void freeze(long durationMs) {
        frozen = true;
        frozenEndTime = System.currentTimeMillis() + durationMs;
    }
    public void unfreeze() {
        frozen = false;
    }

    private static class RespawnGhost {
        final Block ghost;
        final long respawnTime;
        RespawnGhost(Block g, long t) { ghost = g; respawnTime = t; }
    }

    private GhostMovementStrategy createStrategyForType(Block g) {
        switch (g.ghostType) {
            case RED:
                return new RedGhostStrategy(map, game);
            case BLUE:
                return new BlueGhostStrategy(map, game);
            case ORANGE:
                return new OrangeGhostStrategy(map, game);
            case PINK:
                return new PinkGhostStrategy(map, game);
            default:
                return new RedGhostStrategy(map, game);
        }
    }

    public void removeGhost(Block ghost) {
        ghosts.remove(ghost);
    }

    private boolean isOnTunnel(Block g) {
        for (Block t : map.getTunnels()) {
            if (g.x < t.x + t.width &&
                g.x + g.width > t.x &&
                g.y < t.y + t.height &&
                g.y + g.height > t.y) {
                return true;
            }
        }
        return false;
    }
}
