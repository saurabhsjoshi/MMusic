package com.collegecode.mymusic.fragments;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.collegecode.mymusic.Home;
import com.collegecode.mymusic.PlayBackService;
import com.collegecode.mymusic.R;
import com.collegecode.mymusic.objects.STATES;
import com.parse.ParseObject;
import com.squareup.picasso.Picasso;

import java.util.concurrent.TimeUnit;

/**
 * Created by saurabh on 14-10-03.
 */
public class NowPlayingFragment extends Fragment implements SeekBar.OnSeekBarChangeListener {
    PlayBackService instance;
    ImageButton pausePlay;
    ImageView img_album_art, img_album_art_small;
    SeekBar seekBar;
    TextView txt_cur, txt_title, txt_album;
    TextView txt_total;
    Thread t;
    STATES state;

    private boolean total_is_set = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        instance = ((Home) getActivity()).playBackService;


        View view = inflater.inflate(R.layout.fragment_nowplaying, container, false);

        img_album_art = (ImageView) view.findViewById(R.id.img_albumArt);
        img_album_art_small = (ImageView) view.findViewById(R.id.img_small_albumArt);

        pausePlay = (ImageButton) view.findViewById(R.id.btn_play);

        seekBar = (SeekBar) view.findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(this);

        txt_cur = (TextView) view.findViewById(R.id.txt_cur_time);
        txt_total = (TextView) view.findViewById(R.id.txt_total_time);
        txt_title = (TextView) view.findViewById(R.id.txt_title);
        txt_album = (TextView) view.findViewById(R.id.txt_album);

        state = instance.state;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()){

                    case R.id.btn_prev:
                        instance.prevSong();
                        break;

                    case R.id.btn_next:
                        instance.nextSong();
                        break;

                    case R.id.btn_play:
                        if(state == STATES.PLAYING || state == STATES.PREPARING){
                            instance.pauseSong();
                            stopThread();
                        }
                        else{
                            instance.playSong();
                            startThread();
                        }

                        break;
                }
                setUI();
            }
        };

        Integer a = 2;
        int b = 2;

        (view.findViewById(R.id.btn_prev)).setOnClickListener(listener);
        (view.findViewById(R.id.btn_next)).setOnClickListener(listener);
        pausePlay.setOnClickListener(listener);

        startThread();
        return view;

    }

    public void setUI(){
        ParseObject nowPlaying;
        nowPlaying = instance.getCurSong();
        state = instance.state;

        Picasso.with(getActivity())
                .load(nowPlaying.getString("CoverArt"))
                .fit()
                .into(img_album_art);

        Picasso.with(getActivity())
                .load(nowPlaying.getString("CoverArt"))
                .fit()
                .into(img_album_art_small);

        txt_title.setText(nowPlaying.getString("Title"));
        txt_album.setText(nowPlaying.getString("Album"));
        if(state == STATES.PLAYING || state == STATES.PREPARING)
            pausePlay.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_media_pause));
        else
            pausePlay.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_play));
    }

    private void startThread(){
        if(t!=null)
            t.interrupt();

        t = new Thread(){
            @Override
            public void run() {
                while (!Thread.interrupted()){
                    if(instance!=null){
                        try{
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try{
                                        seekBar.setMax(instance.getTotalTime());
                                        seekBar.setProgress(instance.getCurrentTime());
                                        String cur =  String.format("%02d:%02d",
                                                TimeUnit.MILLISECONDS.toMinutes(instance.getCurrentTime()) -
                                                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(instance.getCurrentTime())),
                                                TimeUnit.MILLISECONDS.toSeconds(instance.getCurrentTime()) -
                                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(instance.getCurrentTime())));
                                        txt_cur.setText(cur);
                                        if(!total_is_set){
                                            String total = String.format("%02d:%02d",
                                                    TimeUnit.MILLISECONDS.toMinutes(instance.getTotalTime()) -
                                                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(instance.getTotalTime())),
                                                    TimeUnit.MILLISECONDS.toSeconds(instance.getTotalTime()) -
                                                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(instance.getTotalTime())));
                                            txt_total.setText(total);
                                            total_is_set = true;
                                        }
                                    }catch (Exception ignore){}
                                }
                            });
                            try {
                                Thread.sleep(1000);
                            }catch (InterruptedException e){Thread.currentThread().interrupt();}

                        }catch (Exception ignore){}
                    }
                }
            }
        };

        t.start();
    }

    @Override
    public void onResume() {
        state = instance.state;
        setUI();
        startThread();
        super.onResume();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if(b){
            try {
                if(t != null){
                    stopThread();
                }
                String cur =  String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(i) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(instance.getCurrentTime())), // The change is in this line
                        TimeUnit.MILLISECONDS.toSeconds(i) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(i)));
                txt_cur.setText(cur);
                instance.scrub(i);
                startThread();
            }catch (Exception ignore){}
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public void stopThread(){
        if(t != null)
            t.interrupt();
    }

    @Override
    public void onPause() {
        stopThread();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        stopThread();
        super.onDestroy();
    }
}