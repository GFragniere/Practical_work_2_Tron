package ch.heigvd.dai.game;

import java.awt.*;
import com.raylib.Jaylib.Vector2;
import com.raylib.Jaylib.Color;


enum Direction {
    UP, DOWN, LEFT, RIGHT;
}

public class Player {

    final private Color color;
    final private String name;
    private Vector2 position;
    private Direction direction;
    private boolean dead;

    public Player(Color color, String name, Vector2 position, Direction direction) {
        this.color = color;
        this.name = name;
        this.position = position;
        this.direction = direction;
        this.dead = false;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void setPosition(Vector2 position) {
        this.position = position;
    }

    public Vector2 getPosition() {
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
                position.y(position.y() + 1);
                break;
            case DOWN:
                position.y(position.y() - 1);
                break;
            case LEFT:
                position.x(position.x() - 1);
                break;
            case RIGHT:
                position.x(position.x() + 1);
                break;
            default:
        }
    }
}
