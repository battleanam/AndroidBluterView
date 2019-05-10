package com.hanayue.ayuemobieview.splash;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

public class SplashPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> fragments;

    public SplashPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public SplashPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public Fragment getItem(int i) {
        return fragments.get(i);
    }

}
