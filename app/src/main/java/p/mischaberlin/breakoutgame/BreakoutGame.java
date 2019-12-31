package p.mischaberlin.breakoutgame;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class BreakoutGame extends Activity {

    BreakoutView breakoutView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        breakoutView = new BreakoutView(this);
        setContentView(breakoutView);
    }

    class BreakoutView extends SurfaceView implements Runnable {

        Thread gameThread = null;

        SurfaceHolder surfaceHolder;

        volatile boolean playing;

        boolean paused = true;

        Canvas canvas;
        Paint paint;


        long fps;

        private long timeThisFrame;

        //screen size in pixels
        int screenX;
        int screenY;

        Paddle paddle;
        Ball ball;
        Brick[] bricks = new Brick[200];
        int numBricks = 0;

        //for sound
        SoundPool soundPool;
        int beep1ID = -1;
        int beep2ID = -1;
        int beep3ID = -1;
        int loseLifeID = -1;
        int explodeID = -1;

        int score = 0;

        int lives = 3;

        public BreakoutView(Context context) {
            super(context);

            surfaceHolder = getHolder();
            paint = new Paint();
            //get a display object to access screen details
            Display display = getWindowManager().getDefaultDisplay();
            //load resolution
            Point size = new Point();
            display.getSize(size);
            screenX = size.x;
            screenY = size.y;

            paddle = new Paddle(screenX, screenY);
            ball = new Ball();

            soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC,0);

            try{
                // Create objects of the 2 required classes
                AssetManager assetManager = context.getAssets();
                AssetFileDescriptor descriptor;

                // Load our fx in memory ready for use
                descriptor = assetManager.openFd("beep1.ogg");
                beep1ID = soundPool.load(descriptor, 0);

                descriptor = assetManager.openFd("beep2.ogg");
                beep2ID = soundPool.load(descriptor, 0);

                descriptor = assetManager.openFd("beep3.ogg");
                beep3ID = soundPool.load(descriptor, 0);

                descriptor = assetManager.openFd("loseLife.ogg");
                loseLifeID = soundPool.load(descriptor, 0);

                descriptor = assetManager.openFd("explode.ogg");
                explodeID = soundPool.load(descriptor, 0);

            }catch(IOException e){
                // Print an error message to the console
                Log.e("error", "failed to load sound files");
            }

            createBricksAndRestart();

        }

        public void createBricksAndRestart(){
            ball.reset(screenX, screenY);

            int brickWidth = screenX / 8;
            int brickHeight = screenY / 10;

            numBricks = 0;

            for(int column = 0; column < 8; column ++){
                for(int row = 0; row < 3; row++){
                    bricks[numBricks] = new Brick(row, column, brickWidth, brickHeight);
                    numBricks ++;
                }
            }

            score = 0;
            lives = 3;
        }

        @Override
        public void run() {

            while (playing) {
                long startFrameTime = System.currentTimeMillis();
                if (!paused) {
                    update();
                }

                draw();

                timeThisFrame = System.currentTimeMillis() - startFrameTime;
                if (timeThisFrame >= 1) {
                    fps = 1000 / timeThisFrame;
                }
            }
        }

        private void playSound(int soundId){
            soundPool.play(soundId, 1, 1, 0, 0, 1);
        }

        private boolean isHitSide(Brick brick, Ball ball){
            if(ball.getRect().left == brick.getRect().right || ball.getRect().right == brick.getRect().left) return true;
            else return false;
        }

        public void update() {
            paddle.update(fps);
            for(int i = 0; i < numBricks; i++){
                Brick brick = bricks[i];
                if(brick.getVisibility()){
                    if(RectF.intersects(brick.getRect(), ball.getRect())){
                        boolean hitSide = isHitSide(brick, ball);
                        Log.i("hit side", "" + hitSide);
                        brick.setInvisible();
                        if(hitSide) ball.reverseXVelocity();
                        else ball.reverseYVelocity();
                        score = score + 10;
                        playSound(explodeID);
                    }
                }
            }

            //check if ball is colliding with paddle
            if(RectF.intersects(paddle.getRect(), ball.getRect())){
                ball.setRandomXVelocity();
                ball.reverseYVelocity();
                ball.clearObstacleY(paddle.getRect().top - 2);
                playSound(beep1ID);
            }

            if(ball.getRect().bottom > screenY){
                ball.reverseYVelocity();
                ball.clearObstacleY(screenY - 2);

                lives --;
                playSound(loseLifeID);

                if(lives == 0){
                    paused = true;
                    createBricksAndRestart();
                }
            }

            if(ball.getRect().top < 0){
                ball.reverseYVelocity();
                ball.clearObstacleY(12);
                playSound(beep2ID);
            }

            if(ball.getRect().left < 0){
                ball.reverseXVelocity();
                ball.clearObstacleX(2);
                playSound(beep3ID);
            }

            if(ball.getRect().right > screenX - 10){
                ball.reverseXVelocity();
                ball.clearObstacleX(screenX - 22);
                playSound(beep3ID);
            }

            if(score == numBricks * 10){
                paused = true;
                createBricksAndRestart();
            }

            ball.update(fps);
        }

        public void draw() {
            if (!surfaceHolder.getSurface().isValid()) return;

            canvas = surfaceHolder.lockCanvas();

            canvas.drawColor(Color.argb(255, 26, 128, 182));

            paint.setColor(Color.argb(255, 255, 255, 255));

            //draw the paddle
            canvas.drawRect(paddle.getRect(), paint);

            //draw the ball
            canvas.drawRect(ball.getRect(), paint);

            //draw the bricks
            paint.setColor(Color.argb(255, 249, 120, 0));

            for(int i = 0; i < numBricks; i++){
                if(bricks[i].getVisibility()){
                    canvas.drawRect(bricks[i].getRect(), paint);
                }
            }

            //draw the HUD
            paint.setColor(Color.argb(255, 255, 255, 255));
            paint.setTextSize(40);
            String gameStats = "Score: " + score + "   Lives: " + lives;
            canvas.drawText(gameStats, 50, 80, paint);

            //has the player won?
            if(score == numBricks * 10){
                paint.setTextSize(90);
                canvas.drawText("YOU HAVE WON!", 10,screenY/2, paint);
            }

            //has the player lost?
            if(lives <= 0){
                paint.setTextSize(90);
                canvas.drawText("YOU HAVE LOST!", 10,screenY/2, paint);
            }


            //draw everything to the screen
            surfaceHolder.unlockCanvasAndPost(canvas);

        }

        //if the game engine is paused shutdown the thread

        public void pause() {
            playing = false;
            try {
                gameThread.join();
            } catch (InterruptedException e) {
                Log.e("Error:", "joining thread");
            }
        }

        //if game engine activity is started then joing the threat
        public void resume() {
            playing = true;
            gameThread = new Thread(this);
            gameThread.start();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                //player has touched screen
                case MotionEvent.ACTION_DOWN:

                    paused = false;
                    if (event.getX() > screenX / 2 && paddle.getRect().right < screenY){
                        paddle.setMovementState(paddle.RIGHT);
                    }
                    else if (event.getX() < screenX / 2 && paddle.getRect().left > 0){
                        paddle.setMovementState(paddle.LEFT);
                    }else {
                        paddle.setMovementState(paddle.STOPPED);
                    }

                    break;
                //player has stopped touching screen
                case MotionEvent.ACTION_UP:

                    paddle.setMovementState(paddle.STOPPED);
                    break;
            }
            return true;
        }
    }

    //called when game starts
    @Override
    protected void onResume() {
        super.onResume();
        breakoutView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        breakoutView.pause();
    }
}
// This is the end of the BreakoutGame class