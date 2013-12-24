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

public class BluetoothCommunicator {
  public final static int activityResultBluetoothEnabled = 10;
  private final BluetoothAdapter btAdapter;
  private BluetoothSocket btSocket;
  private BluetoothDevice btDevice;
  private OutputStream btOutputStream;
  private InputStream btInputStream;

  private final Activity context;
  private final String deviceName;
  private final UUID uuid;

  private boolean isConnected = false;

  public BluetoothCommunicator(Activity context, String deviceName) {
    this.context = context;
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
  }

  boolean sendMessage(String message) {
    if (!isConnected) {
      return false;
    }

    message = message + "\n";
    try {
      btOutputStream.write(message.getBytes());
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }

    return true;
  }
}
