package owlsoft.ekgmon;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.res.TypedArrayUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Handler;

public class GraphDrawer extends View{
    Paint line = new Paint();
    Paint dot = new Paint();
    int areaWidth;
    int areaHeight;
    float centerX;
    float centerY;
    boolean isDrawing;
    boolean isTracking;
    List<Vector2> points = new ArrayList<>();
    //List<Float> reservedVal = new ArrayList<>(Arrays.asList(100,-10,80,-20,120,-10));
    float[] reservedVal = new float[0];
    Vector2 dotPos;
    float currentDotTgt;
    float deltaMovement;

    private void init() {
        line.setColor(Color.YELLOW);
        line.setStrokeWidth(5);
        dot.setColor(Color.YELLOW);
        dot.setStrokeWidth(10);
        dotPos = new Vector2(centerX,centerY);
    }

    public void SetRectDimension(int width, int height){
        areaWidth = width;
        areaHeight = height;
    }

    public void SetCenterGraph(float x, float y){
        centerX = x;
        centerY = y;
        dotPos = new Vector2(centerX,centerY);
    }

    public GraphDrawer(Context context) {
        super(context);
        init();
    }

    public GraphDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GraphDrawer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void StartDraw(){
        isDrawing = true;
    }

    public void StopDraw(){
        isDrawing = false;
    }

    public void InputData(float data){
        float[] c = {data};
        InputData(c);
    }

    public  void InputData(float[] data){
        float[] c = (float[]) Array.newInstance(reservedVal.getClass().getComponentType(), reservedVal.length + data.length);
        System.arraycopy(reservedVal, 0, c, 0, reservedVal.length);
        System.arraycopy(data, 0, c, reservedVal.length, data.length);
        reservedVal = c;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if(isDrawing){
            DrawGraph(canvas);
        }
    }

    void DrawGraph(Canvas canvas){
        CalculateDotPos();
        CalculateLines();
        canvas.drawPoint(dotPos._x, dotPos._y, dot);
        if(points.size() > 2){
            for(int i = 1; i < points.size();i++){
                canvas.drawLine(points.get(i-1)._x,points.get(i-1)._y,points.get(i).x(),points.get(i).y(),line);
            }
        }

        invalidate();
    }

    void CalculateLines(){
        if(points.size() > 0){
            for(Vector2 v : points){
                v._x -=5;
            }
        }

        points.add(new Vector2(dotPos));

        if(points.get(0)._x < 0 ){
            points.remove(0);
        }
    }

    void CalculateDotPos(){
        if(reservedVal.length > 0){
            if(!isTracking){
                currentDotTgt = reservedVal[0] + centerY;
                if(reservedVal.length > 1){
                    reservedVal = Arrays.copyOfRange(reservedVal, 1, reservedVal.length);
                } else {
                    reservedVal = new float[0];
                }

                if(currentDotTgt > dotPos._y){
                    deltaMovement = 1;
                } else {
                    deltaMovement = -1;
                }

                isTracking = true;
            } else {
                if(deltaMovement > 0){
                   if(dotPos._y < currentDotTgt){
                       dotPos._y += 10;
                   } else {
                       isTracking = false;
                   }
                } else {
                    if(dotPos._y > currentDotTgt){
                        dotPos._y -=10;
                    } else {
                        isTracking = false;
                    }
                }
            }
        } else {
            if(dotPos._y > centerY){
                dotPos._y -=10;
            } else if(dotPos._y < centerY) {
                dotPos._y +=10;
            } else if((dotPos._y - centerY) < 10){
                dotPos._y = centerY;
            }
        }
    }
}
