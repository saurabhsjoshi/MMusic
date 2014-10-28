package com.collegecode.mymusic.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.collegecode.mymusic.fragments.AlbumsFragment;
import com.collegecode.mymusic.fragments.SongsFragment;

/**
 * Created by saurabh on 14-09-27.
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {
    String titles[] = {"    Albums    ","    Songs    ","    Exclusive    " };
    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        if(i == 0)
            return new AlbumsFragment();
        else if (i == 2)
            return  new Fragment();
        else
            return new SongsFragment();
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }


}
