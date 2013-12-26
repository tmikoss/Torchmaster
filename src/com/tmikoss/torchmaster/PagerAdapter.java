package com.tmikoss.torchmaster;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class PagerAdapter extends FragmentPagerAdapter {
  private ColorFragment colorFragment;
  private AlarmFragment alarmFragment;
  private final Context context;

  public PagerAdapter(FragmentManager fm, Context context) {
    super(fm);
    this.context = context;
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
      return context.getString(R.string.colorFragment_title);
    case 1:
      return context.getString(R.string.alarmFragment_title);
    default:
      return "";
    }
  }
}
