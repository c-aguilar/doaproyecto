package com.example.mz23zx.deltaerpddrapk;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class PagerAdapter extends FragmentPagerAdapter {
    int tab_count;

    public PagerAdapter(FragmentManager fm, int tab_count) {
        super(fm);
        this.tab_count = tab_count;
    }

    @Override
    public Fragment getItem(int i) {
        switch (i){
            case 0:
                return new Receiving();
            case 1:
                return new Supermarket();
            case 2:
                return new Routes();
            case 3:
                return new Conduits();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tab_count;
    }
}
