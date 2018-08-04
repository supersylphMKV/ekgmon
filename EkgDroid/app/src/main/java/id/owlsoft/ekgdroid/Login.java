package id.owlsoft.ekgdroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.util.DiffUtil;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Ack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.Socket;

public class Login extends Activity {

    TextView btnDaftar, login_info;
    Button btnLogin;
    EditText inputUser;
    EditText inputPassword;

    String userId;

    SocketConn io = SocketConn.getInstance();
    AppState state = AppState.getInstance();

    Intent main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intentDaftar = new Intent(this, Daftar.class);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        btnDaftar = (TextView) findViewById(R.id.btn_daftar);
        btnLogin = (Button) findViewById(R.id.btn_login);
        inputUser = (EditText) findViewById(R.id.input_user);
        inputPassword = (EditText) findViewById(R.id.input_password);
        login_info = (TextView) findViewById(R.id.text_login_info);
        main = new Intent(this, Monitor.class);

        if(!state.isLogged){
            userId = state.readFromFile(this);
        } else {
            userId = "";
        }

        btnDaftar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(io.mSocket.connected()){
                    startActivity(intentDaftar);
                } else {
                    SetLoginInfo(getString(R.string.info_noConnection));
                }
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                SetLoginInfo("");
                if(io.mSocket.connected()){
                    io.Login(inputUser.getText().toString(), inputPassword.getText().toString(), new EventListener() {
                        @Override
                        public void call(Object result) {
                            if(result instanceof String){
                                if(result.equals("Not Defined")){
                                    SetLoginInfo(getString(R.string.info_dataNotFound));
                                }else if(result.equals("Wrong Password")){
                                    SetLoginInfo(getString(R.string.info_notMatch));
                                }
                            } else if(result instanceof JSONObject){
                                state.SetUserData((JSONObject)result);
                                state.writeToFile(state.userGetString("id"),getBaseContext());
                                startActivity(main);
                            }

                        }
                    });
                } else {
                    SetLoginInfo(getString(R.string.info_noConnection));
                }

            }
        });

        if(!userId.equals("")){
            if(io.mSocket.connected()){
                io.Login(userId, new EventListener() {
                    @Override
                    public void call(Object result) {
                        if(result instanceof String){
                            if(result.equals("Not Defined")){
                                SetLoginInfo(getString(R.string.info_dataNotFound));
                            }else if(result.equals("Wrong Password")){
                                SetLoginInfo(getString(R.string.info_notMatch));
                            }
                        } else if(result instanceof JSONObject){
                            state.SetUserData((JSONObject)result);
                            startActivity(main);
                        }

                    }
                });
            }
        }
    }

    void SetLoginInfo(String s){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                login_info.setText(s);
            }
        });
    }
    
}
