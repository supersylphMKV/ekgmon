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

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Monitor extends AppCompatActivity {

    Boolean logged = false;

    Button btn_ecg, btn_medrec, btn_info, btn_exit,btn_detect,btn_record,btn_setting,btn_pList_tambah,btn_pList_batal,btn_pSubmit;
    LinearLayout frame_medrec, frame_info, frame_menu, form_pData, frame_pReg;
    ConstraintLayout frame_ecg;
    FrameLayout frame_menu_spacer, ecg_window;
    TableLayout frame_pasien;
    TextView lbl_device_info,btn_card_changeData;
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
                        //Log.i("dev", "Device found: " + device.getDeviceName());
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
        lbl_pBirthDate = findViewById(R.id.input_reg_pasien_bdate);
        lbl_pBirthPlace = findViewById(R.id.input_reg_pasien_bplace);
        lbl_pFullName = findViewById(R.id.input_reg_pasien_name);
        lbl_pTittleName = findViewById(R.id.input_reg_pasien_tname);
        lbl_pGender = findViewById(R.id.check_gender_group);
        lbl_pIdType = findViewById(R.id.input_reg_pasien_idtype);
        lbl_pIdNumb = findViewById(R.id.input_reg_pasien_idnumb);
        lbl_pBloodType = findViewById(R.id.input_reg_pasien_bloodtype);

        btn_ecg = (Button) findViewById(R.id.btn_test);
        btn_medrec = (Button)findViewById(R.id.btn_db);
        btn_info = (Button)findViewById(R.id.btn_info);
        btn_exit = (Button)findViewById(R.id.btn_exit);
        btn_detect = findViewById(R.id.btn_deteksi);
        btn_record = findViewById(R.id.btn_record);
        btn_setting = findViewById(R.id.btn_setting);
        btn_card_changeData = findViewById(R.id.btn_card_changeData);
        btn_pList_batal = findViewById(R.id.btn_cancel_plist);
        btn_pList_tambah = findViewById(R.id.btn_add_plist);
        btn_pSubmit = findViewById(R.id.btn_reg_psubmit);

        frame_ecg = (ConstraintLayout)findViewById(R.id.body_frame);
        frame_menu = (LinearLayout)findViewById(R.id.menu_frame);
        frame_medrec = (LinearLayout)findViewById(R.id.db_frame);
        frame_info = (LinearLayout)findViewById(R.id.info_frame);
        form_pData = findViewById(R.id.lbl_form_data);
        frame_menu_spacer = (FrameLayout) findViewById(R.id.menu_spacer);
        frame_pasien = findViewById(R.id.pasien_frame);
        frame_pReg = findViewById(R.id.pasienreg_frame);

        ecg_window = findViewById(R.id.window_ekg);
        ecg_window.addView(graph);

        if(frames == null){
            frames = new HashMap<String, View>();
        }
        frames.put("frame_ecg", frame_ecg);
        frames.put("frame_medrec", frame_medrec);
        frames.put("frame_info", frame_info);
        frames.put("frame_pasien", frame_pasien);
        frames.put("frame_pReg", frame_pReg);

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
        btn_card_changeData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTab(4);
            }
        });
        btn_pList_batal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTab(1);
            }
        });
        btn_pList_tambah.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTab(5);
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

            }
        });
        btn_pSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnAddPasien();
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
            case 5:
                OnPRegister();
                break;
            default:
                OnECG();
                break;
        }
    }

    void startSerialConnection() {

        if (serial != null) {
            serial.open();
            serial.setBaudRate(9600);
            serial.setDataBits(UsbSerialInterface.DATA_BITS_8);
            serial.setStopBits(UsbSerialInterface.STOP_BITS_1);
            serial.setParity(UsbSerialInterface.PARITY_NONE);
            serial.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);

            usbConnected = true;

            if(graph.isDrawing){
                graph.StopDraw();
            } else {
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

    void OnPRegister(){
        for(View v:frames.values()){
            v.setVisibility(View.GONE);
        }
        frame_menu.setVisibility(View.GONE);
        frame_menu_spacer.setVisibility(View.GONE);
        frame_pReg.setVisibility(View.VISIBLE);
        state.tab = 5;
    }

    void OnMenu(){
        for(View v:frames.values()){
            v.setVisibility(View.GONE);
        }
        isMenu = true;
    }

    void OnAddPasien(){
        JSONObject query = new JSONObject();
        RadioButton gender = findViewById(lbl_pGender.getCheckedRadioButtonId());
        try {
            query.put("fullName", lbl_pFullName.getText().toString());
            query.put("tittleName", lbl_pTittleName.getSelectedItem().toString());
            query.put("gender", gender.getText().toString());
            query.put("blood", lbl_pBloodType.getSelectedItem().toString());
            query.put("idType", lbl_pIdType.getSelectedItem().toString());
            query.put("idNumber", lbl_pIdNumb.getText().toString());
            query.put("birthPlace", lbl_pBirthPlace.getText().toString());
            query.put("birthDate", lbl_pBirthDate.getText().toString());

            socket.AddPasienData(query, new EventListener() {
                @Override
                public void call(Object result) {
                    if(result.toString().equals("Success")){
                        setTab(4);
                    }
                }
            });
        } catch (JSONException e){

        }
    }

    void CheckUser(){
        unVerified();
    }

    void unVerified(){
        Intent loginIntent = new Intent(this, Login.class);
        loginIntent.putExtra("logged",logged);
        startActivity(loginIntent);
    }



    void PopulatePasienList(JSONArray list){

        for (int i = 0; i < list.length(); i++){
            TableRow itemParent = new TableRow(this);
            itemParent.setLayoutParams(new TableLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
            TextView pName = new TextView(this);
            //pName.setLayoutParams(new La);
        }
    }
}
