package com.pacman;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.Light.Point;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

// Gestisce la mappa di gioco, gli oggetti e le interazioni base
public class GameMap {
    // Definisce la struttura della mappa come una serie di stringhe
    // X = muro, ' ' = cibo, O = power-up, P = Pac-Man, - = portale fantasmi
    // b,o,p,r = fantasmi, T = tunnel, n = spazio vuoto
    private static final String[] tileMap = {
            "XXXXXXXXXXXXXXXXXXX",
            "XO       X       OX",
            "X XX XXX X XXX XX X",
            "X                 X",
            "X XX X XXXXX X XX X",
            "X    X       X    X",
            "XXXX XXXXnXXXX XXXX",
            "nnnX XnnnrnnnX Xnnn",
            "XXXX XnXX-XXnX XXXX",
            "Tnnn nnXpboXnn nnnT",
            "XXXX XnXXXXXnX XXXX",
            "nnnX XnREADY!X Xnnn",
            "XXXX XnXXXXXnX XXXX",
            "X        X        X",
            "X XX XXX X XXX XX X",
            "X  X     P     X  X",
            "XX X X XXXXX X X XX",
            "X    X   X   X    X",
            "X XXXXXX X XXXXXX X",
            "XO               OX",
            "XXXXXXXXXXXXXXXXXXX"
    };
    private final HashSet<Block> walls      = new HashSet<>(); // Contiene tutti i muri
    private final HashSet<Block> foods      = new HashSet<>(); // Contiene tutto il cibo normale
    private final HashSet<Block> ghosts     = new HashSet<>(); // Contiene le posizioni iniziali dei fantasmi
    private final HashSet<Block> powerFoods = new HashSet<>(); // Contiene tutti i power-up
    private final List<Block>    tunnels    = new ArrayList<>(); // Contiene i tunnel
    private Block                ghostPortal; // Il portale da cui escono i fantasmi
    private Block                pacman;      // Il blocco di Pac-Man
    private final List<Image>    collectedFruits = new ArrayList<>(); // Per i frutti collezionati (non usato attivamente in questo snippet)
    private final ImageLoader    loader;        // Per caricare le immagini
    private boolean firstLoad = true;       // Indica se è il primo caricamento della mappa (per il messaggio "READY!")


    public GameMap(ImageLoader loader) {
        this.loader = loader;
        loadMap(); // Carica la mappa alla creazione
    }

    // Carica tutti gli elementi (muri, cibo, fantasmi, ecc.) dalla tileMap nelle rispettive collezioni
    public void loadMap() {
        // Pulisce le collezioni prima di ricaricare la mappa
        walls.clear();
        foods.clear();
        ghosts.clear();
        powerFoods.clear();
        tunnels.clear();
        collectedFruits.clear();
        ghostPortal = null;
        pacman = null;

        // Scorre la tileMap per creare i blocchi corrispondenti
        for (int r = 0; r < tileMap.length; r++) { // r = riga
            for (int c = 0; c < tileMap[r].length(); c++) { // c = colonna
                int x = c * PacMan.TILE_SIZE; // Coordinata pixel x
                int y = r * PacMan.TILE_SIZE; // Coordinata pixel y
                char tile = tileMap[r].charAt(c); // Carattere della mappa

                switch (tile) {
                    case 'X': // Muro
                        walls.add(new Block(loader.getWallImage(), x, y, PacMan.TILE_SIZE, PacMan.TILE_SIZE, null));
                        break;
                    case ' ': // Cibo normale
                        foods.add(new Block(null, // Il cibo non ha un'immagine, viene disegnato come rettangolo
                                x + PacMan.TILE_SIZE/2 - 2,
                                y + PacMan.TILE_SIZE/2 - 2,
                                4, 4, // Dimensioni del cibo
                                null));
                        break;
                    case 'n':
                        break;
                    case 'P': // Pac-Man
                        pacman = new Block(loader.getPacmanRightImage(), x, y, PacMan.TILE_SIZE, PacMan.TILE_SIZE, null);
                        break;
                    case '-': // Portale dei fantasmi
                        ghostPortal = new Block(null, x, y, PacMan.TILE_SIZE, 4, null);
                        break;
                    case 'O': // Power-up
                        powerFoods.add(new Block(
                                loader.getPowerFoodImage(), x, y,
                                PacMan.TILE_SIZE, PacMan.TILE_SIZE,
                                null));
                        break;
                    case 'b': // Fantasma Blu
                        ghosts.add(new Block(
                                loader.getBlueGhostImage(), x, y,
                                PacMan.TILE_SIZE, PacMan.TILE_SIZE,
                                Block.GhostType.BLUE));
                        break;
                    case 'o': // Fantasma Arancione
                        ghosts.add(new Block(
                                loader.getOrangeGhostImage(), x, y,
                                PacMan.TILE_SIZE, PacMan.TILE_SIZE,
                                Block.GhostType.ORANGE));
                        break;
                    case 'p': // Fantasma Rosa
                        ghosts.add(new Block(
                                loader.getPinkGhostImage(), x, y,
                                PacMan.TILE_SIZE, PacMan.TILE_SIZE,
                                Block.GhostType.PINK));
                        break;
                    case 'r': // Fantasma Rosso
                        ghosts.add(new Block(
                                loader.getRedGhostImage(), x, y,
                                PacMan.TILE_SIZE, PacMan.TILE_SIZE,
                                Block.GhostType.RED));
                        break;
                    case 'T': // Tunnel
                        tunnels.add(new Block(null, x, y,
                                PacMan.TILE_SIZE, PacMan.TILE_SIZE,
                                null));
                        break;
                    default: // Ignora altri caratteri (come quelli di "READY!")
                        break;
                }
            }
        }
    }

