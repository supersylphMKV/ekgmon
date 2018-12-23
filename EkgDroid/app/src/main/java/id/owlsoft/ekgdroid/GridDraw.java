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
    Paint mark = new Paint();

    private void init() {
        line.setColor(Color.LTGRAY);
        line.setStrokeWidth(2);
        mark.setColor(Color.GRAY);
        mark.setTextSize(24);
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
        for (int w = 50; w < areaWidth-100; w += 50){
            canvas.drawLine(w,0,w,areaHeight-50,line);
        }
        canvas.drawText("Kolom / 100ms",5 ,areaHeight-5 , mark);
        for (int h = 50; h < areaHeight; h += 50){
            canvas.drawLine(0,h,areaWidth,h ,line);
            canvas.drawText(String.valueOf(-(((h/2)-300)/2)),areaWidth-100 ,h-5 , mark);
        }
    }
}
