package com.tmikoss.torchmaster;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
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
  private final BluetoothAdapter btAdapter;
  private final List<String>     commandQueue;
  private ConnectThread          connectThread;
  private CommunicateThread      communicateThread;
  private final String           deviceName;
  private final Handler          handler;
  private int                    connectionAttempts             = 0;
  private final static int       maxConnectionAttempts          = 10;

  private final MainActivity     context;

  public BluetoothCommunicator(Activity _context, String deviceName) {
    this.context = (MainActivity) _context;
    this.btAdapter = BluetoothAdapter.getDefaultAdapter();
    this.commandQueue = Collections.synchronizedList(new LinkedList<String>());
    this.deviceName = deviceName;
    this.handler = new Handler();
  };

  public synchronized void attemptConnection() {
    if (!btAdapter.isEnabled()) {
      Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      context.startActivityForResult(enableBluetooth, activityResultBluetoothEnabled);
    } else {
      connectThread = new ConnectThread(btAdapter, deviceName);
      connectThread.start();
      syncDeviceTime();
    }
  }

  private void connected(BluetoothSocket btSocket) {
    communicateThread = new CommunicateThread(btSocket, commandQueue);
    communicateThread.start();
  }

  public void disconnect() {
    synchronized (commandQueue) {
      commandQueue.clear();
    }
    if (communicateThread != null) {
      communicateThread.close();
    }
  }

  public void sendMessage(String message) {
    synchronized (commandQueue) {
      commandQueue.add(message + "\n");
    }
  }

  public void queryStatus() {
    sendMessage("S");
  }

  private void messageReceived(final String message) {
    handler.post(new Runnable() {
      @Override
      public void run() {
        Log.d("received", message);
        context.receiveMessage(message);
      }
    });
  }

  private void toast(final String message) {
    handler.post(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
      }
    });
  }

  private void syncDeviceTime() {
    long utcTime = System.currentTimeMillis();
    int offset = TimeZone.getDefault().getRawOffset();
    sendMessage("T-" + Long.toString((utcTime + offset) / 1000));
  }

  private class ConnectThread extends Thread {
    private final BluetoothAdapter btAdapter;
    private final String           deviceName;
    private BluetoothDevice        btDevice;
    private BluetoothSocket        btSocket;
    private final UUID             uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    public ConnectThread(BluetoothAdapter btAdapter, String deviceName) {
      this.btAdapter = btAdapter;
      this.deviceName = deviceName;
      connectionAttempts += 1;
    }

    private void onError(String message) {
      synchronized (BluetoothCommunicator.this) {
        connectThread = null;
        if (connectionAttempts < maxConnectionAttempts) {
          attemptConnection();

        } else {
          toast(message);
        }
      }
    }

    @Override
    public void run() {
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
        onError("No device found");
        return;
      }

      btAdapter.cancelDiscovery();

      try {
        btSocket = btDevice.createInsecureRfcommSocketToServiceRecord(uuid);
        btSocket.connect();
      } catch (IOException e) {
        onError("Can't connect");
        return;
      }

      if (btSocket.isConnected()) {
        connected(btSocket);
      } else {
        onError("Did not connect");
      }
    }
  }

  private class CommunicateThread extends Thread {
    private OutputStream          btOutputStream;
    private InputStream           btInputStream;
    private final BluetoothSocket btSocket;
    private final List<String>    commandQueue;
    private final byte[]          readBuffer         = new byte[1024];
    private int                   readBufferPosition = 0;
    private final byte            delimiter          = 10;

    public CommunicateThread(BluetoothSocket btSocket, List<String> commandQueue) {
      this.btSocket = btSocket;
      this.commandQueue = commandQueue;
    }

    private void onError(String message) {
      toast(message);
      synchronized (BluetoothCommunicator.this) {
        communicateThread = null;
      }
    }

    @Override
    public void run() {
      try {
        this.btInputStream = btSocket.getInputStream();
        this.btOutputStream = btSocket.getOutputStream();
      } catch (IOException e) {
        e.printStackTrace();
        onError("Error getting I/O streams");
        return;
      }

      while (!Thread.currentThread().isInterrupted() && btSocket.isConnected()) {
        try {
          sendMessage();
          receiveMessages();
        } catch (IOException e) {
          e.printStackTrace();
          onError("Error handling message");
        }
      }

    }

    public void close() {
      try {
        if (btInputStream != null) {
          btInputStream.close();
        }
        if (btOutputStream != null) {
          btOutputStream.close();
        }

        btSocket.close();

        synchronized (BluetoothCommunicator.this) {
          communicateThread = null;
        }
      } catch (IOException e) {
        e.printStackTrace();
        onError("Error dropping connection");
      }
    }

    void sendMessage() throws IOException {
      synchronized (commandQueue) {
        if (!commandQueue.isEmpty()) {
          String message = commandQueue.remove(0);
          Log.d("sending", message);
          btOutputStream.write(message.getBytes());
        }
      }
    }

    void receiveMessages() throws IOException {
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
            messageReceived(data.trim());
          } else {
            readBuffer[readBufferPosition++] = b;
          }
        }
      }
    }

  }

}
