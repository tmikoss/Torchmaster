package com.tmikoss.torchmaster;

import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.ColorPicker.OnColorChangedListener;

public class MainActivity extends Activity implements OnColorChangedListener, SensorEventListener {
  private BluetoothCommunicator btCommunicator;
  private ColorPicker           colorPicker;
  private SeekBar               opacityBar;
  private SensorManager         sensorManager;
  private Sensor                accSensor;
  private long                  lastRandomAt = 0;
  private final Random          rng          = new Random();
  private final int[]           randomColors = new int[] { Color.GREEN, Color.BLUE, Color.CYAN, Color.MAGENTA, Color.RED, Color.YELLOW };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

    btCommunicator = new BluetoothCommunicator(this, "HC-06");

    colorPicker = (ColorPicker) findViewById(R.id.colorPicker);
    colorPicker.setOnColorChangedListener(this);

    opacityBar = (SeekBar) findViewById(R.id.opacityBar);
    opacityBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        btCommunicator.sendMessage("O-" + Integer.toString(progress));
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent authActivityResult) {
    super.onActivityResult(requestCode, resultCode, authActivityResult);
    switch (requestCode) {
    case BluetoothCommunicator.activityResultBluetoothEnabled:
      if (resultCode == RESULT_OK) {
        btCommunicator.establishConnection();
      }
    }
  }

  public void receiveMessage(String message) {
    String[] tokens = message.split("-");
    switch (message.charAt(0)) {
    case 'S':
      int currentColor = Color.rgb(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]), Integer.parseInt(tokens[3]));
      colorPicker.setColor(currentColor);
      colorPicker.setOldCenterColor(currentColor);
      opacityBar.setProgress(Integer.parseInt(tokens[4]));
    }
  }

  @Override
  public void onColorChanged(int color) {
    colorPicker.setColor(color);
    colorPicker.setOldCenterColor(color);
    btCommunicator.sendMessage("C-" + Integer.toString(Color.red(color)) + "-" + Integer.toString(Color.green(color)) + "-"
        + Integer.toString(Color.blue(color)));
  }

  @Override
  public void onResume() {
    super.onResume();
    btCommunicator.enableBluetooth();
    sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
  }

  @Override
  public void onPause() {
    super.onPause();
    btCommunicator.dropConnection();
    sensorManager.unregisterListener(this, accSensor);
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {

  }

  public void setRandomColor() {
    if (System.currentTimeMillis() - lastRandomAt < 2000) return;
    int index = rng.nextInt(randomColors.length);
    onColorChanged(randomColors[index]);
    lastRandomAt = System.currentTimeMillis();
  }

  @Override
  public void onSensorChanged(SensorEvent event) {
    if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
      if (event.values[1] > 4) {
        setRandomColor();
      }
    }
  }
}
