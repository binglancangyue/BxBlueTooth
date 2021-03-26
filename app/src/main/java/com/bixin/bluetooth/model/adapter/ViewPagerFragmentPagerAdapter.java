package com.bixin.bluetooth.model.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;

public class ViewPagerFragmentPagerAdapter extends FragmentPagerAdapter {
    private ArrayList<Fragment> mFragments;

    public ViewPagerFragmentPagerAdapter(@NonNull FragmentManager fm, int behavior, ArrayList<Fragment> fragments) {
        super(fm, behavior);
        this.mFragments = fragments;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        if (mFragments.size() == 0) {
            return 0;
        }
        return this.mFragments.size();
    }
}
