package com.pacman;

/**
 * Enum che rappresenta le quattro direzioni cardinali
 * usate per il movimento di Pac-Man e dei fantasmi.
 */
public enum Direction {
    UP(0, -1),
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0);

    public final int dx, dy; // Variazione di posizione in x e y

    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    // Restituisce una direzione casuale (utile per il movimento dei fantasmi)
    public static Direction randomDirection() {
        Direction[] directions = values();
        return directions[(int) (Math.random() * directions.length)];
    }
}
