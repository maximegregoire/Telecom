package com.example.max.maxapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.FloatMath;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;


public class MyActivity extends Activity implements SensorEventListener{
    public final static String EXTRA_MESSAGE = "com.example.max.maxapp.DisplayMessageActivity";

    private SensorManager sensorManager;
    private Sensor mGyroSensor;
    private EditText editText;
    private TextView textView;
    private boolean isSensorRegistered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        editText = (EditText) findViewById(R.id.edit_message);
        textView = (TextView) findViewById(R.id.messages);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mGyroSensor= sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        onPause();
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        textView.setText(event.values[0] + "\n\n" + event.values[1] + "\n" + event.values[2]);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        // Register a listener for the sensor.
        super.onResume();
        sensorManager.registerListener(this, mGyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
        isSensorRegistered = true;
    }

    @Override
    protected void onPause() {
        // unregister listener
        super.onPause();
        sensorManager.unregisterListener(this);
        isSensorRegistered = false;
    }

    public void sendMessage(View view)
    {
        String message = editText.getText().toString();
        ScrollView scrollView = (ScrollView) findViewById(R.id.messagesScrollView);
        textView.append(message + "  Azimuth:" + "\n");
        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
    }

    public void stopOrResume(View view)
    {
        if (isSensorRegistered)
        {
            onPause();
        }
        else
        {
            onResume();
        }
    }
}
