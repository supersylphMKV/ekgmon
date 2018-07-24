package id.owlsoft.ekgdroid;

import android.content.Context;
import android.util.Log;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

public class AppState {
    private static AppState instance = null;

    //a private constructor so no instances can be made outside this class
    private AppState() {
    }

    //Everytime you need an instance, call this
    //synchronized to make the call thread-safe
    public static synchronized AppState getInstance() {
        if(instance == null){
            instance = new AppState();
        }
        return instance;
    }

    //Initialize this or any other variables in probably the Application class
    public void init(Context context) {
    }

    public static boolean isLogged = false;
    public static int tab = 1;
}