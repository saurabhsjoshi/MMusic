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
import com.parse.ParseObject;

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
    private boolean isForeground = false;
    private ParseObject CUR_SONG;
    public boolean startAfterPrepare = true;
    public boolean isPreparing = false;
    private MediaPlayer player;
    private final IBinder musicBind = new MusicBinder();
    private static final String LOG_TAG = "com.collegecode.playbackservice";

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
            player = new MediaPlayer(); // initialize it here
            mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            player.setOnPreparedListener(this);
            player.setOnErrorListener(this);
            player.setOnBufferingUpdateListener(this);
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            initMusicPlayer();
        }
        else if (intent.getAction().equals(Constants.ACTION.PREV_ACTION)) {
            Log.i(LOG_TAG, "Clicked Previous");
        } else if (intent.getAction().equals(Constants.ACTION.PLAY_ACTION)) {
            if(player.isPlaying())
                pauseSong();
            else
                playSong();
            setUpAsForeground(CUR_SONG);
        } else if (intent.getAction().equals(Constants.ACTION.NEXT_ACTION)) {
            Log.i(LOG_TAG, "Clicked Next");
        } else if (intent.getAction().equals(
                Constants.ACTION.STOPFOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Stop Foreground Intent");
            stopForeground(true);
            stopSelf();
        }

        return START_STICKY;
    }

    public void initMusicPlayer(){
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(
                AudioManager.STREAM_MUSIC);
    }

    public void playSong(ParseObject song){
        try
        {
            if(player.isPlaying())
                player.reset();
            player.setDataSource(song.getString("url"));
            CUR_SONG = song;
            isPreparing = true;
            player.prepareAsync();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void showNotification(ParseObject song){
        setUpAsForeground(song);
        isForeground = true;
    }
    public void stopNotification(){
        stopForeground(true);
        isForeground = false;
    }

    public boolean isPlaying(){
        return player.isPlaying();
    }

    public ParseObject getCurSong(){
        return CUR_SONG;
    }

    public void pauseSong(){
        player.pause();
    }

    public void playSong(){
        player.start();
    }

    public void scrub(int pos){
        player.seekTo(pos);
    }

    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        stopForeground(true);
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {}

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        isPreparing = false;
        if(startAfterPrepare)
            player.start();
        if(isForeground)
            setUpAsForeground(CUR_SONG);
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



    void setUpAsForeground(ParseObject song) {
        Intent notificationIntent = new Intent(this, Home.class);
        //notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        //notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
          //      | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
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

        if(player.isPlaying())
            playPause = R.drawable.ic_media_pause;
        else
            playPause = R.drawable.ic_action_play;

        try{
            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle(song.getString("Title"))
                    .setTicker(song.getString("Title"))
                    .setContentText(song.getString("Album"))
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
