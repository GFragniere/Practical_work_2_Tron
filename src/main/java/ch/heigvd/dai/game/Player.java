package ch.heigvd.dai.game;

import java.awt.*;
import java.io.Serializable;


public class Player implements Serializable {

    final private Color color;
    final private String name;
    private Vector2D position;
    private Direction direction;
    private boolean dead;

    public Player(Color color, String name, Vector2D position, Direction direction) {
        this.color = color;
        this.name = name;
        this.position = position;
        this.direction = direction;
        this.dead = false;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void setPosition(Vector2D position) {
        this.position = position;
    }

    public Vector2D getPosition() {
        return position;
    }

    public Direction getDirection() {
        return direction;
    }

    public Color getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    public boolean isDead() {
        return dead;
    }

    public void setDead(){
        dead = true;
    }

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
