package com.beaumont.chrisj.bc_solodualcontrols;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FlightControlsActivity extends AppCompatActivity {

    BluetoothDevice bluetoothDevice;
    BluetoothChatService mChatService;

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

        initVars();
    }

    private void initChatService(){
        mChatService = new BluetoothChatService(getApplicationContext(), new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case Constants.MESSAGE_STATE_CHANGE:
                        switch (msg.arg1) {
                            case Constants.STATE_CONNECTED:
                                frame_connect.setVisibility(RelativeLayout.GONE);
                                frame_controls.setVisibility(TableLayout.VISIBLE);

                                displayOptions();

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

    private void displayOptions(){
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(FlightControlsActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.popup_flight_options, null);
        alertDialog.setView(convertView);
        alertDialog.setTitle("Options:");
        alertDialog.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        final AlertDialog alert = alertDialog.create();
        alert.show();
    }

    @Override
    public void onBackPressed() {
        if(frame_controls.getVisibility() == RelativeLayout.VISIBLE)
            displayOptions();
        else
            super.onBackPressed();
    }

    public void btnButtonPress(View view){
        int btnID = view.getId();

        switch (btnID){
            case(R.id.panel_up):
                sendBT("Up");
                break;
            case(R.id.panel_rot_right):
                sendBT("Rot Right");
                break;
            case(R.id.panel_right):
                sendBT("Right");
                break;
            case(R.id.panel_alt_inc):
                sendBT("Alt Inc");
                break;
            case(R.id.panel_down):
                sendBT("Down");
                break;
            case(R.id.panel_alt_dec):
                sendBT("Alt Dec");
                break;
            case(R.id.panel_left):
                sendBT("Left");
                break;
            case(R.id.panel_rot_left):
                sendBT("Rot Left");
                break;
        }
    }

    private void sendBT(String msg){
        mChatService.write(msg.getBytes());
    }

    private void makeToast(String m){
        Toast.makeText(getApplicationContext(), m, Toast.LENGTH_SHORT).show();
    }

    private void initVars(){
        frame_connect = (RelativeLayout)findViewById(R.id.frame_connect);
        frame_controls = (TableLayout)findViewById(R.id.frame_controls);

        progressBar = (ProgressBar)findViewById(R.id.loading_spinner);
        textWarning = (TextView)findViewById(R.id.textWarning);

        btnUp = (ImageView)findViewById(R.id.btnUp);
        btnRotRight = (ImageView)findViewById(R.id.btnRotRight);
        btnRight = (ImageView)findViewById(R.id.btnRight);
        btnAltInc = (ImageView)findViewById(R.id.btnAltInc);
        btnDown = (ImageView)findViewById(R.id.btnDown);
        btnAltDec = (ImageView)findViewById(R.id.btnAltDec);
        btnLeft = (ImageView)findViewById(R.id.btnLeft);
        btnRotLeft = (ImageView)findViewById(R.id.btnRotLeft);

        lblUp = (TextView)findViewById(R.id.lblUp);
        lblRotRight = (TextView)findViewById(R.id.lblRotRight);
        lblRight = (TextView)findViewById(R.id.lblRight);
        lblAltInc = (TextView)findViewById(R.id.lblAltInc);
        lblDown = (TextView)findViewById(R.id.lblDown);
        lblAltDec = (TextView)findViewById(R.id.lblAltDec);
        lblLeft = (TextView)findViewById(R.id.lblLeft);
        lblRotLeft = (TextView)findViewById(R.id.lblRotLeft);
    }




    RelativeLayout frame_connect;
    TableLayout frame_controls;

    ProgressBar progressBar;
    TextView textWarning;

    ImageView btnUp;
    ImageView btnRotRight;
    ImageView btnRight;
    ImageView btnAltInc;
    ImageView btnDown;
    ImageView btnAltDec;
    ImageView btnLeft;
    ImageView btnRotLeft;

    TextView lblUp;
    TextView lblRotRight;
    TextView lblRight;
    TextView lblAltInc;
    TextView lblDown;
    TextView lblAltDec;
    TextView lblLeft;
    TextView lblRotLeft;
}
