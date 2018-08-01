package id.owlsoft.ekgdroid;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class GridDraw extends View{

    int areaWidth;
    int areaHeight;
    Paint line = new Paint();

    private void init() {
        line.setColor(Color.LTGRAY);
        line.setStrokeWidth(2);
    }

    public void SetRectDimension(int width, int height){
        areaWidth = width;
        areaHeight = height;
    }

    public GridDraw(Context context) {
        super(context);
        init();
    }

    public GridDraw(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GridDraw(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void onDraw(Canvas canvas) {
        for (int w = 50; w < areaWidth; w += 50){
            canvas.drawLine(w,0,w,areaHeight,line);
        }
        for (int h = 50; h < areaHeight; h += 50){
            canvas.drawLine(0,h,areaWidth,h,line);
        }
    }
}
