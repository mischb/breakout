package p.mischaberlin.breakoutgame;

import android.graphics.RectF;
import android.util.Log;

public class Paddle {

    //RectF is an object that holds four coordinates - just what we need

    private RectF rect;

    // paddle dimensions
    private float length;
    private float height;

    // x and y coordinates
    private float x;
    private float y;

    //pxs/second -- speed of the paddle
    private float paddleSpeed;

    //paddle directions
    public final int STOPPED = 0;
    public final int LEFT = 1;
    public final int RIGHT = 2;

    // is the paddle moving and where?
    private int paddleMoving = STOPPED;

    //constructor - gets screen width and height
    public Paddle(int screenX, int screenY) {
        //130 pxs wide and 20pxs high
        length = 130;
        height = 20;

        x = screenX / 2;
        y = screenY - 20;

        rect = new RectF(x, y, x + length, y + height);

        paddleSpeed = 350;
    }

    public  RectF getRect(){
        return rect;
    }

    public void setMovementState(int state){
        paddleMoving = state;
    }

    public void update(long fps){
        if(paddleMoving == LEFT){
            x = x - paddleSpeed / fps;
        }

        if(paddleMoving == RIGHT){
            x = x + paddleSpeed / fps;
        }

        rect.left = x;
        rect.right = x + length;
    }

}
