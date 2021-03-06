package com.tmikoss.torchmaster;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

public class MainActivity extends FragmentActivity implements SensorEventListener, ActionBar.TabListener {
  private SensorManager sensorManager;
  private Sensor        accSensor;
  private PagerAdapter  pagerAdapter;
  private ViewPager     viewPager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);

    sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

    initializePager();
  }

  private void initializePager() {
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

  public void receiveMessage(String message) {
    String[] tokens = message.split("-");
    switch (message.charAt(0)) {
    case 'C':
      pagerAdapter.getColorFragment().setDisplayedColor(
          Color.rgb(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]), Integer.parseInt(tokens[3])));
      break;
    case 'O':
      pagerAdapter.getColorFragment().setDisplayedOpacity(Integer.parseInt(tokens[1]));
      break;
    case 'A':
      Alarm weekdayAlarm = new Alarm(tokens[1].equals("T"), Integer.parseInt(tokens[2]), Integer.parseInt(tokens[3]));
      Alarm weekendAlarm = new Alarm(tokens[4].equals("T"), Integer.parseInt(tokens[5]), Integer.parseInt(tokens[6]));
      pagerAdapter.getAlarmFragment().setAlarms(weekdayAlarm, weekendAlarm);
      break;
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
  }

  @Override
  public void onPause() {
    super.onPause();
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

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {}

  @Override
  public void onTabReselected(Tab tab, FragmentTransaction ft) {}

  @Override
  public void onTabSelected(Tab tab, FragmentTransaction ft) {
    viewPager.setCurrentItem(tab.getPosition());
  }

  @Override
  public void onTabUnselected(Tab tab, FragmentTransaction ft) {}

}