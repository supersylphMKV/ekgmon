package id.owlsoft.ekgdroid;

import android.content.Context;
import android.util.Log;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.util.jar.JarException;

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

    public void writeToFile(String data,Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("uid", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public String readFromFile(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("uid");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    //Initialize this or any other variables in probably the Application class
    public void init(Context context) {
    }

    public static boolean isLogged = false;
    public static int tab = 1;
    public static int loginMode = 0;
    public static JSONObject userData;
    public static JSONObject clientData;

    public void SetUserData(JSONObject data){
        userData = data;
        isLogged = true;
    }

    public String userGetString(String key){
        String outReturn = null;

        try{
            outReturn = userData.getString(key);
        }catch (JSONException e){
        }

        return outReturn;
    }

    public void userPut(String key, Object val){
        try{
            userData.put(key, val);
        }catch (JSONException e){
        }
    }
}
