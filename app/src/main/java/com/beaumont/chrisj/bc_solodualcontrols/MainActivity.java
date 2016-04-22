package com.beaumont.chrisj.bc_solodualcontrols;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mDevice;
    private final static int REQUEST_ENABLE_BT = 1;

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
            showPrerequisites();
        }

        if(getIntent().getBooleanExtra("Exit me", false)){
            finish();
            return;
        }
    }

    private void showPrerequisites(){
        TextView mTextView = new TextView(getApplicationContext());
        mTextView.setTextSize(20);
        mTextView.setPadding(15, 15, 15, 15);
        mTextView.setText("Devices to be used have to be paired before using this application." +
                " This can be achieved through the Bluetooth settings.");

        AlertDialog.Builder prerequisites = new AlertDialog.Builder(this);
        prerequisites.setTitle("Prerequisite");

        prerequisites.setView(mTextView);

        if (!mBluetoothAdapter.isEnabled()) {
            prerequisites.setNegativeButton("Enable bluetooth", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!mBluetoothAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                        showBTdevices();
                    }
                }
            });
        } else {
            prerequisites.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showBTdevices();
                }
            });
        }
        prerequisites.show();
    }

    private void showBTdevices(){
        final ArrayAdapter mArrayAdapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1);
        final Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mArrayAdapter.add(device.getName());
            }

            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
            LayoutInflater inflater = getLayoutInflater();
            View convertView = (View) inflater.inflate(R.layout.popup_btdevices, null);
            alertDialog.setView(convertView);
            alertDialog.setTitle("Paired devices - select device to connect to:");
            alertDialog.setNeutralButton("Refresh", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showBTdevices();
                }
            });

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

    private void makeToast(String m){
        Toast.makeText(getApplicationContext(), m, Toast.LENGTH_SHORT).show();
    }
}
