package owlsoft.ekgmon;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;

public class Main extends AppCompatActivity implements Runnable {

    Button burstBtn;
    FrameLayout fl;
    GraphDrawer graph;
    Vector2 dimension;

    private UsbManager usbManager;
    private UsbDevice deviceFound;
    private UsbDeviceConnection usbDeviceConnection;
    private UsbInterface usbInterfaceFound = null;
    private UsbEndpoint endpointOut = null;
    private UsbEndpoint endpointIn = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fl = (FrameLayout) findViewById(R.id.ekg_frame);
        burstBtn = (Button) findViewById(R.id.bt_dummyData);
        graph = new GraphDrawer(this);
        graph.setId((R.id.bt_dummyData));
        fl.addView(graph);
        ViewTreeObserver vto = fl.getViewTreeObserver();

        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    fl.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    fl.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                int width  = fl.getMeasuredWidth();
                int height = fl.getMeasuredHeight();
                dimension = new Vector2(width,height);
                graph.SetRectDimension(width,height);
                graph.SetCenterGraph(width - (width/5),height/2);
                graph.StartDraw();
            }
        });

        burstBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float[] datas = new float[ (new Random().nextInt(10)) + 3 ];
                int minVal = Math.round( dimension._y/4);
                int maxVal = Math.round( dimension._y/2);
                for(int f = 0; f < datas.length; f++){
                    datas[f] = (new Random().nextInt( maxVal))- minVal;
                }
                graph.InputData(datas);
            }
        });

    }

    protected void ReadData(){

    }

    @Override
    public void onResume() {
        super.onResume();

        Intent intent = getIntent();
        String action = intent.getAction();

        UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
            setDevice(device);
        } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
            if (deviceFound != null && deviceFound.equals(device)) {
                setDevice(null);
            }
        }
    }

    private void setDevice(UsbDevice device) {
        usbInterfaceFound = null;
        endpointOut = null;
        endpointIn = null;

        for (int i = 0; i < device.getInterfaceCount(); i++) {
            UsbInterface usbif = device.getInterface(i);

            UsbEndpoint tOut = null;
            UsbEndpoint tIn = null;

            int tEndpointCnt = usbif.getEndpointCount();
            if (tEndpointCnt >= 2) {
                for (int j = 0; j < tEndpointCnt; j++) {
                    if (usbif.getEndpoint(j).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                        if (usbif.getEndpoint(j).getDirection() == UsbConstants.USB_DIR_OUT) {
                            tOut = usbif.getEndpoint(j);
                        } else if (usbif.getEndpoint(j).getDirection() == UsbConstants.USB_DIR_IN) {
                            tIn = usbif.getEndpoint(j);
                        }
                    }
                }

                if (tOut != null && tIn != null) {
                    usbInterfaceFound = usbif;
                    endpointOut = tOut;
                    endpointIn = tIn;
                }
            }

        }

        if (usbInterfaceFound == null) {
            return;
        }

        deviceFound = device;

        if (device != null) {
            UsbDeviceConnection connection =
                    usbManager.openDevice(device);
            if (connection != null && connection.claimInterface(usbInterfaceFound, true)) {
                usbDeviceConnection = connection;
                Thread thread = new Thread(this);
                thread.start();
                byte[] bytes = new byte[endpointIn.getMaxPacketSize()];
                int result = connection.bulkTransfer(endpointIn, bytes, bytes.length, 1000);

            } else {
                usbDeviceConnection = null;
            }
        }
    }

    private void sendCommand(int control) {
        synchronized (this) {

            if (usbDeviceConnection != null) {
                byte[] message = new byte[1];
                message[0] = (byte)control;
                usbDeviceConnection.bulkTransfer(endpointOut,
                        message, message.length, 0);
            }
        }
    }

    @Override
    public void run() {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        UsbRequest request = new UsbRequest();
        request.initialize(usbDeviceConnection, endpointIn);
        while (true) {
            request.queue(buffer, 1);
            if (usbDeviceConnection.requestWait() == request) {
                byte rxCmd = buffer.get(0);
                if(rxCmd!=0){
                    //bar.setProgress((int)rxCmd);
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            } else {
                break;
            }
        }

    }
}