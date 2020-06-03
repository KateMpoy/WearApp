package com.katempoy.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity implements SensorEventListener {

    private static final String TAG = "MainActivity";

    private android.location.LocationListener locListener;

    private TextView mTextViewStepCount;
    private TextView mTextViewStepDetect;
    private TextView mTextViewHeart;
    private Button mStart;
    private Button mStop;
    private Button mCatalog;

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
                mCatalog = (Button) stub.findViewById(R.id.catalog);

                mStart.setOnClickListener(clickButton);
                mStop.setOnClickListener(clickButton2);
                mCatalog.setOnClickListener(clickButton3);

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

    private View.OnClickListener clickButton3 = new View.OnClickListener() {
        public void onClick(View v) {
            dialog();
        }
    };

    private void dialog (){

        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        String path = this.getFilesDir().getAbsolutePath()+"/text";
        String file = "";
        File directory = new File(path);
        File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++)
        {
            file = file + files[i].getName();
        }

        builder1.setMessage("Path:" + path + "\n" + "Files:" + file );
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Close",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();

    }

    private void getStepCount() {


        SensorManager mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));


        Sensor mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        Sensor mStepCountSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        Sensor mStepDetectSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (mHeartRateSensor != null) {
            mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mStepCountSensor != null) {
            mSensorManager.registerListener(this, mStepCountSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mStepDetectSensor != null) {
            mSensorManager.registerListener(this, mStepDetectSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

    }

    private void stopCount() {
        SensorManager mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        mSensorManager.unregisterListener(this);
    }

    private String currentTimeStr() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        return df.format(c.getTime());
    }

    protected boolean isIdleMode(final Context context) {
        final String packageName = context.getPackageName();
        final PowerManager manager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        boolean isIgnoringOptimizations = manager.isIgnoringBatteryOptimizations(packageName);
        return manager.isDeviceIdleMode() && !isIgnoringOptimizations;
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "onAccuracyChanged - accuracy: " + accuracy);
    }

    public void onSensorChanged(SensorEvent event) {
        final int REQUEST_CODE_ASK_PERMISSIONS = 123;


        if (Build.VERSION.SDK_INT >= 26) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                                android.Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_ASK_PERMISSIONS);
                return;
            } else {

                if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
                    String msg = "" + (int) event.values[0];
                    mTextViewHeart.setText(msg);
                    Log.d(TAG, msg);
                } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                    String msg = "GYROSCOPE:  at " + TimeUnit.SECONDS.convert(event.timestamp, TimeUnit.NANOSECONDS) + ","+
                            "X:" + " " + event.values[0] + "," + "Y:" + " " + event.values[1] + "," + "Z:" + " " + event.values[2];
                    mTextViewStepCount.setText(msg);
                    Log.d(TAG, msg);

                    String data = TimeUnit.SECONDS.convert(event.timestamp, TimeUnit.NANOSECONDS) + "," + event.values[0] + ',' + event.values[1] + ',' + event.values[2] + "\n";

                    File file = new File(MainActivity.this.getFilesDir(), "text");
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    try {
                        File gpxfile = new File(file, "gyroscope.txt");
                        FileWriter writer = new FileWriter(gpxfile, true);
                        writer.append(data);
                        writer.flush();
                        writer.close();

                    } catch (Exception e) {
                        Log.d("error", e.getMessage());
                    }

                    if (isIdleMode(this)) {
                        Log.d("idle", "true");
                        stopCount();
                    } else {
                        Log.d("idle", "false");
                        getStepCount();
                    }
                } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

                    String msg = "ACC. at " +  TimeUnit.SECONDS.convert(event.timestamp, TimeUnit.NANOSECONDS) + ","+
                            "X:" + " " + event.values[0] + "," + "Y:" + " " + event.values[1] + "," + "Z:" + " " + event.values[2];
                    mTextViewStepDetect.setText(msg);
                    Log.d(TAG, msg);

                    String data = TimeUnit.SECONDS.convert(event.timestamp, TimeUnit.NANOSECONDS) + "," + event.values[0] + ',' + event.values[1] + ',' + event.values[2] + "\n";

                    File file = new File(MainActivity.this.getFilesDir(), "text");
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    try {
                        File gpxfile = new File(file, "accelerometer.txt");
                        FileWriter writer = new FileWriter(gpxfile, true);
                        writer.append(data);
                        writer.flush();
                        writer.close();


                    } catch (Exception e) {
                        Log.d("error", e.getMessage());
                    }

                    if (isIdleMode(this)) {
                        Log.d("idle", "true");
                        stopCount();
                    } else {
                        Log.d("idle", "false");
                        getStepCount();
                    }
                } else
                    Log.d(TAG, "Unknown sensor type");




            }
        }


    }

}