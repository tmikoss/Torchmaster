package com.tmikoss.torchmaster;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class BluetoothCommunicator {
  public final static int        activityResultBluetoothEnabled = 10;
  private static final String    TAG                            = "bt";
  private final BluetoothAdapter btAdapter;
  private BluetoothSocket        btSocket;
  private BluetoothDevice        btDevice;
  private OutputStream           btOutputStream;
  private InputStream            btInputStream;
  private Thread                 listenerThread;

  private final MainActivity     context;
  private final String           deviceName;
  private final UUID             uuid;

  public BluetoothCommunicator(Activity context, String deviceName) {
    this.context = (MainActivity) context;
    this.deviceName = deviceName;
    this.uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    this.btAdapter = BluetoothAdapter.getDefaultAdapter();
  };

  void enableBluetooth() {
    if (!btAdapter.isEnabled()) {
      Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      context.startActivityForResult(enableBluetooth, activityResultBluetoothEnabled);
    } else {
      establishConnection();
    }
  }

  boolean isConnected() {
    if (btSocket == null) return false;
    return btSocket.isConnected();
  }

  void establishConnection() {
    Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
    if (pairedDevices.size() > 0) {
      for (BluetoothDevice device : pairedDevices) {
        if (device.getName().equals(deviceName)) {
          btDevice = device;
          break;
        }
      }
    }
    if (btDevice == null) {
      Toast.makeText(context, "Device not found", Toast.LENGTH_LONG).show();
      return;
    }

    btAdapter.cancelDiscovery();

    try {
      btSocket = btDevice.createInsecureRfcommSocketToServiceRecord(uuid);
      btSocket.connect();
    } catch (IOException ecreate) {
      Toast.makeText(context, "Error establishing connection", Toast.LENGTH_LONG).show();
      return;
    }

    if (!isConnected()) {
      Toast.makeText(context, "Could not establish connection", Toast.LENGTH_LONG).show();
      return;
    }

    try {
      btInputStream = btSocket.getInputStream();
      btOutputStream = btSocket.getOutputStream();
    } catch (IOException e_getin) {
      Toast.makeText(context, "Error getting I/O streams", Toast.LENGTH_LONG).show();
      return;
    }

    receiveMessages();
    queryStatus();
    syncTime();
  }

  void queryStatus() {
    sendMessage("S");
  }

  void syncTime() {
    long utcTime = System.currentTimeMillis();
    int offset = TimeZone.getDefault().getRawOffset();

    sendMessage("T-" + Long.toString((utcTime + offset) / 1000));
  }

  void dropConnection() {
    if (isConnected()) {
      try {
        btInputStream.close();
        btOutputStream.close();
        btSocket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  boolean sendMessage(String message) {
    if (!isConnected()) { return false; }

    message = message + "\n";
    Log.d("bt out", message);
    try {
      btOutputStream.write(message.getBytes());
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }

    return true;
  }

  void receiveMessages() {
    final Handler handler = new Handler();
    final byte delimiter = 10;

    listenerThread = new Thread(new Runnable() {
      private byte[] readBuffer;
      private int    readBufferPosition;

      @Override
      public void run() {
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        try {
          while (!Thread.currentThread().isInterrupted() && isConnected()) {
            int bytesAvailable = btInputStream.available();
            if (bytesAvailable > 0) {
              byte[] packetBytes = new byte[bytesAvailable];
              btInputStream.read(packetBytes);
              for (int i = 0; i < bytesAvailable; i++) {
                byte b = packetBytes[i];
                if (b == delimiter) {
                  byte[] encodedBytes = new byte[readBufferPosition];
                  System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                  final String data = new String(encodedBytes, "US-ASCII");
                  readBufferPosition = 0;
                  handler.post(new Runnable() {
                    @Override
                    public void run() {
                      Log.d("bt in", data.trim());
                      context.receiveMessage(data.trim());
                    }
                  });
                } else {
                  readBuffer[readBufferPosition++] = b;
                }
              }
            }

          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });

    listenerThread.start();
  }
}
