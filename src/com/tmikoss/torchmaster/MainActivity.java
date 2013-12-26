package com.tmikoss.torchmaster;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

public class MainActivity extends FragmentActivity implements SensorEventListener, ActionBar.TabListener {
  private BluetoothCommunicator btCommunicator;
  private SensorManager         sensorManager;
  private Sensor                accSensor;
  private PagerAdapter          pagerAdapter;
  private ViewPager             viewPager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

    btCommunicator = new BluetoothCommunicator(this, "HC-06");

    pagerAdapter = new PagerAdapter(getSupportFragmentManager(), this);

    final ActionBar actionBar = getActionBar();
    actionBar.setHomeButtonEnabled(false);
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

    viewPager = (ViewPager) findViewById(R.id.pager);
    viewPager.setAdapter(pagerAdapter);
    viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
      @Override
      public void onPageSelected(int position) {
        actionBar.setSelectedNavigationItem(position);
      }
    });

    for (int i = 0; i < pagerAdapter.getCount(); i++) {
      actionBar.addTab(actionBar.newTab().setText(pagerAdapter.getPageTitle(i)).setTabListener(this));
    }
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
      ColorFragment colorFragment = pagerAdapter.getColorFragment();
      colorFragment.setDisplayedColor(Color.rgb(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]), Integer.parseInt(tokens[3])));
      colorFragment.setDisplayedOpacity(Integer.parseInt(tokens[4]));
    }
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
  public void onSensorChanged(SensorEvent event) {
    if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
      if (event.values[1] > 4) {
        pagerAdapter.getColorFragment().setRandomColor();
      }
    }
  }

  public BluetoothCommunicator getCommunicator() {
    return btCommunicator;
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {}

  @Override
  public void onTabReselected(Tab tab, FragmentTransaction ft) {}

  @Override
  public void onTabSelected(Tab tab, FragmentTransaction ft) {}

  @Override
  public void onTabUnselected(Tab tab, FragmentTransaction ft) {}

}