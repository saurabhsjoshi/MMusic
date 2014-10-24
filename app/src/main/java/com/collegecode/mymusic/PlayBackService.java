package com.collegecode.mymusic;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.collegecode.mymusic.objects.Constants;
import com.collegecode.mymusic.objects.STATES;
import com.parse.ParseObject;

import java.util.ArrayList;

/**
 * Created by saurabh on 14-10-11.
 */
public class PlayBackService extends Service implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnBufferingUpdateListener
{

    NotificationManager mNotificationManager;

    //Check if foreground
    private boolean isForeground = false;

    //Current Status
    public STATES state = STATES.IDLE;

    //Currently Playing Song
    private ParseObject CUR_SONG;
    //Current Song List playing
    public ArrayList<ParseObject> CUR_SONG_LIST;
    //Current Index in the List
    public int CUR_INDEX;

    //Check if playing is cancelled
    public boolean startAfterPrepare = true;

    //MediaPlayer instance
    private MediaPlayer player;

    //Binder for Activity
    private final IBinder musicBind = new MusicBinder();

    //Logger
    private static final String LOG_TAG = "com.collegecode.playbackservice";

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try{
            if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
                player = new MediaPlayer(); // initialize it here
                mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                player.setOnPreparedListener(this);
                player.setOnErrorListener(this);
                player.setOnCompletionListener(this);
                player.setOnBufferingUpdateListener(this);
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                initMusicPlayer();
            }
            else if (intent.getAction().equals(Constants.ACTION.PREV_ACTION)) {
                prevSong();
            } else if (intent.getAction().equals(Constants.ACTION.PLAY_ACTION)) {
                if(state == STATES.PLAYING || state == STATES.PREPARING)
                    pauseSong();
                else
                    playSong();
                setUpAsForeground();
            } else if (intent.getAction().equals(Constants.ACTION.NEXT_ACTION)) {
                nextSong();
            } else if (intent.getAction().equals(
                    Constants.ACTION.STOPFOREGROUND_ACTION)) {
                Log.i(LOG_TAG, "Received Stop Foreground Intent");
                stopForeground(true);
                stopSelf();
            }
        }catch (Exception ignore){stopForeground(true);}
        return START_STICKY;
    }

    public void initMusicPlayer(){
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(
                AudioManager.STREAM_MUSIC);
    }

    public void playSong(){
        try
        {
            if(state == STATES.PAUSED)
            {
                player.start();
                state = STATES.PLAYING;
            }
            else{
                player.reset();
                CUR_SONG = CUR_SONG_LIST.get(CUR_INDEX);
                player.setDataSource(CUR_SONG.getString("url"));
                startAfterPrepare = true;
                state = STATES.PREPARING;
                player.prepareAsync();

                if(isForeground)
                    setUpAsForeground();
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void nextSong(){
        resetPlayer();
        ++CUR_INDEX;

        if(CUR_INDEX > CUR_SONG_LIST.size())
            CUR_INDEX = 0;
        playSong();
    }

    public void prevSong(){
        resetPlayer();
        --CUR_INDEX;

        if(CUR_INDEX < 0)
            CUR_INDEX = CUR_SONG_LIST.size() - 1;

        playSong();
    }

    public void resetPlayer(){
        player.reset();
        state = STATES.IDLE;
    }

    public void setList(ArrayList<ParseObject> songs, int index){
        this.CUR_SONG_LIST = songs;
        this.CUR_INDEX = index;
        CUR_SONG = CUR_SONG_LIST.get(CUR_INDEX);
    }
    public void showNotification(){
        setUpAsForeground();
        isForeground = true;
    }
    public void stopNotification(){
        stopForeground(true);
        isForeground = false;
    }

    public ParseObject getCurSong(){
        return CUR_SONG;
    }

    public void pauseSong(){
        if(state == STATES.PREPARING)
            startAfterPrepare = false;
        else
            player.pause();

        state = STATES.PAUSED;
    }

    public int getTotalTime() throws Exception{
        try{
            if(state == STATES.PLAYING || state == STATES.PAUSED)
                return player.getDuration();
            else
                throw new Exception("Preparing");
        }catch (Exception e){throw e;}
    }

    public int getCurrentTime(){
        return player.getCurrentPosition();
    }

    public void scrub(int pos){
        player.seekTo(pos);
    }

    public void stopAll(){
        player.reset();
        player.release();
        stopForeground(true);
    }

    @Override
    public boolean onUnbind(Intent intent){
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if((CUR_INDEX+1) < CUR_SONG_LIST.size())
            nextSong();
        sendBroadcast(new Intent("com.collegecode.playbackservice.changed"));
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        if(startAfterPrepare){
            player.start();
            state = STATES.PLAYING;
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    public class MusicBinder extends Binder {
        PlayBackService getService() {
            return PlayBackService.this;
        }
    }

    void setUpAsForeground() {
        Intent notificationIntent = new Intent(this, Home.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Intent previousIntent = new Intent(this, PlayBackService.class);
        previousIntent.setAction(Constants.ACTION.PREV_ACTION);
        PendingIntent ppreviousIntent = PendingIntent.getService(this, 0,
                previousIntent, 0);

        Intent playIntent = new Intent(this, PlayBackService.class);
        playIntent.setAction(Constants.ACTION.PLAY_ACTION);
        PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                playIntent, 0);

        Intent nextIntent = new Intent(this, PlayBackService.class);
        nextIntent.setAction(Constants.ACTION.NEXT_ACTION);
        PendingIntent pnextIntent = PendingIntent.getService(this, 0,
                nextIntent, 0);
        int playPause;

        if(state == STATES.PLAYING || state == STATES.PREPARING)
            playPause = R.drawable.ic_media_pause;
        else
            playPause = R.drawable.ic_action_play;

        try{
            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle(CUR_SONG.getString("Title"))
                    .setTicker(CUR_SONG.getString("Title"))
                    .setContentText(CUR_SONG.getString("Album"))
                    .setSmallIcon(R.drawable.ic_home)
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .addAction(R.drawable.ic_action_previous,null, ppreviousIntent)
                    .addAction(playPause, null,
                            pplayIntent)
                    .addAction(R.drawable.ic_media_next, "",
                            pnextIntent).build();
            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,notification);
        }catch (Exception e){e.printStackTrace();}
    }
}
