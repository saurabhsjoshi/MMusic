package com.collegecode.mymusic.fragments;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.collegecode.mymusic.Home;
import com.collegecode.mymusic.PlayBackService;
import com.collegecode.mymusic.R;
import com.collegecode.mymusic.objects.STATES;
import com.parse.ParseObject;
import com.squareup.picasso.Picasso;

/**
 * Created by saurabh on 14-10-03.
 */
public class NowPlayingSmallFragment extends Fragment {

    ImageView img_art;
    ImageButton img_play;
    TextView txt_title, txt_album;
    PlayBackService instance;
    STATES state;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playnow_small,container, false);

        instance = ((Home) getActivity()).playBackService;

        img_art = (ImageView) view.findViewById(R.id.img_albumArt);
        img_play = (ImageButton) view.findViewById(R.id.img_play);
        txt_title = (TextView) view.findViewById(R.id.txt_title);
        txt_album = (TextView) view.findViewById(R.id.txt_album);

        img_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                state = instance.state;
                if(state == STATES.PLAYING || state == STATES.PREPARING)
                    instance.pauseSong();
                else
                    instance.playSong();
                setUI();
            }
        });

        setUI();
        return view;
    }

    public void setUI(){
        try{
            if(instance != null){
                state = instance.state;
                ParseObject obj = instance.getCurSong();

                txt_title.setText(obj.getString("Title"));
                txt_album.setText(obj.getString("Album"));

                Picasso.with(getActivity())
                        .load(obj.getString("CoverArt"))
                        .fit()
                        .into(img_art);

                if(state == STATES.PLAYING || state == STATES.PREPARING){
                    img_play.setImageBitmap(BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.ic_media_pause));
                }
                else{
                    img_play.setImageBitmap(BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.ic_action_play));
                }
            }
            else
            {
                instance = ((Home) getActivity()).playBackService;
                if(instance!=null)
                    setUI();
            }


        }catch (Exception ignore){}

    }

}
