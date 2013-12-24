package com.tmikoss.torchmaster;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;

public class BluetoothCommunicator {
  public final static int        activityResultBluetoothEnabled = 10;
  private final BluetoothAdapter btAdapter;
  private BluetoothSocket        btSocket;
  private BluetoothDevice        btDevice;
  private OutputStream           btOutputStream;
  private InputStream            btInputStream;
  private Thread                 listenerThread;

  private final MainActivity     context;
  private final String           deviceName;
  private final UUID             uuid;

  private boolean                isConnected                    = false;

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
      bluetoothEnabled();
    }
  }

  void bluetoothEnabled() {
    Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
    if (pairedDevices.size() > 0) {
      for (BluetoothDevice device : pairedDevices) {
        if (device.getName().equals(deviceName)) {
          btDevice = device;
          break;
        }
      }
    }

    try {
      btSocket = btDevice.createRfcommSocketToServiceRecord(uuid);
      btSocket.connect();
      btOutputStream = btSocket.getOutputStream();
      btInputStream = btSocket.getInputStream();
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }

    isConnected = true;

    receiveMessages();
  }

  boolean sendMessage(String message) {
    if (!isConnected) { return false; }

    message = message + "\n";
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
          while (!Thread.currentThread().isInterrupted()) {
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
                      context.receiveMessage(data);
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
