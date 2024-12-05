package ch.heigvd.dai.game;
import ch.heigvd.dai.tronocol.TronocolClient;
import com.raylib.Jaylib;
import com.raylib.Raylib;

import java.util.Objects;

import static com.raylib.Jaylib.*;

/**
 * A class used to render the game for each client. This class uses the raylib.Jaylib library
 */
public class TronocolGraphics implements Runnable{

    /**
     * A static array of Jaylib.Color used to draw the game. Each value is accessed by getting the short values
     * of the Tronocol board and making it match the corresponding color.
     */
    public static final Jaylib.Color[] COLORS = {
            new Jaylib.Color(255,0,0,255), new Jaylib.Color(255,0,255,255),
            new Jaylib.Color(0,255,0,255), new Jaylib.Color(0,0,255,255),
            new Jaylib.Color(0,0,0,255) , new Jaylib.Color(18,132,223,255),
            new Jaylib.Color(255,255,255,255)
    };
    public final static int HEIGHT = 800;
    public final static int WIDTH = 800;
    public final static int BLOCKSIZE = 8;
    private boolean shouldExit = false;
    private final static String UPDATE = "UPDATE";
    private final static String QUIT = "QUIT";
    private Tronocol game;
    private TronocolClient client;

    /**
     * Constructor for an instance of TronocolGraphics.
     * @param game A reference to the Troncol used to instantiate the game.
     * @param tronocolClient A reference to the client owning the instance.
     */
    public TronocolGraphics(Tronocol game, TronocolClient tronocolClient){
        this.game = game;
        this.client = tronocolClient;
    }

    /**
     * Set the game to the given game.
     * @param game The game the instance will refer to.
     */
    public void setGame(Tronocol game) {
        this.game = game;
    }

    /**
     * Used to set the variable shouldExit to true, making the GUI end.
     */
    public void exit() {
        shouldExit = true;
    }

    /**
     * Start the GUI and draws the window according to the state of the game. Note that this method is also used to
     * send the UPDATE request to the server, because it is the only place we can acquire user input in real time.
     */
    @Override
    public void run() {
        SetTraceLogLevel(7);
        InitWindow(WIDTH, HEIGHT+50, "Tronocol");
        SetTargetFPS(60);
        while (!WindowShouldClose() && !shouldExit) {
            BeginDrawing();
            if(this.game.GameReady()){
                ClearBackground(BLACK);
                if (IsKeyDown(KEY_LEFT)) client.send_update(UPDATE,Direction.LEFT.ordinal(),client.getUsername());
                if (IsKeyDown(KEY_RIGHT)) client.send_update(UPDATE,Direction.RIGHT.ordinal(),client.getUsername());
                if (IsKeyDown(KEY_DOWN)) client.send_update(UPDATE,Direction.DOWN.ordinal(),client.getUsername());
                if (IsKeyDown(KEY_UP)) client.send_update(UPDATE, Direction.UP.ordinal(),client.getUsername());

                short[][] world = game.getWorld();
                Player[] players = game.getPlayer();
                for (int i = 0; i < players.length; ++i) {
                    DrawRectangle(players[i].getPosition().getX() * BLOCKSIZE, players[i].getPosition().getY() * BLOCKSIZE, BLOCKSIZE, BLOCKSIZE, translateColor(players[i].getColor()));
                    DrawRectangle(i*WIDTH/players.length + 10, HEIGHT + 10,30,30,translateColor(players[i].getColor()));
                    Jaylib.Color color = COLORS[6];
                    if(players[i].getName().contentEquals(client.getUsername())){
                        color = translateColor(players[i].getColor());
                    }
                    DrawText(players[i].getName(), i*WIDTH/players.length + 50, HEIGHT + 10, 60/players.length, color);
                }
                for (int y = 0; y < world.length; ++y) {
                    for (int x = 0; x < world[0].length; ++x) {
                        DrawRectangle(x * BLOCKSIZE, y * BLOCKSIZE, BLOCKSIZE, BLOCKSIZE, translateColor(world[y][x]));
                    }
                }
                if(this.game.onePlayerWinner()){
                    Player player = this.game.getWinner();
                    DrawText("Winner-winner chicken dinner is : " + player.getName(),20,20,20,WHITE);
                }
            }else{
                DrawText("Waiting for players....", 20, 20, 20, WHITE);
            }
            EndDrawing();
        }
        client.send_update(QUIT);
        CloseWindow();
    }

    /**
     * Used to translate a short color index to a Jaylib.Color.
     * @param color The short color code corresponding to the color.
     * @return The Jaylib.Color corresponding to the index.
     */
    private Jaylib.Color translateColor(Short color){
        return COLORS[color];
    }
}


