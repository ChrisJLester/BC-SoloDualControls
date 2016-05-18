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
    boolean[] deviceB = {false, false, false, false};

    String msg;
    int dragged_lbl;

    Boolean flag;

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
    }

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
        mChatService.connect(mDevice, true);
    }

    private void parseMsg(String s){
        int code = Integer.parseInt(s);

        switch (code) {
            case (999):
                reset();
                break;
            case (101):
                findViewById(R.id.lblLaunchCons).setVisibility(TextView.INVISIBLE);
                findViewById(R.id.lc_2).setVisibility(TextView.VISIBLE);
                break;
            case (102):
                findViewById(R.id.lblStreamCons).setVisibility(TextView.INVISIBLE);
                findViewById(R.id.lblFlightCons).setVisibility(TextView.INVISIBLE);
                findViewById(R.id.sc_2).setVisibility(TextView.VISIBLE);
                findViewById(R.id.fc_1).setVisibility(TextView.VISIBLE);
                break;
            case (103):
                findViewById(R.id.lblFlightCons).setVisibility(TextView.INVISIBLE);
                findViewById(R.id.lblStreamCons).setVisibility(TextView.INVISIBLE);
                findViewById(R.id.fc_2).setVisibility(TextView.VISIBLE);
                findViewById(R.id.sc_1).setVisibility(TextView.VISIBLE);
                break;
            case (104):
                findViewById(R.id.lblLandCons).setVisibility(TextView.INVISIBLE);
                findViewById(R.id.lsc_2).setVisibility(TextView.VISIBLE);
                break;

            case (201):
                findViewById(R.id.lblLaunchCons).setVisibility(TextView.INVISIBLE);
                findViewById(R.id.lc_1).setVisibility(TextView.VISIBLE);
                break;
            case (202):
                findViewById(R.id.lblStreamCons).setVisibility(TextView.INVISIBLE);
                findViewById(R.id.lblFlightCons).setVisibility(TextView.INVISIBLE);
                findViewById(R.id.sc_1).setVisibility(TextView.VISIBLE);
                findViewById(R.id.fc_2).setVisibility(TextView.VISIBLE);
                break;
            case (203):
                findViewById(R.id.lblFlightCons).setVisibility(TextView.INVISIBLE);
                findViewById(R.id.lblStreamCons).setVisibility(TextView.INVISIBLE);
                findViewById(R.id.fc_1).setVisibility(TextView.VISIBLE);
                findViewById(R.id.sc_2).setVisibility(TextView.VISIBLE);
                break;
            case (204):
                findViewById(R.id.lblLandCons).setVisibility(TextView.INVISIBLE);
                findViewById(R.id.lsc_1).setVisibility(TextView.VISIBLE);
                break;
            default:
                break;
        }

        Button btnReset = (Button) findViewById(R.id.btnRestart);
        if((code < 999) && (btnReset.getVisibility() == Button.INVISIBLE))
            btnReset.setVisibility(Button.VISIBLE);
    }

    public void flight_controls(View view){
        Intent intent = new Intent (this, FlightControlsActivity.class);
        intent.putExtra("btdevice", mDevice);
        startActivity(intent);
    }

    public void stream_controls(View view){
        Intent intent = new Intent (this, StreamControlsActivity.class);
        intent.putExtra("btdevice", mDevice);
        startActivity(intent);
    }

    private void enable_DD(){
        TextView lblLaunchCons = (TextView) findViewById(R.id.lblLaunchCons);
        TextView lblStreamCons = (TextView) findViewById(R.id.lblStreamCons);
        TextView lblFlightCons = (TextView) findViewById(R.id.lblFlightCons);
        TextView lblLandCons = (TextView) findViewById(R.id.lblLandCons);

        lblLaunchCons.setOnDragListener(new mOnDragListener());
        lblLaunchCons.setOnTouchListener(new mOnTouchListener());

        lblStreamCons.setOnDragListener(new mOnDragListener());
        lblStreamCons.setOnTouchListener(new mOnTouchListener());

        lblFlightCons.setOnDragListener(new mOnDragListener());
        lblFlightCons.setOnTouchListener(new mOnTouchListener());

        lblLandCons.setOnDragListener(new mOnDragListener());
        lblLandCons.setOnTouchListener(new mOnTouchListener());

        RelativeLayout frame_stream = (RelativeLayout) findViewById(R.id.frame_stream);
        RelativeLayout frame_flight = (RelativeLayout) findViewById(R.id.frame_flight);
        frame_stream.setOnDragListener(new mOnDragListener());
        frame_flight.setOnDragListener(new mOnDragListener());
    }

    public class mOnDragListener implements View.OnDragListener{

        @Override
        public boolean onDrag(View v, DragEvent event) {

            boolean result = false;

            switch (dragged_lbl){
                case R.id.lblLaunchCons:
                    result = parseDragEvent(v, event);
                    break;
                case R.id.lblStreamCons:
                    result = parseDragEvent(v, event);
                    break;
                case R.id.lblFlightCons:
                    result = parseDragEvent(v, event);
                    break;
                case R.id.lblLandCons:
                    result = parseDragEvent(v, event);
            }

            return result;
        }
    }

    private boolean parseDragEvent(View v, DragEvent event){
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                Log.d(msg, "Action is DragEvent.ACTION_DRAG_STARTED");
                break;
            case DragEvent.ACTION_DRAG_ENTERED:
                Log.d(msg, "Action is DragEvent.ACTION_DRAG_ENTERED");

                if(v.getId() == R.id.frame_flight){
                    v.setBackgroundColor(Color.BLUE);
                    flag = true;
                } else if (v.getId() == R.id.frame_stream){
                    v.setBackgroundColor(Color.BLUE);
                    flag = false;
                }
                break;
            case DragEvent.ACTION_DRAG_EXITED:
                Log.d(msg, "Action is DragEvent.ACTION_DRAG_EXITED");
                if(v.getId() == R.id.frame_flight || v.getId() == R.id.frame_stream)
                    v.setBackgroundColor(Color.TRANSPARENT);
                break;
            case DragEvent.ACTION_DRAG_LOCATION:
                Log.d(msg, "Action is DragEvent.ACTION_DRAG_LOCATION");
                break;
            case DragEvent.ACTION_DRAG_ENDED:
                Log.d(msg, "Action is DragEvent.ACTION_DRAG_ENDED");
                findViewById(R.id.frame_flight).setBackgroundColor(Color.TRANSPARENT);
                findViewById(R.id.frame_stream).setBackgroundColor(Color.TRANSPARENT);
                moveToList();
                makeToast("Ended");
                break;
            case DragEvent.ACTION_DROP:
                Log.d(msg, "ACTION_DROP event");
                break;
            default:
                break;
        }
        return true;
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

    private void moveToList(){
        Button btnRestart = (Button)findViewById(R.id.btnRestart);
        if(btnRestart.getVisibility() == Button.INVISIBLE)
            btnRestart.setVisibility(Button.VISIBLE);

        if(!flag){
            switch (dragged_lbl){
                case R.id.lblLaunchCons:
                    findViewById(R.id.lc_1).setVisibility(TextView.VISIBLE);
                    deviceA[0] = true;
                    sendBT("101");
                    break;
                case R.id.lblStreamCons:
                    findViewById(R.id.sc_1).setVisibility(TextView.VISIBLE);
                    findViewById(R.id.fc_2).setVisibility(TextView.VISIBLE);
                    findViewById(R.id.lblFlightCons).setVisibility(TextView.INVISIBLE);
                    deviceA[1] = true;
                    deviceB[2] = true;
                    sendBT("102");
                    break;
                case R.id.lblFlightCons:
                    findViewById(R.id.fc_1).setVisibility(TextView.VISIBLE);
                    findViewById(R.id.sc_2).setVisibility(TextView.VISIBLE);
                    findViewById(R.id.lblStreamCons).setVisibility(TextView.INVISIBLE);
                    deviceA[2] = true;
                    deviceB[1] = true;
                    sendBT("103");
                    break;
                case R.id.lblLandCons:
                    findViewById(R.id.lsc_1).setVisibility(TextView.VISIBLE);
                    deviceA[3] = true;
                    sendBT("104");
                    break;
                default:
                    break;
            }
        } else {
            switch (dragged_lbl){
                case R.id.lblLaunchCons:
                    findViewById(R.id.lc_2).setVisibility(TextView.VISIBLE);
                    deviceB[0] = true;
                    sendBT("201");
                    break;
                case R.id.lblStreamCons:
                    findViewById(R.id.sc_2).setVisibility(TextView.VISIBLE);
                    findViewById(R.id.fc_1).setVisibility(TextView.VISIBLE);
                    findViewById(R.id.lblFlightCons).setVisibility(TextView.INVISIBLE);
                    deviceB[1] = true;
                    deviceA[2] = true;
                    sendBT("202");
                    break;
                case R.id.lblFlightCons:
                    findViewById(R.id.fc_2).setVisibility(TextView.VISIBLE);
                    findViewById(R.id.sc_1).setVisibility(TextView.VISIBLE);
                    findViewById(R.id.lblStreamCons).setVisibility(TextView.INVISIBLE);
                    deviceB[2] = true;
                    deviceA[1] = true;
                    sendBT("203");
                    break;
                case R.id.lblLandCons:
                    findViewById(R.id.lsc_2).setVisibility(TextView.VISIBLE);
                    deviceB[3] = true;
                    sendBT("204");
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
        }

    }

    public void onBtnReset(View v){
        sendBT("999");
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

        for(int i = 0; i < 4; i++) {
            deviceA[i] = false;
            deviceB[i] = false;
        }
    }

    public void onBtnStart(View v){

    }

    private void sendBT(String msg){
        mChatService.write(msg.getBytes());
    }

    private void makeToast(String m){
        Toast.makeText(getApplicationContext(), m, Toast.LENGTH_SHORT).show();
    }
}
