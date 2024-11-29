package ch.heigvd.dai.game;
import static com.raylib.Jaylib.Color;

import java.util.Arrays;

public class Tronocol {
    final private int numberOfPlayer;
    private int currentNumberOfPlayer = 0;
    private Player[] players;
    private Color[][] world;
    final private Color boardColor = new Color(0,0,0,255);
    final private Color borderColor = new Color(200,0,200,255);

    public Tronocol(int numberOfPlayer,int height,int width) {
        this.numberOfPlayer = numberOfPlayer;
        this.world = new Color[height][width];
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
        return !world[(int) player.getPosition().y()][(int) player.getPosition().x()].equals(boardColor);
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

    public Color[][] getWorld() {
        return world.clone();
    }

    public Player[] getPlayer(){
        return players.clone();
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
