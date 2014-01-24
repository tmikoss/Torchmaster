package com.tmikoss.torchmaster;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TimePicker;

public class AlarmFragment extends Fragment {
  private BluetoothCommunicator btCommunicator;

  private TimePicker            weekdayAlarmPicker;
  private TimePicker            weekendAlarmPicker;
  private Switch                weekdayAlarmSwitch;
  private Switch                weekendAlarmSwitch;

  private Alarm                 weekdayAlarm;
  private Alarm                 weekendAlarm;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_alarm, container, false);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    weekdayAlarmPicker = (TimePicker) getView().findViewById(R.id.pickerWeekdayAlarm);
    weekdayAlarmPicker.setIs24HourView(true);
    weekdayAlarmPicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
      @Override
      public void onTimeChanged(TimePicker view, int hour, int minute) {
        weekdayAlarm.hour = hour;
        weekdayAlarm.minute = minute;
        broadcastAlarms();
      }
    });

    weekendAlarmPicker = (TimePicker) getView().findViewById(R.id.pickerWeekendAlarm);
    weekendAlarmPicker.setIs24HourView(true);
    weekendAlarmPicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
      @Override
      public void onTimeChanged(TimePicker view, int hour, int minute) {
        weekendAlarm.hour = hour;
        weekendAlarm.minute = minute;
        broadcastAlarms();
      }
    });

    weekdayAlarmSwitch = (Switch) getView().findViewById(R.id.switchWeekdayAlarm);
    weekdayAlarmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton view, boolean checked) {
        weekdayAlarm.enabled = checked;
        updateUI();
        broadcastAlarms();
      }
    });

    weekendAlarmSwitch = (Switch) getView().findViewById(R.id.switchWeekendAlarm);
    weekendAlarmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton view, boolean checked) {
        weekendAlarm.enabled = checked;
        updateUI();
        broadcastAlarms();
      }
    });

    btCommunicator = ((MainActivity) getActivity()).getCommunicator();
    btCommunicator.queryStatus();

    setAlarms(new Alarm(false, 6, 30), new Alarm(false, 9, 30));
  }

  private void updateValues() {
    weekdayAlarmPicker.setCurrentHour(weekdayAlarm.hour);
    weekdayAlarmPicker.setCurrentHour(weekdayAlarm.minute);
    weekendAlarmPicker.setCurrentHour(weekendAlarm.hour);
    weekendAlarmPicker.setCurrentHour(weekendAlarm.minute);
    weekdayAlarmSwitch.setChecked(weekdayAlarm.enabled);
    weekendAlarmSwitch.setChecked(weekendAlarm.enabled);

    updateUI();
  }

  private void updateUI() {
    weekdayAlarmPicker.setEnabled(weekdayAlarm.enabled);
    weekendAlarmPicker.setEnabled(weekendAlarm.enabled);
  }

  public void setAlarms(Alarm weekdayAlarm, Alarm weekendAlarm) {
    this.weekdayAlarm = weekdayAlarm;
    this.weekendAlarm = weekendAlarm;

    updateValues();
  }

  private void broadcastAlarms() {
    btCommunicator.sendMessage("A-" + (weekdayAlarm.enabled == true ? 'T' : 'F') + "-" + Integer.toString(weekdayAlarm.hour) + "-"
        + Integer.toString(weekdayAlarm.minute) + "-" + (weekendAlarm.enabled == true ? 'T' : 'F') + "-"
        + Integer.toString(weekendAlarm.hour) + "-" + Integer.toString(weekendAlarm.minute));
  }
}