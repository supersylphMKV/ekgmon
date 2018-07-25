package id.owlsoft.ekgdroid;

import android.content.Context;
import android.support.v7.util.DiffUtil;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Ack;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

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
            mSocket = IO.socket("http://103.43.47.17:1116");
        } catch (URISyntaxException e) {
            Log.e("error", e.toString());
        }
    }

    public static synchronized void AddPasienData(JSONObject query, EventListener cb){

        HashMap<String, Object > filter = new HashMap<String, Object>();

        try {
            filter.put("fullName", query.getString("fullName"));
            filter.put("birthPlace", query.getString("birthPlace"));
            filter.put("birthDate", query.getString("birthDate"));
            filter.put("gender",query.getString("gender"));

            CheckData("p", filter, new EventListener() {
                @Override
                public void call(Object result) {
                    switch ((int)result){
                        case -1 :
                            cb.call("error");
                            break;
                        case 0 :
                            InputData("p", query, new EventListener() {
                                @Override
                                public void call(Object result) {
                                    cb.call(result);
                                }
                            });
                            break;
                        default:
                            cb.call("duplicate");
                            break;
                    }
                }
            });
        } catch (JSONException e){
            cb.call(e);
        }
    }

    public static synchronized void InputData(String table, JSONObject data, EventListener cb){
        JSONObject query = new JSONObject();

        try {
            query.put("table", table);
            query.put("data", data);

            mSocket.emit("input", query, new Ack() {
                @Override
                public void call(Object... args) {
                    cb.call("Success");
                }
            });

        } catch (JSONException e){
            cb.call(e);
        }

    }

    public static synchronized void CheckData(String table, HashMap<String, Object> filter, EventListener cb){
        JSONObject filterObj = new JSONObject();
        JSONObject query = new JSONObject();

        try{
            query.put("table", table);
            if(filter != null){
                for(Map.Entry<String,Object> entry : filter.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();

                    filterObj.put(key, value);
                }
                query.put("filter", filterObj);

                mSocket.emit("list", query, new Ack() {

                    @Override
                    public void call(Object... args) {
                        if(args[args.length-1] != null){
                            if(args[args.length-1] instanceof JSONArray || args[args.length-1] instanceof ArrayList){
                                JSONArray resObj = (JSONArray) args[args.length-1];
                                if(resObj.length() > 0){
                                    cb.call(1);
                                } else {
                                    cb.call(0);
                                }
                            }
                        } else {
                            cb.call(0);
                        }
                    }
                });
            } else {
                cb.call(-1);
            }
        } catch (JSONException e){
            cb.call(-1);
        }
    }

    public static synchronized void GetList(String table, HashMap<String, Object> filter, EventListener cb){
        JSONObject filterObj = new JSONObject();
        JSONObject query = new JSONObject();

        try {
            query.put("table", table);
            if(filter != null) {
                for (Map.Entry<String, Object> entry : filter.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();

                    filterObj.put(key, value);
                }
                query.put("filter", filterObj);
            }

            mSocket.emit("list", query, new Ack() {
                @Override
                public void call(Object... args) {
                    if(args[args.length-1] != null){
                        JSONArray resObj = (JSONArray) args[args.length-1];
                        cb.call(resObj);
                    } else {
                        cb.call(args[0]);
                    }
                }
            });
        } catch (JSONException e){
            cb.call(e);
        }
    }

}
