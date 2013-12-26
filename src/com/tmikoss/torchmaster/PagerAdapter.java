package com.tmikoss.torchmaster;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class PagerAdapter extends FragmentPagerAdapter {
  private ColorFragment colorFragment;
  private AlarmFragment alarmFragment;

  public PagerAdapter(FragmentManager fm) {
    super(fm);
  }

  public ColorFragment getColorFragment() {
    if (colorFragment == null) {
      colorFragment = new ColorFragment();
    }
    return colorFragment;
  }

  public AlarmFragment getAlarmFragment() {
    if (alarmFragment == null) {
      alarmFragment = new AlarmFragment();
    }
    return alarmFragment;
  }

  @Override
  public Fragment getItem(int i) {
    switch (i) {
    case 0:
      return getColorFragment();
    case 1:
      return getAlarmFragment();
    default:
      return new Fragment();
    }
  }

  @Override
  public int getCount() {
    return 2;
  }

  @Override
  public CharSequence getPageTitle(int position) {
    switch (position) {
    case 0:
      return "Color";
    case 1:
      return "Alarm";
    default:
      return "";
    }
  }
}
