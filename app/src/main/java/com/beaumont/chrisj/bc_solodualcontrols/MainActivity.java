package com.beaumont.chrisj.bc_solodualcontrols;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mDevice;
    BluetoothChatService mChatService;
    private final static int REQUEST_ENABLE_BT = 1;

    boolean[] deviceA = {false, false, false, false};

    int dragged_lbl;
    String last_msg = "";
    Boolean frame_flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            AlertDialog.Builder prerequisites = new AlertDialog.Builder(this);
            prerequisites.setMessage("This device does not support Bluetooth, sorry!");
            prerequisites.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent i = new Intent();
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    i.putExtra("Exit me", true);
                    startActivity(i);
                    finish();
                }
            });
            prerequisites.show();
        } else {
            showBT_devices();
        }

        if(getIntent().getBooleanExtra("Exit me", false)){
            finish();
            return;
        }

        enable_DD();

        reset();
    }

    //Setup
    //==============================================================================================
    private void showBT_devices(){
        final ArrayAdapter mArrayAdapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1);
        final Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mArrayAdapter.add(device.getName());
            }
        }

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.popup_btdevices, null);
        alertDialog.setView(convertView);
        alertDialog.setTitle("Paired devices");
        alertDialog.setNeutralButton("Refresh", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showBT_devices();
            }
        });

        if (!mBluetoothAdapter.isEnabled()) {
            alertDialog.setNegativeButton("Enable Bluetooth", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    showBT_devices();
                }
            });
        }

        final AlertDialog alert = alertDialog.create();
        alert.show();

        ListView lv = (ListView) convertView.findViewById(R.id.list_bt_devices);
        lv.setAdapter(mArrayAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (BluetoothDevice device : pairedDevices) {
                    if (device.getName().equals(mArrayAdapter.getItem(position).toString())) {
                        mDevice = device;
                        ((TextView) findViewById(R.id.lblOtherDevice)).setText("Other device: " + mDevice.getName());
                        initChatService();
                        break;
                    }
                }
                alert.cancel();
            }
        });
    }

    private void initChatService(){
        mChatService = new BluetoothChatService(getApplicationContext(), new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case Constants.MESSAGE_STATE_CHANGE:
                        break;
                    case Constants.MESSAGE_WRITE:
                        break;
                    case Constants.MESSAGE_READ:
                        // construct a string from the valid bytes in the buffer
                        parseMsg(new String((byte[]) msg.obj, 0, msg.arg1));
                        break;
                    case Constants.MESSAGE_DEVICE_NAME:
                        break;
                    case Constants.MESSAGE_TOAST:
                        break;
                }
            }
        });
        mChatService.start();
        mChatService.connect(mDevice, true);
    }

    private void enable_DD(){
        findViewById(R.id.lblLaunchCons).setOnDragListener(new mOnDragListener());
        findViewById(R.id.lblLaunchCons).setOnTouchListener(new mOnTouchListener());

        findViewById(R.id.lblStreamCons).setOnDragListener(new mOnDragListener());
        findViewById(R.id.lblStreamCons).setOnTouchListener(new mOnTouchListener());

        findViewById(R.id.lblFlightCons).setOnDragListener(new mOnDragListener());
        findViewById(R.id.lblFlightCons).setOnTouchListener(new mOnTouchListener());

        findViewById(R.id.lblLandCons).setOnDragListener(new mOnDragListener());
        findViewById(R.id.lblLandCons).setOnTouchListener(new mOnTouchListener());

        findViewById(R.id.frame_stream).setOnDragListener(new mOnDragListener());
        findViewById(R.id.frame_flight).setOnDragListener(new mOnDragListener());
    }


    //Bluetooth stuff
    //==============================================================================================
    private void sendBT(String s){
        //Since multiple onDragListeners are running, got to make sure you don't send the same message multiple times
        if(!s.equals(last_msg)){
            last_msg = s;
            mChatService.write(s.getBytes());
            makeToast(s);
        }
    }

    private void parseMsg(String s){
        int code = Integer.parseInt(s);

        switch (code) {
            case (101):
                reset();
                break;
            case (102):
                findViewById(R.id.btnStart).setVisibility(Button.VISIBLE);
                break;
            case (103):
                start();
                break;
            case (104):
                findViewById(R.id.lblLaunchCons).setVisibility(TextView.INVISIBLE);
                findViewById(R.id.lc_2).setVisibility(TextView.VISIBLE);
                break;
            case (105):
                findViewById(R.id.lblStreamCons).setVisibility(TextView.INVISIBLE);
                findViewById(R.id.lblFlightCons).setVisibility(TextView.INVISIBLE);
                findViewById(R.id.sc_2).setVisibility(TextView.VISIBLE);
                findViewById(R.id.fc_1).setVisibility(TextView.VISIBLE);
                deviceA[2] = true;
                break;
            case (106):
                findViewById(R.id.lblFlightCons).setVisibility(TextView.INVISIBLE);
                findViewById(R.id.lblStreamCons).setVisibility(TextView.INVISIBLE);
                findViewById(R.id.fc_2).setVisibility(TextView.VISIBLE);
                findViewById(R.id.sc_1).setVisibility(TextView.VISIBLE);
                deviceA[1] = true;
                break;
            case (107):
                findViewById(R.id.lblLandCons).setVisibility(TextView.INVISIBLE);
                findViewById(R.id.lsc_2).setVisibility(TextView.VISIBLE);
                break;

            case (108):
                findViewById(R.id.lblLaunchCons).setVisibility(TextView.INVISIBLE);
                findViewById(R.id.lc_1).setVisibility(TextView.VISIBLE);
                deviceA[0] = true;
                break;
            case (109):
                findViewById(R.id.lblStreamCons).setVisibility(TextView.INVISIBLE);
                findViewById(R.id.lblFlightCons).setVisibility(TextView.INVISIBLE);
                findViewById(R.id.sc_1).setVisibility(TextView.VISIBLE);
                findViewById(R.id.fc_2).setVisibility(TextView.VISIBLE);
                deviceA[1] = true;
                break;
            case (110):
                findViewById(R.id.lblFlightCons).setVisibility(TextView.INVISIBLE);
                findViewById(R.id.lblStreamCons).setVisibility(TextView.INVISIBLE);
                findViewById(R.id.fc_1).setVisibility(TextView.VISIBLE);
                findViewById(R.id.sc_2).setVisibility(TextView.VISIBLE);
                deviceA[2] = true;
                break;
            case (111):
                findViewById(R.id.lblLandCons).setVisibility(TextView.INVISIBLE);
                findViewById(R.id.lsc_1).setVisibility(TextView.VISIBLE);
                deviceA[3] = true;
                break;
            default:
                break;
        }

        Button btnReset = (Button) findViewById(R.id.btnRestart);
        if((code > 103) && (btnReset.getVisibility() == Button.INVISIBLE))
            btnReset.setVisibility(Button.VISIBLE);
    }


    //Button actions
    //==============================================================================================
    public void onBtnReset(View v){
        sendBT("101");
        reset();
    }

    private void reset(){
        findViewById(R.id.btnRestart).setVisibility(Button.INVISIBLE);
        findViewById(R.id.btnStart).setVisibility(Button.INVISIBLE);

        findViewById(R.id.lblLaunchCons).setVisibility(TextView.VISIBLE);
        findViewById(R.id.lblFlightCons).setVisibility(TextView.VISIBLE);
        findViewById(R.id.lblStreamCons).setVisibility(TextView.VISIBLE);
        findViewById(R.id.lblLandCons).setVisibility(TextView.VISIBLE);

        findViewById(R.id.lc_1).setVisibility(TextView.GONE);
        findViewById(R.id.fc_1).setVisibility(TextView.GONE);
        findViewById(R.id.sc_1).setVisibility(TextView.GONE);
        findViewById(R.id.lsc_1).setVisibility(TextView.GONE);

        findViewById(R.id.lc_2).setVisibility(TextView.GONE);
        findViewById(R.id.fc_2).setVisibility(TextView.GONE);
        findViewById(R.id.sc_2).setVisibility(TextView.GONE);
        findViewById(R.id.lsc_2).setVisibility(TextView.GONE);

        for(int i = 0; i < 4; i++)
            deviceA[i] = false;
    }

    public void onBtnStart(View v){
        sendBT("103");
        start();
    }

    private void start(){
        Intent intent;

        if(deviceA[1])
            intent = new Intent (this, StreamControlsActivity.class);
        else
            intent = new Intent (this, FlightControlsActivity.class);

        intent.putExtra("btdevice", mDevice);
        intent.putExtra("controls_lst", deviceA);
        startActivity(intent);
    }


    //Classes for enabling drag and drop

    //==============================================================================================
    public class mOnDragListener implements View.OnDragListener{

        @Override
        public boolean onDrag(View v, DragEvent event) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_ENTERED:
                    if (v.getId() == R.id.frame_flight) {
                        v.setBackgroundColor(Color.BLUE);
                        frame_flag = true;
                    } else if (v.getId() == R.id.frame_stream) {
                        v.setBackgroundColor(Color.BLUE);
                        frame_flag = false;
                    }
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    if (v.getId() == R.id.frame_flight || v.getId() == R.id.frame_stream)
                        v.setBackgroundColor(Color.TRANSPARENT);
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    findViewById(R.id.frame_flight).setBackgroundColor(Color.TRANSPARENT);
                    findViewById(R.id.frame_stream).setBackgroundColor(Color.TRANSPARENT);
                    moveToList();
                    break;
                default:
                    break;
            }
            return true;
        }
    }

    public class mOnTouchListener implements View.OnTouchListener{
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);

                v.startDrag(data, shadowBuilder, v, 0);
                v.setVisibility(View.INVISIBLE);
                dragged_lbl = v.getId();
                return true;
            }
            else
            {
                return false;
            }
        }
    }


    //Other stuff
    //==============================================================================================
    private void moveToList(){
        Button btnRestart = (Button)findViewById(R.id.btnRestart);
        if(btnRestart.getVisibility() == Button.INVISIBLE)
            btnRestart.setVisibility(Button.VISIBLE);

        if(!frame_flag){
            switch (dragged_lbl){
                case R.id.lblLaunchCons:
                    findViewById(R.id.lc_1).setVisibility(TextView.VISIBLE);
                    deviceA[0] = true;
                    sendBT("104");
                    break;
                case R.id.lblStreamCons:
                    findViewById(R.id.sc_1).setVisibility(TextView.VISIBLE);
                    findViewById(R.id.fc_2).setVisibility(TextView.VISIBLE);
                    findViewById(R.id.lblFlightCons).setVisibility(TextView.INVISIBLE);
                    deviceA[1] = true;
                    sendBT("105");
                    break;
                case R.id.lblFlightCons:
                    findViewById(R.id.fc_1).setVisibility(TextView.VISIBLE);
                    findViewById(R.id.sc_2).setVisibility(TextView.VISIBLE);
                    findViewById(R.id.lblStreamCons).setVisibility(TextView.INVISIBLE);
                    deviceA[2] = true;
                    sendBT("106");
                    break;
                case R.id.lblLandCons:
                    findViewById(R.id.lsc_1).setVisibility(TextView.VISIBLE);
                    deviceA[3] = true;
                    sendBT("107");
                    break;
                default:
                    break;
            }
        } else {
            switch (dragged_lbl){
                case R.id.lblLaunchCons:
                    findViewById(R.id.lc_2).setVisibility(TextView.VISIBLE);
                    sendBT("108");
                    break;
                case R.id.lblStreamCons:
                    findViewById(R.id.sc_2).setVisibility(TextView.VISIBLE);
                    findViewById(R.id.fc_1).setVisibility(TextView.VISIBLE);
                    findViewById(R.id.lblFlightCons).setVisibility(TextView.INVISIBLE);
                    deviceA[2] = true;
                    sendBT("109");
                    break;
                case R.id.lblFlightCons:
                    findViewById(R.id.fc_2).setVisibility(TextView.VISIBLE);
                    findViewById(R.id.sc_1).setVisibility(TextView.VISIBLE);
                    findViewById(R.id.lblStreamCons).setVisibility(TextView.INVISIBLE);
                    deviceA[1] = true;
                    sendBT("110");
                    break;
                case R.id.lblLandCons:
                    findViewById(R.id.lsc_2).setVisibility(TextView.VISIBLE);
                    sendBT("111");
                    break;
                default:
                    break;
            }
        }

        if(findViewById(R.id.lblLaunchCons).getVisibility() == TextView.INVISIBLE &&
                findViewById(R.id.lblStreamCons).getVisibility() == TextView.INVISIBLE &&
                findViewById(R.id.lblFlightCons).getVisibility() == TextView.INVISIBLE &&
                findViewById(R.id.lblLandCons).getVisibility() == TextView.INVISIBLE){

            findViewById(R.id.btnStart).setVisibility(Button.VISIBLE);
            sendBT("102");
        }

    }

    private void makeToast(String m){
        Toast.makeText(getApplicationContext(), m, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume(){
        reset();
        super.onResume();
    }
}
