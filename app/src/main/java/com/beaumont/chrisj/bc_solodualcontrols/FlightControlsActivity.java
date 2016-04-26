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
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FlightControlsActivity extends AppCompatActivity {

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

    private void initChatService(){
        mChatService = new BluetoothChatService(getApplicationContext(), new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case Constants.MESSAGE_STATE_CHANGE:
                        switch (msg.arg1) {
                            case Constants.STATE_CONNECTED:
                                RelativeLayout frame_connect = (RelativeLayout)findViewById(R.id.frame_connect);
                                TableLayout frame_controls = (TableLayout)findViewById(R.id.frame_controls);

                                frame_connect.setVisibility(RelativeLayout.GONE);
                                frame_controls.setVisibility(TableLayout.VISIBLE);
                                break;
                            case Constants.STATE_CONNECTING:
                                break;
                            case Constants.STATE_LISTEN:
                                progressBar.setVisibility(ProgressBar.INVISIBLE);
                                textWarning.setVisibility(TextView.VISIBLE);
                                break;
                            case Constants.STATE_NONE:
                                makeToast("State: None");
                                break;
                        }
                        break;
                    case Constants.MESSAGE_WRITE:
                        byte[] writeBuf = (byte[]) msg.obj;
                        // construct a string from the buffer
                        String writeMessage = new String(writeBuf);
                        makeToast(writeMessage);
                        break;
                    case Constants.MESSAGE_READ:
                        byte[] readBuf = (byte[]) msg.obj;
                        // construct a string from the valid bytes in the buffer
                        String readMessage = new String(readBuf, 0, msg.arg1);
                        makeToast(readMessage);
                        break;
                    case Constants.MESSAGE_DEVICE_NAME:
                        break;
                    case Constants.MESSAGE_TOAST:
                        break;
                }
            }
        });
    }

    public void btnConnect(View view){
        progressBar.setVisibility(ProgressBar.VISIBLE);
        textWarning.setVisibility(TextView.INVISIBLE);

        mChatService.connect(bluetoothDevice, true);
    }

    public void onBtnAction(View view){

    }

    private void makeToast(String m){
        Toast.makeText(getApplicationContext(), m, Toast.LENGTH_SHORT).show();
    }
}
