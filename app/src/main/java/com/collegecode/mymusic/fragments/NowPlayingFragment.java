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
import com.parse.ParseObject;
import com.squareup.picasso.Picasso;

import java.util.concurrent.TimeUnit;

/**
 * Created by saurabh on 14-10-03.
 */
public class NowPlayingFragment extends Fragment implements SeekBar.OnSeekBarChangeListener {
    private ParseObject nowPlaying;
    PlayBackService instance;
    ImageButton pausePlay;
    SeekBar seekBar;
    TextView txt_cur;
    TextView txt_total;
    Thread t;
    boolean isPlaying;

    private boolean total_is_set = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        instance = ((Home) getActivity()).playBackService;

        View view = inflater.inflate(R.layout.fragment_nowplaying, container, false);
        ImageView img = (ImageView) view.findViewById(R.id.img_albumArt);

        nowPlaying = ((Home) getActivity()).cur_playing;

        pausePlay = (ImageButton) view.findViewById(R.id.btn_play);
        seekBar = (SeekBar) view.findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(this);
        txt_cur = (TextView) view.findViewById(R.id.txt_cur_time);
        txt_total = (TextView) view.findViewById(R.id.txt_total_time);

        isPlaying = ((Home) getActivity()).isPlaying;

        pausePlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isPlaying)
                {
                    isPlaying = false;
                    ((Home) getActivity()).pauseMusic();
                }
                else{
                    isPlaying = true;
                    if(instance!= null)
                        ((Home)getActivity()).playMusic();
                    else{
                        ((Home)getActivity()).startSong();
                        instance = ((Home) getActivity()).playBackService;
                    }
                }
                setUI();
                if(t == null)
                    startThread();
            }
        });
        setUI();
        Picasso.with(getActivity())
                .load(nowPlaying.getString("CoverArt"))
                .fit()
                .into(img);

        Picasso.with(getActivity())
                .load(nowPlaying.getString("CoverArt"))
                .fit()
                .into(((ImageView) view.findViewById(R.id.img_small_albumArt)));


        TextView txt_title = (TextView) view.findViewById(R.id.txt_title);
        TextView txt_album = (TextView) view.findViewById(R.id.txt_album);


        txt_title.setText(nowPlaying.getString("Title"));
        txt_album.setText(nowPlaying.getString("Album"));
        startThread();

        return view;

    }

    private void setUI(){
        if(isPlaying)
            pausePlay.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_media_pause));
        else
            pausePlay.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_play));

    }

    private void startThread(){
        t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    if(instance!=null ){
                        try{
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
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
                                }
                            });
                            Thread.sleep(1000);
                        }catch (Exception ignore){}
                    }
                }

            }
        });
        t.start();
    }

    @Override
    public void onResume() {
        isPlaying = ((Home) getActivity()).isPlaying;
        setUI();
        super.onResume();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if(b){
            try {
                if(t != null){
                    t.interrupt();
                    t = null;
                }
                String cur =  String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(i) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(instance.getCurrentTime())), // The change is in this line
                        TimeUnit.MILLISECONDS.toSeconds(i) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(i)));
                txt_cur.setText(cur);
                instance.scrub(i);
                t.start();

            }catch (Exception ignore){}
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onDestroy() {
        if(t != null){
            t.interrupt();
            t= null;
        }
        super.onDestroy();
    }
}
