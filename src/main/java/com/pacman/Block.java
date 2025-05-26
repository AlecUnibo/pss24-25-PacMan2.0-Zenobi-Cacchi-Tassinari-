package com.pacman;

import javafx.scene.image.Image;

/**
 * Rappresenta un blocco del gioco (fantasma, muro, cibo, ecc.)
 */
public class Block {
    public int x, y;                   // Posizione
    public int width, height;         // Dimensioni
    public Image image;               // Immagine corrente
    public final Image originalImage; // Immagine originale (non cambia)
    public final GhostType ghostType; // Tipo di fantasma (null se non è un fantasma)
    public Direction direction;       // Direzione di movimento (solo fantasmi)
    public boolean isExiting = false; // Il fantasma sta uscendo dalla "casa"
    public boolean isScared  = false; // Il fantasma è spaventato (modalità blu)
    public int velocityX = 0, velocityY = 0; // Velocità di movimento

    // Costruttore per blocchi con dimensioni standard
    public Block(Image image, int x, int y) {
        this(image, x, y, PacMan.TILE_SIZE, PacMan.TILE_SIZE, null);
    }

    // Costruttore completo
    public Block(Image image, int x, int y, int width, int height, GhostType ghostType) {
        this.image         = image;
        this.originalImage = image;
        this.x             = x;
        this.y             = y;
        this.width         = width;
        this.height        = height;
        this.ghostType     = ghostType;
        this.direction     = (ghostType != null) ? Direction.randomDirection() : null;
        this.isScared      = false;
    }

    // Aggiorna immagine e velocità in base alla direzione ('L', 'R', 'U', 'D')
    public void updateDirection(char dir, Image img) {
        this.image = img;
        this.velocityX = (dir == 'L' ? -4 : dir == 'R' ? 4 : 0);
        this.velocityY = (dir == 'U' ? -4 : dir == 'D' ? 4 : 0);
    }

    // Tipi possibili di fantasmi
    public enum GhostType {
        RED, BLUE, ORANGE, PINK
    }
}
