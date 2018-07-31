package id.owlsoft.ekgdroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.nkzawa.socketio.client.Ack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class Daftar extends Activity {

    SocketConn socketConn = SocketConn.getInstance();
    Button btn_submit;
    EditText input_fullname,input_nip;
    EditText input_pFullname, input_pIdNumb, input_pBirthPlace, input_pBirthDate;
    Spinner input_pTittleName, input_pIdType, input_pBloodType;
    RadioGroup input_pGender;
    EditText input_user,input_password,input_passCheck;
    Spinner input_usertype;
    TextView lbl_info;
    LinearLayout group_dData,group_pData;

    Intent intentLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_daftar);

        intentLogin = new Intent(this, Login.class);

        btn_submit = (Button)findViewById(R.id.btn_submit);
        input_usertype = findViewById(R.id.input_usertype);

        input_user = findViewById(R.id.input_user);
        input_password = findViewById(R.id.input_sandi);
        input_passCheck = findViewById(R.id.input_sandi2);

        group_dData = findViewById(R.id.group_dData);
        input_fullname = findViewById(R.id.input_nama);
        input_nip = findViewById(R.id.input_nip);

        group_pData = findViewById(R.id.group_pData);
        input_pFullname = findViewById(R.id.input_reg_pasien_name);
        input_pTittleName = findViewById(R.id.input_reg_pasien_tname);
        input_pBloodType = findViewById(R.id.input_reg_pasien_bloodtype);
        input_pIdType = findViewById(R.id.input_reg_pasien_idtype);
        input_pIdNumb = findViewById(R.id.input_reg_pasien_idnumb);
        input_pGender = findViewById(R.id.check_gender_group);
        input_pBirthPlace = findViewById(R.id.input_reg_pasien_bplace);
        input_pBirthDate = findViewById(R.id.input_reg_pasien_bdate);

        lbl_info = findViewById(R.id.lbl_register_info);

        input_usertype.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(adapterView.getItemAtPosition(i).toString().equals("Dokter")){
                    group_dData.setVisibility(View.VISIBLE);
                    group_pData.setVisibility(View.GONE);
                } else {
                    group_pData.setVisibility(View.VISIBLE);
                    group_dData.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SetInfoText("");
                if(input_password.getText().toString().equals(input_passCheck.getText().toString())){
                    //Log.i("password",input_password.getText().toString()+", "+ input_passCheck.getText().toString());
                    OnRegister();
                } else {
                    SetInfoText("Cek kembali konfirmasi password");

                }
            }
        });
    }

    void SetInfoText(String s){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lbl_info.setText(s);
            }
        });
    }

    void OnRegister(){
        JSONObject userData = CollectData();
        JSONObject filter = new JSONObject();
        JSONObject query = new JSONObject();
        JSONObject inputData = new JSONObject();

        try {
            filter.put("userName", input_user.getText().toString());
            query.put("table", "d");
            query.put("filter", filter);

            inputData.put("table", "d");
            inputData.put("data", userData);

            if(socketConn.mSocket.connected()){
                socketConn.mSocket.emit("list", query, new Ack() {
                    @Override
                    public void call(Object... args) {
                        Log.i("type", args[args.length-1].getClass().toString());
                        if(args[args.length-1] != null){
                            if(args[args.length-1] instanceof JSONArray || args[args.length-1] instanceof ArrayList){
                                JSONArray resObj = (JSONArray) args[args.length-1];
                                if(resObj.length() > 0){
                                    SetInfoText("Silahkan pilih username lainnya");
                                } else {
                                    socketConn.mSocket.emit("input", inputData, new Ack() {
                                        @Override
                                        public void call(Object... args) {
                                            startActivity(intentLogin);
                                        }
                                    });
                                }
                            }
                        } else {
                            socketConn.mSocket.emit("input", inputData, new Ack() {
                                @Override
                                public void call(Object... args) {
                                    startActivity(intentLogin);
                                }
                            });
                        }
                    }
                });
            } else {
                SetInfoText("Tidak ada koneksi");
            }
        } catch (JSONException e){

        }
    }

    JSONObject CollectData(){
        JSONObject query = new JSONObject();

        try {
            query.put("userName", input_user.getText().toString());
            query.put("fullName", input_fullname.getText().toString());
            query.put("nip", input_nip.getText().toString());
            query.put("userType", input_usertype.getSelectedItem().toString());
            query.put("password",input_password.getText().toString());
        } catch (JSONException e){
            query = null;
        }


        return query;
    }
}
