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
  BluetoothCommunicator btCommunicator;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    btCommunicator = new BluetoothCommunicator(this, "HC-06");
    btCommunicator.enableBluetooth();

    final View colorView = findViewById(R.id.viewColor);
    colorView.setBackgroundColor(Color.BLACK);

    final ColorPickerDialog colorPickerDialog = new ColorPickerDialog(this, Color.WHITE, new OnColorSelectedListener() {
      @Override
      public void onColorSelected(int color) {
        btCommunicator.sendMessage("C-" + Integer.toString(Color.red(color)) + "-" + Integer.toString(Color.green(color)) + "-"
            + Integer.toString(Color.blue(color)));
        colorView.setBackgroundColor(color);
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
}
