package ch.heigvd.dai.game;

import java.io.Serializable;

/**
 * A class used to represent a position in the game as a vector of 2 int values.
 */
public class Vector2D implements Serializable {
    private int x;
    private int y;

    /**
     * Constructor for initializing an instance of Vector2D.
     * @param x X coordinate.
     * @param y Y coordinate.
     */
    public Vector2D(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Gets the x coordinate of the vector.
     * @return The x coordinate.
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the y coordinate of the vector.
     * @return The y coordinate.
     */
    public int getY() {
        return y;
    }

    /**
     * Sets the x coordinate of the vector.
     * @param x The value to modify the x coordinate to.
     */
    void setX(int x) {
        this.x = x;
    }

    /**
     * Sets the y coordinate of the vector.
     * @param y The value to modify the y coordinate to.
     */
    void setY(int y) {
        this.y = y;
    }
}
