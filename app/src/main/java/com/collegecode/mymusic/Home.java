package com.collegecode.mymusic;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.IntentCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.collegecode.mymusic.adapters.ViewPagerAdapter;
import com.collegecode.mymusic.fragments.NowPlayingFragment;
import com.collegecode.mymusic.fragments.NowPlayingSmallFragment;
import com.collegecode.mymusic.objects.SlidingTabs.SlidingTabLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;


public class Home extends ActionBarActivity {

    private SlidingUpPanelLayout mLayout;
    FragmentTransaction transaction;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if(preferences.getBoolean("newUser", true)) {
            startActivity(new Intent(this, NewUser.class).addFlags(IntentCompat.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
            overridePendingTransition(R.anim.enter, R.anim.exit);
        }
        getSupportActionBar().setIcon(R.drawable.ic_home);

        ViewPager mViewPager;
        SlidingTabLayout mSlidingTabLayout;

        FragmentManager fragmentManager = getSupportFragmentManager();

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(new ViewPagerAdapter(fragmentManager));
        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(mViewPager);

        mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View view, float v) {}

            @Override
            public void onPanelCollapsed(View view) {
                System.out.println("ANCHORED");
                transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.frm_nowPlaying, new NowPlayingSmallFragment());
                transaction.commit();
            }

            @Override
            public void onPanelExpanded(View view) {
                transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.frm_nowPlaying, new NowPlayingFragment());
                transaction.commit();
            }

            @Override
            public void onPanelAnchored(View view) {}

            @Override
            public void onPanelHidden(View view) {}
        });
        transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frm_nowPlaying, new NowPlayingSmallFragment());
        transaction.commit();

        mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return Color.rgb(0,153,204);
            }

            @Override
            public int getDividerColor(int position) {
                return Color.LTGRAY;
            }

        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mLayout != null && mLayout.isPanelExpanded() || mLayout.isPanelAnchored())
            mLayout.collapsePanel();
        else
            super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
