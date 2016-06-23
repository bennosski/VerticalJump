package com.example.ben.verticaljump;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.StrictMath.sqrt;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    public final static String EXTRA_MESSAGE = "myfirstapp.MESSAGE";
    private SensorManager mSensorManager;
    private Sensor accel;
    public static boolean running_measurement = false;
    //public static ArrayList<Double> accelerations = new ArrayList<Double>();
    public static int size_accels = 100000;
    public static double accelerations[] = new double[100000];
    public static long times[] = new long[100000];
    public static int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

    }

    public void startMeasurement(View view) {
        /*
        Intent intent = new Intent(this, StartButtonActivity.class);
        EditText editText = (EditText) findViewById(R.id.edit_message);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
        */

        //mSensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);
        Button button = (Button) findViewById(R.id.start_button);

        if(!running_measurement) {
            mSensorManager.registerListener(this, accel, accel.getMinDelay());

            button.setText("Stop");
        }
        else{
            mSensorManager.unregisterListener(this);

            button.setText("Start");

            TextView textView = (TextView) findViewById(R.id.text_box1);
            textView.setText(calculateVertical());


            //TextView textView = (TextView) findViewById(R.id.text_box1);
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File filename = new File(path, "_myfile2.txt");
            String string = "";

            try {
                FileOutputStream outputStream = new FileOutputStream(filename, false);
                int max_i = min(count, size_accels);
                for(int i=0; i<max_i; i++) {
                    string = Long.toString(times[i]);
                    outputStream.write(string.getBytes());
                    string = ",";
                    outputStream.write(string.getBytes());
                    string = Double.toString(accelerations[i]);
                    outputStream.write(string.getBytes());
                    string = ",";
                    outputStream.write(string.getBytes());
                }
                outputStream.close();

                //textView.setText(Double.toString(accel.getMinDelay()));

            } catch (Exception e) {
                //textView.setText("Error");
                e.printStackTrace();
            }

            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(filename));
            sendBroadcast(intent);
            //*/

            accelerations = new double[100000];
            times = new long[100000];
            count = 0;
        }
        running_measurement = !running_measurement;

        //EditText editText = (EditText) findViewById(R.id.edit_message);
        //String message = editText.getText().toString();

        /*
        TextView textView = (TextView) findViewById(R.id.text_box1);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            textView.setText("GOOD");
        }
        else {
            textView.setText("NO ACCEL");
        }
        */
    }


    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        // The light sensor returns a single value.
        // Many sensors return 3 values, one for each axis.
        //float lux = event.values[0];
        double a0 = (double) event.values[0];
        double a1 = (double) event.values[1];
        double a2 = (double) event.values[2];

        double a = sqrt(a0*a0+a1*a1+a2*a2);

        // Do something with this sensor value.

        TextView textView = (TextView) findViewById(R.id.text_box2);
        textView.setText(Double.toString(a));

        if(count<size_accels) {
            accelerations[count] = a;
            times[count] = event.timestamp;
        }
        count = count + 1;
    }

    public String calculateVertical(){
        ArrayList cluster_start = new ArrayList<Integer>();
        ArrayList cluster_length = new ArrayList<Integer>();

        int l=0;
        boolean appending=false;
        for(int i=0; i<count; i++){
            if(accelerations[i]<5.0){
                if(!appending) {
                    cluster_start.add(i);
                    appending = true;
                    l = 0;
                }

                l+=1;
            }

            if(accelerations[i]>5.0 && appending){
                cluster_length.add(l);
                appending = false;
            }
        }

        //find max cluster length
        int max = -1;
        int max_index = 0;
        int c=0;
        while(c < cluster_length.size()){
            l = (Integer)cluster_length.get(c);
            if(l > max) {
                max_index = c;
                max = l;
            }
            c=c+1;
        }

        int index1 = (Integer)cluster_start.get(max_index) - 25;
        int index2 = (Integer)cluster_start.get(max_index) + (Integer)cluster_length.get(max_index) + 25;

        int i1=0,i2=0;
        for(int i=0; i<index2-index1; i++)
            if(accelerations[index1+i]<5.0){
                i1 = index1+i;
                break;
            }
        for(int i=0; i<index2-index1; i++)
            if(accelerations[index2-1-i]<5.0){
                i2 = index2-1-i;
                break;
            }

        double vertical = 1.0/8.*386.09*((double)(times[i2]-times[i1])/pow(10.,9))*((double)(times[i2]-times[i1])/pow(10.,9));
        String out = String.format("%.2f inches!", vertical);
        return out;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //mSensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }



}
