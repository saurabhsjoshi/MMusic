package com.collegecode.mymusic.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.collegecode.mymusic.R;
import com.squareup.picasso.Picasso;

/**
 * Created by saurabh on 14-10-03.
 */
public class NowPlayingSmallFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playnow_small,container, false);

        ImageView img = (ImageView) view.findViewById(R.id.img_albumArt);

        Picasso.with(getActivity())
                .load("https://dl.dropboxusercontent.com/u/37268256/MMusic/Ayushyawar-Bolu-Kahi/albumart.jpg")
                .fit()
                .into(img);
        return view;
    }

}
