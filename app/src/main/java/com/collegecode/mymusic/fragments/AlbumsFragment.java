package com.collegecode.mymusic.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.collegecode.mymusic.R;
import com.collegecode.mymusic.adapters.AlbumsGridAdapter;
import com.collegecode.mymusic.objects.Album;

import java.util.ArrayList;

/**
 * Created by saurabh on 7/28/14.
 */
public class AlbumsFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_albums,container, false);
        GridView gridview = (GridView) view.findViewById(R.id.gridview);

        ArrayList<Album> albums = new ArrayList<Album>();

        Album a = new Album();
        a.album_art = "http://poponandon.com/wp-content/uploads/2013/04/onerepublic-native-review-2013.jpg";
        a.title = "Native";

        for(int i = 0 ; i < 8; i++)
           albums.add(a);

        a = new Album();
        a.album_art = "http://p.playserver1.com/ProductImages/8/6/2/1/7/9/3/3/33971268_700x700min_1.jpg";
        a.title = "#3";

        for(int i = 0 ; i < 7; i++)
            albums.add(a);
        gridview.setAdapter(new AlbumsGridAdapter(getActivity(), albums));

        return view;
    }
}
