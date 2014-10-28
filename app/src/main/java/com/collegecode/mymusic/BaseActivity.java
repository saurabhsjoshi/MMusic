package com.collegecode.mymusic;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

/**
 * Created by saurabh on 14-10-26.
 */
public abstract class BaseActivity extends ActionBarActivity {
    private Toolbar toolbar;
    private static PlayBackService playBackService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource());
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (toolbar != null) {
            toolbar.setTitleTextColor(Color.WHITE);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        }
    }

    protected static void setPlayBackService(PlayBackService newPlayBackService){
        playBackService = newPlayBackService;
    }

    public static PlayBackService getPlayBackService()
    {
        return playBackService;
    }

    protected abstract int getLayoutResource();

    protected void setActionBarIcon(int iconRes) {
        toolbar.setNavigationIcon(iconRes);
    }
}
