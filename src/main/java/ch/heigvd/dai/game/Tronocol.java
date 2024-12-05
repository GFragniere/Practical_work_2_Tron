package ch.heigvd.dai.game;

import java.io.Serializable;

/**
 * The class that manages the game logic and data allowing to run the games smoothly.
 */
public class Tronocol implements Serializable{

    private final static int worldWidth = TronocolGraphics.WIDTH / TronocolGraphics.BLOCKSIZE;
    private final static int worldHeight = TronocolGraphics.HEIGHT / TronocolGraphics.BLOCKSIZE;
    public static final Vector2D[] POSITIONS = {
            new Vector2D(5, 5),
            new Vector2D(worldWidth - 5, worldHeight - 5),
            new Vector2D(5, worldHeight - 5),
            new Vector2D(worldWidth - 5,  5),
    };
    final private int numberOfPlayer;
    private int currentNumberOfPlayer = 0;
    private final Player[] players;
    private final short[][] world;
    final private int boardColor = 4;
    final private int borderColor = 5;

    /**
     * The constructor for a Tronocol instance.
     * @param numberOfPlayer The number of maximum players the game can handle
     */
    public Tronocol(int numberOfPlayer) {
        this.numberOfPlayer = numberOfPlayer;
        this.world = new short[worldWidth][worldHeight];
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

    /**
     * Used to add a player to the game.
     * @param player The player to add to the game.
     */
    public void addPlayer(Player player) {
        players[currentNumberOfPlayer++] = player;
    }

    /**
     * Called to know if there is enough players in the game to start the game.
     * @return true if the number of players is the maximum number of players to handle, false otherwise.
     */
    public boolean GameReady(){
        return currentNumberOfPlayer == numberOfPlayer;
    }

    /**
     * Used to get the current number of players. This method is called when adding a player to the game by
     * the TronocolServer class.
     * @return The number of players currently in the game.
     */
    public int getCurrentNumberOfPlayer() {
        return currentNumberOfPlayer;
    }

    /**
     * Used to update all players by moving them according to their direction. This method will only move players
     * if they are not dead. Otherwise, it will simply ignore them. Before moving a player, the game board updates
     * the position of where the player is with the player's color. If the player is colliding with a wall or
     * a player's track, it will be set to dead and removed from the board, effectively removing all of his own
     * track.
     */
    public void update(){
        for(Player player : players){
            if(!player.isDead()) {
                if(collision(player)){
                    player.setDead();
                    removePlayerFromBoard(player);
                    continue;
                }
                world[player.getPosition().getY()][player.getPosition().getX()] = player.getColor();
                player.move();
            }
        }
    }

    /**
     * Used to check if the given player is colliding with a wall or a player's track. It checks to see if the player's
     * current position on the board is of the color of the board (meaning no one came on this cell yet, or it is not a
     * border.
     * @param player The player to be checked for collision.
     * @return true if the player is colliding, false otherwise.
     */
    private boolean collision(Player player) {
        return !(world[player.getPosition().getY()][player.getPosition().getX()] == boardColor);
    }

    /**
     * Used to remove a player and its track from the board by setting
     * @param player The player to remove from the board.
     */
    private void removePlayerFromBoard(Player player) {
        for(int y = 0; y < world.length; y++){
            for(int x = 0; x < world[y].length; x++){
                if(world[y][x] == player.getColor()){
                    world[y][x] = boardColor;
                }
            }
        }
    }

    /**
     * Used to get the Tronocol's board data, notably for drawing it.
     * @return A 2D array of shorts representing the board's state.
     */
    public short[][] getWorld() {
        return world;
    }

    /**
     * Used to get the array of players currently in the game.
     * @return The array of players in the game.
     */
    public Player[] getPlayer(){
        return players;
    }

    /**
     * Used to check if all players in the game are dead or not. Called by the TronocolServer class to know when to
     * stop running.
     * @return true if all players are dead, false otherwise.
     */
    public boolean allPlayersDead() {
        for (Player player : players) {
            if (!player.isDead()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Used to check if only player in a game with more than one player is alive. Called by the TronocolServer class to
     * know when to stop running.
     * @return true if the game has more than one player and if only one is alive, false otherwise.
     */
    public boolean onePlayerWinner() {
        int playersAlive = 0;
        for (Player player : players) {
            if(!player.isDead())
                playersAlive++;
        }
        return playersAlive == 1;
    }

    public Player getWinner() {
        for (Player player : players){
            if (!player.isDead())
                return player;
        }
        return null;
    }
}
