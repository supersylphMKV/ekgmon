package id.owlsoft.ekgdroid;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Space;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONObject;

import java.net.URISyntaxException;

public class Monitor extends AppCompatActivity {

    Boolean logged = false;

    Button btn_ecg, btn_medrec, btn_info, btn_exit;
    LinearLayout frame_medrec, frame_info, frame_menu, form_pData;
    ConstraintLayout frame_ecg;
    FrameLayout frame_menu_spacer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        logged = this.getIntent().getBooleanExtra("logged", false);
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

        btn_ecg = (Button) findViewById(R.id.btn_test);
        btn_medrec = (Button)findViewById(R.id.btn_db);
        btn_info = (Button)findViewById(R.id.btn_info);
        btn_exit = (Button)findViewById(R.id.btn_exit);

        frame_ecg = (ConstraintLayout)findViewById(R.id.body_frame);
        frame_menu = (LinearLayout)findViewById(R.id.menu_frame);
        frame_medrec = (LinearLayout)findViewById(R.id.db_frame);
        frame_info = (LinearLayout)findViewById(R.id.info_frame);
        form_pData = findViewById(R.id.lbl_form_data);
        frame_menu_spacer = (FrameLayout) findViewById(R.id.menu_spacer);

        if(!logged){
            CheckUser();
        } else {
            btn_ecg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    OnECG();
                }
            });
            btn_medrec.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    OnMedRec();
                }
            });
            btn_info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    OnInfo();
                }
            });
            btn_exit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finishAffinity();
                }
            });
            frame_menu_spacer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    frame_menu.setTranslationX(0);
                    frame_menu_spacer.setVisibility(View.GONE);
                    frame_menu.animate().translationX(-frame_menu.getWidth()).setDuration(300).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation, boolean isReverse) {
                            frame_menu.setVisibility(View.GONE);
                        }
                    });
                }
            });

            OnECG();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                frame_menu.setTranslationX(-frame_menu.getWidth());
                frame_menu.setVisibility(View.VISIBLE);
                frame_menu_spacer.setVisibility(View.VISIBLE);
                frame_menu.animate().translationX(0).setDuration(300);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void OnECG(){
        frame_ecg.setVisibility(View.VISIBLE);
        frame_medrec.setVisibility(View.GONE);
        frame_info.setVisibility(View.GONE);
        frame_menu.setVisibility(View.GONE);
    }

    void OnMedRec(){
        frame_ecg.setVisibility(View.GONE);
        frame_medrec.setVisibility(View.VISIBLE);
        frame_info.setVisibility(View.GONE);
        frame_menu.setVisibility(View.GONE);
    }

    void OnInfo(){
        frame_ecg.setVisibility(View.GONE);
        frame_medrec.setVisibility(View.GONE);
        frame_info.setVisibility(View.VISIBLE);
        frame_menu.setVisibility(View.GONE);
    }

    void CheckUser(){
        unVerified();
    }

    void unVerified(){
        Intent loginIntent = new Intent(this, Login.class);

        startActivity(loginIntent);
    }
}
