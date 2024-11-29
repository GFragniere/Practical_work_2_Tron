package ch.heigvd.dai.game;
import java.util.List;

import static com.raylib.Jaylib.*;
import static com.raylib.Jaylib.Color;
import static com.raylib.Jaylib.Vector2;

public class TronocolGraphics implements Runnable{

    public final static int HEIGHT = 900;
    public final static int WIDTH = 1800;
    public final static int BLOCKSIZE = 5;

    private Tronocol game;

    public TronocolGraphics(Tronocol game){
        this.game = game;
    }

    public void updateGame(Tronocol game){
        this.game = game;
    }

    @Override
    public void run() {
        InitWindow(WIDTH, HEIGHT, "Tronocol");
        while (!WindowShouldClose()) {
            BeginDrawing();
            if(this.game.GameReady()){
                ClearBackground(BLACK);
                Color[][] world = game.getWorld();
                for (Player p : game.getPlayer()) {
                    DrawRectangle((int) p.getPosition().x() * BLOCKSIZE, (int) p.getPosition().y() * BLOCKSIZE, BLOCKSIZE, BLOCKSIZE, p.getColor());
                }
                for (int y = 0; y < world.length; ++y) {
                    for (int x = 0; x < world[0].length; ++x) {
                        DrawRectangle(x * BLOCKSIZE, y * BLOCKSIZE, BLOCKSIZE, BLOCKSIZE, world[y][x]);
                    }
                }
            }
            EndDrawing();
        }
        CloseWindow();
    }
}

