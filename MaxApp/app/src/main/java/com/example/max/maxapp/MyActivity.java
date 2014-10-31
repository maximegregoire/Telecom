package com.example.max.maxapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.method.ScrollingMovementMethod;
import android.util.FloatMath;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.conn.util.InetAddressUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.DatagramSocketImpl;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;


public class MyActivity extends Activity implements SensorEventListener{
    public final static String EXTRA_MESSAGE = "com.example.max.maxapp.DisplayMessageActivity";
    public final int PORT = 7777;
    public final int BYTES_IN_FLOAT = 4;

    // max byte value(127) / max sensor value(10)
    public final int SENSOR_MULTIPLE = 12;

    private SensorManager sensorManager;
    private Sensor mGyroSensor;

    private EditText ipText;
    private TextView textView;
    private Button startPauseButton;

    private boolean isSensorInitialized;
    private DatagramSocket socket;
    private InetAddress netAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        ipText = (EditText) findViewById(R.id.edit_ip);
        textView = (TextView) findViewById(R.id.messages);
        startPauseButton = (Button) findViewById(R.id.startPauseButton);

        try {
            socket = new DatagramSocket(PORT);
            socket.setBroadcast(true);
            socket.setSoTimeout(500);
            SocketAddress sa = socket.getLocalSocketAddress();
            netAddress = InetAddress.getByName("127.0.0.1");
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        isSensorInitialized = false;
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        textView.setText(event.values[0] + "\n\n" + event.values[1] + "\n" + event.values[2]);
        byte[] data = new byte[]{(byte)(event.values[1]*SENSOR_MULTIPLE)};
        //byte[] arr = ByteBuffer.allocate(BYTES_IN_FLOAT).putFloat(event.values[1]).array();
        DatagramPacket p = new DatagramPacket(
                data,
                1,
                netAddress,
                PORT);
        try
        {
            socket.send(p);
        }
         catch (Exception e)
         {
            e.printStackTrace();
        }
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
    }

    @Override
    protected void onPause() {
        // unregister listener
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    public void UpdateIp(View view)
    {
        String message = ipText.getText().toString();
        if (!InetAddressUtils.isIPv4Address(message))
        {
            printInvalidIp();
        }
        else {
            try {
                netAddress = InetAddress.getByName(message);
            } catch (UnknownHostException e) {
                printInvalidIp();
            }
        }
    }

    public void startOrPause(View view)
    {
        if (isSensorInitialized)
        {
            startPauseButton.setText(R.string.button_start);
            startPauseButton.setBackgroundColor(Color.GREEN);
            startPauseButton.setTextColor(Color.WHITE);
            isSensorInitialized = false;
            onPause();
        }
        else
        {
            byte[] data = new byte[]{-127};
            //byte[] arr = ByteBuffer.allocate(BYTES_IN_FLOAT).putFloat(event.values[1]).array();
            DatagramPacket p = new DatagramPacket(
                    data,
                    1,
                    netAddress,
                    PORT);
            try
            {
                socket.send(p);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            mGyroSensor= sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            startPauseButton.setText(R.string.button_pause);
            startPauseButton.setBackgroundColor(Color.RED);
            startPauseButton.setTextColor(Color.WHITE);
            isSensorInitialized = true;
            onResume();
        }
    }

    private void printInvalidIp()
    {
        Toast toast = Toast.makeText(getApplicationContext(), R.string.invalid_ip, Toast.LENGTH_LONG);
        toast.show();
    }

}
