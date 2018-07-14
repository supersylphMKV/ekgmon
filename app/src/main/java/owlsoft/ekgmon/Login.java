package owlsoft.ekgmon;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class Login extends AppCompatActivity {

    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButton = (Button) findViewById(R.id.bt_login);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(LoginCheck("a", "a")){
                    OpenMain();
                }
            }
        });
    }

    protected boolean LoginCheck(String userName, String password){
        boolean retState = true;

        return  retState;
    }

    protected void OpenMain(){
        Intent loadMain = new Intent(this, Main.class);
        startActivity(loadMain);
    }
}
