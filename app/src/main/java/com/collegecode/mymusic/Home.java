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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.IntentCompat;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.collegecode.mymusic.adapters.ViewPagerAdapter;
import com.collegecode.mymusic.fragments.NowPlayingFragment;
import com.collegecode.mymusic.fragments.NowPlayingSmallFragment;
import com.collegecode.mymusic.objects.Constants;
import com.collegecode.mymusic.objects.STATES;
import com.collegecode.mymusic.objects.SlidingTabs.SlidingTabLayout;
import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.List;


public class Home extends BaseActivity {



    public SlidingUpPanelLayout mLayout;
    FragmentTransaction transaction;
    SharedPreferences preferences;
    Context context;
    ViewPager mViewPager;
    private int CUR_SONGS_NUM = 0;

    private BroadcastReceiver receiver;

    public PlayBackService playBackService;
    private Intent playIntent;

    private ProgressDialog progressDialog;

    boolean is_active = true;

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
                    if(e == null){
                        playBackService.setList(new ArrayList<ParseObject>(lst_songs), 0);
                        CUR_SONGS_NUM = lst_songs.size();
                        progressDialog.dismiss();
                        setPlayBackService(playBackService);
                        loadUI();
                        checkForUpdate();
                    }
                    else
                        Toast.makeText(context, "Error loading database!",Toast.LENGTH_LONG).show();
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private void checkForUpdate(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Music");
        query.countInBackground(new CountCallback() {
            @Override
            public void done(int i, ParseException e) {
                if(e == null){
                    if(CUR_SONGS_NUM != i){
                        if(is_active)
                            new updateDatabase().execute();
                    }
                }
            }
        });
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_home;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBarIcon(R.drawable.ic_home);

        context = this;
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if(preferences.getBoolean("newUser", true)) {
            is_active = false;
            startActivity(new Intent(this, NewUser.class).addFlags(IntentCompat.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
            overridePendingTransition(R.anim.enter, R.anim.exit);
            finish();
        }

        //On Song change
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

        if(is_active) {
            if(getPlayBackService() == null){
                progressDialog = new ProgressDialog(this);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Loading...");
                progressDialog.setCancelable(true);
                progressDialog.show();
                connectToService();
            }
            else
                playBackService = getPlayBackService();

        }


        final SlidingTabLayout mSlidingTabLayout;

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

        mSlidingTabLayout.setSelectedIndicatorColors(Color.GRAY);


        mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return Color.rgb(229,229,229);
            }

            @Override
            public int getDividerColor(int position) {
                return Color.TRANSPARENT;
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
        if(playBackService != null
                &&((playBackService.state == STATES.PREPARING
                && playBackService.startAfterPrepare)
                || playBackService.state == STATES.PLAYING))
            playBackService.showNotification();

        super.onPause();
    }

    @Override
    protected void onResume() {
        if(getPlayBackService() != null){
            if(playBackService == null)
                playBackService = getPlayBackService();

            playBackService.stopNotification();
            updateSmallPlayer();
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if(playIntent != null){
            unbindService(musicConnection);
            playBackService.stopAll();
            setPlayBackService(null);
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

    private class updateDatabase extends AsyncTask<Void,Void,Void>{
        ProgressDialog dialog;
        boolean success = false;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(context);
            dialog.setMessage("Updating database...");
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if(loadDatabase("Albums"))
                if(loadDatabase("Music"))
                    success = true;

            return null;
        }

        private boolean loadDatabase(final String table){
            ParseQuery<ParseObject> query = ParseQuery.getQuery(table);
            try
            {
                ParseObject.unpinAll(table);
                ParseObject.pinAll(table, query.find());
            }catch (Exception ignore){return false;}
            return true;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
            if(success){
                Toast.makeText(context, "Updated music database!", Toast.LENGTH_LONG).show();
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Music");
                query.orderByAscending("Title");
                query.fromLocalDatastore();
                query.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> lst_songs, ParseException e) {
                        if(e == null){
                            playBackService.setList(new ArrayList<ParseObject>(lst_songs), 0);
                            CUR_SONGS_NUM = lst_songs.size();
                            progressDialog.dismiss();

                            final FragmentManager fragmentManager = getSupportFragmentManager();
                            mViewPager = (ViewPager) findViewById(R.id.viewpager);
                            mViewPager.setAdapter(new ViewPagerAdapter(fragmentManager));

                            checkForUpdate();
                        }
                        else
                            Toast.makeText(context, "Error loading database!",Toast.LENGTH_LONG).show();
                    }
                });
            }

            else
                Toast.makeText(getApplicationContext(), "Error occured while updating database!", Toast.LENGTH_LONG).show();
        }
    }
}
