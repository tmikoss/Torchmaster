package com.tmikoss.torchmaster;

import java.util.Random;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.ColorPicker.OnColorChangedListener;

public class ColorFragment extends Fragment implements OnColorChangedListener {
  private BluetoothCommunicator btCommunicator;

  private ColorPicker           colorPicker;
  private SeekBar               opacityBar;

  private long                  lastRandomAt = 0;
  private final Random          rng          = new Random();
  private final int[]           randomColors = new int[] { Color.GREEN, Color.BLUE, Color.CYAN, Color.MAGENTA, Color.RED, Color.YELLOW };

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_color, container, false);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    colorPicker = (ColorPicker) getView().findViewById(R.id.colorPicker);
    colorPicker.setOnColorChangedListener(this);

    opacityBar = (SeekBar) getView().findViewById(R.id.opacityBar);
    opacityBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        btCommunicator.sendMessage("O-" + Integer.toString(progress));
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {}

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {}
    });

    btCommunicator = ((MainActivity) getActivity()).getCommunicator();
    btCommunicator.queryStatus();
  }

  public void setDisplayedColor(int color) {
    if (colorPicker != null) {
      colorPicker.setColor(color);
      colorPicker.setOldCenterColor(color);
    }
  }

  public void setDisplayedOpacity(int opacity) {
    if (opacityBar != null) {
      opacityBar.setProgress(opacity);
    }
  }

  @Override
  public void onColorChanged(int color) {
    setDisplayedColor(color);
    btCommunicator.sendMessage("C-" + Integer.toString(Color.red(color)) + "-" + Integer.toString(Color.green(color)) + "-"
        + Integer.toString(Color.blue(color)));
  }

  public void setRandomColor() {
    if (System.currentTimeMillis() - lastRandomAt < 2000) return;
    int index = rng.nextInt(randomColors.length);
    onColorChanged(randomColors[index]);
    lastRandomAt = System.currentTimeMillis();
  }
}