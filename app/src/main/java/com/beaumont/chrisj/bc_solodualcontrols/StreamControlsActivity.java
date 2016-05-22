package com.beaumont.chrisj.bc_solodualcontrols;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.SurfaceTexture;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.o3dr.android.client.ControlTower;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.ControlApi;
import com.o3dr.android.client.apis.GimbalApi;
import com.o3dr.android.client.apis.VehicleApi;
import com.o3dr.android.client.apis.solo.SoloCameraApi;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.android.client.interfaces.TowerListener;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.attribute.error.CommandExecutionError;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.connection.ConnectionResult;
import com.o3dr.services.android.lib.drone.connection.ConnectionType;
import com.o3dr.services.android.lib.drone.property.Altitude;
import com.o3dr.services.android.lib.drone.property.Attitude;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.drone.property.Type;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.model.SimpleCommandListener;
import com.o3dr.services.android.lib.util.MathUtils;

public class StreamControlsActivity extends AppCompatActivity implements TowerListener, DroneListener {

    BluetoothDevice mDevice;
    BluetoothChatService mChatService;

    //Drone Variables
    private ControlTower controlTower;
    private Drone drone;
    private int droneType = Type.TYPE_UNKNOWN;
    private final Handler handler = new Handler();
    private boolean towerConn;
    private double drone_yaw;
    private double target_yaw;
    private double yaw_before_action;
    private boolean launch_procedure;

    //Drone movement Variables
    private int MOVEMENT_YAW;
    private int MOVEMENT_ALT;
    private int MOVEMENT_DEG;
    private float TURN_SPD;
    private int YAW_CHK_DUR;
    private int LAUNCH_HGHT;

    //Stream Variables
    private boolean stream_loaded;
    GimbalApi.GimbalOrientation orientation;
    public orientationListener ol;

    Button btnLoadStream;
    TextureView stream;

    boolean[] controls_list;

