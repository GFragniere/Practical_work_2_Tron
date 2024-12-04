package ch.heigvd.dai.game;
import java.awt.*;

import java.io.Serializable;
import java.util.Arrays;

public class Tronocol implements Serializable{

    private static int worldWidth = TronocolGraphics.WIDTH / TronocolGraphics.BLOCKSIZE;
    private static int worldHeight = TronocolGraphics.HEIGHT / TronocolGraphics.BLOCKSIZE;

    public static final Vector2D[] POSITIONS = {
            new Vector2D(5, 5),
            new Vector2D(worldWidth - 5, worldHeight - 5),
            new Vector2D(5, worldHeight - 5),
            new Vector2D(worldWidth - 5,  5),
    };

    final private int numberOfPlayer;
    private int currentNumberOfPlayer = 0;
    private Player[] players;
    private short[][] world;
    final private int boardColor = 4;
    final private int borderColor = 5;

    public Tronocol(int numberOfPlayer,int height,int width) {
        this.numberOfPlayer = numberOfPlayer;
        this.world = new short[height][width];
        this.players = new Player[numberOfPlayer];
        for(int y = 0; y < world.length; y++) {
            for(int x = 0; x < world[0].length; x++) {
                if(y == 0 || y == world.length - 1 || x == 0 || x == world[0].length - 1) {
                    world[y][x] = borderColor;
                }else{
                    world[y][x] = boardColor;
                }
            }
        }
    }

    public void addPlayer(Player player) {
        players[currentNumberOfPlayer++] = player;
    }

    public boolean GameReady(){
        return currentNumberOfPlayer == numberOfPlayer;
    }

    public int getCurrentNumberOfPlayer() {
        return currentNumberOfPlayer;
    }

    public void update(){
        for(Player player : players){
            if(!player.isDead()) {
                if(collision(player)){
                    player.setDead();
                    removePlayerFromBoard(player);
                    continue;
                }
                world[(int) player.getPosition().getY()][(int) player.getPosition().getX()] = player.getColor();
                player.move();
            }
        }
    }

    private boolean collision(Player player) {
        return !(world[(int) player.getPosition().getY()][(int) player.getPosition().getX()] == boardColor);
    }

    private void removePlayerFromBoard(Player player) {
        for(int y = 0; y < world.length; y++){
            for(int x = 0; x < world[y].length; x++){
                if(world[y][x] == player.getColor()){
                    world[y][x] = boardColor;
                }
            }
        }
    }

    public short[][] getWorld() {
        return world;
    }

    public Player[] getPlayer(){
        return players;
    }

    public void updateGameFromPlayer(Player player) {
        for (int i = 0; i < numberOfPlayer; i++) {
            if (players[i].getName().contentEquals(player.getName())) {
                players[i] = player;
                return;
            }
        }
    }
}
