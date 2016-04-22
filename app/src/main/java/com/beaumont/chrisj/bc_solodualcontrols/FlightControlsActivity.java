package com.beaumont.chrisj.bc_solodualcontrols;

import android.bluetooth.BluetoothDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class FlightControlsActivity extends AppCompatActivity {

    BluetoothDevice bluetoothDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_controls);

        if(getIntent().getExtras() != null){
            bluetoothDevice = getIntent().getExtras().getParcelable("btdevice");
        }

        if(bluetoothDevice != null){
            makeToast(bluetoothDevice.getName());
        }
    }

    private void makeToast(String m){
        Toast.makeText(getApplicationContext(), m, Toast.LENGTH_SHORT).show();
    }
}