    @Override
    public void onStart() {
        super.onStart();
        this.controlTower.connect(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (this.drone.isConnected()) {
            this.drone.disconnect();
        }
        this.controlTower.unregisterDrone(this.drone);
        this.controlTower.disconnect();
    }

    @Override
    public void onTowerConnected(){
        this.controlTower.registerDrone(this.drone, this.handler);
        this.drone.registerDroneListener(this);
        towerConn = true;
    }

    @Override
    public void onTowerDisconnected(){
        towerConn = false;
    }

    @Override
    public void onDroneConnectionFailed(ConnectionResult cr){
        makeToast("Drone Connection failed");
    }

    @Override
    public void onDroneServiceInterrupted(String s){
        makeToast("Drone service interrupted");
    }

    @Override
    public void onDroneEvent(String event, Bundle extras) {
        State droneState;
        switch (event) {
            case AttributeEvent.STATE_CONNECTED:
                makeToast("Drone Connected");
                droneState = this.drone.getAttribute(AttributeType.STATE);

                if(!controls_list[0]){
                    if(droneState.isFlying()){
                        sendBT("101");
                    } else
                        sendBT("102");
                } else {
                    if (droneState.isFlying()) {
                        findViewById(R.id.frame_stream_takeoff).setVisibility(RelativeLayout.GONE);
                        findViewById(R.id.frame_stream).setVisibility(RelativeLayout.VISIBLE);
                        launch_procedure = false;
                    } else
                        findViewById(R.id.btnArm).setVisibility(Button.VISIBLE);
                }
                break;
            case AttributeEvent.STATE_DISCONNECTED:
                makeToast("Drone Disconnected");
                if(!controls_list[0]){
                    sendBT("103");
                } else {
                    findViewById(R.id.btnArm).setVisibility(Button.INVISIBLE);
                    findViewById(R.id.btnTakeOff).setVisibility(Button.INVISIBLE);
                }
                break;
            case AttributeEvent.STATE_UPDATED:
            case AttributeEvent.STATE_ARMING:
                droneState = this.drone.getAttribute(AttributeType.STATE);

                if(!controls_list[0]){
                    if(this.drone.isConnected() && droneState.isArmed())
                        sendBT("104");
                    else
                        sendBT("105");
                } else {
                    if(this.drone.isConnected() && droneState.isArmed())
                        findViewById(R.id.btnTakeOff).setVisibility(Button.VISIBLE);
                    else
                        findViewById(R.id.btnTakeOff).setVisibility(Button.INVISIBLE);
                }
                break;
            case AttributeEvent.TYPE_UPDATED:
                Type newDroneType = this.drone.getAttribute(AttributeType.TYPE);
                if (newDroneType.getDroneType() != this.droneType) {
                    this.droneType = newDroneType.getDroneType();
                }
                break;
            case AttributeEvent.ATTITUDE_UPDATED:
                updateAttitude();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream_controls);

        MOVEMENT_YAW = 20;
        MOVEMENT_ALT = 10;
        MOVEMENT_DEG = 90;
        TURN_SPD = 0.5f;
        YAW_CHK_DUR = 5000;
        LAUNCH_HGHT = 15;

        launch_procedure = true;

        this.controlTower = new ControlTower(getApplicationContext());
        this.drone = new Drone(getApplicationContext());

        WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage("Make sure this device is connected to the Solo's WiFi.");
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                initBT();
            }
        });
        alert.show();

        stream = (TextureView) findViewById(R.id.stream);
        stream.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
        stream_loaded = false;
        ol = new orientationListener();

        if(getIntent().getExtras() != null){
            mDevice = getIntent().getExtras().getParcelable("btdevice");
            controls_list = getIntent().getExtras().getBooleanArray("controls_lst");
        }

        if(controls_list != null) {
            if (!controls_list[0]) {
                findViewById(R.id.frame_stream_takeoff).setVisibility(RelativeLayout.GONE);
                findViewById(R.id.frame_stream).setVisibility(RelativeLayout.VISIBLE);
            }
        }
    }


    //Launch controls actions
    //=========================================================================
    public void onBtnConn(View v){
        conn();
    }

    public void onBtnArm(View v){
        arm();
    }

    public void onBtnTakeOff(View v){
        takeoff();
    }

    private void conn(){
        if(!this.drone.isConnected()) {
            if(!towerConn)
                makeToast("Make sure 3DR Services app is running, or restart this app");
            else{
                Bundle extraParams = new Bundle();
                extraParams.putInt(ConnectionType.EXTRA_UDP_SERVER_PORT, 14550); // Set default port to 14550

                ConnectionParameter connectionParams = new ConnectionParameter(ConnectionType.TYPE_UDP, extraParams, null);
                this.drone.connect(connectionParams);
            }
        } else {
            makeToast("Already Connected!");
        }
    }

    private void arm(){
        State vehicleState = this.drone.getAttribute(AttributeType.STATE);
        if (vehicleState.isConnected() && !vehicleState.isArmed()){
            VehicleApi.getApi(this.drone).arm(true);
        } else if (vehicleState.isArmed())
            makeToast("Already Armed!");
    }

    private void takeoff(){
        State vehicleState = this.drone.getAttribute(AttributeType.STATE);
        if(vehicleState.isConnected() && vehicleState.isArmed() && !vehicleState.isFlying()){
            ControlApi.getApi(this.drone).takeoff(LAUNCH_HGHT, new AbstractCommandListener() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(int executionError) {
                    makeToast("Failed");
                }

                @Override
                public void onTimeout() {
                    makeToast("Timeout");
                }
            });
        }
    }

    private void force_Guided_mode(){
        VehicleApi.getApi(this.drone).setVehicleMode(VehicleMode.COPTER_GUIDED);
    }

    //Bluetooth stuff
    //=========================================================================
    private void initBT(){
        mChatService = new BluetoothChatService(getApplicationContext(), new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case Constants.MESSAGE_WRITE:
                        byte[] writeBuf = (byte[]) msg.obj;
                        // construct a string from the buffer
                        String writeMessage = new String(writeBuf);
                        makeToast("Write: " + writeMessage);
                        break;
                    case Constants.MESSAGE_READ:
                        byte[] readBuf = (byte[]) msg.obj;
                        // construct a string from the valid bytes in the buffer
                        String readMessage = new String(readBuf, 0, msg.arg1);
                        parseMsg(readMessage);
                        break;
                    default:
                        break;
                }
            }
        });
        mChatService.start();
        mChatService.connect(mDevice, true);
    }

    private void parseMsg(String msg){
        int code = Integer.parseInt(msg);

        //10 - launch controls
        //20 - direction controls
        //30 - rotation controls
        //40 - altitude controls

        switch (code){
            case(101):
                makeToast("Connecting");
                conn();
                break;
            case(102):
                makeToast("Arming");
                arm();
                break;
            case(103):
                makeToast("Taking off");
                takeoff();
                break;
            case(201):
                makeToast("Forward");
                moveForward();
                break;
            case(202):
                makeToast("Right");
                moveRight();
                break;
            case(203):
                makeToast("Backward");
                moveBackward();
                break;
            case(204):
                makeToast("Left");
                moveLeft();
                break;
            case(301):
                makeToast("Rotate Right");
                rotateRight();
                break;
            case(302):
                makeToast("Rotate Left");
                rotateLeft();
                break;
            case(401):
                makeToast("Increase altitude");
                altitudeInc();
                break;
            case(402):
                makeToast("Decrease altitude");
                altitudeDec();
                break;
            default:
                break;
        }
    }

    private void sendBT(String s){
        mChatService.write(s.getBytes());
        makeToast(s);
    }


    //Flight Controls
    //=========================================================================
    private void moveForward(){
        moveDrone(0.0);
    }

    private void moveBackward(){
        moveDrone(180.0);
    }

    private void moveLeft(){
        moveDrone(270.0);
    }

    private void moveRight(){
        moveDrone(90.0);
    }

    private void rotateRight(){
        yaw_before_action = drone_yaw;

        //Drone yaw goes from 0 to 180 and then -179 back to 0. This converts it to 0-360
        double current_yaw = (drone_yaw < 0 ? (180 + (180 - (-drone_yaw))) : drone_yaw);

        target_yaw = current_yaw + MOVEMENT_DEG;
        target_yaw = (target_yaw >= 360 ? (target_yaw - 360) : target_yaw);
            ControlApi.getApi(this.drone).turnTo((float)target_yaw, TURN_SPD, false, new AbstractCommandListener() {
                @Override
                public void onSuccess() {}
                @Override
                public void onError(int executionError) {
                    read_executionError("Failed to rotate", executionError);
                }
                @Override
                public void onTimeout() {
                    makeToast("Failed to rotate (timeout)");
                }

            });
    }

    private void rotateLeft(){
        yaw_before_action = drone_yaw;

        //Drone yaw goes from 0 to 180 and then -179 back to 0. This converts it to 0-360
        double current_yaw = (drone_yaw < 0 ? (180 + (180 - (-drone_yaw))) : drone_yaw);

        target_yaw = current_yaw - MOVEMENT_DEG;
        target_yaw = (target_yaw < 0 ? (target_yaw + 360) : target_yaw);

        ControlApi.getApi(this.drone).turnTo((float) target_yaw, -TURN_SPD, false, new AbstractCommandListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(int executionError) {
                read_executionError("Failed to rotate", executionError);
            }

            @Override
                public void onTimeout() {
                    makeToast("Failed to rotate (timeout)");
                }

            });
    }

    private void altitudeInc(){
        yaw_before_action = drone_yaw;

            Altitude alt = this.drone.getAttribute(AttributeType.ALTITUDE);
            ControlApi.getApi(this.drone).climbTo(alt.getAltitude() + MOVEMENT_ALT);
            check_yaw();
    }

    private void altitudeDec(){
        yaw_before_action = drone_yaw;
            Altitude alt = this.drone.getAttribute(AttributeType.ALTITUDE);
            double target_alt = alt.getAltitude() - MOVEMENT_ALT;

            if (target_alt <= 0)
                makeToast("This will put the drone below the ground! Try landing");
            else {
                ControlApi.getApi(this.drone).climbTo(alt.getAltitude() - MOVEMENT_ALT);
                check_yaw();
            }
    }

    private void moveDrone(double bearing){
            yaw_before_action = drone_yaw;

            double target_bearing = bearing + drone_yaw;
            if (target_bearing >= 360)
                target_bearing = target_bearing - 360;

            LatLong current;
            try {
                Gps gps = this.drone.getAttribute(AttributeType.GPS);
                current = new LatLong(gps.getPosition().getLatitude(), gps.getPosition().getLongitude());
            } catch (Exception e) {
                current = new LatLong(54.068164, -2.801859);
            }

            LatLong target = MathUtils.newCoordFromBearingAndDistance(current, target_bearing, MOVEMENT_YAW);

            ControlApi.getApi(this.drone).goTo(target, true, new AbstractCommandListener() {
                @Override
                public void onSuccess() {
                    check_yaw();
                }

                @Override
                public void onError(int executionError) {
                    makeToast("Couldn't move (Error)");
                }

                @Override
                public void onTimeout() {
                    makeToast("Couldn't move (Timeout)");
                }
            });
    }

    private void read_executionError(String msg, int error){
        if (error == CommandExecutionError.COMMAND_DENIED)
            makeToast(msg + ": Command Denied");
        else if (error == CommandExecutionError.COMMAND_FAILED)
            makeToast(msg + ": Command Failed");
        else if (error == CommandExecutionError.COMMAND_TEMPORARILY_REJECTED)
            makeToast(msg + ": Command rejected");
        else if (error == CommandExecutionError.COMMAND_UNSUPPORTED)
            makeToast(msg + ": unsupported");
        else
            makeToast(msg + ": Error didn't match");
    }

    private void check_yaw() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                if(yaw_before_action != drone_yaw)
                    rotate();
            }
        }, YAW_CHK_DUR);
    }

    private void rotate(){
        ControlApi.getApi(this.drone).turnTo((float) yaw_before_action, TURN_SPD, false, new AbstractCommandListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(int executionError) {
                read_executionError("Failed to rotate", executionError);
            }

            @Override
            public void onTimeout() {
                makeToast("Failed to rotate (timeout)");
            }
        });
    }

    protected void updateAttitude(){
        Attitude droneAttitude = this.drone.getAttribute(AttributeType.ATTITUDE);
        drone_yaw = droneAttitude.getYaw();

        if(launch_procedure) {
            Altitude droneAlt = this.drone.getAttribute(AttributeType.ALTITUDE);
            double alt = droneAlt.getAltitude();

            if ((alt > (LAUNCH_HGHT - 1))) {
                if (controls_list[0]) {
                    findViewById(R.id.frame_stream_takeoff).setVisibility(RelativeLayout.GONE);
                    findViewById(R.id.frame_stream).setVisibility(RelativeLayout.VISIBLE);
                } else {
                    sendBT("106");
                }
                launch_procedure = false;
            }
        }


    }


    //Stream Controls
    //=========================================================================
    public void onBtnLoadStream(View view){
        if(stream_loaded){
            stopVideoStream();
        } else {
            if(stream.isAvailable()){
                makeToast("Stream available");
                startVideoStream(new Surface(stream.getSurfaceTexture()));
            } else {
                makeToast("Stream not available");
            }
        }
    }

    public void onBtnPhoto(View view) {
        SoloCameraApi.getApi(this.drone).takePhoto(new AbstractCommandListener() {
            @Override
            public void onSuccess() {
                makeToast("Photo taken.");
            }

            @Override
            public void onError(int executionError) {
                read_executionError("Photo error: ", executionError);
            }

            @Override
            public void onTimeout() {
                makeToast("Timeout while trying to take the photo.");
            }
        });
    }

    public void onBtnRecord(View view) {
        SoloCameraApi.getApi(drone).toggleVideoRecording(new AbstractCommandListener() {
            @Override
            public void onSuccess() {
                makeToast("Video recording toggled.");
            }

            @Override
            public void onError(int executionError) {
                read_executionError("Error toggling record: ", executionError);
            }

            @Override
            public void onTimeout() {
                makeToast("Timeout while trying to toggle video recording.");
            }
        });
    }

    public void onBtnLookUp(View view){
        orientation = GimbalApi.getApi(this.drone).getGimbalOrientation();
        float pitch = orientation.getPitch();
        pitch = pitch + 5.0f;

        GimbalApi.getApi(this.drone).updateGimbalOrientation(pitch, orientation.getRoll(), orientation.getYaw(), ol);
    }

    public void onBtnLookDown(View view){
        orientation = GimbalApi.getApi(this.drone).getGimbalOrientation();
        float pitch = orientation.getPitch();
        pitch = pitch - 5.0f;

        GimbalApi.getApi(this.drone).updateGimbalOrientation(pitch, orientation.getRoll(), orientation.getYaw(), ol);
    }

    private void startVideoStream(Surface videoSurface) {
        SoloCameraApi.getApi(drone).startVideoStream(videoSurface, "", true, new AbstractCommandListener() {
            @Override
            public void onSuccess() {
                stream_loaded = true;
                btnLoadStream.setText("Stop stream");
            }

            @Override
            public void onError(int executionError) {
                read_executionError("Cant load stream: ", executionError);
            }

            @Override
            public void onTimeout() {
                makeToast("Timed out while attempting to start the video stream.");
            }
        });
        GimbalApi.getApi(this.drone).startGimbalControl(ol);
    }

    private void stopVideoStream() {
        SoloCameraApi.getApi(drone).stopVideoStream(new SimpleCommandListener() {
            @Override
            public void onSuccess() {
                stream_loaded = false;
                btnLoadStream.setText("Load stream");
            }
        });
        GimbalApi.getApi(this.drone).stopGimbalControl(ol);
    }

    private void makeToast(String m){
        Toast.makeText(getApplicationContext(), m, Toast.LENGTH_SHORT).show();
    }

    public class orientationListener implements GimbalApi.GimbalOrientationListener{
        @Override
        public void onGimbalOrientationUpdate(GimbalApi.GimbalOrientation orientation) {}

        @Override
        public void onGimbalOrientationCommandError(int error) {
            if (error == CommandExecutionError.COMMAND_DENIED)
                makeToast("Gimball error: Command Denied");
            else if (error == CommandExecutionError.COMMAND_FAILED)
                makeToast("Gimball error: Command Failed");
            else if (error == CommandExecutionError.COMMAND_TEMPORARILY_REJECTED)
                makeToast("Gimball error: Command rejected");
            else if (error == CommandExecutionError.COMMAND_UNSUPPORTED)
                makeToast("Gimball error: unsupported");
            else
                makeToast("Error didn't match");
        }
    }

}