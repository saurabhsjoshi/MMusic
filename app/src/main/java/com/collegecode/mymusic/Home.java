package com.collegecode.mymusic;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.collegecode.mymusic.objects.STATES;
import com.collegecode.mymusic.objects.SlidingTabs.SlidingTabLayout;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.List;


public class Home extends ActionBarActivity {

    public SlidingUpPanelLayout mLayout;
    FragmentTransaction transaction;
    SharedPreferences preferences;

    private BroadcastReceiver receiver;

    public PlayBackService playBackService;
    private Intent playIntent;

    private ProgressDialog progressDialog;

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayBackService.MusicBinder binder = (PlayBackService.MusicBinder)service;
            playBackService = binder.getService();

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Music");
            query.orderByAscending("Title");
            query.fromLocalDatastore();
            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> lst_songs, ParseException e) {
                    playBackService.setList(new ArrayList<ParseObject>(lst_songs), 0);
                    progressDialog.dismiss();
                    loadUI();
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        boolean is_active = true;
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if(preferences.getBoolean("newUser", true)) {
            is_active = false;
            startActivity(new Intent(this, NewUser.class).addFlags(IntentCompat.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
            overridePendingTransition(R.anim.enter, R.anim.exit);
            finish();
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.collegecode.playbackservice.changed");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateSmallPlayer();
                updateBigPlayer();
            }
        };
        registerReceiver(receiver, filter);

        getSupportActionBar().setIcon(R.drawable.ic_home);

        if(is_active) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Loading...");
            progressDialog.setCancelable(true);
            progressDialog.show();
            connectToService();
        }

        ViewPager mViewPager;
        SlidingTabLayout mSlidingTabLayout;

        final FragmentManager fragmentManager = getSupportFragmentManager();

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
                try{
                    ((NowPlayingFragment) getSupportFragmentManager().findFragmentById(R.id.frm_nowPlaying)).stopThread();
                }catch (Exception ignore){}
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
            public void onPanelAnchored(View view) {
                updateSmallPlayer();
            }

            @Override
            public void onPanelHidden(View view) {}
        });
        transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frm_nowPlaying, new NowPlayingSmallFragment());
        transaction.commit();

        mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return Color.rgb(0, 153, 204);
            }

            @Override
            public int getDividerColor(int position) {
                return Color.LTGRAY;
            }

        });
    }

    private void loadUI(){
        updateSmallPlayer();
    }

    public void connectToService() {
        if (playIntent == null) {
            playIntent = new Intent(this, PlayBackService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            playIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
            startService(playIntent);
        }
    }

    public void updateBigPlayer(){
        try{
            NowPlayingFragment nowPlayingFragment;
            nowPlayingFragment = (NowPlayingFragment)getSupportFragmentManager().findFragmentById(R.id.frm_nowPlaying);
            nowPlayingFragment.setUI();

        }
        catch(Exception ignore){}
    }

    public void updateSmallPlayer(){
        try{
            NowPlayingSmallFragment nowPlayingFragmentInstance;
            nowPlayingFragmentInstance = (NowPlayingSmallFragment)getSupportFragmentManager().findFragmentById(R.id.frm_nowPlaying);
            nowPlayingFragmentInstance.setUI();

        }
        catch(Exception ignore){}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    protected void onStop() {
        if(playBackService!=null && playBackService.state == STATES.PLAYING)
            playBackService.showNotification();
        super.onStop();
    }

    @Override
    protected void onPause() {
        if(playBackService!=null
                &&((playBackService.state == STATES.PREPARING
                && playBackService.startAfterPrepare)
                || playBackService.state == STATES.PLAYING))
            playBackService.showNotification();
        super.onPause();
    }

    @Override
    protected void onResume() {
        if(playBackService!=null){
            playBackService.stopNotification();
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
        unregisterReceiver(receiver);
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
