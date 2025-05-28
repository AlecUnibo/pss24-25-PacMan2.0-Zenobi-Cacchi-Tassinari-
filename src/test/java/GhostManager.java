
//Sostituire questo codice a quello presente nel file GhostManager.java, per testare il movimento dei fantasmi;
import java.util.*;

public class GhostManager {

    private static final int ghostSPEED = 2;
    private static final int PINK_PREDICT_TILES = 4;
    private final List<Block> ghosts;
    private final Block ghostPortal;
    private final GameMap map;
    private final PacMan game;
    private final Map<Block, Long> nextChangeTime = new HashMap<>();
    private final Map<Block, Boolean> orangeChaseState = new HashMap<>();
    private static final long ORANGE_PHASE_MS = 5_000;
    private static final int PINK_PHASE_MS = 10_000;
    private final Set<Block> ghostsInTunnel = new HashSet<>();
    private final Random rand = new Random();

    public GhostManager(List<Block> allGhosts, Block ghostPortal, GameMap map, PacMan game) {
        this.map = map;
        this.game = game;
        this.ghostPortal = ghostPortal;
        this.ghosts = new ArrayList<>(allGhosts);

        long now = System.currentTimeMillis();
        for (Block g : ghosts) {
            nextChangeTime.put(g, now + randomInterval());
        }
    }

    public void moveGhosts() {
        long now = System.currentTimeMillis();

        ghosts.sort(Comparator.comparingInt(g -> g.ghostType.ordinal()));
        boolean chasePhase = ((now / ORANGE_PHASE_MS) % 2) == 1;

        for (Block g : ghosts) {
            Direction next;

            switch (g.ghostType) {
                case RED, BLUE -> next = timedRandom(g, now);

                case ORANGE -> {
                    Boolean last = orangeChaseState.get(g);
                    if (last == null || last != chasePhase) {
                        next = chasePhase ? chase(g) : randomAvailable(g);
                        orangeChaseState.put(g, chasePhase);
                    } else {
                        next = g.direction;
                    }
                }

                case PINK -> {
                    long pinkPhaseTime = now % PINK_PHASE_MS;
                    next = (pinkPhaseTime < 6000)
                        ? bestAvailableDirection(g, predictedPacmanTarget())
                        : timedRandom(g, now);
                }

                default -> next = timedRandom(g, now);
            }

            moveAlong(g, next);
            handleWrap(g);
        }
    }

    private void moveAlong(Block g, Direction d) {
        int nx = g.x + d.dx * ghostSPEED;
        int ny = g.y + d.dy * ghostSPEED;
        boolean free = !collidesWithWall(nx, ny);

        boolean onGridX = (g.x % PacMan.TILE_SIZE) == 0;
        boolean onGridY = (g.y % PacMan.TILE_SIZE) == 0;
        if (!(onGridX && onGridY)) {
            Direction cur = g.direction;
            int cx = g.x + cur.dx * ghostSPEED;
            int cy = g.y + cur.dy * ghostSPEED;
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
            Direction alt = freeDirs.get(rand.nextInt(freeDirs.size()));
            g.x += alt.dx * ghostSPEED;
            g.y += alt.dy * ghostSPEED;
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

    private Direction timedRandom(Block g, long now) {
        Long t = nextChangeTime.getOrDefault(g, 0L);
        if (now >= t) {
            Direction d = randomAvailable(g);
            nextChangeTime.put(g, now + randomInterval());
            return d;
        }
        return g.direction;
    }

    private Direction chase(Block g) {
        Block pac = game.getPacmanBlock();
        Point target = new Point(pac.x, pac.y);
        return bestAvailableDirection(g, target);
    }

    private Direction bestAvailableDirection(Block g, Point target) {
        double bestDist = Double.MAX_VALUE;
        Direction best = g.direction;
        for (Direction d : availableDirections(g)) {
            double nx = g.x + d.dx * ghostSPEED;
            double ny = g.y + d.dy * ghostSPEED;
            double dist = hypot(nx - target.x, ny - target.y);
            if (dist < bestDist) {
                bestDist = dist;
                best = d;
            }
        }
        return best;
    }

    private Point predictedPacmanTarget() {
        Block pac = game.getPacmanBlock();
        Direction pd = game.getPacmanDirection();
        return new Point(
            pac.x + pd.dx * PINK_PREDICT_TILES * PacMan.TILE_SIZE,
            pac.y + pd.dy * PINK_PREDICT_TILES * PacMan.TILE_SIZE
        );
    }

    private List<Direction> availableDirections(Block g) {
        List<Direction> ok = new ArrayList<>();
        for (Direction d : Direction.values()) {
            int nx = g.x + d.dx * ghostSPEED;
            int ny = g.y + d.dy * ghostSPEED;
            if (!collidesWithWall(nx, ny)) ok.add(d);
        }
        return ok;
    }

    private Direction randomAvailable(Block g) {
        List<Direction> ok = availableDirections(g);
        if (ok.isEmpty()) return g.direction;
        return ok.get(rand.nextInt(ok.size()));
    }

    private boolean collidesWithWall(int x, int y) {
        Block test = new Block(null, x, y, PacMan.TILE_SIZE, PacMan.TILE_SIZE, null);
        return map.isCollisionWithWallOrPortal(test);
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

    private long randomInterval() {
        return (4 + rand.nextInt(3)) * 1000L;
    }

    private static double hypot(double dx, double dy) {
        return Math.hypot(dx, dy);
    }

    private static class Point {
        final int x, y;
        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
