package com.tmikoss.torchmaster;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;

import com.chiralcode.colorpicker.ColorPickerDialog;
import com.chiralcode.colorpicker.ColorPickerDialog.OnColorSelectedListener;

public class MainActivity extends Activity {
  private BluetoothCommunicator btCommunicator;
  private int                   currentColor;
  private int                   currentOpacity;
  private View                  colorView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    btCommunicator = new BluetoothCommunicator(this, "HC-06");
    btCommunicator.enableBluetooth();

    colorView = findViewById(R.id.viewColor);
    setCurrentColor(Color.BLACK);

    final ColorPickerDialog colorPickerDialog = new ColorPickerDialog(this, Color.WHITE, new OnColorSelectedListener() {
      @Override
      public void onColorSelected(int color) {
        setCurrentColor(color);
      }
    });

    colorView.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        colorPickerDialog.show();
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
      setCurrentColor(Color.rgb(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]), Integer.parseInt(tokens[3])));
      currentOpacity = Integer.parseInt(tokens[4]);
    }
  }

  public void setCurrentColor(int color) {
    currentColor = color;
    colorView.setBackgroundColor(currentColor);
    btCommunicator.sendMessage("C-" + Integer.toString(Color.red(currentColor)) + "-" + Integer.toString(Color.green(currentColor)) + "-"
        + Integer.toString(Color.blue(currentColor)));
  }
}
