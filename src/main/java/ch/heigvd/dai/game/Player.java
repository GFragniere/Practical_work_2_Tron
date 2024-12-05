package ch.heigvd.dai.game;

import java.awt.*;
import java.io.Serializable;

/**
 * This class represents the players present in the game, each having their position, color, direction and name associated.
 */
public class Player implements Serializable {

    final private short color;
    final private String name;
    private Vector2D position;
    private Direction direction;
    private boolean dead;

    /**
     * Constructor of the player class. The player is an instance representing a client within the game, identified by
     * their color and name. 2 players can't have the same name and/or color, this case is handled when connecting
     * to the server.
     *
     * @param color The color given to the player, represented by a short and with a value in range [0:3]
     * @param name The name of the player.
     * @param position The current position of the player on the board.
     * @param direction The direction the player is heading towards.
     */
    public Player(short color, String name, Vector2D position, Direction direction) {
        this.color = color;
        this.name = name;
        this.position = position;
        this.direction = direction;
        this.dead = false;
    }

    /**
     * Used to set the direction of the player to a given value.
     * @param direction The direction the player will head towards.
     */
    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    /**
     * Used to get the current player's position, notably when drawing the game board.
     * @return The position the player is on, expressed with a Vector2D instance.
     */
    public Vector2D getPosition() {
        return position;
    }

    /**
     * Used to get the player's color as a short, which will be later translated into a RaylibColor with a
     * corresponding table.
     * @return The value of the color as a short.
     */
    public short getColor() {
        return color;
    }

    /**
     * Used to get the name of the player, notably when checking if the player's name matches the client's name for
     * updating the direction.
     * @return A string with the name of the player.
     */
    public String getName() {
        return name;
    }

    /**
     * Used to check if the player is dead before moving it or representing it on the game board.
     * @return true if the player is dead, false otherwise.
     */
    public boolean isDead() {
        return dead;
    }

    /**
     * Used to kill the player, for example when colliding with a border or a wall.
     */
    public void setDead(){
        dead = true;
    }

    /**
     * Used to move the player according to the direction he's facing. This method should only be used when the player
     * is not dead (isDead() == false) in order to guarantee that it does not get out of bounds.
     */
    public void move(){
        switch (direction) {
            case UP:
                position.setY(position.getY() - 1);
                break;
            case DOWN:
                position.setY(position.getY() + 1);
                break;
            case LEFT:
                position.setX(position.getX() - 1);
                break;
            case RIGHT:
                position.setX(position.getX() + 1);
                break;
            default:
        }
    }
}
