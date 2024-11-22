package ch.heigvd.dai.game;
import com.raylib.Jaylib.Color;

import java.util.Arrays;

public class Tronocol {
    final private int numberOfPlayer;
    private int currentNumberOfPlayer = 0;
    private Player[] players;
    private Color[][] world;
    final private Color boardColor;

    public Tronocol(int numberOfPlayer,int height,int width) {
        this.numberOfPlayer = numberOfPlayer;
        this.world = new Color[height][width];
        this.players = new Player[numberOfPlayer];
        this.boardColor = new Color(0,0,0,0);
        for (Color[] row : world)
            Arrays.fill(row, boardColor);
    }

    public void addPlayer(Player player) {
        players[currentNumberOfPlayer++] = player;
    }

    public boolean GameReady(){
        return currentNumberOfPlayer == numberOfPlayer;
    }

    public void update(){
        for(Player player : players){
            if(!player.isDead()) {
                if(collision(player)){
                    player.setDead();
                    removePlayerFromBoard(player);
                    continue;
                }
                world[(int) player.getPosition().y()][(int) player.getPosition().x()] = player.getColor();
                player.move();
            }
        }
    }

    private boolean collision(Player player) {
        if(world[(int) player.getPosition().y()][(int) player.getPosition().x()].equals(boardColor)){
            return false;
        }
        return true;
    }

    private void removePlayerFromBoard(Player player) {
        for(int y = 0; y < world.length; y++){
            for(int x = 0; x < world[y].length; x++){
                if(world[y][x].equals(player.getColor())){
                    world[y][x] = boardColor;
                }
            }
        }
    }
}
