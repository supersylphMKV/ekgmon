package id.owlsoft.ekgdroid;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static id.owlsoft.ekgdroid.AppState.testResult;

public class PopResult extends Activity {

    Button btnSave, btnClose;
    TextView pr_res,pr_range,pr_diag,qt_res,qt_range,qt_diag,rr_res,rr_range,rr_diag,qrs_res,qrs_range,qrs_diag;

    AppState state = AppState.getInstance();
    SocketConn socket = SocketConn.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popresult_window);

        pr_diag = findViewById(R.id.result_pr_diag);
        pr_range = findViewById(R.id.result_pr_range);
        pr_res = findViewById(R.id.result_pr);
        qt_diag = findViewById(R.id.result_qt_diag);
        qt_range = findViewById(R.id.result_qt_range);
        qt_res = findViewById(R.id.result_qt);
        rr_diag = findViewById(R.id.result_rr_diag);
        rr_range = findViewById(R.id.result_rr_range);
        rr_res = findViewById(R.id.result_rr);
        qrs_diag = findViewById(R.id.result_qrs_diag);
        qrs_range = findViewById(R.id.result_qrs_range);
        qrs_res = findViewById(R.id.result_qrs);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width * .7), ViewGroup.LayoutParams.WRAP_CONTENT);

        pr_res.setText(String.valueOf(testResult.prRate));
        pr_range.setText(testResult.prMin + " ms - " + testResult.prMax + " ms");
        if(testResult.prRate > 120 && testResult.prRate < 200){
            pr_diag.setText("Normal");
        } else {
            pr_diag.setText("Konsultasikan dengan Dokter");
        }

        qt_res.setText(String.valueOf(testResult.qtRate));
        qt_range.setText(testResult.qtMin + " ms - " + testResult.qtMax + " ms");
        if( testResult.qtRate < 440){
            qt_diag.setText("Normal");
        } else {
            qt_diag.setText("Konsultasikan dengan Dokter");
        }

        qrs_res.setText(String.valueOf(testResult.qrsRate));
        qrs_range.setText(testResult.qrsMin + " ms - " + testResult.qrsMax + " ms");
        if( testResult.qrsRate > 80 && testResult.qrsRate < 100){
            qrs_diag.setText("Normal");
        } else {
            qrs_diag.setText("Konsultasikan dengan Dokter");
        }

        rr_res.setText(String.valueOf(testResult.beatRate));
        rr_range.setText(testResult.rrMin + " ms - " + testResult.rrMax + " ms");
        if(testResult.beatRate > 60 && testResult.beatRate < 100){
            rr_diag.setText("Normal");
        } else {
            rr_diag.setText("Konsultasikan dengan Dokter");
        }

        btnClose = findViewById(R.id.btn_close);
        btnSave = findViewById(R.id.btn_save);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONArray testRes = new JSONArray();
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
                            finish();
                            Log.d("save", "Success");
                        }
                    });
                } catch (JSONException e){

                }
            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        if(state.isReplay){
            btnSave.setVisibility(View.GONE);
        } else {
            btnSave.setVisibility(View.VISIBLE);
        }
    }
}
