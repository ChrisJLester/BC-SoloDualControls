package com.beaumont.chrisj.bc_solodualcontrols;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FlightControlsActivity extends AppCompatActivity {

    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    public static final int MESSAGE_STATE_CHANGE = 4;
    public static final int MESSAGE_READ = 5;
    public static final int MESSAGE_WRITE = 6;
    public static final int MESSAGE_DEVICE_NAME = 7;
    public static final int MESSAGE_TOAST = 8;

    BluetoothDevice bluetoothDevice;
    BluetoothChatService mChatService;

    ProgressBar progressBar;
    TextView textWarning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_controls);

        WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(false);

        if(getIntent().getExtras() != null){
            bluetoothDevice = getIntent().getExtras().getParcelable("btdevice");
            initChatService();
        }

        progressBar = (ProgressBar)findViewById(R.id.loading_spinner);
        textWarning = (TextView)findViewById(R.id.textWarning);
    }

    public void btnConnect(View view){
        progressBar.setVisibility(ProgressBar.VISIBLE);
        textWarning.setVisibility(TextView.INVISIBLE);

        mChatService.connect(bluetoothDevice, true);
    }

    private void initChatService(){
        mChatService = new BluetoothChatService(getApplicationContext(), new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MESSAGE_STATE_CHANGE:
                        switch (msg.arg1) {
                            case BluetoothChatService.STATE_CONNECTED:
                                RelativeLayout frame_connect = (RelativeLayout)findViewById(R.id.frame_connect);
                                frame_connect.setVisibility(RelativeLayout.GONE);
                                break;
                            case BluetoothChatService.STATE_CONNECTING:
                                break;
                            case BluetoothChatService.STATE_LISTEN:
                                progressBar.setVisibility(ProgressBar.INVISIBLE);
                                textWarning.setVisibility(TextView.VISIBLE);
                                break;
                            case BluetoothChatService.STATE_NONE:
                                makeToast("State: None");
                                break;
                        }
                        break;
                    case MESSAGE_WRITE:
                        byte[] writeBuf = (byte[]) msg.obj;
                        // construct a string from the buffer
                        String writeMessage = new String(writeBuf);
                        makeToast(writeMessage);
                        break;
                    case MESSAGE_READ:
                        byte[] readBuf = (byte[]) msg.obj;
                        // construct a string from the valid bytes in the buffer
                        String readMessage = new String(readBuf, 0, msg.arg1);
                        makeToast(readMessage);
                        break;
                    case MESSAGE_DEVICE_NAME:
                        break;
                    case MESSAGE_TOAST:
                        break;
                }
            }
        });
    }

    private void makeToast(String m){
        Toast.makeText(getApplicationContext(), m, Toast.LENGTH_SHORT).show();
    }
}
