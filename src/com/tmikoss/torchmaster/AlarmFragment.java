package com.tmikoss.torchmaster;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

public class AlarmFragment extends Fragment {
  private BluetoothCommunicator btCommunicator;

  private TextView              weekdayAlarmText;
  private TextView              weekendAlarmText;
  private Switch                weekdayAlarmSwitch;
  private Switch                weekendAlarmSwitch;

  private final Alarm           weekdayAlarm = new Alarm(false, 6, 30);
  private final Alarm           weekendAlarm = new Alarm(false, 9, 30);

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_alarm, container, false);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    weekdayAlarmText = (TextView) getView().findViewById(R.id.textWeekdayAlarm);
    weekendAlarmText = (TextView) getView().findViewById(R.id.textWeekendAlarm);
    weekdayAlarmSwitch = (Switch) getView().findViewById(R.id.switchWeekdayAlarm);
    weekendAlarmSwitch = (Switch) getView().findViewById(R.id.switchWeekendAlarm);

    weekdayAlarmText.setOnClickListener(new TextView.OnClickListener() {
      @Override
      public void onClick(View v) {
        AlarmTimePicker timePicker = new AlarmTimePicker(weekdayAlarm);
        timePicker.show(getFragmentManager(), "weekdayAlarmTimePicker");
      }
    });

    weekendAlarmText.setOnClickListener(new TextView.OnClickListener() {
      @Override
      public void onClick(View v) {
        AlarmTimePicker timePicker = new AlarmTimePicker(weekendAlarm);
        timePicker.show(getFragmentManager(), "weekendAlarmTimePicker");
      }
    });

    weekdayAlarmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton view, boolean checked) {
        weekdayAlarm.enabled = checked;
        broadcastAlarms();
      }
    });

    weekendAlarmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton view, boolean checked) {
        weekendAlarm.enabled = checked;
        broadcastAlarms();
      }
    });

    btCommunicator = ((MainActivity) getActivity()).getCommunicator();
    btCommunicator.queryStatus();

    updateUI();
  }

  private void updateUI() {
    weekdayAlarmText.setText(Integer.toString(weekdayAlarm.hour) + ":" + Integer.toString(weekdayAlarm.minute));
    weekendAlarmText.setText(Integer.toString(weekendAlarm.hour) + ":" + Integer.toString(weekendAlarm.minute));
    weekdayAlarmSwitch.setChecked(weekdayAlarm.enabled);
    weekendAlarmSwitch.setChecked(weekendAlarm.enabled);
  }

  public void setAlarms(Alarm weekdayAlarm, Alarm weekendAlarm) {
    this.weekdayAlarm.update(weekdayAlarm);
    this.weekendAlarm.update(weekendAlarm);
    updateUI();
  }

  private void broadcastAlarms() {
    btCommunicator.sendMessage("A-" + (weekdayAlarm.enabled == true ? 'T' : 'F') + "-" + Integer.toString(weekdayAlarm.hour) + "-"
        + Integer.toString(weekdayAlarm.minute) + "-" + (weekendAlarm.enabled == true ? 'T' : 'F') + "-"
        + Integer.toString(weekendAlarm.hour) + "-" + Integer.toString(weekendAlarm.minute));
    updateUI();
  }

  @SuppressLint("ValidFragment")
  public class AlarmTimePicker extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    private final Alarm alarm;

    public AlarmTimePicker(Alarm alarm) {
      this.alarm = alarm;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      return new TimePickerDialog(getActivity(), this, alarm.hour, alarm.minute, DateFormat.is24HourFormat(getActivity()));
    }

    @Override
    public void onTimeSet(TimePicker view, int hour, int minute) {
      alarm.hour = hour;
      alarm.minute = minute;
      broadcastAlarms();
    }
  }
}