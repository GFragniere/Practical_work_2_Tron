package ch.heigvd.dai.game;
import ch.heigvd.dai.tronocol.TronocolClient;
import com.raylib.Jaylib;

import static com.raylib.Jaylib.*;
import java.awt.Color;

public class TronocolGraphics implements Runnable{

    public final static int HEIGHT = 200;
    public final static int WIDTH = 200;
    public final static int BLOCKSIZE = 5;
    private boolean shouldExit = false;
    private final static String UPDATE = "UPDATE";
    private Tronocol game;
    private TronocolClient client;

    public TronocolGraphics(Tronocol game, TronocolClient tronocolClient){
        this.game = game;
        this.client = tronocolClient;
    }

    public void setGame(Tronocol game) {
        this.game = game;
    }

    public void updateGame(Tronocol game){
        this.game = game;
    }

    public void exit() {
        shouldExit = true;
    }

    @Override
    public void run() {
        SetTraceLogLevel(7);
        InitWindow(WIDTH, HEIGHT, "Tronocol");
        SetTargetFPS(60);
        while (!WindowShouldClose() && !shouldExit) {
            BeginDrawing();
            if(this.game.GameReady()){
                System.out.println("Coucou");
                if (IsKeyDown(KEY_LEFT)) client.send_update(UPDATE,Direction.LEFT);
                if (IsKeyDown(KEY_RIGHT)) client.send_update(UPDATE,Direction.RIGHT);
                if (IsKeyDown(KEY_DOWN)) client.send_update(UPDATE,Direction.DOWN);
                if (IsKeyDown(KEY_UP)) client.send_update(UPDATE,Direction.UP);
                ClearBackground(BLACK);

                Color[][] world = game.getWorld();
                for (Player p : game.getPlayer()) {
                    DrawRectangle((int) p.getPosition().getX() * BLOCKSIZE, (int) p.getPosition().getY() * BLOCKSIZE, BLOCKSIZE, BLOCKSIZE, translateColor(p.getColor()));
                }
                for (int y = 0; y < world.length; ++y) {
                    for (int x = 0; x < world[0].length; ++x) {
                        DrawRectangle(x * BLOCKSIZE, y * BLOCKSIZE, BLOCKSIZE, BLOCKSIZE, translateColor(world[y][x]));
                    }
                }
            }
            EndDrawing();
        }
        CloseWindow();
    }

    private Jaylib.Color translateColor(Color color){
        return new Jaylib.Color(color.getRed(),color.getBlue(),color.getGreen(),color.getAlpha());
    }
}


