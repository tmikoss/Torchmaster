package com.tmikoss.torchmaster;

public class Alarm {
  public boolean enabled;
  public int     hour;
  public int     minute;

  public Alarm(boolean enabled, int hour, int minute) {
    this.enabled = enabled;
    this.hour = hour;
    this.minute = minute;
  }

  public void update(Alarm other) {
    this.enabled = other.enabled;
    this.hour = other.hour;
    this.minute = other.minute;
  }
}
