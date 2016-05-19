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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FlightControlsActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener{

    BluetoothDevice bluetoothDevice;
    BluetoothChatService mChatService;

    boolean controls_directional;
    boolean controls_rotation;
    boolean controls_altitude;

    boolean display_arrows;
    boolean display_text;

    boolean[] controls_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_controls);

        WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(false);

        if(getIntent().getExtras() != null){
            bluetoothDevice = getIntent().getExtras().getParcelable("btdevice");
            controls_list = getIntent().getExtras().getBooleanArray("controls_lst");
            initChatService();
        }

        if(controls_list != null){
            if(!controls_list[0]) {
                findViewById(R.id.frame_flight_takeoff).setVisibility(RelativeLayout.GONE);
                findViewById(R.id.frame_controls).setVisibility(TableLayout.VISIBLE);
            }
        }

        initVars();
    }


    //Launch Controls
    //==============================================================================================
    public void onBtnLaunchAction(View v){
        makeToast("Flight Controls");
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
                            case Constants.STATE_NONE:
                                makeToast("State: None");
                                break;
                            default:
                                break;
                        }
                        break;
                    case Constants.MESSAGE_WRITE:
                        break;
                    case Constants.MESSAGE_READ:
                        byte[] readBuf = (byte[]) msg.obj;
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

        final CheckBox chkDirectional = (CheckBox) alert.findViewById(R.id.chkDirectional);
        final CheckBox chkRotation = (CheckBox) alert.findViewById(R.id.chkRotation);
        final CheckBox chkAltitude = (CheckBox) alert.findViewById(R.id.chkAltitude);

        chkDirectional.setOnCheckedChangeListener(this);
        chkRotation.setOnCheckedChangeListener(this);
        chkAltitude.setOnCheckedChangeListener(this);

        chkDirectional.setChecked(controls_directional);
        chkRotation.setChecked(controls_rotation);
        chkAltitude.setChecked(controls_altitude);
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
                sendBT("101");
                break;
            case(R.id.panel_rot_right):
                sendBT("201");
                break;
            case(R.id.panel_right):
                sendBT("102");
                break;
            case(R.id.panel_alt_inc):
                sendBT("301");
                break;
            case(R.id.panel_down):
                sendBT("103");
                break;
            case(R.id.panel_alt_dec):
                sendBT("302");
                break;
            case(R.id.panel_left):
                sendBT("104");
                break;
            case(R.id.panel_rot_left):
                sendBT("202");
                break;
        }
    }

    private void sendBT(String msg){
        mChatService.write(msg.getBytes());
    }

    @Override
    public void onCheckedChanged(CompoundButton chkView, boolean isChecked) {
        switch(chkView.getId()){
            case R.id.chkDirectional:
                controls_directional = isChecked;
                break;
            case R.id.chkRotation:
                controls_rotation = isChecked;
                break;
            case R.id.chkAltitude:
                controls_altitude = isChecked;
                break;
            case R.id.chkArrows:
                display_arrows = isChecked;
                break;
            case R.id.chkText:
                display_text = isChecked;
                break;

        }

        updateControls();

    }

    private void updateControls(){
        int directional_visible, rotation_visible, altitude_visible;

        if(controls_directional)
            directional_visible = ImageView.VISIBLE;
        else
            directional_visible = ImageView.GONE;

        if(controls_rotation)
            rotation_visible = ImageView.VISIBLE;
        else
            rotation_visible = ImageView.GONE;

        if(controls_altitude)
            altitude_visible = ImageView.VISIBLE;
        else
            altitude_visible = ImageView.GONE;


        btnUp.setVisibility(directional_visible);
        btnRight.setVisibility(directional_visible);
        btnDown.setVisibility(directional_visible);
        btnLeft.setVisibility(directional_visible);

        btnRotRight.setVisibility(rotation_visible);
        btnRotLeft.setVisibility(rotation_visible);

        btnAltInc.setVisibility(altitude_visible);
        btnAltDec.setVisibility(altitude_visible);
    }

    private void makeToast(String m){
        Toast.makeText(getApplicationContext(), m, Toast.LENGTH_SHORT).show();
    }

    private void initVars(){

        controls_directional = true;
        controls_rotation = true;
        controls_altitude = true;

        display_arrows = true;
        display_text = false;

        frame_controls = (TableLayout)findViewById(R.id.frame_controls);

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
