package com.tmikoss.torchmaster;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.CompoundButton;
import android.widget.Switch;

public class MainActivity extends Activity {
  BluetoothCommunicator btCommunicator;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    btCommunicator = new BluetoothCommunicator(this, "HC-06");
    btCommunicator.connect();

    Switch onOffSwitch = (Switch) findViewById(R.id.switchCurrentStatus);
    onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
          btCommunicator.sendMessage("C-200-200-200");
        } else {
          btCommunicator.sendMessage("C-0-0-0");
        }
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

}
