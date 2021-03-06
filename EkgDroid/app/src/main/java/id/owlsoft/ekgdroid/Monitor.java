package id.owlsoft.ekgdroid;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Monitor extends AppCompatActivity {

    Boolean logged = false;

    Button btn_ecg, btn_medrec, btn_info, btn_exit,btn_detect,btn_record, btn_change_user;
    LinearLayout frame_ecg, frame_medrec, frame_info, frame_menu, form_pData, frame_pReg;
    FrameLayout frame_menu_spacer, ecg_window, ecg_grid;
    TableLayout db_frame;
    TextView lbl_device_info, resBpm,set_client, lbl_diagnose;
    TextView res_range_rr, res_cur_rr, res_range_pr, res_cur_pr, res_range_qt, res_cur_qt, res_range_qrs, res_cur_qrs;
    TextView lbl_pFullName, lbl_pBirthPlace, lbl_pBirthDate, lbl_pIdNumb;
    Spinner lbl_pIdType, lbl_pTittleName, lbl_pBloodType, client_list;
    RadioGroup  lbl_pGender;

    GraphDrawer graph;
    GridDraw grid;

    ViewTreeObserver vto;

    Vector2 dimension;

    UsbDevice usbD;
    UsbManager usbManager;
    UsbDeviceConnection usbConn;
    UsbSerialDevice serial;

    SocketConn socket = SocketConn.getInstance();

    AppState state = AppState.getInstance();

    Intent popResult;

    UsbSerialInterface.UsbReadCallback usbReadCallback = new UsbSerialInterface.UsbReadCallback() {

        @Override
        public void onReceivedData(byte[] bytes) {
            ByteBuffer wrap = ByteBuffer.wrap(bytes);
            String tVal = new String(bytes, StandardCharsets.US_ASCII);
            String[] pVal = tVal.split("\\s+");
            int val = 0;
            try {
                for(String s : pVal){
                    val = Integer.parseInt(s.trim());
                    graph.InputData(val);
                    lastVal = val;
                }

            } catch (NumberFormatException e){
                Log.e("parse",e.toString());
                graph.InputData(lastVal);
            }
        }
    };

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

    protected void DetectDevice(){
        usbManager = (UsbManager)getSystemService(Context.USB_SERVICE);
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
        grid = new GridDraw(this);

        client_list = findViewById(R.id.client_list);

        lbl_device_info = findViewById(R.id.lbl_device_info);
        lbl_diagnose = findViewById(R.id.lbl_diagnosa);
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
        btn_change_user = findViewById(R.id.btn_change_user);
        set_client = findViewById(R.id.btn_set_client);

        frame_ecg = findViewById(R.id.body_frame);
        frame_menu = (LinearLayout)findViewById(R.id.menu_frame);
        frame_medrec = (LinearLayout)findViewById(R.id.db_frame);
        frame_info = (LinearLayout)findViewById(R.id.info_frame);
        frame_menu_spacer = (FrameLayout) findViewById(R.id.menu_spacer);

        db_frame = findViewById(R.id.db_parent);

        ecg_window = findViewById(R.id.window_ekg);
        ecg_grid = findViewById(R.id.ekg_grid);

        ecg_window.addView(graph);
        ecg_grid.addView(grid);

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

        Date dt = Calendar.getInstance().getTime();
        popResult = new Intent(this, PopResult.class);

        graph.OnTestDoneListener(new EventListener() {
            @Override
            public void call(Object result) {
                GraphDrawer.ECGRes testResult = (GraphDrawer.ECGRes)result;
                state.testResult = testResult;
                popResult.putExtra("isReplay", graph.rePlaying);
                startActivity(popResult);
                /*JSONArray testRes = new JSONArray();
                Date dt = Calendar.getInstance(Locale.getDefault()).getTime();
                JSONObject dataSend = new JSONObject();

                try{
                    for(float f : (float[])testResult.record){
                        testRes.put(f);
                    }
                    dataSend.put("data", testRes);
                    dataSend.put("user", state.userData.get("id"));
                    dataSend.put("name", state.userData.get("fullName"));
                    dataSend.put("date", dt);

                    socket.InputData("r", dataSend, new EventListener() {
                        @Override
                        public void call(Object result) {
                            Log.d("save", "Success");
                        }
                    });
                } catch (JSONException e){

                }*/
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
                graph.SetCenterGraph(width - 150, height/2);

                grid.SetRectDimension(width, height);
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
                state.isReplay = false;
                if(graph.isDrawing){
                    stopSerialConnection();
                    btn_detect.setText("DETEKSI");
                } else {
                    if(usbManager != null && usbD != null){
                        usbConn = usbManager.openDevice(usbD);
                        serial = UsbSerialDevice.createUsbSerialDevice(usbD, usbConn);
                        startSerialConnection();
                        btn_detect.setText("STOP");
                    } else {
                        DetectDevice();
                        if(usbD != null){
                            usbConn = usbManager.openDevice(usbD);
                            serial = UsbSerialDevice.createUsbSerialDevice(usbD, usbConn);
                            startSerialConnection();
                            btn_detect.setText("STOP");
                        } else {
                            lbl_device_info.setText(R.string.info_noDevice);
                            lbl_device_info.setTextColor(Color.RED);
                        }
                    }
                }
            }
        });
        btn_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        btn_change_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unVerified();
                Log.d("click", "change");
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

        //startActivity(popResult);
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
                OnReplay();
                break;
            default:
                OnECG();
                break;
        }
    }

    void stopSerialConnection(){
        if(serial != null){
            serial.read(null);
            serial.close();
            usbConnected = false;
            graph.StopDraw(true);
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
            serial.read(usbReadCallback);
            graph.StartDraw();
        }
    }

    void OnECG(){
        for(View v:frames.values()){
            v.setVisibility(View.GONE);
        }
        graph.StopDraw();

        ResetDiagVal();

        btn_record.setVisibility(View.GONE);
        btn_detect.setVisibility(View.VISIBLE);
        frame_menu.setVisibility(View.GONE);
        frame_menu_spacer.setVisibility(View.GONE);
        frame_ecg.setVisibility(View.VISIBLE);
        lbl_diagnose.setText(R.string.lbl_diagnose);
        state.tab = 1;
    }

    void OnReplay(){
        for(View v:frames.values()){
            v.setVisibility(View.GONE);
        }

        ResetDiagVal();

        btn_record.setVisibility(View.GONE);
        btn_detect.setVisibility(View.GONE);
        frame_menu.setVisibility(View.GONE);
        frame_menu_spacer.setVisibility(View.GONE);
        frame_ecg.setVisibility(View.VISIBLE);
        lbl_diagnose.setText(R.string.lbl_medresult);
    }

    void OnMedRec(){
        for(View v:frames.values()){
            v.setVisibility(View.GONE);
        }
        graph.StopDraw();
        frame_menu.setVisibility(View.GONE);
        frame_menu_spacer.setVisibility(View.GONE);
        frame_medrec.setVisibility(View.VISIBLE);

        db_frame.removeAllViews();

        HashMap<String, Object> filter = new HashMap<>();
        HashMap<String, Object> dFilter = new HashMap<>();

        try {
            if(state.userData.get("userType").toString().equals("User")){
                filter.put("user", state.userData.get("id"));
                dFilter.put("userType", "Dokter");
                socket.GetList("d", dFilter, new EventListener() {
                    @Override
                    public void call(Object result) {
                        JSONArray objRes = (JSONArray)result;
                        try {
                            if(objRes != null && objRes.length() > 0){
                                List<String> dokterSpinner = new ArrayList<>();
                                dokterSpinner.add("Tanpa Dokter");

                                for(int i = 0; i < objRes.length(); i++){
                                    JSONObject item = objRes.getJSONObject(i);

                                    dokterSpinner.add(item.getString("fullName"));
                                }

                                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_item, dokterSpinner);

                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        client_list.setAdapter(adapter);
                                    }
                                });
                            }
                        } catch (JSONException e){

                        }
                    }
                });
            } else {

            }

        } catch (JSONException e){

        }

        socket.GetList("r", filter, new EventListener() {
            @Override
            public void call(Object result) {
                JSONArray objRes = (JSONArray)result;
                if(objRes != null && objRes.length() > 0){
                    for(int it = 0; it < objRes.length(); it++){
                        try {
                            JSONObject item = objRes.getJSONObject(it);
                            DisplayDataList(it, item);
                        } catch (JSONException e){

                        }
                    };
                }
            }
        });

        state.tab = 2;
    }

    void DisplayDataList(int i,JSONObject item){
        try{
            socket.GetData("d", item.getString("user"), ("age_" + i).hashCode(), new EventListener() {
                @Override
                public void call(Object retUser) {
                    JSONObject usr = (JSONObject)retUser;
                    try {
                        String ds = item.getString("date");
                        Date recDate = new Date(ds);
                        Date dob = new Date(usr.getString("birthDate"));
                        int ageId = usr.getInt("reqId");
                        int yob = dob.getYear();
                        int yon = new Date().getYear();
                        int a = yon - yob;

                        TableRow container = new TableRow(getBaseContext());
                        TextView name = new TextView(getBaseContext());
                        TextView age = new TextView(getBaseContext());
                        TextView date = new TextView(getBaseContext());
                        TextView resBtn = new TextView(getBaseContext());

                        TableRow.LayoutParams nameParam = new TableRow.LayoutParams(0,TableRow.LayoutParams.WRAP_CONTENT);
                        TableRow.LayoutParams ageParam = new TableRow.LayoutParams(0,TableRow.LayoutParams.WRAP_CONTENT);
                        TableRow.LayoutParams dateParam = new TableRow.LayoutParams(0,TableRow.LayoutParams.WRAP_CONTENT);
                        TableRow.LayoutParams btnParam = new TableRow.LayoutParams(0,TableRow.LayoutParams.WRAP_CONTENT);

                        nameParam.weight = 6;
                        ageParam.weight = 2;
                        dateParam.weight = 3;
                        btnParam.weight = 2;

                        container.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                        name.setLayoutParams(nameParam);
                        date.setLayoutParams(dateParam);
                        age.setLayoutParams(ageParam);
                        resBtn.setLayoutParams(btnParam);

                        name.setPadding(2,2,2,2);
                        date.setPadding(2,2,2,2);
                        age.setPadding(2,2,2,2);
                        resBtn.setPadding(2,2,2,2);

                        name.setText(item.getString("name"));
                        date.setText(Date(item.getString("date")));
                        resBtn.setText(R.string.db_lbl_hasil);
                        age.setText("" + a);

                        name.setTextColor(getColor(R.color.color_dark_navy_blue));
                        date.setTextColor(getColor(R.color.color_dark_navy_blue));
                        age.setTextColor(getColor(R.color.color_dark_navy_blue));
                        resBtn.setTextColor(getColor(R.color.color_dark_navy_blue));


                        container.addView(name);
                        container.addView(age);
                        container.addView(date);
                        container.addView(resBtn);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                db_frame.addView(container);
                                resBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        setTab(4);
                                        JSONArray arrayObj;
                                        try {
                                            arrayObj = item.getJSONArray("data");
                                            graph.InputData(arrayObj);
                                            graph.StartDraw(true);
                                            state.isReplay = true;
                                        } catch (JSONException e){

                                        }
                                    }
                                });
                            }
                        });


                    } catch (JSONException e){

                    }

                }
            });
        }catch(JSONException e){

        }
    }

    String Date(String s){
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        String[] sArr = s.split(" ");
        int m = 0;
        for(int i = 0; i < months.length; i++){
            if(months[i].equals(sArr[1])){
                m = i+1;
                i = months.length;
            }
        }
        String outRet = sArr[2] + "/" + m + "/" + sArr[5] ;
        return outRet;
    }

    void ResetDiagVal(){
        resBpm.setText("");
        res_cur_qrs.setText("");
        res_cur_qt.setText("");
        res_cur_pr.setText("");
        res_cur_rr.setText("");
        res_range_qrs.setText("");
        res_range_qt.setText("");
        res_range_pr.setText("");
        res_range_rr.setText("");
    }

    void OnInfo(){
        for(View v:frames.values()){
            v.setVisibility(View.GONE);
        }
        graph.StopDraw();
        frame_menu.setVisibility(View.GONE);
        frame_menu_spacer.setVisibility(View.GONE);
        frame_info.setVisibility(View.VISIBLE);
        state.tab = 3;
    }

    void OnMenu(){
        for(View v:frames.values()){
            v.setVisibility(View.GONE);
        }
        graph.StopDraw();
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
