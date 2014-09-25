package com.tmikoss.torchmaster;

import java.util.Random;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.ColorPicker.OnColorChangedListener;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class ColorFragment extends Fragment implements OnColorChangedListener {
  private ColorPicker                   colorPicker;
  private SeekBar                       opacityBar;

  private long                          lastRandomAt    = 0;
  private final Random                  rng             = new Random();
  private final int[]                   randomColors    = new int[] { Color.GREEN, Color.BLUE, Color.CYAN, Color.MAGENTA, Color.RED,
      Color.YELLOW                                     };

  private final JsonHttpResponseHandler responseHandler = new JsonHttpResponseHandler() {
                                                          @Override
                                                          public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                                                            try {
                                                              setDisplayedOpacity(json.getInt("a"));
                                                              setDisplayedColor(Color.rgb(json.getInt("r"), json.getInt("g"),
                                                                  json.getInt("b")));
                                                            } catch (JSONException e) {
                                                              e.printStackTrace();
                                                            }
                                                          }

                                                          @Override
                                                          public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {}
                                                        };

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
        RequestParams params = new RequestParams();
        params.add("a", Integer.toString(progress));
        JSONClient.post(params);
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {}

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {}
    });

    JSONClient.get(responseHandler);

    Button buttonMin = (Button) getView().findViewById(R.id.buttonMin);
    buttonMin.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        RequestParams params = new RequestParams();
        params.add("a", "0");
        JSONClient.post(params);
        setDisplayedOpacity(0);
      }
    });

    Button buttonMax = (Button) getView().findViewById(R.id.buttonMax);
    buttonMax.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        RequestParams params = new RequestParams();
        params.add("a", "100");
        JSONClient.post(params);
        setDisplayedOpacity(100);
      }
    });
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
    RequestParams params = new RequestParams();
    params.add("r", Integer.toString(Color.red(color)));
    params.add("g", Integer.toString(Color.green(color)));
    params.add("b", Integer.toString(Color.blue(color)));
    JSONClient.post(params);

    setDisplayedColor(color);
  }

  public void setRandomColor() {
    if (System.currentTimeMillis() - lastRandomAt < 2000) return;
    int index = rng.nextInt(randomColors.length);
    onColorChanged(randomColors[index]);
    lastRandomAt = System.currentTimeMillis();
  }
}