    // Ripristina solo le posizioni iniziali di Pac-Man e dei fantasmi, non l'intera mappa
    public void resetEntities() {
        ghosts.clear(); // Rimuove i fantasmi attuali
        // Ricrea Pac-Man e i fantasmi nelle loro posizioni originali definite in tileMap
        for (int r = 0; r < tileMap.length; r++) {
            for (int c = 0; c < tileMap[r].length(); c++) {
                int x = c * PacMan.TILE_SIZE;
                int y = r * PacMan.TILE_SIZE;
                char tile = tileMap[r].charAt(c);

                switch (tile) {
                    case 'P':
                        pacman = new Block(loader.getPacmanRightImage(), x, y, PacMan.TILE_SIZE, PacMan.TILE_SIZE, null);
                        break;
                    case 'b':
                        ghosts.add(new Block(
                                loader.getBlueGhostImage(), x, y,
                                PacMan.TILE_SIZE, PacMan.TILE_SIZE,
                                Block.GhostType.BLUE));
                        break;
                    case 'o':
                        ghosts.add(new Block(
                                loader.getOrangeGhostImage(), x, y,
                                PacMan.TILE_SIZE, PacMan.TILE_SIZE,
                                Block.GhostType.ORANGE));
                        break;
                    case 'p':
                        ghosts.add(new Block(
                                loader.getPinkGhostImage(), x, y,
                                PacMan.TILE_SIZE, PacMan.TILE_SIZE,
                                Block.GhostType.PINK));
                        break;
                    case 'r':
                        ghosts.add(new Block(
                                loader.getRedGhostImage(), x, y,
                                PacMan.TILE_SIZE, PacMan.TILE_SIZE,
                                Block.GhostType.RED));
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public void setFirstLoad(boolean v) { firstLoad = v; }

    // Disegna gli elementi statici della mappa (muri, cibo, power-up) e il messaggio "READY!"
    public void draw(GraphicsContext gc) {
        // Disegna i muri
        for (Block w : walls) {
            if (w.image != null) // L'immagine del muro potrebbe essere null per l'effetto flash
                gc.drawImage(w.image, w.x, w.y,
                        PacMan.TILE_SIZE, PacMan.TILE_SIZE);
        }
        // Disegna il cibo normale
        gc.setFill(Color.WHITE);
        for (Block f : foods) {
            gc.fillRect(f.x, f.y, f.width, f.height);
        }
        // Disegna i power-up
        for (Block pf : powerFoods) {
            gc.drawImage(pf.image, pf.x, pf.y,
                    PacMan.TILE_SIZE, PacMan.TILE_SIZE);
        }

        // Se è il primo caricamento, mostra "READY!"
        if (firstLoad) {
            String msg = "READY!";
            Font   f   = Font.font("PressStart2P",
                    FontWeight.BOLD, PacMan.TILE_SIZE);
            gc.setFont(f);
            gc.setFill(Color.YELLOW);

            // Misura il testo per centrarlo
            Text measure = new Text(msg);
            measure.setFont(f);
            double textW = measure.getLayoutBounds().getWidth();

            // Trova la riga in tileMap che contiene "READY!" per determinare la posizione Y
            for (int r = 0; r < tileMap.length; r++) {
                if (tileMap[r].contains(msg)) { // Cerca la stringa "READY!"
                    double x = (PacMan.BOARD_WIDTH - textW) / 2;
                    double y = r * PacMan.TILE_SIZE + PacMan.TILE_SIZE;
                    gc.fillText(msg, x, y);
                    break;
                }
            }
        }
    }

    public Block getPacman()     { return pacman; }
    // Ricarica l'intera mappa e restituisce Pac-Man alla sua posizione iniziale
    public Block resetPacman()   { loadMap(); return pacman; }
    public HashSet<Block> getWalls()     { return walls; }
    public HashSet<Block> getFoods()     { return foods; }
    // Restituisce una nuova lista di fantasmi per evitare modifiche esterne alla lista originale
    public List<Block>    getGhosts()    { return new ArrayList<>(ghosts); }
    public Block          getGhostPortal() { return ghostPortal; }
    // Restituisce una nuova lista di power-up
    public List<Block>    getPowerFoods(){ return new ArrayList<>(powerFoods); }
    // Restituisce una nuova lista di frutti collezionati
    public List<Image>    getCollectedFruits(){return new ArrayList<>(collectedFruits);}
    public List<Block>    getTunnels()   { return tunnels; }

    // Gestisce il passaggio di un blocco (Pac-Man o fantasma) attraverso i tunnel
    public void wrapAround(Block b) {
        for (Block t : tunnels) { // Controlla se il blocco 'b' è su un tunnel 't'
            if (collision(b, t)) {
                for (Block o : tunnels) { // Trova l'altro tunnel 'o'
                    if (o != t) { // Assicurati che sia un tunnel diverso
                        b.x = o.x; // Sposta il blocco 'b' all'altro tunnel
                        b.y = o.y;
                        return; // Esce dopo aver spostato il blocco
                    }
                }
            }
        }
    }

    // Controlla se due blocchi si sovrappongono (collisione AABB)
    private boolean collision(Block a, Block c) {
        return a.x < c.x + c.width &&
                a.x + a.width > c.x &&
                a.y < c.y + c.height &&
                a.y + a.height > c.y;
    }

    // Verifica se un blocco collide con un muro o con il portale dei fantasmi
    public boolean isCollisionWithWallOrPortal(Block b) {
        for (Block w : walls) if (collision(b, w)) return true; // Controlla collisione con i muri
        if (ghostPortal != null && collision(b, ghostPortal)) return true; // Controlla collisione con il portale
        return false;
    }

    // Controlla se un blocco 'b' può muoversi nella direzione indicata da 'key'
    public boolean canMove(Block b, KeyCode key) {
        int nx = b.x, ny = b.y; // Posizione attuale
        // Calcola la potenziale nuova posizione basata sulla direzione
        switch (key) {
            case UP    -> ny -= 4; // Piccolo step per il test di movimento
            case DOWN  -> ny += 4;
            case LEFT  -> nx -= 4;
            case RIGHT -> nx += 4;
            default    -> { /* Non fa nulla per altri tasti */ }
        }
        // Crea un blocco temporaneo nella nuova posizione per testare la collisione
        Block test = new Block(null, nx, ny, b.width, b.height, null); // Usa le dimensioni del blocco originale
        return !isCollisionWithWallOrPortal(test); // Può muoversi se non c'è collisione
    }

    // Se Pac-Man (blocco 'b') mangia cibo, lo rimuove e restituisce il punteggio
    public int collectFood(Block b) {
        Iterator<Block> it = foods.iterator();
        while (it.hasNext()) {
            Block f = it.next();
            if (collision(b, f)) { // Se Pac-Man collide con il cibo 'f'
                it.remove(); // Rimuove il cibo
                return 10;   // Restituisce il punteggio per il cibo
            }
        }
        return 0; // Nessun cibo raccolto
    }

    // Se Pac-Man (blocco 'b') mangia un power-up, lo rimuove e restituisce true
    public boolean collectPowerFood(Block b) {
        Iterator<Block> it = powerFoods.iterator();
        while (it.hasNext()) {
            Block pf = it.next();
            if (collision(b, pf)) { // Se Pac-Man collide con il power-up 'pf'
                it.remove(); // Rimuove il power-up
                return true; // Indica che un power-up è stato raccolto
            }
        }
        return false; // Nessun power-up raccolto
    }

    public int getPowerFoodCount() { return powerFoods.size(); }

    // Rende i muri temporaneamente invisibili (per effetto flash) e poi esegue onFinished
    public void flashWalls(Runnable onFinished) {
        walls.forEach(w->w.image=null); // Imposta l'immagine di tutti i muri a null
        onFinished.run(); // Esegue l'azione specificata (es. ripristinare le immagini)
    }

    // Ricarica l'intera mappa da zero
    public void reload() { loadMap(); }

    // Imposta l'immagine per tutti i muri (usato per ripristinare dopo il flash)
    public void setWallImage(Image img) {
        for (Block w : walls) {
            w.image = img;
        }
    }
    /** Rappresenta un punto nella griglia della mappa (colonna, riga), non in pixel. */
    public static class Point {
        public final int x, y; // x = colonna, y = riga
        public Point(int x, int y) { this.x = x; this.y = y; }
        @Override public boolean equals(Object o) { // Necessario per usare Point come chiave in HashMap
            if (this == o) return true;
            if (!(o instanceof Point)) return false;
            Point p = (Point)o;
            return x == p.x && y == p.y;
        }
        @Override public int hashCode() { // Necessario per usare Point come chiave in HashMap
            return 31 * x + y;
        }
    }


    /**
     * Costruisce un grafo di navigazione per il pathfinding dei fantasmi.
     * I nodi sono intersezioni o punti significativi (tunnel, area fantasmi).
     * Gli archi connettono nodi raggiungibili in linea retta.
     */
    public Map<Point, List<Point>> buildNavigationGraph() {
        Map<Point, List<Point>> graph = new HashMap<>();
        int rows = tileMap.length;
        int cols = tileMap[0].length();

        // 1. Identifica i nodi del grafo
        // Un nodo è una cella percorribile che è un'intersezione, un tunnel o in una riga speciale (riga 11).
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int x_pixel = c * PacMan.TILE_SIZE;
                int y_pixel = r * PacMan.TILE_SIZE;
                Block b = new Block(null, x_pixel, y_pixel, PacMan.TILE_SIZE, PacMan.TILE_SIZE, null);
                if (!isCollisionWithWallOrPortal(b)) { // Se la cella non è un muro
                    // Conta le direzioni libere da questa cella
                    int freeDirs = 0;
                    for (Direction d : Direction.values()) { // Ipotizzando un enum Direction {UP, DOWN, LEFT, RIGHT}
                        Block nb = new Block(null, x_pixel + d.dx*PacMan.TILE_SIZE, y_pixel + d.dy*PacMan.TILE_SIZE,
                                PacMan.TILE_SIZE, PacMan.TILE_SIZE, null);
                        if (!isCollisionWithWallOrPortal(nb)) freeDirs++;
                    }
                    // È un nodo se non è un corridoio semplice (freeDirs != 2),
                    // o se è un tunnel, o se è sulla riga 11 (area speciale, es. casa fantasmi).
                    if (freeDirs != 2 || isOnTunnel(b) || r==11) {
                        Point p = new Point(c, r); // Usa coordinate di griglia
                        graph.put(p, new ArrayList<>());
                    }
                }
            }
        }

        // 2. Connetti i nodi con archi
        // Per ogni nodo, cerca altri nodi raggiungibili in linea retta.
        for (Point p : graph.keySet()) {
            for (Direction d : Direction.values()) { // Per ogni direzione
                int nc = p.x + d.dx; // Prossima colonna
                int nr = p.y + d.dy; // Prossima riga
                // Avanza lungo il corridoio fino a trovare un altro nodo o un muro
                while (nc>=0 && nr>=0 && nr<rows && nc<cols) {
                    Block pathBlock = new Block(null, nc*PacMan.TILE_SIZE, nr*PacMan.TILE_SIZE, PacMan.TILE_SIZE, PacMan.TILE_SIZE, null);
                    if(isCollisionWithWallOrPortal(pathBlock)) break; // Interrompi se c'è un muro

                    Point q = new Point(nc, nr);
                    if (graph.containsKey(q)) { // Se la cella corrente (nc, nr) è un nodo
                        graph.get(p).add(q); // Aggiungi connessione da p a q
                        break; // Trovato il prossimo nodo in questa direzione
                    }
                    // Continua nella stessa direzione
                    nc += d.dx;
                    nr += d.dy;
                }
            }
        }
        return graph;
    }

    /**
     * Verifica se un blocco si trova su una cella che è un tunnel.
     * Utile per la costruzione del grafo di navigazione.
     */
    private boolean isOnTunnel(Block b) {
        for (Block t : tunnels) {
            if (collision(b, t)) return true; // Se il blocco 'b' collide con un blocco tunnel 't'
        }
        return false;
    }

}