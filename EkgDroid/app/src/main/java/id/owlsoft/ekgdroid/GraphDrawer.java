package id.owlsoft.ekgdroid;

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
import java.util.Collections;
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
    boolean isRecording;
    List<Vector2> points = new ArrayList<>();
    //List<Float> reservedVal = new ArrayList<>(Arrays.asList(100,-10,80,-20,120,-10));
    byte[] buffers = new byte[0];
    double[] reservedVal = new double[0];
    List<Double> recordedVal = new ArrayList<>();
    List<Integer> pIdx = new ArrayList<>();
    List<Integer> qIdx = new ArrayList<>();
    List<Integer> rIdx = new ArrayList<>();
    List<Integer> sIdx = new ArrayList<>();
    List<Float> rr = new ArrayList<>();
    List<Float> qt = new ArrayList<>();
    List<Float> pr = new ArrayList<>();
    List<Float> qrs = new ArrayList<>();

    float currRRIntervals = 0;
    float minRR = 0;
    float maxRR = 0;
    float currQTIntervals = 0;
    float minQT = 0;
    float maxQT = 0;
    float currPRIntervals = 0;
    float minPR = 0;
    float maxPR = 0;
    float currQRSDuration = 0;
    float minQRS = 0;
    float maxQRS = 0;

    Vector2 dotPos;
    float currentDotTgt;
    float deltaMovement;
    int rCounter = 0;

    List<EventListener> diagnoseChange = new ArrayList<>();

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

    public void OnChangeDIagnosticListener(EventListener e){
        diagnoseChange.add(e);
    }

    public void StartDraw(){
        Log.d("draw","start");
        isDrawing = true;
    }

    public void StopDraw(){
        Log.d("draw","stop");
        isDrawing = false;
        points.clear();
    }

    public void InputData(byte data){
        double[] c = {(double)data};
        InputData(c);
    }

    public void InputData(int data){
        double[] c = {(double)data};
        InputData(c);
    }

    public void InputData(float data){
        double[] c = {(double)data};
        InputData(c);
    }

    public Double[] GetRecord(){
        Double[] objRet = recordedVal.toArray(new Double[recordedVal.size()]);

        recordedVal.clear();

        return objRet;
    }

    public  void InputData(double[] data){
        double[] c = (double[]) Array.newInstance(reservedVal.getClass().getComponentType(), reservedVal.length + data.length);
        System.arraycopy(reservedVal, 0, c, 0, reservedVal.length);
        System.arraycopy(data, 0, c, reservedVal.length, data.length);
        reservedVal = c;
    }

    public void InputBuffers(byte[] buff){
        byte[] c = new byte[buffers.length + buff.length];
    }

    @Override
    public void onDraw(Canvas canvas) {
        if(isDrawing){
            //Log.i("val",String.valueOf(dotPos._y));
            DrawGraph(canvas);
        }
        invalidate();
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
            rCounter++;
            recordedVal.add(reservedVal[0]);
            float mappedVal = (-(float)reservedVal[0] + 255) * 2;
            //Log.i("val", String.valueOf(dotPos._y));
            if(reservedVal.length > 1){
                float nextVal = (-(float)reservedVal[1] + 255) * 2;
                if(Math.abs(nextVal - mappedVal) < 5){
                    mappedVal = mappedVal + ((nextVal - mappedVal)/2);
                }
                if(recordedVal.size() > 1){
                    double pp =  recordedVal.get(recordedVal.size() -2) - recordedVal.get(recordedVal.size()-1);
                    double pa = reservedVal[1] - reservedVal[0];
                    if(pa <= 0 && pp < 0 && reservedVal[0] > 95){
                        //Log.i("peak", String.valueOf(reservedVal[0]));
                        currRRIntervals = rCounter * 10;
                        rIdx.add(recordedVal.size()-1);
                        if(rIdx.size() > 1){
                            if(minRR == 0 || currRRIntervals < minRR){
                                minRR = currRRIntervals;
                            }
                            if(maxRR == 0 || currRRIntervals > maxRR){
                                maxRR = currRRIntervals;
                            }
                        }
                        CalculateQRInterval();
                        CalculateQTInterval();
                        rCounter = 0;
                        if(diagnoseChange.size() > 0){
                            for(EventListener e : diagnoseChange){
                                IntervalData d = new IntervalData();
                                d.currRRIntervals = currRRIntervals;
                                d.minRR = minRR;
                                d.maxRR = maxRR;
                                d.bpm = Math.round(60000/currRRIntervals);
                                d.currPRIntervals = currPRIntervals;
                                d.minPR = minPR;
                                d.maxPR = maxPR;
                                d.currQTIntervals = currQTIntervals;
                                d.minQT = minQT;
                                d.maxQT = maxQT;
                                d.currQRSDuration = currQRSDuration;
                                d.minQRS = minQRS;
                                d.maxQRS = maxQRS;
                                e.call(d);
                            }
                        }
                    }
                }
                reservedVal = Arrays.copyOfRange(reservedVal, 1, reservedVal.length);
            } else {
                reservedVal = new double[0];
            }
            dotPos._y = mappedVal;
        } else {
            dotPos._y = centerY;
        }
    }

    void CalculateQTInterval(){
        boolean t = false;
        boolean at = false;
        int currIdx, lastIdx, nextIdx;
        int ittCount = 0;

        nextIdx = 2;
        currIdx = 1;
        lastIdx = 0;

        while (!at){
            float currVal, lastVal, nextVal;

            if(currIdx < reservedVal.length && nextIdx < reservedVal.length){
                currVal = (float)reservedVal[currIdx];
                nextVal = (float)reservedVal[nextIdx];
                lastVal = (float)reservedVal[lastIdx];
                float n = nextVal - currVal;
                float p = lastVal - currVal;

                nextIdx++;
                currIdx++;
                lastIdx++;

                ittCount++;

                if(!t){
                    if(n > 0 && p >= 0){
                        currQRSDuration = ittCount + (rIdx.get(rIdx.size()-1) - qIdx.get(qIdx.size() -1))  * 10;

                        if(minQRS == 0 || currQRSDuration < minQRS){
                            minQRS = currQRSDuration;
                        }
                        if(maxQRS == 0 || currQRSDuration > maxQRS){
                            maxQRS = currQRSDuration;
                        }

                        t = true;
                    }
                } else {
                    if(n == 0 && p > 0){
                        currQTIntervals = ittCount + (rIdx.get(rIdx.size()-1) - qIdx.get(qIdx.size() -1))  * 10;

                        if(minQT == 0 || currQTIntervals < minQT){
                            minQT = currQTIntervals;
                        }
                        if(maxQT == 0 || currQTIntervals > maxQT){
                            maxQT = currQTIntervals;
                        }

                        at = true;
                    }
                }

            } else {
                at = true;
            }
        }
    }

    void CalculateQRInterval(){
        boolean q = false;
        boolean iq = false;
        int currIdx, lastIdx, nextIdx;
        int ittCount = 0;

        nextIdx = rIdx.get(rIdx.size()-1);
        currIdx = nextIdx-1;
        lastIdx = currIdx-1;

        while (!iq){
            float currVal, lastVal, nextVal;

            if(currIdx >= 0 && lastIdx >= 0){
                currVal = recordedVal.get(currIdx).floatValue();
                nextVal = recordedVal.get(nextIdx).floatValue();
                lastVal = recordedVal.get(lastIdx).floatValue();
                float n = nextVal - currVal;
                float p = lastVal - currVal;

                nextIdx--;
                currIdx--;
                lastIdx--;

                if(!q){
                    if(n < 0 && p <= 0){
                        q = true;
                        qIdx.add(currIdx);
                    }
                } else {
                    ittCount++;

                    if(n > 0 && p >=0){

                        currPRIntervals = ittCount * 10;

                        if(minPR == 0 || currPRIntervals < minPR){
                            minPR = currPRIntervals;
                        }
                        if(maxPR == 0 || currPRIntervals > maxPR) {
                            maxPR = currPRIntervals;
                        }

                        iq = true;
                    }
                }

            } else {
                iq = true;
            }

        }
    }
}
