package com.tmikoss.torchmaster;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.ColorPicker.OnColorChangedListener;

public class MainActivity extends Activity implements OnColorChangedListener {
  private BluetoothCommunicator btCommunicator;
  private ColorPicker           colorPicker;
  private SeekBar               opacityBar;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    btCommunicator = new BluetoothCommunicator(this, "HC-06");
    btCommunicator.enableBluetooth();

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
        btCommunicator.bluetoothEnabled();
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
    colorPicker.setOldCenterColor(color);
    btCommunicator.sendMessage("C-" + Integer.toString(Color.red(color)) + "-" + Integer.toString(Color.green(color)) + "-"
        + Integer.toString(Color.blue(color)));
  }
}
