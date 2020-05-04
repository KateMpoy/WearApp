package com.katempoy.myapplication;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends Activity implements SensorEventListener {

    private static final String TAG = "MainActivity";
    private TextView mTextViewStepCount;
    private TextView mTextViewStepDetect;
    private TextView mTextViewHeart;
    private Button mStart;
    private Button mStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Keep the Wear screen always on (for testing only!)
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextViewStepCount = (TextView) stub.findViewById(R.id.step_count);
                mTextViewStepDetect = (TextView) stub.findViewById(R.id.step_detect);
                mTextViewHeart = (TextView) stub.findViewById(R.id.heart);
                mStart = (Button) stub.findViewById(R.id.start);
                mStop = (Button) stub.findViewById(R.id.stop);

                mStart.setOnClickListener(clickButton);
                mStop.setOnClickListener(clickButton2);

            }
        });

    }

    private View.OnClickListener clickButton = new View.OnClickListener() {
        public void onClick(View v) {
            getStepCount();

        }
    };

    private View.OnClickListener clickButton2 = new View.OnClickListener() {
        public void onClick(View v) {
            stopCount();

        }
    };

    private void getStepCount() {
        SensorManager mSensorManager = ((SensorManager)getSystemService(SENSOR_SERVICE));

       // Sensor mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        //Sensor mStepCountSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        Sensor mStepDetectSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
       // mSensorManager.registerListener(this, mStepCountSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mStepDetectSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void stopCount() {
        SensorManager mSensorManager = ((SensorManager)getSystemService(SENSOR_SERVICE));
        mSensorManager.unregisterListener(this);
    }

    private String currentTimeStr() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        return df.format(c.getTime());
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "onAccuracyChanged - accuracy: " + accuracy);
    }

    public void onSensorChanged(SensorEvent event) {
        final int REQUEST_CODE_ASK_PERMISSIONS = 123;

        if ( Build.VERSION.SDK_INT >= 26){
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED  ){
                requestPermissions(new String[]{
                                android.Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_ASK_PERMISSIONS);
                return ;
            }else{

                if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
                    String msg = "" + (int)event.values[0];
                    mTextViewHeart.setText(msg);
                    Log.d(TAG, msg);
                }
                else if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                    String msg = "Count: " + (int)event.values[0];
                    mTextViewStepCount.setText(msg);
                    Log.d(TAG, msg);
                }
                else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

                    String msg = "Detected at " + currentTimeStr() + "\n"+
                            "X" + " " + event.values[0] + "\n" + "Y" +" "+ event.values[1] +  "\n" + "Z" +" " + event.values[2];
                    mTextViewStepDetect.setText(msg);
                    Log.d(TAG, msg);
                }
                else
                    Log.d(TAG, "Unknown sensor type");

            }
        }


    }

}