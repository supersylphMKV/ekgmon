package id.owlsoft.ekgdroid;

import android.content.Context;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

class SocketConn {

    private static SocketConn instance = null;

    //a private constructor so no instances can be made outside this class
    private SocketConn() {
    }

    //Everytime you need an instance, call this
    //synchronized to make the call thread-safe
    public static synchronized SocketConn getInstance() {
        if(instance == null){
            instance = new SocketConn();
            instance.mSocket.connect();
        }
        return instance;
    }

    //Initialize this or any other variables in probably the Application class
    public void init(Context context) {
    }

    public static Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://192.168.57.1:1116");
        } catch (URISyntaxException e) {
            Log.e("error", e.toString());
        }
    }


}
