package p.mischaberlin.breakoutgame;

import android.graphics.RectF;

public class Brick {

    private RectF rect;
    private boolean isVisible;
    private int height;
    public Brick(int row, int column, int width, int height){
        isVisible = true;
        this.height = height;

        int padding = 1;
        float left = column * width + padding;
        float top = row * height + padding;
        float right = column * width + width - padding;
        float bottom = row * height + height - padding;
        rect = new RectF(left, top, right, bottom);
    }

    public int getHeight(){
        return this.height;
    }

    public RectF getRect(){
        return this.rect;
    }

    public void setInvisible(){
        isVisible = false;
    }

    public boolean getVisibility(){
        return isVisible;
    }



}
