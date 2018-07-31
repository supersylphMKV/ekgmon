package id.owlsoft.ekgdroid;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Space;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.Wrapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Monitor extends AppCompatActivity {

    Boolean logged = false;

    Button btn_ecg, btn_medrec, btn_info, btn_exit,btn_detect,btn_record,btn_setting;
    LinearLayout frame_ecg, frame_medrec, frame_info, frame_menu, form_pData, frame_pReg;
    FrameLayout frame_menu_spacer, ecg_window;
    TableLayout frame_pasien;
    TextView lbl_device_info, resBpm;
    TextView res_range_rr, res_cur_rr, res_range_pr, res_cur_pr, res_range_qt, res_cur_qt, res_range_qrs, res_cur_qrs;
    TextView lbl_pFullName, lbl_pBirthPlace, lbl_pBirthDate, lbl_pIdNumb;
    Spinner lbl_pIdType, lbl_pTittleName, lbl_pBloodType;
    RadioGroup  lbl_pGender;

    GraphDrawer graph;

    ViewTreeObserver vto;

    Vector2 dimension;

    UsbDevice usbD;
    UsbManager usbManager;
    UsbDeviceConnection usbConn;
    UsbSerialDevice serial;

    SocketConn socket = SocketConn.getInstance();

    AppState state = AppState.getInstance();

    Map<String, View> frames;

    boolean usbConnected;
    boolean isMenu;

    int lastVal = 0;

    @Override
    protected void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        if(usbManager == null){
            usbManager = getSystemService(UsbManager.class);
        }
        if(intent != null && usbManager != null){
            String actionType = intent.getAction();
            if( actionType != null && actionType.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                Map<String, UsbDevice> connectedDevices = usbManager.getDeviceList();
                for (UsbDevice device : connectedDevices.values()) {
                    if (device.getVendorId() == 0x1a86 && device.getProductId() == 0x7523) {
                        Log.i("dev", "Device found: " + device.getDeviceName());
                        usbD = device;
                        lbl_device_info.setText("ready");
                        lbl_device_info.setTextColor(Color.BLUE);
                        break;
                    }
                }
            }
            if(actionType != null && actionType.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)){
                usbD = null;
                lbl_device_info.setText("not connected");
                lbl_device_info.setTextColor(Color.RED);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        logged = state.isLogged;
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_monitor);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_drawer);
        actionbar.setDisplayShowTitleEnabled(false);

        graph = new GraphDrawer(this);

        lbl_device_info = findViewById(R.id.lbl_device_info);
        resBpm = findViewById(R.id.res_bpm);
        res_cur_rr = findViewById(R.id.res_current_rr);
        res_range_rr = findViewById(R.id.res_range_rr);
        res_cur_pr = findViewById(R.id.res_current_pr);
        res_range_pr = findViewById(R.id.res_range_pr);
        res_cur_qt = findViewById(R.id.res_current_qt);
        res_range_qt = findViewById(R.id.res_range_qt);
        res_cur_qrs = findViewById(R.id.res_current_qrs);
        res_range_qrs = findViewById(R.id.res_range_qrs);

        btn_ecg = (Button) findViewById(R.id.btn_test);
        btn_medrec = (Button)findViewById(R.id.btn_db);
        btn_info = (Button)findViewById(R.id.btn_info);
        btn_exit = (Button)findViewById(R.id.btn_exit);
        btn_detect = findViewById(R.id.btn_deteksi);
        btn_record = findViewById(R.id.btn_record);
        btn_setting = findViewById(R.id.btn_setting);

        frame_ecg = findViewById(R.id.body_frame);
        frame_menu = (LinearLayout)findViewById(R.id.menu_frame);
        frame_medrec = (LinearLayout)findViewById(R.id.db_frame);
        frame_info = (LinearLayout)findViewById(R.id.info_frame);
        frame_menu_spacer = (FrameLayout) findViewById(R.id.menu_spacer);

        ecg_window = findViewById(R.id.window_ekg);
        ecg_window.addView(graph);

        if(frames == null){
            frames = new HashMap<String, View>();
        }
        frames.put("frame_ecg", frame_ecg);
        frames.put("frame_medrec", frame_medrec);
        frames.put("frame_info", frame_info);

        graph.OnChangeDIagnosticListener(new EventListener() {
            @Override
            public void call(Object result) {
                IntervalData res = (IntervalData)result;
                resBpm.setText(String.valueOf(res.bpm));
                res_cur_rr.setText(String.valueOf(res.currRRIntervals));
                res_range_rr.setText(res.minRR + " - " + res.maxRR + " ms");
                res_cur_pr.setText(String.valueOf(res.currPRIntervals));
                res_range_pr.setText(res.minPR + " - " + res.maxPR + " ms");
                res_cur_qt.setText(String.valueOf(res.currQTIntervals));
                res_range_qt.setText(res.minQT + " - " + res.maxQT + " ms");
                res_cur_qrs.setText(String.valueOf(res.currQRSDuration));
                res_range_qrs.setText(res.minQRS + " - " + res.maxQRS + " ms");
            }
        });

        vto = ecg_window.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ecg_window.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width = ecg_window.getWidth();
                int height = ecg_window.getHeight();

                dimension = new Vector2(width, height);
                graph.SetRectDimension(width,height);
                graph.SetCenterGraph(width - 20, height/2);
            }
        });

        if(!logged){
            CheckUser();
        }

        btn_ecg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTab(1);
            }
        });
        btn_medrec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTab(2);
            }
        });
        btn_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTab(3);
            }
        });
        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishAffinity();
            }
        });
        btn_detect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(usbManager != null && usbD != null){
                    usbConn = usbManager.openDevice(usbD);
                    serial = UsbSerialDevice.createUsbSerialDevice(usbD, usbConn);
                    startSerialConnection();
                }
            }
        });
        btn_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                graph.isRecording = !graph.isRecording;

                if(!graph.isRecording){
                    Double[] recordRes = graph.GetRecord();
                    graph.StopDraw();
                }
            }
        });
        frame_menu_spacer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                frame_menu.setTranslationX(0);
                frame_menu_spacer.setVisibility(View.GONE);
                setTab(state.tab);
                frame_menu.animate().translationX(-frame_menu.getWidth()).setDuration(300).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation, boolean isReverse) {
                        frame_menu.setVisibility(View.GONE);
                    }
                });
            }
        });

        setTab(state.tab);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(!isMenu){
                    frame_menu.setTranslationX(-frame_menu.getWidth());
                    frame_menu.setVisibility(View.VISIBLE);
                    frame_menu_spacer.setVisibility(View.VISIBLE);
                    frame_menu.animate().translationX(0).setDuration(300);
                    OnMenu();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void setTab(int state){
        isMenu = false;
        switch (state){
            case 1 :
                OnECG();
                break;
            case 2 :
                OnMedRec();
                break;
            case 3:
                OnInfo();
                break;
            case 4:
                OnPasien();
                break;
            default:
                OnECG();
                break;
        }
    }

    void startSerialConnection() {
        if (serial != null) {
            usbConnected = true;

            if(graph.isDrawing){
                serial.close();
                graph.StopDraw();
            } else {
                serial.open();
                serial.setBaudRate(9600);
                serial.setDataBits(UsbSerialInterface.DATA_BITS_8);
                serial.setStopBits(UsbSerialInterface.STOP_BITS_1);
                serial.setParity(UsbSerialInterface.PARITY_NONE);
                serial.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                serial.read(new UsbSerialInterface.UsbReadCallback() {

                    @Override
                    public void onReceivedData(byte[] bytes) {
                        ByteBuffer wrap = ByteBuffer.wrap(bytes);
                        String tVal = new String(bytes, StandardCharsets.US_ASCII);
                        String[] pVal = tVal.split("\\s+");
                        int val = 0;
                        try {
                            for(String s : pVal){
                                val = Integer.parseInt(s.trim());
                                //Log.i("rcvd ", "char : " + tVal + ", val : " + val);
                                graph.InputData(val);
                                lastVal = val;
                            }

                        } catch (NumberFormatException e){
                            Log.e("parse",e.toString());
                            graph.InputData(lastVal);
                        }
                        //graph.InputData(wrap.getInt(1));

                       /* if(bytes.length == 4){
                            int intVal = wrap.getInt();
                            graph.InputData(intVal);
                            //Log.i("rcvd", "char : " + wrap.getChar() + "int : " + intVal);
                        }*/
                    }
                });
                graph.StartDraw();
            }
        }
    }

    void OnECG(){
        for(View v:frames.values()){
            v.setVisibility(View.GONE);
        }
        frame_menu.setVisibility(View.GONE);
        frame_menu_spacer.setVisibility(View.GONE);
        frame_ecg.setVisibility(View.VISIBLE);
        state.tab = 1;
    }

    void OnMedRec(){
        for(View v:frames.values()){
            v.setVisibility(View.GONE);
        }
        frame_menu.setVisibility(View.GONE);
        frame_menu_spacer.setVisibility(View.GONE);
        frame_medrec.setVisibility(View.VISIBLE);
        state.tab = 2;
    }

    void OnInfo(){
        for(View v:frames.values()){
            v.setVisibility(View.GONE);
        }
        frame_menu.setVisibility(View.GONE);
        frame_menu_spacer.setVisibility(View.GONE);
        frame_info.setVisibility(View.VISIBLE);
        state.tab = 3;
    }

    void  OnPasien(){

        socket.GetList("p", null, new EventListener() {
            @Override
            public void call(Object result) {
                if(result instanceof JSONArray){
                    JSONArray resObj = (JSONArray)result;
                    if(resObj.length() > 0){

                    }
                }
            }
        });

        for(View v:frames.values()){
            v.setVisibility(View.GONE);
        }
        frame_menu.setVisibility(View.GONE);
        frame_menu_spacer.setVisibility(View.GONE);
        frame_pasien.setVisibility(View.VISIBLE);
        state.tab = 4;
    }

    void OnMenu(){
        for(View v:frames.values()){
            v.setVisibility(View.GONE);
        }
        isMenu = true;
    }

    void CheckUser(){
        unVerified();
    }

    void unVerified(){
        Intent loginIntent = new Intent(this, Login.class);
        loginIntent.putExtra("logged",logged);
        startActivity(loginIntent);
    }
}
