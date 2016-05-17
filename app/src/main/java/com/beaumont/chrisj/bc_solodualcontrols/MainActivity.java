package com.beaumont.chrisj.bc_solodualcontrols;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mDevice;

    private final static int REQUEST_ENABLE_BT = 1;

    String msg;
    int draggedlbl;

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
                        break;
                    }
                }
                alert.cancel();
            }
        });
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
                    moveControl();
                    break;
                case DragEvent.ACTION_DROP:
                    Log.d(msg, "ACTION_DROP event");
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
                draggedlbl = v.getId();
                return true;
            }
            else
            {
                return false;
            }
        }
    }

    private void moveControl(){
        Button btnRestart = (Button)findViewById(R.id.btnRestart);
        if(btnRestart.getVisibility() == Button.INVISIBLE)
            btnRestart.setVisibility(Button.VISIBLE);

        if(!flag){
            switch (draggedlbl){
                case R.id.lblLaunchCons:
                    findViewById(R.id.lc_1).setVisibility(TextView.VISIBLE);
                    break;
                case R.id.lblStreamCons:
                    findViewById(R.id.sc_1).setVisibility(TextView.VISIBLE);
                    findViewById(R.id.fc_2).setVisibility(TextView.VISIBLE);
                    findViewById(R.id.lblFlightCons).setVisibility(TextView.INVISIBLE);
                    break;
                case R.id.lblFlightCons:
                    findViewById(R.id.fc_1).setVisibility(TextView.VISIBLE);
                    findViewById(R.id.sc_2).setVisibility(TextView.VISIBLE);
                    findViewById(R.id.lblStreamCons).setVisibility(TextView.INVISIBLE);
                    break;
                case R.id.lblLandCons:
                    findViewById(R.id.lsc_1).setVisibility(TextView.VISIBLE);
                    break;
                default:
                    break;
            }
        } else {
            switch (draggedlbl){
                case R.id.lblLaunchCons:
                    findViewById(R.id.lc_2).setVisibility(TextView.VISIBLE);
                    break;
                case R.id.lblStreamCons:
                    findViewById(R.id.sc_2).setVisibility(TextView.VISIBLE);
                    findViewById(R.id.fc_1).setVisibility(TextView.VISIBLE);
                    findViewById(R.id.lblFlightCons).setVisibility(TextView.INVISIBLE);
                    break;
                case R.id.lblFlightCons:
                    findViewById(R.id.fc_2).setVisibility(TextView.VISIBLE);
                    findViewById(R.id.sc_1).setVisibility(TextView.VISIBLE);
                    findViewById(R.id.lblStreamCons).setVisibility(TextView.INVISIBLE);
                    break;
                case R.id.lblLandCons:
                    findViewById(R.id.lsc_2).setVisibility(TextView.VISIBLE);
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

    public void onBtnRestart(View v){
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
    }

    private void makeToast(String m){
        Toast.makeText(getApplicationContext(), m, Toast.LENGTH_SHORT).show();
    }
}
