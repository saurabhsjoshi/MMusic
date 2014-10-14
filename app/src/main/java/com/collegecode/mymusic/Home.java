package com.collegecode.mymusic;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
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
import com.collegecode.mymusic.objects.Constants;
import com.collegecode.mymusic.objects.SlidingTabs.SlidingTabLayout;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.List;


public class Home extends ActionBarActivity {

    private SlidingUpPanelLayout mLayout;
    FragmentTransaction transaction;
    SharedPreferences preferences;
    private NowPlayingSmallFragment nowPlayingFragmentInstance;


    public PlayBackService playBackService;
    private Intent playIntent;
    public ParseObject cur_playing;
    public boolean isPlaying = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
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

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Music");
        query.orderByAscending("Title");
        query.fromLocalDatastore();
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> lst_songs, ParseException e) {
                cur_playing = lst_songs.get(0);
                updateSmallPlayer();
            }
        });
    }

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayBackService.MusicBinder binder = (PlayBackService.MusicBinder)service;
            //get service
            playBackService = binder.getService();
            playBackService.playSong(cur_playing);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private void updateSmallPlayer(){
        try{
            nowPlayingFragmentInstance = (NowPlayingSmallFragment)getSupportFragmentManager().findFragmentById(R.id.frm_nowPlaying);
            nowPlayingFragmentInstance.setUI();
        }
        catch(Exception ignore){}
    }

    public void startSong(){
        if(playIntent==null){
            playIntent = new Intent(this, PlayBackService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            playIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
            startService(playIntent);
        }
        else{
            unbindService(musicConnection);
            stopService(playIntent);
            playIntent = new Intent(this, PlayBackService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            playIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
            startService(playIntent);
        }
        isPlaying = true;
        updateSmallPlayer();

    }

    public void pauseMusic(){
        if(playBackService.isPreparing)
            playBackService.startAfterPrepare = false;
        else
            playBackService.pauseSong();

        isPlaying = false;
        updateSmallPlayer();

    }

    public void playMusic(){
        if(playBackService == null)
            startSong();
        else{
            if(!playBackService.startAfterPrepare)
                playBackService.startAfterPrepare = true;

            playBackService.playSong();
        }
        isPlaying = true;
        updateSmallPlayer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    protected void onStop() {
        if(playBackService!=null)
            playBackService.showNotification(cur_playing);
        super.onStop();
    }

    @Override
    protected void onPause() {
        if(playBackService!=null && isPlaying)
            playBackService.showNotification(cur_playing);
        super.onPause();
    }

    @Override
    protected void onResume() {
        if(playBackService!=null){
            playBackService.stopNotification();
            if(playBackService.isPlaying()){
                isPlaying = true;
                cur_playing = playBackService.getCurSong();
            }
            else
                isPlaying = false;
            updateSmallPlayer();
        }

        super.onResume();
    }


    @Override
    protected void onDestroy() {
        if(playIntent != null){
            unbindService(musicConnection);
            stopService(playIntent);
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mLayout != null && mLayout.isPanelExpanded() || mLayout.isPanelAnchored())
            mLayout.collapsePanel();
        else
            this.moveTaskToBack(true);
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
